package cn.sanenen.sunutils.queue;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class SMQTest {
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void sQueueRejectsOversizedMessage() throws Exception {
		SQueue queue = new SQueue(temporaryFolder.newFolder("oversized").getAbsolutePath(), 32);
		try {
			Assert.assertThrows(IllegalArgumentException.class, () -> queue.add(new byte[16]));
		} finally {
			queue.close();
		}
	}

	@Test
	public void sQueueRejectsFilePathAsDirectory() throws Exception {
		File file = temporaryFolder.newFile("not-dir");
		Assert.assertThrows(IOException.class, () -> new SQueue(file.getAbsolutePath(), 1024));
	}

	@Test
	public void smqRejectsInvalidSettingsAndTopic() throws Exception {
		File dir = temporaryFolder.newFolder("smq-invalid");
		Assert.assertThrows(IllegalArgumentException.class, () -> SMQ.setting(dir.getAbsolutePath(), 0, 0));

		SMQ.setting(dir.getAbsolutePath(), 1, 0);
		try {
			Assert.assertThrows(IllegalArgumentException.class, () -> SMQ.push("../bad", "data"));
		} finally {
			SMQ.close();
		}
	}

	@Test
	public void smqCanCloseAndReopenOnAnotherDirectory() throws Exception {
		File first = temporaryFolder.newFolder("smq-first");
		SMQ.setting(first.getAbsolutePath(), 1, 0);
		SMQ.push("topic", "first");
		Assert.assertEquals("first", SMQ.pop("topic"));
		SMQ.close();

		File second = temporaryFolder.newFolder("smq-second");
		SMQ.setting(second.getAbsolutePath(), 1, 0);
		try {
			SMQ.push("topic", "second");
			Assert.assertEquals("second", SMQ.pop("topic"));
		} finally {
			SMQ.close();
		}
	}

	@Test
	public void dataEntityRejectsCorruptMessageLength() throws Exception {
		File file = temporaryFolder.newFile("corrupt.d");
		DataEntityAccessor.writeCorruptLengthFile(file, 1024, -1);

		cn.sanenen.sunutils.queue.data.DataEntity entity = new cn.sanenen.sunutils.queue.data.DataEntity(file.getAbsolutePath(), 1, 1024);
		try {
			Assert.assertThrows(IllegalStateException.class, entity::readNextAndRemove);
		} finally {
			entity.close();
		}
	}

	@Test
	public void sQueueKeepsEveryDistinctMessageDuringConcurrentReadWriteAndRotation() throws Exception {
		SQueue queue = new SQueue(temporaryFolder.newFolder("concurrent").getAbsolutePath(), 128);
		int producers = 4;
		int perProducer = 500;
		int total = producers * perProducer;
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch finished = new CountDownLatch(producers);
		Set<String> received = ConcurrentHashMap.newKeySet();
		AtomicInteger consumed = new AtomicInteger();
		Thread[] consumers = new Thread[4];
		for (int c = 0; c < consumers.length; c++) {
			consumers[c] = new Thread(() -> {
				try {
					start.await();
					while (consumed.get() < total || finished.getCount() > 0) {
						byte[] value = queue.readNextAndRemove();
						if (value != null) {
							Assert.assertTrue("消息重复", received.add(new String(value, StandardCharsets.UTF_8)));
							consumed.incrementAndGet();
						} else Thread.yield();
					}
				} catch (Exception e) { throw new RuntimeException(e); }
			});
			consumers[c].start();
		}
		Thread[] writers = new Thread[producers];
		for (int p = 0; p < producers; p++) {
			final int id = p;
			writers[p] = new Thread(() -> {
				try { start.await(); for (int i = 0; i < perProducer; i++) queue.add((id + "-" + i).getBytes(StandardCharsets.UTF_8)); }
				catch (Exception e) { throw new RuntimeException(e); }
				finally { finished.countDown(); }
			});
			writers[p].start();
		}
		start.countDown();
		for (Thread writer : writers) writer.join(10_000);
		for (Thread consumer : consumers) { consumer.join(10_000); Assert.assertFalse("消费者未完成", consumer.isAlive()); }
		try {
			Assert.assertEquals(total, consumed.get());
			Assert.assertEquals(total, received.size());
			for (int p = 0; p < producers; p++) for (int i = 0; i < perProducer; i++) Assert.assertTrue(received.contains(p + "-" + i));
		} finally { queue.close(); }
	}

	private static class DataEntityAccessor {
		private static void writeCorruptLengthFile(File file, int fileLimitLength, int length) throws IOException {
			try (java.io.RandomAccessFile randomAccessFile = new java.io.RandomAccessFile(file, "rwd")) {
				randomAccessFile.write(QueueConstant.MAGIC.getBytes(StandardCharsets.UTF_8));
				randomAccessFile.writeInt(fileLimitLength);
				randomAccessFile.writeInt(cn.sanenen.sunutils.queue.data.DataEntity.DATA_START_POSITION);
				randomAccessFile.writeInt(cn.sanenen.sunutils.queue.data.DataEntity.DATA_START_POSITION + 4);
				randomAccessFile.writeInt(-1);
				randomAccessFile.writeInt(length);
			}
		}
	}
}

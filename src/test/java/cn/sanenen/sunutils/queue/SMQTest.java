package cn.sanenen.sunutils.queue;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

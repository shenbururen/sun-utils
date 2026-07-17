package cn.sanenen.sunutils.queue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 吞吐量基准测试：单线程与多线程场景。
 *
 * <p>对齐 Rust 版 smq 的 tests/benchmark.rs，便于两版横向对比。
 *
 * <p>所有消息在计时开始前预先生成，避免字符串构造开销干扰吞吐结果。
 * 计时区间内只包含 {@link SQueue#add(byte[])} / {@link SQueue#readNextAndRemove()} 的真实开销。
 *
 * <p>以单测方式运行即可，吞吐数据打印到标准输出；断言阈值设得宽松，仅防止严重回归。
 *
 * <p>2026-07-17 大工作量基准结果：100B 消息；纯读、纯写、多个生产者、多个消费者各处理
 * 6,050,000 条消息；读写交替和混合读写各计 12,100,000 次操作。以下为三轮 ops/s
 *（第 1 轮 / 第 2 轮 / 第 3 轮 / 中位数）：
 * <ul>
 *     <li>单线程纯写：11,214,370 / 10,102,412 / 10,702,358 / 10,702,358</li>
 *     <li>单线程纯读：13,400,405 / 13,314,234 / 13,692,953 / 13,400,405</li>
 *     <li>单线程读写交替：15,794,955 / 16,455,526 / 14,320,093 / 15,794,955</li>
 *     <li>8 生产者并发写：7,509,407 / 6,683,174 / 6,561,373 / 6,683,174</li>
 *     <li>4 消费者并发读：8,017,084 / 7,948,052 / 7,416,210 / 7,948,052</li>
 *     <li>4 生产者 + 4 消费者混合读写：10,346,876 / 10,355,315 / 9,306,569 / 10,346,876</li>
 * </ul>
 *
 * <p>测试硬件与运行时：AMD Ryzen 9 7945HX with Radeon Graphics（16 核 32 线程）、
 * Windows 11 x64、Eclipse Temurin OpenJDK 11.0.26、Apache Maven 3.9.11。
 */
public class BenchmarkTest {

	/** 每条消息固定为 100 个字符的字符串。 */
	private static final int MSG_SIZE = 100;
	/** 写入足以创建第三个 300MB 数据文件的最小消息数（每条占 100B 数据 + 4B 长度）。 */
	private static final long THREE_FILE_COUNT = 6_050_000L;
	/** 单线程基准的消息总条数。 */
	private static final long SINGLE_COUNT = THREE_FILE_COUNT;
	/** 8 个生产者合计写入 THREE_FILE_COUNT 条时，每个生产者的消息数。 */
	private static final long MULTI_PRODUCER_PER_PRODUCER = THREE_FILE_COUNT / 8;
	/** 多消费者基准的预写入消息总数。 */
	private static final long MULTI_CONSUMER_TOTAL = THREE_FILE_COUNT;
	/** 4 个生产者合计写入 THREE_FILE_COUNT 条时，每个生产者的消息数。 */
	private static final long MIXED_PER_PRODUCER = THREE_FILE_COUNT / 4;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	/** 当前测试使用的队列实例，每个用例独立创建。 */
	private SQueue queue;

	@Before
	public void setUp() throws Exception {
		// 单文件 300MB，避免写满触发频繁滚动影响单线程纯写基准
		queue = new SQueue(temporaryFolder.newFolder().getAbsolutePath(), 300 * 1024 * 1024);
	}

	@After
	public void tearDown() {
		if (queue != null) {
			queue.close();
		}
		// 关闭 SMQ 全局单例，避免残留的目录锁影响后续用例
		SMQ.close();
	}

	// ============== 工具方法 ==============

	/** 生成固定 100 个字符的消息字节。前 20 字符为补零序号，其余用字母循环填充。 */
	private static byte[] makeMsg(long idx) {
		StringBuilder s = new StringBuilder(MSG_SIZE);
		// 序号段：补零对齐到 20 字符
		String head = String.format("%020d", idx);
		s.append(head);
		// 用字母表循环填充剩余字符
		String filler = "abcdefghijklmnopqrstuvwxyz";
		int fillerLen = filler.length();
		while (s.length() < MSG_SIZE) {
			s.append(filler.charAt(s.length() % fillerLen));
		}
		return s.substring(0, MSG_SIZE).getBytes(StandardCharsets.UTF_8);
	}

	/** 预先生成 count 条消息，避免构造开销进入计时区间。 */
	private static List<byte[]> genMessages(long count) {
		List<byte[]> msgs = new ArrayList<>((int) count);
		for (long i = 0; i < count; i++) {
			msgs.add(makeMsg(i));
		}
		return msgs;
	}

	/** 打印并返回 ops/s。 */
	private static double report(String label, long count, long elapsedNanos) {
		double secs = Math.max(elapsedNanos / 1_000_000_000.0, 1e-9);
		double ops = count / secs;
		double mbps = count * MSG_SIZE / 1_024_024.0 / secs;
		System.out.printf(
				"[bench] %-32s %10d 条 | %8.4fs | %14.0f ops/s | %8.2f MB/s%n",
				label, count, secs, ops, mbps);
		return ops;
	}

	// ============== 单线程场景 ==============

	@Test
	public void benchSingleThreadWrite() throws Exception {
		// 消息预生成（不计入耗时）
		List<byte[]> msgs = genMessages(SINGLE_COUNT);

		long start = System.nanoTime();
		for (byte[] m : msgs) {
			queue.add(m);
		}
		double ops = report("单线程纯写", SINGLE_COUNT, System.nanoTime() - start);
		// 宽松下限：纯写应至少 5k ops/s
		Assert.assertTrue("单线程写吞吐过低: " + (long) ops + " ops/s", ops > 5_000);
	}

	@Test
	public void benchSingleThreadRead() throws Exception {
		// 预先写入（不计入耗时）
		List<byte[]> msgs = genMessages(SINGLE_COUNT);
		for (byte[] m : msgs) {
			queue.add(m);
		}

		long start = System.nanoTime();
		long got = 0;
		while (queue.readNextAndRemove() != null) {
			got++;
		}
		Assert.assertEquals("读取条数不匹配", SINGLE_COUNT, got);
		double ops = report("单线程纯读", got, System.nanoTime() - start);
		Assert.assertTrue("单线程读吞吐过低: " + (long) ops + " ops/s", ops > 5_000);
	}

	@Test
	public void benchSingleThreadPushPopAlternating() throws Exception {
		// 消息预生成（不计入耗时）
		List<byte[]> msgs = genMessages(SINGLE_COUNT);

		long start = System.nanoTime();
		for (byte[] m : msgs) {
			queue.add(m);
			// 此时 pop 能取回刚写入的（FIFO，且队列深度始终 <= 1）
			queue.readNextAndRemove();
		}
		// 一次 add + 一次 readNextAndRemove 计为 2 次操作
		double ops = report("单线程读写交替", SINGLE_COUNT * 2, System.nanoTime() - start);
		Assert.assertTrue("单线程读写交替吞吐过低: " + (long) ops + " ops/s", ops > 3_000);
	}

	// ============== 多线程场景 ==============

	/**
	 * 多生产者并发写。
	 *
	 * <p>注意：{@link SQueue#add(byte[])} 是 synchronized 实例方法，并发写会被串行化，
	 * 此处主要衡量多线程争用下的吞吐，而非真正并行写入。
	 */
	@Test
	public void benchMultiProducerWrite() throws Exception {
		int producerCount = 8;
		long total = producerCount * MULTI_PRODUCER_PER_PRODUCER;

		// 消息预生成（不计入耗时），各生产者按分片索引取用
		List<byte[]> msgs = genMessages(total);

		long start = System.nanoTime();
		List<CompletableFuture<Void>> futures = new ArrayList<>(producerCount);
		for (int p = 0; p < producerCount; p++) {
			final int base = (int) (p * MULTI_PRODUCER_PER_PRODUCER);
			final int end = base + (int) MULTI_PRODUCER_PER_PRODUCER;
			futures.add(CompletableFuture.runAsync(() -> {
				for (int i = base; i < end; i++) {
					try {
						queue.add(msgs.get(i));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}));
		}
		for (CompletableFuture<Void> f : futures) {
			f.join();
		}
		double ops = report("多生产者并发写(8线程)", total, System.nanoTime() - start);
		Assert.assertEquals("写入条数不匹配", total, queue.getQueueSize());
		Assert.assertTrue("多生产者写吞吐过低: " + (long) ops + " ops/s", ops > 5_000);
	}

	@Test
	public void benchMultiConsumerRead() throws Exception {
		int consumerCount = 4;
		long total = MULTI_CONSUMER_TOTAL;
		// 预先写入（不计入耗时）
		List<byte[]> msgs = genMessages(total);
		for (byte[] m : msgs) {
			queue.add(m);
		}

		AtomicLong consumed = new AtomicLong(0);
		long start = System.nanoTime();
		List<CompletableFuture<Void>> futures = new ArrayList<>(consumerCount);
		for (int c = 0; c < consumerCount; c++) {
			futures.add(CompletableFuture.runAsync(() -> {
				while (true) {
					try {
						if (queue.readNextAndRemove() != null) {
							consumed.incrementAndGet();
						} else {
							// 短暂空窗，检查是否已消费完
							if (consumed.get() >= total) {
								break;
							}
							Thread.sleep(0, 100_000);
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}));
		}
		for (CompletableFuture<Void> f : futures) {
			f.join();
		}
		long got = consumed.get();
		Assert.assertEquals("读取条数不匹配", total, got);
		double ops = report("多消费者并发读(4线程)", got, System.nanoTime() - start);
		Assert.assertTrue("多消费者读吞吐过低: " + (long) ops + " ops/s", ops > 5_000);
	}

	@Test
	public void benchMixedProducersAndConsumers() throws Exception {
		int producerCount = 4;
		int consumerCount = 4;
		long total = producerCount * MIXED_PER_PRODUCER;

		// 消息预生成（不计入耗时）
		List<byte[]> msgs = genMessages(total);

		AtomicLong consumed = new AtomicLong(0);
		long start = System.nanoTime();

		// 启动消费者
		List<CompletableFuture<Void>> consumerFutures = new ArrayList<>(consumerCount);
		for (int c = 0; c < consumerCount; c++) {
			consumerFutures.add(CompletableFuture.runAsync(() -> {
				while (true) {
					try {
						if (queue.readNextAndRemove() != null) {
							consumed.incrementAndGet();
						} else {
							if (consumed.get() >= total) {
								break;
							}
							Thread.sleep(0, 50_000);
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}));
		}

		// 启动生产者
		List<CompletableFuture<Void>> producerFutures = new ArrayList<>(producerCount);
		for (int p = 0; p < producerCount; p++) {
			final int base = (int) (p * MIXED_PER_PRODUCER);
			final int end = base + (int) MIXED_PER_PRODUCER;
			producerFutures.add(CompletableFuture.runAsync(() -> {
				for (int i = base; i < end; i++) {
					try {
						queue.add(msgs.get(i));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}));
		}

		for (CompletableFuture<Void> f : producerFutures) {
			f.join();
		}
		for (CompletableFuture<Void> f : consumerFutures) {
			f.join();
		}

		long got = consumed.get();
		Assert.assertEquals("读取条数不匹配", total, got);
		// 混合场景计 add + readNextAndRemove 共 2*total 次操作
		double ops = report("混合读写(4生产+4消费)", total * 2, System.nanoTime() - start);
		Assert.assertTrue("混合读写吞吐过低: " + (long) ops + " ops/s", ops > 3_000);
	}
}

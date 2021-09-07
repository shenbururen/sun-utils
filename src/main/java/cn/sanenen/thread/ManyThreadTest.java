package cn.sanenen.thread;

import cn.hutool.log.Log;

import java.util.concurrent.CountDownLatch;

/**
 * 多线程测试类
 * 有时候会需要多线程测试 性能、多线程环境下执行是否正常 等。
 *
 * @author sun
 * @date 2021-09-07
 **/
public class ManyThreadTest {
	private static final Log log = Log.get();

	/**
	 * 执行测试
	 *
	 * @param threadNum 执行线程数
	 * @param forNum    每个线程循环执行多少次
	 * @param run       无参数，无返回的函数接口。非实际线程。
	 * @return 执行总耗时   
	 */
	public static long test(int threadNum, int forNum, Runnable run) {
		CountDownLatch latch = new CountDownLatch(threadNum);
		long start = System.currentTimeMillis();
		for (int i = 0; i < threadNum; i++) {
			new Thread(() -> {
				try {
					for (int j = 0; j < forNum; j++) {
						run.run();
					}
				} catch (Exception e) {
					log.error(e);
				} finally {
					latch.countDown();
				}
			}).start();
		}
		try {
			latch.await();
		} catch (Exception e) {
			log.error(e);
		}
		long hs = System.currentTimeMillis() - start;
		log.info("total time:{}ms", hs);
		return hs;
	}
}

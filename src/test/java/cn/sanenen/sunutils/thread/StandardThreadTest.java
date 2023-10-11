package cn.sanenen.sunutils.thread;

import cn.hutool.core.lang.Console;
import cn.hutool.core.thread.ThreadUtil;
import cn.sanenen.sunutils.thread.StandardThread;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 标准线程类 测试及使用
 *
 * @author sun
 * @date 2021-09-13
 **/
public class StandardThreadTest {

	@Test
	public void standardThreadTest() {
		BusinessThread thread = new BusinessThread();
		thread.init();
		ThreadUtil.sleep(10000);
		thread.close();
	}

	private static class BusinessThread extends StandardThread {

		private final AtomicLong num = new AtomicLong();

		@Override
		public void handler() {
			try {
				Console.log(num.incrementAndGet());
				ThreadUtil.sleep(1000);
			} catch (Exception e) {
				log.error(e);
				ThreadUtil.sleep(10000);
			}
		}

		@Override
		protected void initBefore() {
			Console.log("线程要启动了");
		}

		@Override
		protected void closeAfter() {
			Console.log("线程关闭了");
		}
	}
}

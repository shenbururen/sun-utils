package cn.sanenen.thread;

import cn.hutool.core.thread.ThreadUtil;
import org.junit.Test;

/**
 * 多线程测试类 测试及使用
 * @author sun
 * @date 2021-09-07
 **/
public class ManyThreadTestTest {
	@Test
	public void manyThreadTestTest(){
		long totalTime = ManyThreadTest.test(5, 10, () -> {
			//需要测试的代码
			ThreadUtil.sleep(100);
		});
		//total time:1106hs
	}
}

package cn.sanenen.sunutils.thread;

import cn.hutool.core.thread.ThreadUtil;
import cn.sanenen.sunutils.thread.ManyThreadRun;
import org.junit.Test;

/**
 * 多线程测试类 测试及使用
 * @author sun
 * @date 2021-09-07
 **/
public class ManyThreadRunTest {
	@Test
	public void manyThreadTestTest(){
		long totalTime = ManyThreadRun.run(5, 10, () -> {
			//需要测试的代码
			ThreadUtil.sleep(100);
		});
		//total time:1106ms
	}
}

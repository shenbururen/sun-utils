package cn.sanenen.other;

import cn.sanenen.thread.ManyThreadTest;
import cn.sanenen.utils.other.IntervalSecondSpeeder;
import org.junit.Test;

/**
 * 流控类测试及使用
 *
 * @author sun
 * @date 2021-09-07
 **/
public class IntervalSecondSpeederTest {
	@Test
	public void intervalSecondSpeederTest() {
		//限制每秒可通过10个
		IntervalSecondSpeeder speeder = new IntervalSecondSpeeder(10);
		//十个线程，每个线程执行10次，共一百次，预计10s左右执行完成。
		ManyThreadTest.test(10, 10, speeder::limitSpeed);
		//total time:9854ms
	}
}

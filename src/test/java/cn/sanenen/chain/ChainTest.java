package cn.sanenen.chain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 职责链封装测试及使用方法演示
 *
 * @author sun
 * @date 2021-09-06
 **/
public class ChainTest {
	@Test
	public void simpleMsgChainTest() {
		SimpleMsgChain<TestData> msgChain = new SimpleMsgChain<>();
		msgChain.setHandlers(CollUtil.newArrayList(new TestHandler(),
				new Test2Handler()));

		TestData testData = new TestData();
		msgChain.execute(testData);
		Assert.assertEquals(testData.count.get(), 100L);
	}

	/**
	 * 业务处理职责链1
	 */
	private static class TestHandler implements IMsgHandler<TestData> {
		@Override
		public boolean process(TestData data) {
			try {
				//业务处理
				Console.log("userName:{},count:{}", data.userName, data.count);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//该职责链 业务处理不重要，出现异常也继续向下执行。
			return false;
		}
	}

	/**
	 * 业务处理职责链2
	 */
	private static class Test2Handler implements IMsgHandler<TestData> {
		@Override
		public boolean process(TestData data) {
			//业务处理
			Console.log("userName:{},count:{}", data.userName, data.count.addAndGet(100));
			return false;
		}
	}

	/**
	 * 数据类
	 */
	@Data
	private static class TestData {
		private String userName;
		private AtomicLong count = new AtomicLong(0);
	}
}

package cn.sanenen.sunutils.queue;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.log.dialect.console.ConsoleLogFactory;
import cn.sanenen.queue.SMQ;
import cn.sanenen.thread.ManyThreadRun;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 持久化多线程测试
 *
 * @author sun
 * @date 2021-09-10
 **/
public class SMQTest {
	static {
		LogFactory.setCurrentLogFactory(new ConsoleLogFactory());
	}

	@Test
	public void settingTest() {
		SMQ.setting("/data/nnn");
		SMQ.push("abc", "2");
	}

	@Test
	public void settingTest2() {
		SMQ.setting("/data/nnn", 100, 0);
		SMQ.push("abc", "2");
	}

	@Test
	public void putTest() {
		SMQ.setting("/data/nnn", 100, 0);
		//多线程写入的同时，多线程读取。
		Map<String, Integer> cache = new ConcurrentHashMap<>();
		Map<String, Integer> cache2 = new ConcurrentHashMap<>();

		new Thread(() -> ManyThreadRun.run(20, 1000000, () -> {
			String data = IdUtil.fastSimpleUUID();
			cache.put(data, 1);
			SMQ.push("topic1", data);
			String data2 = IdUtil.fastSimpleUUID();
			cache2.put(data2, 1);
			SMQ.push("topic2", data2);
		})).start();
		ThreadUtil.sleep(1000);
		new Thread(() -> ManyThreadRun.run(3, 1, () -> {
			String data;
			int i = 0;
			while (true) {
				data = SMQ.pop("topic1");
				if (data == null) {
					i++;
					if (i > 10) {
						break;
					}
					ThreadUtil.sleep(10);
					continue;
				}
				i = 0;
				cache.remove(data);
			}
		})).start();
		ThreadUtil.sleep(1000);
		ManyThreadRun.run(3, 1, () -> {
			String data;
			int i = 0;
			while (true) {
				data = SMQ.pop("topic2");
				if (data == null) {
					i++;
					if (i > 10) {
						break;
					}
					ThreadUtil.sleep(10);
					continue;
				}
				i = 0;
				cache2.remove(data);
			}
		});
		ThreadUtil.sleep(10000);

		System.out.println(cache.size());
		System.out.println(cache2.size());
		System.out.println(SMQ.size("topic1"));
		System.out.println(SMQ.size("topic2"));
	}
}

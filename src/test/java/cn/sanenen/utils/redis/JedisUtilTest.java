package cn.sanenen.utils.redis;

import cn.hutool.core.lang.Console;
import cn.hutool.db.nosql.redis.RedisDS;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sun
 * @date 2021-10-19
 **/
public class JedisUtilTest {
	private final JedisUtil test = new JedisUtil(RedisDS.create("test"));

	@Test
	public void test0() {
		test.hincrByFloat("aafd", "der",2.85);
		String aafd = test.hget("aafd","der");
		Console.log(Double.parseDouble(aafd));
	}
	@Test
	public void test1() {
		List<JSONObject> list = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("1", "a" + i);
			jsonObject.put("2", "b" + i);
			jsonObject.put("3", "c" + i);
			jsonObject.put("4", "d" + i);
			list.add(jsonObject);
		}
		Long test1 = test.lpush("test1", list);
		Console.log(test1);
	}

	@Test
	public void test2() {
		List<JSONObject> test2 = test.rpop("test1", 50, JSONObject.class);
		Console.log(test2);
	}
	@Test
	public void test3() {
		List<JSONObject> test3 = test.rpopByLua("test1", 9000, JSONObject.class);
		Console.log(test3);
	}
	@Test
	public void test4() {
		JSONObject test4 = test.rpop("test1", JSONObject.class);
		Console.log(test4);
	}
}

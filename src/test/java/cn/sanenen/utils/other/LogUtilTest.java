package cn.sanenen.utils.other;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.junit.Test;

/**
 * @author sun
 * @date 2022-11-02
 **/
public class LogUtilTest {

	@Test
	public void logUtilTest() {
		String abd = LogUtil.formatValue(StrUtil.repeat("abd", 100), 30);
		Console.log(abd);//abdabdabdabdab...dabdabdabdabd
	}

	@Test
	public void logUtilTest1() {
		JSONObject content = new JSONObject();
		content.put("content",StrUtil.repeat("content1111",30));
		
		JSONArray objects = new JSONArray();
		JSONObject content1 = new JSONObject();
		content1.put("objectscontent",StrUtil.repeat("objects22222",30));
		JSONObject content2 = new JSONObject();
		content2.put("objectscontent",StrUtil.repeat("objects333",30));
		objects.add(content1);
		objects.add(content2);
		
		String[] strings = {StrUtil.repeat("strings1",30),StrUtil.repeat("strings2",20)};
		int[] ints = {123,3545};
		
		JSONObject body = new JSONObject();
		body.put("cpCode", "cpCode");
		body.put("cpCode22", StrUtil.repeat("objects22222",30));
		body.put("content", content);
		body.put("objects", objects);
		body.put("strings", strings);
		body.put("ints", ints);
		body.put("booble", true);
		body.put("nullkey", null);
		TestData testData = new TestData();
		body.put("testData",testData);
		Console.log(LogUtil.formatValue(body, 30));
		Console.log(LogUtil.formatValue(body.toJSONString(), 30));
		Console.log(body.toJSONString());
	}

	@Data
	private static class TestData {
		private String userName = StrUtil.repeat("a", 100);
		private int count = 0;
		private JSONObject body;

	}

}

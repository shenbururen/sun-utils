package cn.sanenen.sunutils.utils.http;

import cn.hutool.core.lang.Console;
import cn.sanenen.sunutils.utils.http.HttpUtil;
import org.apache.hc.core5.http.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sun
 * @date 2021-02-25
 **/
public class HttpUtilTest {

	@Test
	public void httpUtilGetTest() throws IOException, ParseException {
		Map<String, Object> map = new HashMap<>();
		map.put("code", "utf-8");
		map.put("q", "java编程");
		map.put("callback", "cb");
		String result = HttpUtil.get("https://suggest.taobao.com/sug", map);
		Console.log(result);
	}

}
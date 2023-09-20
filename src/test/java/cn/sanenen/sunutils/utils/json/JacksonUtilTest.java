package cn.sanenen.sunutils.utils.json;

import cn.sanenen.sunutils.utils.json.JacksonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author sun
 * @date 2021-09-06
 **/
public class JacksonUtilTest {

	@Test
	public void jacksonUtilTest() {
		JsonNode jsonNode = JacksonUtil.toObj("{\"key1\":\"123\"}");
		Assert.assertEquals(jsonNode.get("key1").asInt(), 123);
	}
}

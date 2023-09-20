package cn.sanenen.sunutils.utils.json;

import cn.hutool.json.JSONException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sun
 * @date 2020-06-08
 **/
public class JacksonUtil {

	private final static ObjectMapper mapper = JsonMapper.builder()
			.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS,
					JsonReadFeature.ALLOW_JAVA_COMMENTS,
					JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES,
					JsonReadFeature.ALLOW_SINGLE_QUOTES,
					JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
			.serializationInclusion(JsonInclude.Include.NON_NULL)
			.build();


	public static String toJson(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new JSONException(e);
		}
	}

	public static <T> T toObj(String json, Class<T> clazz) {
		try {
			return mapper.readValue(json, clazz);
		} catch (Exception e) {
			throw new JSONException(e);
		}
	}

	public static JsonNode toObj(String json) {
		try {
			return mapper.readTree(json);
		} catch (Exception e) {
			throw new JSONException(e);
		}
	}

	public static <T> List<T> toObjs(String json, Class<T> clazz) {
		try {
			return mapper.readValue(json, mapper.getTypeFactory().constructParametricType(ArrayList.class, clazz));
		} catch (Exception e) {
			throw new JSONException(e);
		}
	}
}

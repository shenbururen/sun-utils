package cn.sanenen.sunutils.utils.other;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * 日志打印处理
 *
 * @author sun
 * @date 2022-11-02
 **/
public class LogUtil {

	/**
	 * 处理需要打印的日志。避免值过长，进行截取操作。
	 * 如果value是对象则转换为json进行处理。
	 * 如果是字符串则判断是否为json串，如果为json则处理json的值，如果不为json则直接调用 StrUtil.brief(result, maxLength);
	 *
	 * @param value     需要打印的值
	 * @param maxLength 字符串的最大长度
	 * @return 处理后的值
	 */
	public static String formatValue(@Nullable Object value, int maxLength) {
		if (value == null) {
			return StrUtil.NULL;
		}
		if (value instanceof String) {
			JSONValidator jsonValidator = JSONValidator.from((String) value);
			if (jsonValidator.validate()) {
				JSON json = null;
				JSONValidator.Type type = jsonValidator.getType();
				if (type == JSONValidator.Type.Object) {
					json = JSON.parseObject((String) value);
				} else if (type == JSONValidator.Type.Array) {
					json = JSON.parseArray((String) value);
				}
				if (json != null) {
					formatJson(json, maxLength);
					return json.toJSONString();
				}
			}
			return StrUtil.brief((String) value, maxLength);
		} else {
			//非字符串，转为json字符串再处理
			return formatValue(JSON.toJSONString(value), maxLength);
		}
	}

	/**
	 * 处理需要打印的日志。json值处理
	 *
	 * @param json      json对象
	 * @param maxLength 字符串的最大长度
	 */
	private static void formatJson(final JSON json, final int maxLength) {
		if (json instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) json;
			for (int i = 0; i < jsonArray.size(); i++) {
				Object obj = jsonArray.get(i);
				if (obj instanceof JSON) {
					formatJson((JSON) obj, maxLength);
				} else if (obj instanceof String) {
					String v = StrUtil.brief((String) obj, maxLength);
					jsonArray.set(i, v);
				}
			}
		} else if (json instanceof JSONObject) {
			for (Map.Entry<String, Object> entry : ((JSONObject) json).entrySet()) {
				if (entry.getValue() instanceof JSON) {
					formatJson((JSON) entry.getValue(), maxLength);
				} else if (entry.getValue() instanceof String) {
					String v = StrUtil.brief((String) entry.getValue(), maxLength);
					entry.setValue(v);
				}
			}
		}
	}
}

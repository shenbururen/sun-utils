package cn.sanenen.sunutils.utils.sms.template.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 模版变量提取结果
 *
 * @author sun
 * @date 2021-10-08
 **/
@Data
public class TemplateResult {
	/**
	 * 模版id
	 */
	private Long templateId;
	/**
	 * 模版内容
	 */
	private String templateContent;
	/**
	 * 匹配内容
	 */
	private String content;
	/**
	 * 参数 键值对
	 */
	private Map<String, String> paramMap = new HashMap<>();

	public void putParam(String key, String val) {
		this.paramMap.put(key, val);
	}
}

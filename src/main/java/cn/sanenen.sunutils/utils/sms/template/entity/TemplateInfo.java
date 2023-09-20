package cn.sanenen.sunutils.utils.sms.template.entity;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模版对象
 */
@Data
public class TemplateInfo {
	private static final Log log = Log.get();

	/**
	 * 模版id
	 */
	private Long id;
	/**
	 * 模版内容
	 */
	private String content;

	public TemplateInfo(String content) {
		this.setContent(content);
	}

	/**
	 * 模板参数列表name
	 */
	@JSONField(deserialize = false)
	@Setter(AccessLevel.NONE)
	private String[] paramNames;
	/**
	 * 模板拆分后的模板内容集合
	 */
	@JSONField(deserialize = false)
	@Setter(AccessLevel.NONE)
	private String[] contents;

	public void setContent(String content) {
		this.content = content;
		try {
			String reg = "\\{\\{.*?}}";
			Pattern compile = Pattern.compile(reg);
			Matcher matcher = compile.matcher(content);
			List<String> paramList = new ArrayList<>();
			while (matcher.find()) {
				String group = matcher.group();
				paramList.add(StrUtil.strip(group, "{{", "}}"));
			}
			paramNames = ArrayUtil.toArray(paramList, String.class);
			contents = content.split(reg, -1);
		} catch (Exception e) {
			log.error(e, "set template content error");
			this.content = null;
		}
	}

	public TemplateResult matchIn(String msg) {
		if (StrUtil.isBlank(msg) || StrUtil.isBlank(this.content)) {
			return null;
		}
		TemplateResult templateResult = new TemplateResult();
		int index = 0;
		for (int i = 0; i < contents.length; i++) {
			if (StrUtil.isBlank(contents[i])) {
				if (i == contents.length - 1) {
					templateResult.putParam(paramNames[i - 1], msg.substring(index));
				}
				continue;
			}
			int s = msg.indexOf(contents[i], index);
			if (i == 0) {
				if (s != 0) {
					index = -1;
					break;
				} else {
					index = contents[i].length();
					continue;
				}
			} else if (i == contents.length - 1) {
				if (msg.length() - s != contents[i].length()) {
					index = -1;
					break;
				}
			}
			if (s >= 0) {
				templateResult.putParam(paramNames[i - 1], msg.substring(index, s));
				index = s + contents[i].length();
			} else {
				index = -1;
				break;
			}
		}
		if (index == -1) {
			return null;
		}
		return templateResult;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TemplateInfo that = (TemplateInfo) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
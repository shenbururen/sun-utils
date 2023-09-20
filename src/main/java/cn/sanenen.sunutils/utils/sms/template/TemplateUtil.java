package cn.sanenen.sunutils.utils.sms.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.sanenen.sunutils.utils.sms.template.entity.TemplateInfo;
import cn.sanenen.sunutils.utils.sms.template.entity.TemplateResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 模版匹配工具类
 *
 * @author sun
 * @date 2020-06-12
 **/
public class TemplateUtil {

	/**
	 * 调用模版对象的匹配方法，并返回结果。
	 * 主要为提取模版中变量对应的值。
	 * 例：
	 * 模版：您的欢乐豆还剩余{{count}}个。
	 * 内容：您的欢乐豆还剩余9584个。
	 * 结果：count:9584
	 *
	 * @param templateInfo 模版对象
	 * @param content      待匹配内容
	 * @return TemplateResult
	 */
	public static TemplateResult matchIn(TemplateInfo templateInfo, String content) {
		return templateInfo.matchIn(content);
	}

	/**
	 * 查找内容匹配的模版字符串,适合字符不怎么重复的场景。
	 *
	 * @param templates 模版字符串集合
	 * @param content   待匹配内容
	 * @return 匹配到的模版集合。未匹配返回  new HashSet<>()
	 */
	public static Set<String> matchIn(Collection<String> templates, String content) {
		return matchIn(templates, content, null);
	}

	/**
	 * 查找内容匹配的模版字符串,适合字符不怎么重复的场景。
	 *
	 * @param templates 模版字符串集合
	 * @param content   待匹配内容
	 * @param separator 模版分隔符
	 * @return 匹配到的模版集合。未匹配返回  new HashSet<>()
	 */
	public static Set<String> matchIn(Collection<String> templates, String content, String separator) {
		HashSet<String> set = new HashSet<>();
		if (CollUtil.isEmpty(templates) || StrUtil.isBlank(content)) {
			return set;
		}
		for (String template : templates) {
			if (matchIn(template, content, separator)) {
				set.add(template);
			}
		}
		return set;
	}

	/**
	 * 验证内容是否匹配模版，分隔符为"{{}}",适合字符不怎么重复的场景。
	 *
	 * @param template 模版字符串 例:您的验证码是{{}}
	 * @param content  待匹配内容 例:您的验证码是1234
	 * @return 匹配模版返回true
	 */
	public static boolean matchIn(String template, String content) {
		return matchIn(template, content, null);
	}

	/**
	 * 验证内容是否匹配模版，分隔符为"{{}}",适合字符不怎么重复的场景。
	 *
	 * @param template  模版字符串 例:您的验证码是separator
	 * @param content   待匹配内容 例:您的验证码是1234
	 * @param separator 分隔符
	 * @return 匹配模版返回true
	 */
	public static boolean matchIn(String template, String content, String separator) {
		if (StrUtil.isBlank(template) || StrUtil.isBlank(content)) {
			return false;
		}
		if (StrUtil.isBlank(separator)) {
			separator = "{{}}";
		}
		String[] split = ArrayUtil.toArray(StrUtil.split(template, separator), String.class);

		int index = 0;
		for (int i = 0; i < split.length; i++) {
			if (i == split.length - 1 && StrUtil.isBlank(split[i])) {
				break;
			}
			if (StrUtil.isBlank(split[i]))
				continue;
			int s = content.indexOf(split[i], index);
			if (i == 0) {
				if (s != 0) {
					index = -1;
					break;
				} else {
					index = split[i].length();
					continue;
				}
			}
			if (s >= 0) {
				index = s + split[i].length();
			} else {
				index = -1;
				break;
			}
		}
		return index != -1;
	}
}

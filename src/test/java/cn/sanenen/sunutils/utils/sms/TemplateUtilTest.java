package cn.sanenen.sunutils.utils.sms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import cn.sanenen.sunutils.utils.sms.template.TemplateUtil;
import cn.sanenen.sunutils.utils.sms.template.entity.TemplateInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author sun
 * @date 2021-09-14
 **/
public class TemplateUtilTest {
	@Test
	public void test() {
		Assert.assertTrue(TemplateUtil.matchIn("你好,()哦买噶的", "你好,嘿嘿哦买噶的", "()"));
		Assert.assertTrue(TemplateUtil.matchIn("你好,()哦买噶的", "你好,哦买噶的", "()"));
		Assert.assertTrue(TemplateUtil.matchIn("你好,()哦买噶的", "你好,123456哦买噶的", "()"));
		Assert.assertTrue(TemplateUtil.matchIn("你好,()哦买噶的", "你好,123456()哦买噶的", "()"));
	}

	@Test
	public void test1() {
		Assert.assertTrue(TemplateUtil.matchIn("你好,{{}}哦买噶的", "你好,嘿嘿哦买噶的"));
		Assert.assertTrue(TemplateUtil.matchIn("你好,{{}}哦买噶的{{}}", "你好,嘿嘿哦买噶的"));
		Assert.assertTrue(TemplateUtil.matchIn("你好,{{}}哦买噶的{{}}", "你好,嘿嘿哦买噶的1234"));
	}

	@Test
	public void test2() {
		Assert.assertTrue(TemplateUtil.matchIn("你好,{{}}哦买噶的", "你好,嘿嘿哦买噶的"));
		Assert.assertTrue(TemplateUtil.matchIn("你好,{{}}哦买噶的{{}}", "你好,嘿嘿哦买噶的"));
		Assert.assertTrue(TemplateUtil.matchIn("你好,{{}}哦买噶的{{}}", "你好,嘿嘿哦买噶的1234"));
	}

	@Test
	public void test3() {
		List<String> list = CollUtil.newArrayList(
				"您的验证码是{{}}",
				"您好，您的余额是{{}}");
		Assert.assertTrue(TemplateUtil.matchIn(list, "您的验证码是8578").contains("您的验证码是{{}}"));
		Assert.assertTrue(TemplateUtil.matchIn(list, "您好，您的余额是857亿元").contains("您好，您的余额是{{}}"));
	}

	@Test
	public void test4() {
		List<String> list = CollUtil.newArrayList(
				"您的验证码是()",
				"您好，您的余额是()");
		Assert.assertTrue(TemplateUtil.matchIn(list, "您的验证码是8578", "()").contains("您的验证码是()"));
		Assert.assertTrue(TemplateUtil.matchIn(list, "您好，您的余额是857亿元", "()").contains("您好，您的余额是()"));
	}

	@Test
	public void test5() {
		TemplateInfo templateInfo = new TemplateInfo("{{a}}您的验证码是{{b}},{{c}}{{d}}");
		Console.log(TemplateUtil.matchIn(templateInfo, "1的您的验证码是854587,34的,/。$").getParamMap());
	}
}

package cn.sanenen.sms;

import cn.sanenen.utils.sms.SignUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * 短信签名工具测试及使用
 *
 * @author sun
 * @date 2021-09-07
 **/
public class SignUtilTest {
	@Test
	public void hasSignTest() {
		boolean isHas = SignUtil.hasSign("【宇宙银行】您的验证码是88548");
		Assert.assertTrue(isHas);
	}

	@Test
	public void getSignTest() {
		String sign = SignUtil.getSign("【宇宙银行】您的验证码是88548");
		Assert.assertEquals("宇宙银行", sign);
	}
	@Test
	public void afterSignTest() {
		String content = SignUtil.afterSign("【宇宙银行】您的验证码是88548");
		Assert.assertEquals("您的验证码是88548【宇宙银行】", content);
	}
	@Test
	public void frontSignTest() {
		String content = SignUtil.frontSign("您的验证码是88548【宇宙银行】");
		Assert.assertEquals("【宇宙银行】您的验证码是88548", content);
	}
	@Test
	public void removeSignTest() {
		String content = SignUtil.removeSign("【宇宙银行】您的验证码是88548");
		Assert.assertEquals("您的验证码是88548", content);
	}

}

package cn.sanenen.sunutils.utils.sms;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.RandomUtil;
import cn.sanenen.sunutils.thread.ManyThreadRun;
import cn.sanenen.sunutils.utils.sms.phonegeo.PhoneNumberGeo;
import cn.sanenen.sunutils.utils.sms.phonegeo.PhoneNumberInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author sun
 * @date 2021-10-09
 **/
public class PhoneGeoTest {
	@Test
	public void test() {
		PhoneNumberInfo lookup = PhoneNumberGeo.lookup("1599406");
		assert lookup != null;
		Assert.assertEquals(lookup.getCityCode(), "411000");
	}
	@Test
	public void test2() {
		PhoneNumberInfo lookup = PhoneNumberGeo.lookup("1599406");
		Console.log(lookup);
	}

	@Test
	public void test1() {
		AtomicLong find = new AtomicLong(0);
		AtomicLong noFind = new AtomicLong(0);
		ManyThreadRun.run(5, 10, () -> {
			String mobile = getMobile();
			PhoneNumberInfo lookup = PhoneNumberGeo.lookup(mobile);
			if (lookup == null) {
				Console.log(mobile);
				noFind.incrementAndGet();
			} else {
				find.incrementAndGet();
			}
		});
		System.out.println(noFind.get());
		System.out.println(find.get());
	}

	private final static int[] optArr = {
			130, 131, 132, 133, 134, 135, 136, 137, 138, 139,
			150, 151, 152, 153, 155, 156, 157, 158, 159,
			162, 165, 166, 167,
			170, 171, 172, 173, 175, 176, 177, 178,
			180, 181, 182, 183, 184, 185, 186, 187, 188, 189,
			191, 195, 198, 199
	};

	public static String getMobile() {
		StringBuilder mobile = new StringBuilder();
		int startNum = optArr[RandomUtil.randomInt(optArr.length)];
		mobile.append(startNum);
		mobile.append(RandomUtil.randomNumbers(11 - mobile.length()));
		return mobile.toString();
	}
}

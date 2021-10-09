package cn.sanenen.utils.sms;

import cn.sanenen.utils.sms.phonegeo.PhoneNumberGeo;
import cn.sanenen.utils.sms.phonegeo.PhoneNumberInfo;
import org.junit.Assert;
import org.junit.Test;

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

}

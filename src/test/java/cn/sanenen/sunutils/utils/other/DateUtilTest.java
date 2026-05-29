package cn.sanenen.sunutils.utils.other;

import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class DateUtilTest {

	@Test
	public void isInterceptRejectsInvalidTimeFormat() {
		assertThrows(IllegalArgumentException.class, () -> DateUtil.isIntercept("bad", "12:00"));
	}

	@Test
	public void isInterceptRejectsSameInvalidTimeFormat() {
		assertThrows(IllegalArgumentException.class, () -> DateUtil.isIntercept("bad", "bad"));
	}
}

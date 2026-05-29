package cn.sanenen.sunutils.utils.other;

import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class OtherUtilBehaviorTest {

	@Test
	public void progressRejectsInvalidTotal() {
		assertThrows(IllegalArgumentException.class, () -> OtherUtil.printSimpleProgress(0, 0));
		assertThrows(IllegalArgumentException.class, () -> OtherUtil.printProgressWithETA(0, 0, System.currentTimeMillis()));
	}

	@Test
	public void progressRejectsNegativeCurrent() {
		assertThrows(IllegalArgumentException.class, () -> OtherUtil.printSimpleProgress(-1, 10));
		assertThrows(IllegalArgumentException.class, () -> OtherUtil.printProgressWithETA(-1, 10, System.currentTimeMillis()));
	}
}

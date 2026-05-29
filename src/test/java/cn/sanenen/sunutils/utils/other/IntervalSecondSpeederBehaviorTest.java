package cn.sanenen.sunutils.utils.other;

import org.junit.Test;

import static org.junit.Assert.*;

public class IntervalSecondSpeederBehaviorTest {

	@Test
	public void limitSpeedRestoresInterruptedStatus() {
		IntervalSecondSpeeder speeder = new IntervalSecondSpeeder(1);
		try {
			speeder.limitSpeed();
			Thread.currentThread().interrupt();

			speeder.limitSpeed();

			assertTrue(Thread.currentThread().isInterrupted());
		} finally {
			Thread.interrupted();
			speeder.close();
		}
	}

	@Test
	public void negativeBatchSizeIsRejected() {
		IntervalSecondSpeeder speeder = new IntervalSecondSpeeder(10);
		try {
			assertThrows(IllegalArgumentException.class, () -> speeder.limitSpeed(-1));
		} finally {
			speeder.close();
		}
	}
}

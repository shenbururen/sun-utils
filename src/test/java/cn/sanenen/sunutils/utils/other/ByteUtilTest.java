package cn.sanenen.sunutils.utils.other;

import org.junit.Test;

import static org.junit.Assert.*;

public class ByteUtilTest {

	@Test
	public void concatTreatsNullArraysAsEmpty() {
		byte[] result = ByteUtil.concat(new byte[]{1}, null, new byte[]{2, 3});

		assertArrayEquals(new byte[]{1, 2, 3}, result);
	}

	@Test
	public void fillRejectsNegativeLength() {
		assertThrows(IllegalArgumentException.class, () -> ByteUtil.rFillBytes(new byte[]{1}, -1));
		assertThrows(IllegalArgumentException.class, () -> ByteUtil.lFillBytes(new byte[]{1}, -1));
	}

	@Test
	public void fillReturnsCopyWhenLengthUnchanged() {
		byte[] src = new byte[]{1, 2};

		assertNotSame(src, ByteUtil.rFillBytes(src, 2));
		assertNotSame(src, ByteUtil.lFillBytes(src, 2));
	}

	@Test
	public void trimReturnsCopyWhenNoTrimNeeded() {
		byte[] src = new byte[]{1, 2};

		assertNotSame(src, ByteUtil.rtrimBytes(src));
		assertNotSame(src, ByteUtil.ltrimBytes(src));
	}
}

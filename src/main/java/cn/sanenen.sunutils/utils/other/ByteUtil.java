package cn.sanenen.sunutils.utils.other;

/**
 * 扩展hutool的byteUtil
 * @author sun
 * @date 2021-11-10
 **/
public class ByteUtil extends cn.hutool.core.util.ByteUtil {
	
	/**
	 * 从 guava 复制过来的。
	 * 将每个提供的数组的值组合成一个数组返回。
	 * 例如，concat(new byte[] {a, b}， new byte[] {}， new byte[] {c}
	 * 返回数组{a, b, c}。
	 *
	 * @param arrays ——零个或多个字节数组
	 * @return 一个数组，按顺序包含源数组中的所有值
	 */
	public static byte[] concat(byte[]... arrays) {
		int length = 0;
		for (byte[] array : arrays) {
			length += array.length;
		}
		byte[] result = new byte[length];
		int pos = 0;
		for (byte[] array : arrays) {
			System.arraycopy(array, 0, result, pos, array.length);
			pos += array.length;
		}
		return result;
	}

	/**
	 * 将数组右填充0至len长度，或者截取至len长度。
	 * @param src 待补齐数组
	 * @param len 最终长度
	 * @return 结果
	 */
	public static byte[] rFillBytes(byte[] src, int len) {
		if (src.length == len) {
			return src;
		} else {
			byte[] copy = new byte[len];
			System.arraycopy(src, 0, copy, 0, Math.min(src.length, len));
			return copy;
		}
	}
	/**
	 * 将数组左填充0至len长度，或者截取至len长度。
	 * @param src 待补齐数组
	 * @param len 最终长度
	 * @return 结果
	 */
	public static byte[] lFillBytes(byte[] src, int len) {
		if (src.length == len) {
			return src;
		} else if (src.length > len) {
			byte[] tmp = new byte[len];
			System.arraycopy(src, 0, tmp, 0, len);
			return tmp;
		} else {
			byte[] tmp = new byte[len];
			System.arraycopy(src, 0, tmp, len - src.length, src.length);
			return tmp;
		}
	}

	/**
	 * 移除给定字节数组 右侧空字节
	 * @param src 待移除的字节数组
	 * @return 结果
	 */
	public static byte[] rtrimBytes(byte[] src) {
		int i = src.length - 1;
		for (; i >= 0; i--) {
			if (src[i] != 0) {
				break;
			}
		}
		if (i == src.length - 1) {
			return src;
		}
		if (i == -1) {
			return new byte[0];
		}
		byte[] tmp = new byte[i + 1];
		System.arraycopy(src, 0, tmp, 0, i + 1);
		return tmp;
	}
	
	/**
	 * 移除给定字节数组 左侧空字节
	 * @param src 待移除的字节数组
	 * @return 结果
	 */
	public static byte[] ltrimBytes(byte[] src) {
		int i = 0;
		for (; i < src.length; i++) {
			if (src[i] != 0) {
				break;
			}
		}
		if (i == src.length) {
			return new byte[0];
		}
		if (i == 0) {
			return src;
		}
		byte[] tmp = new byte[src.length - i];
		System.arraycopy(src, i, tmp, 0, src.length - i);
		return tmp;
	}
}

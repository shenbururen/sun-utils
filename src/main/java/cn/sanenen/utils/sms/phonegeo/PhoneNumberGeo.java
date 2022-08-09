package cn.sanenen.utils.sms.phonegeo;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class PhoneNumberGeo {
	private static final Log log = Log.get();

	private static final int INDEX_SEGMENT_LENGTH = 9;
	private static final String PHONE_DAT_FILE_PATH = "phone-number-geo/phone.dat";

	private static byte[] dataByteArray;
	private static int indexAreaOffset;
	private static int phoneRecordCount;

	private static ByteBuffer BYTE_BUFFER;

	private PhoneNumberGeo() {
	}

	static {
		initData();
	}

	private static synchronized void initData() {
		if (dataByteArray == null) {
			dataByteArray = ResourceUtil.readBytes(PHONE_DAT_FILE_PATH);
			BYTE_BUFFER = ByteBuffer.wrap(dataByteArray).asReadOnlyBuffer().order(ByteOrder.BIG_ENDIAN);
			
			byte[] version = new byte[4];
			BYTE_BUFFER.get(version);
			log.info("手机号归属地数据版本：{}", new String(version));
			indexAreaOffset = BYTE_BUFFER.getInt();
			phoneRecordCount = (dataByteArray.length - indexAreaOffset) / INDEX_SEGMENT_LENGTH;
			log.info("手机号归属地数据数量：{}", phoneRecordCount);
		}
	}

	public static PhoneNumberInfo lookup(String phoneNumber) {
		ByteBuffer byteBuffer = BYTE_BUFFER.asReadOnlyBuffer().order(ByteOrder.BIG_ENDIAN);
		if (phoneNumber == null || phoneNumber.length() > 11 || phoneNumber.length() < 7) {
			return null;
		}
		int phoneNumberPrefix;
		try {
			phoneNumberPrefix = Integer.parseInt(phoneNumber.substring(0, 7));
		} catch (Exception e) {
			return null;
		}
		int left = 0;
		int right = phoneRecordCount;
		while (left <= right) {
			int middle = (left + right) >> 1;
			int currentOffset = indexAreaOffset + middle * INDEX_SEGMENT_LENGTH;
			if (currentOffset >= dataByteArray.length) {
				return null;
			}

			byteBuffer.position(currentOffset);
			int currentPrefix = byteBuffer.getInt();
			if (currentPrefix > phoneNumberPrefix) {
				right = middle - 1;
			} else if (currentPrefix < phoneNumberPrefix) {
				left = middle + 1;
			} else {
				int infoBeginOffset = byteBuffer.getInt();
				int phoneType = byteBuffer.get();

				int infoLength = -1;
				for (int i = infoBeginOffset; i < indexAreaOffset; ++i) {
					if (dataByteArray[i] == 0) {
						infoLength = i - infoBeginOffset;
						break;
					}
				}

				String infoString = new String(dataByteArray, infoBeginOffset, infoLength, StandardCharsets.UTF_8);
				String[] infoSegments = StrUtil.splitToArray(infoString, "|");

				PhoneNumberInfo phoneNumberInfo = new PhoneNumberInfo();
				phoneNumberInfo.setPhoneNumber(phoneNumber);
				phoneNumberInfo.setProvince(infoSegments[0]);
				phoneNumberInfo.setCity(infoSegments[1]);
				phoneNumberInfo.setZipCode(infoSegments[2]);
				phoneNumberInfo.setAreaCode(infoSegments[3]);
				phoneNumberInfo.setCityCode(infoSegments[4]);
				phoneNumberInfo.setPhoneType(phoneType + "");
				return phoneNumberInfo;
			}
		}
		return null;
	}

}



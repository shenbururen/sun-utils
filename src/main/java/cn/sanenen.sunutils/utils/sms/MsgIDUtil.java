package cn.sanenen.sunutils.utils.sms;

import cn.hutool.core.util.RandomUtil;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 短信cmpp协议msgid工具类
 */
public class MsgIDUtil {
	private static int MsgSeq = (int) (Math.random() * 9999.0D);

	private static final long GwSeq = RandomUtil.randomLong(1, 32767L);

	private final static AtomicInteger sequenceId = new AtomicInteger(RandomUtil.randomInt());

	/**
	 * 生成一个四字节 消息头id，循环使用。
	 * @return 消息头id
	 */
	public static int getSequenceNo() {
		return sequenceId.incrementAndGet();
	}
	
	/**
	 * 生成cmpp协议的msgId
	 *
	 * @return msgId
	 */
	public static synchronized long getMsgId() {
		Calendar c = Calendar.getInstance();
		long month = c.get(Calendar.MONTH) + 1;
		long day = c.get(Calendar.DATE);
		long hour = c.get(Calendar.HOUR_OF_DAY);
		long minute = c.get(Calendar.MINUTE);
		long second = c.get(Calendar.SECOND);
		return month << 60 | day << 55 | hour << 50 | minute << 44 | second << 38 | GwSeq << 16 | getMsgidSeq();
	}

	/**
	 * 生成一个20位可读的msgid，可以用于smgp协议（非标准）。
	 *
	 * @return 可读的msgid
	 */
	public static synchronized String getStrMsgId() {
		Calendar c = Calendar.getInstance();
		long month = c.get(Calendar.MONTH) + 1;
		long day = c.get(Calendar.DATE);
		long hour = c.get(Calendar.HOUR_OF_DAY);
		long minute = c.get(Calendar.MINUTE);
		long second = c.get(Calendar.SECOND);
		return String.format("%1$02d%2$02d%3$02d%4$02d%5$02d%6$05d%7$05d",
				month, day, hour, minute, second, GwSeq, getMsgidSeq());
	}

	/**
	 * 将long类型msgId转为可读msgId
	 *
	 * @param msgId 八字节long类型
	 * @return 可读msgId
	 */
	public static String showMsgID(long msgId) {
		long mm = (msgId >>> 60) & 0xf;
		long dd = (msgId >>> 55) & 0x1f;
		long HH = (msgId >>> 50) & 0x1f;
		long MM = (msgId >>> 44) & 0x3f;
		long SS = (msgId >>> 38) & 0x3f;
		long gw = (msgId >>> 16) & 0x3fffff;
		long sq = msgId & 0xffff;
		return String.format("%1$02d%2$02d%3$02d%4$02d%5$02d%6$07d%7$05d",
				mm, dd, HH, MM, SS, gw, sq);
	}

	/**
	 * 将可读msgId转为八字节long类型
	 *
	 * @param msgId 可读msgId
	 * @return long类型
	 */
	public static long msgIdSTL(String msgId) {
		long mm = Long.parseLong(msgId.substring(0, 2));
		long dd = Long.parseLong(msgId.substring(2, 4));
		long HH = Long.parseLong(msgId.substring(4, 6));
		long MM = Long.parseLong(msgId.substring(6, 8));
		long SS = Long.parseLong(msgId.substring(8, 10));
		long gw = Long.parseLong(msgId.substring(10, 17));
		long sq = Long.parseLong(msgId.substring(17));
		return mm << 60 | dd << 55 | HH << 50 | MM << 44 | SS << 38 | gw << 16 | sq;
	}

	public static synchronized int getMsgidSeq() {
		//二字节 有符号最大 32767
		if (MsgSeq > 32767) {
			MsgSeq = 100;
		}
		return MsgSeq++;
	}
}

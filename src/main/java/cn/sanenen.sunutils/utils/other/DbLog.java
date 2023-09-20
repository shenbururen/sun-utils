package cn.sanenen.sunutils.utils.other;

import cn.hutool.log.Log;

/**
 * 有错误时，使用此类将数据持久化到本地
 */
public class DbLog {
	private static final Log log = Log.get();

	public static void log(String msg, Object... objects) {
		log.info(msg, objects);
	}
}

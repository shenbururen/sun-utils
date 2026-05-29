package cn.sanenen.sunutils.utils.other;

import cn.hutool.log.Log;

/**
 * 队列组件错误日志代理。
 */
public class DbLog {
	private static final Log log = Log.get();

	public static void log(String msg, Object... objects) {
		log.info(msg, objects);
	}
}

package cn.sanenen;

import cn.hutool.core.io.FileUtil;
import cn.hutool.setting.Setting;

/**
 * 参数配置
 *
 * @author sun
 * @date 2021-09-01
 **/
public class SunSetting {
	private static final Setting setting;

	static {
		if (FileUtil.exist("sun.setting")){
			setting = new Setting("sun.setting");
		}else {
			setting = new Setting();
		}
	}

	public static long getConnectTimeout() {
		return setting.getLong("connectTimeout", "http", 10L);
	}

	public static long getRequestTimeout() {
		return setting.getLong("requestTimeout", "http", 30L);
	}

	public static int getMaxConnTotal() {
		return setting.getInt("maxConnTotal", "http", 200);
	}
}

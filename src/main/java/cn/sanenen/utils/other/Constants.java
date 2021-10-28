package cn.sanenen.utils.other;

import cn.hutool.core.net.NetUtil;

/**
 * 一些常量
 * @author sun
 * @date 2021-10-28
 **/
public interface Constants {
	/**
	 * 运行应用的服务器ip
	 */
	String SERVER_IP = NetUtil.getLocalhost().getHostAddress();
}

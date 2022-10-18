package cn.sanenen.utils.other;

import cn.hutool.core.util.StrUtil;

import java.util.Collections;
import java.util.List;

/**
 * 定义一些空常量
 * @author sun
 * @date 2021-10-29
 **/
public interface Emptys {
	String STR = StrUtil.EMPTY;
	byte[] BYTES = new byte[0];
	String[] STR_ARRAY = new String[0];
	int[] INT_ARRAY = new int[0];
	List<Object> LIST = Collections.emptyList();
}

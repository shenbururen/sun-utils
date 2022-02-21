package cn.sanenen.utils.other;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.SystemClock;
import cn.hutool.core.util.StrUtil;

/**
 * 扩展hutool 日期工具类
 *
 * @author sun
 * @date 2022-02-21
 **/
public class DateUtil extends cn.hutool.core.date.DateUtil {

	/**
	 * 判断是否在拦截时段内
	 *
	 * @param start 格式HH:mm 或 HH:mm:ss
	 * @param end   格式HH:mm 或 HH:mm:ss
	 * @return true 在拦截时段内
	 */
	public boolean isIntercept(String start, String end) {
		if (StrUtil.isAllNotBlank(start, end)) {
			//全天24小时拦截
			if (start.equals(end)) {
				return true;
			}
			DateTime startDate = parseTimeToday(start);
			DateTime endDate = parseTimeToday(end);
			DateTime now = new DateTime(SystemClock.now());
			//结束日期在开始日期之前
			if (endDate.isBefore(startDate)) {
				//如果当前时间在结束时间之前，在拦截中。
				if (now.isBefore(endDate)) {
					return true;
				}
				//结束日期加一天
				endDate = cn.hutool.core.date.DateUtil.offsetDay(endDate, 1);
			}
			return cn.hutool.core.date.DateUtil.isIn(now, startDate, endDate);
		} else {
			return false;
		}
	}
}

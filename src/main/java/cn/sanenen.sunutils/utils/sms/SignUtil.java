package cn.sanenen.sunutils.utils.sms;

/**
 * 签名工具类
 *
 * @author sun
 * 2019年2月13日 下午3:47:48
 */
public class SignUtil {

	/**
	 * 是否包含签名
	 *
	 * @param content 内容
	 * @return true 包含
	 */
	public static boolean hasSign(String content) {
		boolean haveSign = false;
		// 前置签名的情况
		if ((content.startsWith("【") && content.indexOf("】") > 0)
				|| (content.startsWith("[") && content.indexOf("]") > 0)) {
			haveSign = true;
		}
		// 后置签名的情况
		if ((content.endsWith("】") && content.indexOf("【") > 0)
				|| (content.endsWith("]") && content.indexOf("[") > 0)) {
			haveSign = true;
		}
		return haveSign;
	}

	/**
	 * 签名抽取方法
	 *
	 * @param content 短信内容
	 */
	public static String getSign(String content) {
		String sign = "";
		boolean fontFlag = false;
		int startIndex;// 半角签名
		int startQIndex;// 全角签名
		int endIndex;// 半角签名
		int endQIndex;// 全角签名
		if (content.startsWith("[") || content.startsWith("【")) {
			fontFlag = true;
		}
		// 签名前置
		if (fontFlag) {
			// 签名前置中文符号的情况
			if (content.startsWith("【")) {
				startQIndex = 0;
				endQIndex = content.indexOf("】");
				if (endQIndex > 0)
					sign = content.substring(startQIndex + 1, endQIndex);
			}
			// 签名前置英文符号的情况
			if (content.startsWith("[")) {
				startIndex = 0;
				endIndex = content.indexOf("]");
				if (endIndex > 0)
					sign = content.substring(startIndex + 1, endIndex);
			}

		} else {
			if (content.endsWith("】")) {
				startQIndex = content.lastIndexOf("【");
				if (startQIndex > -1)
					sign = content.substring(startQIndex + 1, content.length() - 1);
			}
			if (content.endsWith("]")) {
				startIndex = content.lastIndexOf("[");
				if (startIndex > -1)
					sign = content.substring(startIndex + 1, content.length() - 1);
			}
		}
		return sign;
	}

	/**
	 * 将签名后置
	 *
	 * @param content
	 * @return
	 */
	public static String afterSign(String content) {
		String tempCon = content;
		String sign = "";
		// 判断签名是否前置了
		if (content.startsWith("【")) {
			int index = content.indexOf("】");
			if (index > 0) {//确保有后半个括号
				sign = content.substring(0, index + 1);
				tempCon = content.substring(index + 1);
				String dealCont = tempCon + sign;
				return dealCont;
			}
		} else if (content.startsWith("[")) {
			int qIndex = content.indexOf("]");
			if (qIndex > 0) {//确保有后半个括号
				sign = content.substring(0, qIndex + 1);//截取到签名
				tempCon = content.substring(qIndex + 1);//截取到内容
				String dealCont = tempCon + sign;
				return dealCont;
			}
		}
		return content;
	}

	/**
	 * 将签名前置
	 *
	 * @param content
	 * @return
	 */
	public static String frontSign(String content) {
		String tempCont = content;
		String sign = "";
		// 判断签名是否后置了
		if (content.endsWith("】")) {
			int index = content.lastIndexOf("【");
			if (index > 0) {//确保有前半个括号
				sign = content.substring(index);
				tempCont = content.substring(0, index);
				String dealCont = sign + tempCont;
				return dealCont;
			}
		} else if (content.endsWith("]")) {
			int qIndex = content.lastIndexOf("[");
			if (qIndex > 0) {//确保有前半个括号
				sign = content.substring(qIndex);
				tempCont = content.substring(0, qIndex);
				String dealCont = sign + tempCont;
				return dealCont;
			}
		}
		return content;
	}

	/**
	 * 移除签名
	 *
	 * @param content
	 * @return
	 */
	public static String removeSign(String content) {
		String tempCont = content;
		// 前置签名情况
		if (content.startsWith("【")) {
			int index = content.indexOf("】");
			if (index > 0)
				tempCont = content.substring(index + 1);
		} else if (content.startsWith("[")) {
			int qIndex = content.indexOf("]");
			if (qIndex > 0)
				tempCont = content.substring(qIndex + 1);
		} else if (content.endsWith("】")) {
			int index = content.lastIndexOf("【");
			if (index > 0)
				tempCont = content.substring(0, index);
		} else if (content.endsWith("]")) {
			int qIndex = content.lastIndexOf("[");
			if (qIndex > 0)
				tempCont = content.substring(0, qIndex);
		}
		return tempCont;
	}

	public static void main(String[] args) {
		String content = "【签名】【签名2】内容[签名]";

		System.out.println(removeSign(content));
		System.out.println(getSign(content));
		System.out.println(afterSign(content));
	}
}

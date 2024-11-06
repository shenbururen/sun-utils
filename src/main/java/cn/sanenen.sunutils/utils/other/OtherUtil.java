package cn.sanenen.sunutils.utils.other;

/**
 *  其他工具
 * @author sun
 * @date 2024-11-06
 **/
public class OtherUtil {
    /**
     * 根据百分比判断 是否在比率内
     * 说明：调用10次，参数为40，则10次中4次左右会返回true
     *
     * @param percentage 百分比
     * @return true 在比率内
     */
    public static boolean isInPercentage(double percentage) {
        double random = Math.random() * 100;
        return random <= percentage;
    }
}

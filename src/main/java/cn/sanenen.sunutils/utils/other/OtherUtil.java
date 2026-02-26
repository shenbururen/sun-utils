package cn.sanenen.sunutils.utils.other;

import java.util.concurrent.TimeUnit;

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
    
    /**
     * 打印一个简单的进度条
     *
     * @param current 当前进度值
     * @param total   总的进度值
     */
    public static void printSimpleProgress(int current, int total) {
        // 计算百分比，使用 double 避免整数除法
        double percentage = (double) current / total;

        // 定义进度条总长度
        final int barLength = 50;
        // 计算已完成部分的长度
        int filledLength = (int) (barLength * percentage);

        // 构建进度条字符串
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filledLength) {
                bar.append("█"); // 或者用 "=" 也行
            } else {
                bar.append("░"); // 或者用 "-" 或 " "
            }
        }
        bar.append("] ");

        // 格式化百分比，保留两位小数
        System.out.printf("\r%s%3.1f%%", bar, percentage * 100);

        // 在最后一次调用时换行
        if (current >= total) {
            System.out.println(); // 换行，防止后续输出覆盖进度条
        }
    }

    /**
     * 打印一个带预估剩余时间的进度条
     *
     * @param current 当前进度值
     * @param total   总的进度值
     * @param startTime 进度条开始时间
     */
    public static void printProgressWithETA(int current, int total, long startTime) {
        if (current == 0) {
            startTime = System.currentTimeMillis();
        }

        double percentage = (double) current / total;
        long currentTimeMillis = System.currentTimeMillis();
        long elapsedMillis = currentTimeMillis - startTime;

        // 计算预估总耗时（毫秒）
        long estimatedTotalMillis = (long) (elapsedMillis / percentage);
        // 计算预估剩余耗时（毫秒）
        long remainingMillis = estimatedTotalMillis - elapsedMillis;

        // 定义进度条总长度
        final int barLength = 50;
        int filledLength = (int) (barLength * percentage);

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filledLength) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        bar.append("] ");

        String etaString;
        if (current == 0) {
            etaString = "N/A"; // 初始状态无法估算
        } else {
            // 将毫秒转换为更易读的格式 (分钟:秒)
            etaString = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(remainingMillis),
                    TimeUnit.MILLISECONDS.toSeconds(remainingMillis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingMillis))
            );
        }

        System.out.printf("\r%s%3.1f%% (剩余: %s)", bar, percentage * 100, etaString);

        if (current >= total) {
            System.out.println(); // 换行
        }
    }
}

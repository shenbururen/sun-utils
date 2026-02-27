package cn.sanenen.sunutils.utils.other;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author sun
 * @date 2026-02-27
 **/
public class OtherUtilTest {
    public static void main(String[] args) throws Exception {
        printProgressWithETATest();
    }
    public static void printProgressWithETATest() throws Exception {
        int size = 100;
        AtomicInteger count = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(10);
        long l = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            ThreadUtil.execute(() -> {
                try {
                    for (int i1 = 0; i1 < 10; i1++) {
                        ThreadUtil.sleep(RandomUtil.randomInt(100, 1000));
                        int i2 = count.incrementAndGet();
                        OtherUtil.printProgressWithETA(i2, size, l);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        System.out.println("结束");
    }
}

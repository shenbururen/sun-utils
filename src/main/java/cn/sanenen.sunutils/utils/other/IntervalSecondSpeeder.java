package cn.sanenen.sunutils.utils.other;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程安全的速度控制类
 */
public class IntervalSecondSpeeder {
	//间隔时间最大发送速度,比如：间隔时间=100MS，那么该变量=每100MS最大可以发多少条。
	private int intervalMaxCount;
	//间隔时间，单位毫秒
	private final int intervalInMillis = 100;
	//发送计数
	private final AtomicInteger secondCount = new AtomicInteger();
	//计数的时间边界
	private final AtomicLong time = new AtomicLong(System.currentTimeMillis());
	//同步锁对象1
	private final Object object = new Object();
	//同步锁对象2
	private final Object object2 = new Object();


	/**
	 * 构造方法
	 */
	public IntervalSecondSpeeder(int speed) {
		this.intervalMaxCount = speed * intervalInMillis / 1000;
		if (this.intervalMaxCount <= 0) {
			this.intervalMaxCount = 1;
		}
		Thread clearThread = new SendCountClearThread();
		clearThread.setDaemon(true);
		clearThread.start();
	}

	public void setIntervalMaxCount(int speed) {
		int tmpintervalMaxCount = speed * intervalInMillis / 1000;
		if (tmpintervalMaxCount <= 0) {
			tmpintervalMaxCount = 1;
		}
		if (this.intervalMaxCount != tmpintervalMaxCount) {
			this.intervalMaxCount = tmpintervalMaxCount;
		}
	}

	public synchronized void limitSpeed() {
		while (secondCount.get() >= intervalMaxCount) {
			try {
				synchronized (object) {
					object.wait(1);
				}
			} catch (InterruptedException e) {
			}
		}
		secondCount.incrementAndGet();
	}

	/**
	 * 判断是否超速。超速了返回true，没超速FALSE
	 */
	public synchronized boolean isFast() {
		if (secondCount.get() >= intervalMaxCount) {
			return true;
		}
		secondCount.incrementAndGet();
		return false;
	}

	public synchronized void limitSpeed(int size) {
		int needAddCount = size;
		while (needAddCount > 0) {
			if (secondCount.get() >= intervalMaxCount) {
				try {
					synchronized (object) {
						object.wait(1);
					}
				} catch (InterruptedException e) {
				}
			} else {
				int newCount = secondCount.addAndGet(needAddCount);
				needAddCount = newCount > intervalMaxCount ? newCount - intervalMaxCount : 0;
			}
		}
	}


	private class SendCountClearThread extends Thread {
		@Override
		public void run() {
			time.set(System.currentTimeMillis());
			while (true) {
				try {
					long waitTime = intervalInMillis - System.currentTimeMillis() + time.get();
					if (waitTime > 0) {
						synchronized (object2) {
							object2.wait(waitTime);
						}
					}
					time.addAndGet(intervalInMillis);
					secondCount.set(0);
				} catch (Exception e) {
				}
			}
		}
	}


//    public static void main(String[] args) {
//        IntervalSecondSpeeder speeder = new IntervalSecondSpeeder(100000, 250);
//        long start = System.currentTimeMillis();
//        AtomicLong aLong = new AtomicLong(0);
//        while (true) {
//            if (System.currentTimeMillis() - start >= 5000) {
//                break;
//            }
//            speeder.limitSpeed();
//            aLong.incrementAndGet();
//        }
//        System.out.println(aLong.get());
//    }
}

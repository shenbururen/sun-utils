package cn.sanenen.thread;

import cn.hutool.log.Log;

/**
 * 标准线程类，继承该类，覆写handler方法。
 *
 * @author sun
 */
public abstract class StandardThread implements Runnable {
	protected static final Log log = Log.get();

	private boolean runFlag = true;

	@Override
	public void run() {
		while (runFlag) {
			handler();
		}
	}

	/**
	 * 线程初始化
	 */
	public void init() {
		initBefore();
		Thread thread = new Thread(this);
		thread.start();
		initAfter();
		log.info("{} init", this.getClass().getSimpleName());
	}

	/**
	 * 线程关闭调用
	 */
	public void close() {
		closeBefore();
		runFlag = false;
		closeAfter();
		log.info("{} close", this.getClass().getSimpleName());
	}

	/**
	 * 具体业务处理，子类自己处理异常和睡眠。
	 */
	public abstract void handler();

	public void initBefore() {}

	public void initAfter() {}

	public void closeBefore() {}

	public void closeAfter() {}

}
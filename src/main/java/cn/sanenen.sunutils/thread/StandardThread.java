package cn.sanenen.sunutils.thread;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;

/**
 * 无限循环执行标准线程类，继承该类，覆写handler方法。
 *
 * @author sun
 */
public abstract class StandardThread implements Runnable {
	protected final Log log = Log.get(this.getClass());

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
	protected abstract void handler();

	/**
	 * handler处理前执行
	 */
	protected void handlerBefore() {}

	/**
	 * handler处理后执行
	 */
	protected void handlerAfter() {
		ThreadUtil.sleep(1000);
	}
	/**
	 * 线程启动前执行
	 */
	protected void initBefore() {}

	/**
	 * 线程启动后执行
	 */
	protected void initAfter() {}

	/**
	 * 线程关闭后执行
	 */
	protected void closeBefore() {}

	/**
	 * 线程关闭后执行
	 */
	protected void closeAfter() {}

}
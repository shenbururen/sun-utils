package cn.sanenen.thread;

import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 阻塞式线程池<br>
 * 此线程池最大特点：当线程中没有空闲线程时，不调用RejectedExecutionHandler，而是直接处于阻塞状态<br>
 * @author sun 
 */
public class BlockedThreadPoolExecutor extends ThreadPoolExecutor {

	private final Semaphore semaphore;

	/**
	 * @param poolSize 线程数量
	 */
    public BlockedThreadPoolExecutor(int poolSize) {
	    super(0, Integer.MAX_VALUE,
			    60L, TimeUnit.SECONDS,
			    new SynchronousQueue<>());
	    semaphore = new Semaphore(poolSize);
    }

	
    @Override
	public void execute(Runnable command) {
	    try {
		    semaphore.acquire();
		    super.execute(command);
	    } catch (InterruptedException e) {
		    e.printStackTrace();
	    }
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		semaphore.release();
	}

}

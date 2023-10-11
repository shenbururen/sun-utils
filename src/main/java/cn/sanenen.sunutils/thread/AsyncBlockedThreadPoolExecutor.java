package cn.sanenen.sunutils.thread;

import cn.hutool.log.Log;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.ListenableFutureTask;

import java.util.concurrent.*;

/**
 * "@async"使用阻塞式线程池  方法不可以有返回值
 * 使用方法：
 *
 * @Bean public AsyncTaskExecutor taskExecutor(){
 * AsyncBlockedThreadPoolExecutor executor = new AsyncBlockedThreadPoolExecutor(线程数量);
 * executor.setThreadNamePrefix("asyncBlocked");
 * return executor;
 * }
 */
public class AsyncBlockedThreadPoolExecutor extends ThreadPoolTaskExecutor {
	private static final Log log = Log.get();
	private final Semaphore semaphore;

	public AsyncBlockedThreadPoolExecutor(int poolSize) {
		super.setCorePoolSize(poolSize);
		semaphore = new Semaphore(poolSize);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		try {
			semaphore.acquire();
			ListenableFutureTask<T> listenableFutureTask = new ListenableFutureTask<>(task);
			listenableFutureTask.addCallback(new ListenableFutureCallback<Object>() {
				@Override
				public void onFailure(Throwable ex) {
					semaphore.release();
					log.error(ex);
				}

				@Override
				public void onSuccess(Object result) {
					semaphore.release();
				}
			});
			super.submitListenable(listenableFutureTask);
		} catch (InterruptedException e) {
			log.error(e);
		}
		//spring默认就调用带返回值的，这里不得已返回个没有意义的Future。
		return new Future<T>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public boolean isDone() {
				return false;
			}

			@Override
			public T get() throws InterruptedException, ExecutionException {
				return null;
			}

			@Override
			public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				return null;
			}
		};
	}
}
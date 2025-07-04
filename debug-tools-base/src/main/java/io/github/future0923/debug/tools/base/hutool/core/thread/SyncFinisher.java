/*
 * Copyright (C) 2024-2025 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.future0923.debug.tools.base.hutool.core.thread;

import io.github.future0923.debug.tools.base.hutool.core.exceptions.UtilException;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * 线程同步结束器<br>
 * 在完成一组正在其他线程中执行的操作之前，它允许一个或多个线程一直等待。
 *
 * <pre>
 * ps:
 * //模拟1000个线程并发
 * SyncFinisher sf = new SyncFinisher(1000);
 * sf.addWorker(() -&gt; {
 *      // 需要并发测试的业务代码
 * });
 * sf.start()
 * </pre>
 *
 * @author Looly
 * @since 4.1.15
 */
public class SyncFinisher implements Closeable {

	private final Set<Worker> workers;
	private final int threadSize;
	private ExecutorService executorService;

	private boolean isBeginAtSameTime;
	/**
	 * 启动同步器，用于保证所有worker线程同时开始
	 */
	private final CountDownLatch beginLatch;
	/**
	 * 结束同步器，用于等待所有worker线程同时结束
	 */
	private CountDownLatch endLatch;
	/**
	 * 异常处理
	 */
	private Thread.UncaughtExceptionHandler exceptionHandler;

	/**
	 * 构造
	 *
	 * @param threadSize 线程数
	 */
	public SyncFinisher(int threadSize) {
		this.beginLatch = new CountDownLatch(1);
		this.threadSize = threadSize;
		this.workers = new LinkedHashSet<>();
	}

	/**
	 * 设置自定义线程池，默认为{@link ExecutorBuilder}创建的线程池
	 *
	 * @param executorService 线程池
	 * @return this
	 * @since 5.8.33
	 */
	public SyncFinisher setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
		return this;
	}

	/**
	 * 设置是否所有worker线程同时开始
	 *
	 * @param isBeginAtSameTime 是否所有worker线程同时开始
	 * @return this
	 */
	public SyncFinisher setBeginAtSameTime(boolean isBeginAtSameTime) {
		this.isBeginAtSameTime = isBeginAtSameTime;
		return this;
	}

	/**
	 * 设置异常处理
	 *
	 * @param exceptionHandler 异常处理器
	 * @return this
	 * @since 5.8.19
	 */
	public SyncFinisher setExceptionHandler(final Thread.UncaughtExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
		return this;
	}

	/**
	 * 增加定义的线程数同等数量的worker
	 *
	 * @param runnable 工作线程
	 * @return this
	 */
	public SyncFinisher addRepeatWorker(final Runnable runnable) {
		for (int i = 0; i < this.threadSize; i++) {
			addWorker(new Worker() {
				@Override
				public void work() {
					runnable.run();
				}
			});
		}
		return this;
	}

	/**
	 * 增加工作线程
	 *
	 * @param runnable 工作线程
	 * @return this
	 */
	public SyncFinisher addWorker(final Runnable runnable) {
		return addWorker(new Worker() {
			@Override
			public void work() {
				runnable.run();
			}
		});
	}

	/**
	 * 增加工作线程
	 *
	 * @param worker 工作线程
	 * @return this
	 */
	synchronized public SyncFinisher addWorker(Worker worker) {
		workers.add(worker);
		return this;
	}

	/**
	 * 开始工作<br>
	 * 执行此方法后如果不再重复使用此对象，需调用{@link #stop()}关闭回收资源。
	 */
	public void start() {
		start(true);
	}

	/**
	 * 开始工作<br>
	 * 执行此方法后如果不再重复使用此对象，需调用{@link #stop()}关闭回收资源。
	 *
	 * @param sync 是否阻塞等待
	 * @since 4.5.8
	 */
	public void start(boolean sync) {
		endLatch = new CountDownLatch(workers.size());

		if (null == this.executorService || this.executorService.isShutdown()) {
			this.executorService = buildExecutor();
		}
		for (Worker worker : workers) {
			if(null != this.exceptionHandler){
				executorService.execute(worker);
			} else{
				executorService.submit(worker);
			}
		}
		// 保证所有worker同时开始
		this.beginLatch.countDown();

		if (sync) {
			try {
				this.endLatch.await();
			} catch (InterruptedException e) {
				throw new UtilException(e);
			}
		}
	}

	/**
	 * 结束线程池。此方法执行两种情况：
	 * <ol>
	 *     <li>执行start(true)后，调用此方法结束线程池回收资源</li>
	 *     <li>执行start(false)后，用户自行判断结束点执行此方法</li>
	 * </ol>
	 *
	 * @since 5.6.6
	 */
	public void stop() {
		if (null != this.executorService) {
			this.executorService.shutdown();
			this.executorService = null;
		}

		clearWorker();
	}

	/**
	 * 立即结束线程池所有线程。此方法执行两种情况：
	 * <ol>
	 *     <li>执行start(true)后，调用此方法结束线程池回收资源</li>
	 *     <li>执行start(false)后，用户自行判断结束点执行此方法</li>
	 * </ol>
	 *
	 * @since 5.8.11
	 */
	public void stopNow() {
		if (null != this.executorService) {
			this.executorService.shutdownNow();
			this.executorService = null;
		}

		clearWorker();
	}

	/**
	 * 清空工作线程对象
	 */
	public void clearWorker() {
		workers.clear();
	}

	/**
	 * 剩余任务数
	 *
	 * @return 剩余任务数
	 */
	public long count() {
		return endLatch.getCount();
	}

	@Override
	public void close() throws IOException {
		stop();
	}

	/**
	 * 工作者，为一个线程
	 *
	 * @author xiaoleilu
	 */
	public abstract class Worker implements Runnable {

		@Override
		public void run() {
			if (isBeginAtSameTime) {
				try {
					beginLatch.await();
				} catch (InterruptedException e) {
					throw new UtilException(e);
				}
			}
			try {
				work();
			} finally {
				endLatch.countDown();
			}
		}

		/**
		 * 任务内容
		 */
		public abstract void work();
	}

	/**
	 * 构建线程池，加入了自定义的异常处理
	 *
	 * @return {@link ExecutorService}
	 */
	private ExecutorService buildExecutor() {
		return ExecutorBuilder.create()
			.setCorePoolSize(threadSize)
			.setThreadFactory(new NamedThreadFactory("hutool-", null, false, exceptionHandler))
			.build();
	}
}

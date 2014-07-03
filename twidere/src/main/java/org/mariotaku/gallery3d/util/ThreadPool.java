/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.gallery3d.util;

import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
	@SuppressWarnings("unused")
	private static final String TAG = "ThreadPool";
	private static final int CORE_POOL_SIZE = 4;
	private static final int MAX_POOL_SIZE = 8;
	private static final int KEEP_ALIVE_TIME = 10; // 10 seconds

	// Resource type
	public static final int MODE_NONE = 0;
	public static final int MODE_CPU = 1;
	public static final int MODE_NETWORK = 2;

	ResourceCounter mCpuCounter = new ResourceCounter(2);
	ResourceCounter mNetworkCounter = new ResourceCounter(2);

	private final Executor mExecutor;

	public ThreadPool() {
		this(CORE_POOL_SIZE, MAX_POOL_SIZE);
	}

	private ThreadPool(final int initPoolSize, final int maxPoolSize) {
		mExecutor = new ThreadPoolExecutor(initPoolSize, maxPoolSize, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("thread-pool",
						android.os.Process.THREAD_PRIORITY_BACKGROUND));
	}

	public <T> Future<T> submit(final Job<T> job) {
		return submit(job, null);
	}

	// Submit a job to the thread pool. The listener will be called when the
	// job is finished (or cancelled).
	private <T> Future<T> submit(final Job<T> job, final FutureListener<T> listener) {
		final Worker<T> w = new Worker<T>(job, listener);
		mExecutor.execute(w);
		return w;
	}

	public interface CancelListener {
		public void onCancel();
	}

	// A Job is like a Callable, but it has an addition JobContext parameter.
	public interface Job<T> {
		public T run(JobContext jc);
	}

	public interface JobContext {
		boolean isCancelled();

		void setCancelListener(CancelListener listener);

		boolean setMode(int mode);
	}

	private static class ResourceCounter {
		public int value;

		public ResourceCounter(final int v) {
			value = v;
		}
	}

	private class Worker<T> implements Runnable, Future<T>, JobContext {
		private static final String TAG = "Worker";
		private final Job<T> mJob;
		private final FutureListener<T> mListener;
		private CancelListener mCancelListener;
		private ResourceCounter mWaitOnResource;
		private volatile boolean mIsCancelled;
		private boolean mIsDone;
		private T mResult;
		private int mMode;

		public Worker(final Job<T> job, final FutureListener<T> listener) {
			mJob = job;
			mListener = listener;
		}

		// Below are the methods for Future.
		@Override
		public synchronized void cancel() {
			if (mIsCancelled) return;
			mIsCancelled = true;
			if (mWaitOnResource != null) {
				synchronized (mWaitOnResource) {
					mWaitOnResource.notifyAll();
				}
			}
			if (mCancelListener != null) {
				mCancelListener.onCancel();
			}
		}

		@Override
		public synchronized T get() {
			while (!mIsDone) {
				try {
					wait();
				} catch (final Exception ex) {
					Log.w(TAG, "ingore exception", ex);
					// ignore.
				}
			}
			return mResult;
		}

		@Override
		public boolean isCancelled() {
			return mIsCancelled;
		}

		@Override
		public synchronized boolean isDone() {
			return mIsDone;
		}

		// This is called by a thread in the thread pool.
		@Override
		public void run() {
			if (mListener != null) {
				mListener.onFutureStart(this);
			}
			T result = null;

			// A job is in CPU mode by default. setMode returns false
			// if the job is cancelled.
			if (setMode(MODE_CPU)) {
				try {
					result = mJob.run(this);
				} catch (final Throwable ex) {
					Log.w(TAG, "Exception in running a job", ex);
				}
			}

			synchronized (this) {
				setMode(MODE_NONE);
				mResult = result;
				mIsDone = true;
				notifyAll();
			}
			if (mListener != null) {
				mListener.onFutureDone(this);
			}
		}

		// Below are the methods for JobContext (only called from the
		// thread running the job)
		@Override
		public synchronized void setCancelListener(final CancelListener listener) {
			mCancelListener = listener;
			if (mIsCancelled && mCancelListener != null) {
				mCancelListener.onCancel();
			}
		}

		@Override
		public boolean setMode(final int mode) {
			// Release old resource
			ResourceCounter rc = modeToCounter(mMode);
			if (rc != null) {
				releaseResource(rc);
			}
			mMode = MODE_NONE;

			// Acquire new resource
			rc = modeToCounter(mode);
			if (rc != null) {
				if (!acquireResource(rc)) return false;
				mMode = mode;
			}

			return true;
		}

		private boolean acquireResource(final ResourceCounter counter) {
			while (true) {
				synchronized (this) {
					if (mIsCancelled) {
						mWaitOnResource = null;
						return false;
					}
					mWaitOnResource = counter;
				}

				synchronized (counter) {
					if (counter.value > 0) {
						counter.value--;
						break;
					} else {
						try {
							counter.wait();
						} catch (final InterruptedException ex) {
							// ignore.
						}
					}
				}
			}

			synchronized (this) {
				mWaitOnResource = null;
			}

			return true;
		}

		private ResourceCounter modeToCounter(final int mode) {
			if (mode == MODE_CPU)
				return mCpuCounter;
			else if (mode == MODE_NETWORK)
				return mNetworkCounter;
			else
				return null;
		}

		private void releaseResource(final ResourceCounter counter) {
			synchronized (counter) {
				counter.value++;
				counter.notifyAll();
			}
		}
	}
}

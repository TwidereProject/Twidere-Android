/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.task;

import android.os.Handler;

import java.util.concurrent.ExecutorService;

public abstract class AsyncTask<Param, Progress, Result> {

	private Thread mThread;
	private final Handler mHandler;
	private final ExecutorService mExecutor;
	private final Runnable mRunnable;

	private boolean mCancelled;
	private Param[] mParams;
	private Status mStatus = Status.PENDING;

	public AsyncTask() {
		this(new Handler(), null);
	}

	public AsyncTask(final ExecutorService executor) {
		this(new Handler(), executor);
	}

	public AsyncTask(final Handler handler) {
		this(handler, null);
	}

	public AsyncTask(final Handler handler, final ExecutorService executor) {
		if (handler == null) throw new NullPointerException();
		mHandler = handler;
		mExecutor = executor;
		mRunnable = new BackgroundRunnable();
	}

	public void cancel(final boolean mayInterruptIfRunning) {
		mCancelled = true;
		if (mExecutor == null && mThread != null) {
			mThread.interrupt();
		}
		onCancelled();
		mStatus = Status.FINISHED;
	}

	public AsyncTask<Param, Progress, Result> execute(final Param... params) {
		switch (mStatus) {
			case RUNNING:
				throw new IllegalStateException("Cannot execute task:" + " the task is already running.");
			case FINISHED:
				throw new IllegalStateException("Cannot execute task:" + " the task has already been executed "
						+ "(a task can be executed only once)");
			default:
				break;
		}

		mStatus = Status.RUNNING;
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				onPreExecute();
				mParams = params;
				if (mExecutor != null) {
					mExecutor.execute(mRunnable);
				} else {
					mThread = new Thread(mRunnable);
					mThread.start();
				}
			}
		});

		return this;
	}

	public Status getStatus() {
		return mStatus;
	}

	public boolean isCancelled() {
		return mCancelled;
	}

	protected abstract Result doInBackground(Param... params);

	protected void onCancelled() {

	}

	protected void onCancelled(final Result result) {

	}

	protected void onPostExecute(final Result result) {

	}

	protected void onPreExecute() {

	}

	protected void onProgressUpdate(final Progress... values) {

	}

	protected final void publishProgress(final Progress... progress) {
		if (isCancelled()) return;
		mHandler.post(new OnProgressUpdateRunnable(progress));
	}

	public enum Status {
		RUNNING, PENDING, FINISHED
	}

	private final class BackgroundRunnable implements Runnable {

		@Override
		public void run() {
			final Result result = doInBackground(mParams);
			mHandler.post(new OnPostExecuteRunnable(result));
		}
	}

	private final class OnPostExecuteRunnable implements Runnable {

		private final Result mResult;

		private OnPostExecuteRunnable(final Result result) {
			mResult = result;
		}

		@Override
		public void run() {
			if (isCancelled()) {
				onCancelled(mResult);
			} else {
				onPostExecute(mResult);
			}
			mStatus = Status.FINISHED;
		}
	}

	private final class OnProgressUpdateRunnable implements Runnable {

		private final Progress[] mResult;

		private OnProgressUpdateRunnable(final Progress... result) {
			mResult = result;
		}

		@Override
		public void run() {
			if (isCancelled()) return;
			onProgressUpdate(mResult);
		}
	}
}

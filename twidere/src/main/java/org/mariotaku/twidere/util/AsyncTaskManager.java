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

package org.mariotaku.twidere.util;

import android.os.Handler;

import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.task.ManagedAsyncTask;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class AsyncTaskManager {

	private final ArrayList<ManagedAsyncTask<?, ?, ?>> mTasks = new ArrayList<ManagedAsyncTask<?, ?, ?>>();
	private final Handler mHandler;
	private static AsyncTaskManager sInstance;

	AsyncTaskManager() {
		this(new Handler());
	}

	AsyncTaskManager(final Handler handler) {
		mHandler = handler;
	}

	public <T> int add(final ManagedAsyncTask<T, ?, ?> task, final boolean exec, final T... params) {
		final int hashCode = task.hashCode();
		mTasks.add(task);
		if (exec) {
			execute(hashCode);
		}
		return hashCode;
	}

	public boolean cancel(final int hashCode) {
		return cancel(hashCode, true);
	}

	public boolean cancel(final int hashCode, final boolean mayInterruptIfRunning) {
		final ManagedAsyncTask<?, ?, ?> task = findTask(hashCode);
		if (task != null) {
			task.cancel(mayInterruptIfRunning);
			mTasks.remove(task);
			return true;
		}
		return false;
	}

	/**
	 * Cancel all tasks added, then clear all tasks.
	 */
	public void cancelAll() {
		for (final ManagedAsyncTask<?, ?, ?> task : getTaskSpecList()) {
			task.cancel(true);
		}
		mTasks.clear();
	}

	@SuppressWarnings("unchecked")
	public <T> boolean execute(final int hashCode, final T... params) {
		final ManagedAsyncTask<T, ?, ?> task = (ManagedAsyncTask<T, ?, ?>) findTask(hashCode);
		if (task != null) {
			task.execute(params == null || params.length == 0 ? null : params);
			return true;
		}
		return false;
	}

	public Handler getHandler() {
		return mHandler;
	}

	public ArrayList<ManagedAsyncTask<?, ?, ?>> getTaskSpecList() {
		return new ArrayList<ManagedAsyncTask<?, ?, ?>>(mTasks);
	}

	public boolean hasRunningTask() {
		for (final ManagedAsyncTask<?, ?, ?> task : getTaskSpecList()) {
			if (task.getStatus() == ManagedAsyncTask.Status.RUNNING) return true;
		}
		return false;
	}

	public boolean hasRunningTasksForTag(final String tag) {
		if (tag == null) return false;
		for (final ManagedAsyncTask<?, ?, ?> task : getTaskSpecList()) {
			if (task.getStatus() == ManagedAsyncTask.Status.RUNNING && tag.equals(task.getTag())) return true;
		}
		return false;
	}

	public boolean isExcuting(final int hashCode) {
		final ManagedAsyncTask<?, ?, ?> task = findTask(hashCode);
		if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) return true;
		return false;
	}

	public void remove(final int hashCode) {
		try {
			mTasks.remove(findTask(hashCode));
		} catch (final ConcurrentModificationException e) {
			// Ignore.
		}
	}

	private <T> ManagedAsyncTask<?, ?, ?> findTask(final int hashCode) {
		for (final ManagedAsyncTask<?, ?, ?> task : getTaskSpecList()) {
			if (hashCode == task.hashCode()) return task;
		}
		return null;
	}

	public static AsyncTaskManager getInstance() {
		if (sInstance == null) {
			sInstance = new AsyncTaskManager();
		}
		return sInstance;
	}

}

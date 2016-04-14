/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by mariotaku on 15/3/30.
 */
public class AsyncTaskUtils {

    public static final Executor DEFAULT_EXECUTOR = Executors.newFixedThreadPool(2);

    private AsyncTaskUtils() {
    }

    @SafeVarargs
    public static <T extends AsyncTask<Parameter, ?, ?>, Parameter> T executeTask(T task, Parameter... params) {
        task.executeOnExecutor(DEFAULT_EXECUTOR, params);
        return task;
    }

    public static boolean isTaskRunning(@Nullable AsyncTask task) {
        return task != null && task.getStatus() == AsyncTask.Status.RUNNING;
    }
}

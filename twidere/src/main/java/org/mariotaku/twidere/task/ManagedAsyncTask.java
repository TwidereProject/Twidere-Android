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

import android.content.Context;
import android.os.AsyncTask;

import com.squareup.otto.Bus;

import org.mariotaku.twidere.model.message.TaskStateChangedEvent;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import javax.inject.Inject;

public abstract class ManagedAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    @Inject
    protected AsyncTaskManager manager;
    @Inject
    protected Bus bus;
    @Inject
    protected UserColorNameManager mUserColorNameManager;
    @Inject
    protected SharedPreferencesWrapper mPreferences;
    @Inject
    protected AsyncTwitterWrapper mAsyncTwitterWrapper;
    private final Context context;
    private final String tag;

    public ManagedAsyncTask(final Context context) {
        this(context, null);
    }

    public ManagedAsyncTask(final Context context, final String tag) {
        //noinspection unchecked
        GeneralComponentHelper.build(context).inject((ManagedAsyncTask<Object, Object, Object>) this);
        this.context = context;
        this.tag = tag;
    }

    public Context getContext() {
        return context;
    }

    public String getTag() {
        return tag;
    }

    @Override
    protected void finalize() throws Throwable {
        manager.remove(hashCode());
        super.finalize();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        bus.post(new TaskStateChangedEvent());
    }

    @Override
    protected void onPostExecute(final Result result) {
        super.onPostExecute(result);
        bus.post(new TaskStateChangedEvent());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        bus.post(new TaskStateChangedEvent());
    }

}

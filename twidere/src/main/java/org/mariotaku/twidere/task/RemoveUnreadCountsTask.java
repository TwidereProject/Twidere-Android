/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.support.v4.util.SimpleArrayMap;

import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.TwitterWrapper;

import java.util.Set;

/**
 * Created by mariotaku on 2017/2/10.
 */
public final class RemoveUnreadCountsTask extends AsyncTask<Object, Object, Integer> {
    private final Context context;
    private final int position;
    private final SimpleArrayMap<UserKey, Set<String>> counts;

    public RemoveUnreadCountsTask(Context context, final int position, final SimpleArrayMap<UserKey, Set<String>> counts) {
        this.context = context;
        this.position = position;
        this.counts = counts;
    }

    @Override
    protected Integer doInBackground(final Object... params) {
        return TwitterWrapper.removeUnreadCounts(context, position, counts);
    }

}

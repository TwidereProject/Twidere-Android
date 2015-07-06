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

package org.mariotaku.twidere.loader.support;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.util.collection.NoDuplicatesArrayList;

import java.util.List;

public abstract class ParcelableActivitiesLoader extends AsyncTaskLoader<List<ParcelableActivity>> implements Constants {

    private final List<ParcelableActivity> mData = new NoDuplicatesArrayList<>();
    private final boolean mFirstLoad;
    private final int mTabPosition;

    private Long mLastViewedId;

    public ParcelableActivitiesLoader(final Context context, final List<ParcelableActivity> data, final int tab_position) {
        super(context);
        mFirstLoad = data == null;
        if (data != null) {
            mData.addAll(data);
        }
        mTabPosition = tab_position;
    }

    public Long getLastViewedId() {
        return mLastViewedId;
    }

    protected boolean containsStatus(final long id) {
        for (final ParcelableActivity activity : mData) {
            if (activity.max_position <= id && activity.min_position >= id) return true;
        }
        return false;
    }

    protected boolean deleteActivity(final List<ParcelableActivity> activities, final long id) {
        if (activities == null || activities.isEmpty()) return false;
        boolean result = false;
        for (final ParcelableActivity activity : activities.toArray(new ParcelableActivity[activities.size()])) {
            if (id <= activity.max_position && id >= activity.min_position) {
                result |= activities.remove(activity);
            }
        }
        return result;
    }

    protected List<ParcelableActivity> getData() {
        return mData;
    }

    protected int getTabPosition() {
        return mTabPosition;
    }

    protected boolean isFirstLoad() {
        return mFirstLoad;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

}

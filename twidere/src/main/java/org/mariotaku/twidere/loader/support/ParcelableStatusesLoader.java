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

package org.mariotaku.twidere.loader.support;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.loader.iface.IExtendedLoader;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.collection.NoDuplicatesArrayList;

import java.util.List;

public abstract class ParcelableStatusesLoader extends AsyncTaskLoader<List<ParcelableStatus>>
        implements Constants, IExtendedLoader {

    private final List<ParcelableStatus> mData = new NoDuplicatesArrayList<>();
    private final boolean mFirstLoad;
    private final int mTabPosition;
    private boolean mFromUser;

    public ParcelableStatusesLoader(final Context context, final List<ParcelableStatus> data,
                                    final int tabPosition, final boolean fromUser) {
        super(context);
        mFirstLoad = data == null;
        if (data != null) {
            mData.addAll(data);
        }
        mTabPosition = tabPosition;
        mFromUser = fromUser;
    }

    @Override
    public boolean isFromUser() {
        return mFromUser;
    }

    @Override
    public void setFromUser(boolean fromUser) {
        mFromUser = fromUser;
    }

    protected boolean containsStatus(final long statusId) {
        for (final ParcelableStatus status : mData) {
            if (status.id == statusId) return true;
        }
        return false;
    }

    protected boolean deleteStatus(final List<ParcelableStatus> statuses, final long status_id) {
        if (statuses == null || statuses.isEmpty()) return false;
        boolean result = false;
        for (int i = statuses.size() - 1; i >= 0; i--) {
            if (statuses.get(i).id == status_id) {
                statuses.remove(i);
                result = true;
            }
        }
        return result;
    }

    @Nullable
    protected List<ParcelableStatus> getData() {
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

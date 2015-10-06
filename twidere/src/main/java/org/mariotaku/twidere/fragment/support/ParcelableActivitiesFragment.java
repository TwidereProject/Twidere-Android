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

package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter;
import org.mariotaku.twidere.adapter.iface.IActivitiesAdapter;
import org.mariotaku.twidere.model.ParcelableActivity;

import java.util.List;

/**
 * Created by mariotaku on 14/12/3.
 */
public abstract class ParcelableActivitiesFragment extends AbsActivitiesFragment<List<ParcelableActivity>> {

    @Override
    public int getActivities(long[] accountIds, final long[] maxIds, final long[] sinceIds) {
        final Bundle args = new Bundle(getArguments());
        args.putLongArray(EXTRA_ACCOUNT_IDS, accountIds);
        args.putLongArray(EXTRA_MAX_IDS, maxIds);
        args.putLongArray(EXTRA_SINCE_IDS, sinceIds);
        getLoaderManager().restartLoader(0, args, this);
        return -1;
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @Override
    public void onStop() {
        mBus.unregister(this);
        super.onStop();
    }

    @Override
    public void onLoadMoreContents() {
        final IActivitiesAdapter<List<ParcelableActivity>> adapter = getAdapter();
        final long[] maxIds = new long[]{adapter.getActivity(adapter.getActivityCount() - 1).min_position};
        getActivities(getAccountIds(), maxIds, null);
    }

    @Override
    public boolean triggerRefresh() {
        final IActivitiesAdapter<List<ParcelableActivity>> adapter = getAdapter();
        final long[] accountIds = getAccountIds();
//        if (adapter.getActivityCount() > 0) {
//            final long[] sinceIds = new long[]{adapter.getActivity(0).max_position};
//            getActivities(accountIds, null, sinceIds);
//        } else {
//            getActivities(accountIds, null, null);
//        }
        getActivities(accountIds, null, null);
        return true;
    }

    @Override
    public boolean isRefreshing() {
        return getLoaderManager().hasRunningLoaders();
    }

    @Override
    protected long[] getAccountIds() {
        return new long[]{getAccountId()};
    }

    @NonNull
    @Override
    protected ParcelableActivitiesAdapter onCreateAdapter(final Context context, final boolean compact) {
        return new ParcelableActivitiesAdapter(context, compact, isByFriends());
    }

    protected abstract boolean isByFriends();

    protected long getAccountId() {
        final Bundle args = getArguments();
        return args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
    }

    protected abstract String[] getSavedActivitiesFileArgs();


}

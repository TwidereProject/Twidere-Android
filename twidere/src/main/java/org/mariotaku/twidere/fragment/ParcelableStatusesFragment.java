/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.text.TextUtils;

import com.squareup.otto.Subscribe;

import org.mariotaku.twidere.adapter.ListParcelableStatusesAdapter;
import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.model.BaseRefreshTaskParam;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.RefreshTaskParam;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.FavoriteTaskEvent;
import org.mariotaku.twidere.model.message.StatusDestroyedEvent;
import org.mariotaku.twidere.model.message.StatusListChangedEvent;
import org.mariotaku.twidere.model.message.StatusRetweetedEvent;
import org.mariotaku.twidere.util.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mariotaku on 14/12/3.
 */
public abstract class ParcelableStatusesFragment extends AbsStatusesFragment {

    private String mLastId;
    private int mPage = 1, mPageDelta;

    public final void deleteStatus(@NonNull final String statusId) {
        final List<ParcelableStatus> list = getAdapterData();
        if (list == null) return;
        final Set<ParcelableStatus> dataToRemove = new HashSet<>();
        for (int i = 0, j = list.size(); i < j; i++) {
            final ParcelableStatus status = list.get(i);
            if (TextUtils.equals(status.id, statusId) || TextUtils.equals(status.retweet_id, statusId)) {
                dataToRemove.add(status);
            } else if (TextUtils.equals(status.my_retweet_id, statusId)) {
                status.my_retweet_id = null;
                status.retweet_count = status.retweet_count - 1;
            }
        }
        list.removeAll(dataToRemove);
        setAdapterData(list);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mPage = savedInstanceState.getInt(EXTRA_PAGE);
        }
    }

    @Override
    public boolean getStatuses(RefreshTaskParam param) {
        final Bundle args = new Bundle(getArguments());
        String[] maxIds = param.getMaxIds();
        if (maxIds != null) {
            args.putString(EXTRA_MAX_ID, maxIds[0]);
            args.putBoolean(EXTRA_MAKE_GAP, false);
        }
        String[] sinceIds = param.getSinceIds();
        if (sinceIds != null) {
            args.putString(EXTRA_SINCE_ID, sinceIds[0]);
        }
        if (mPage > 0) {
            args.putInt(EXTRA_PAGE, mPage);
        }
        args.putBoolean(EXTRA_LOADING_MORE, param.isLoadingMore());
        args.putBoolean(EXTRA_FROM_USER, true);
        getLoaderManager().restartLoader(0, args, this);
        return true;
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
    protected boolean hasMoreData(List<ParcelableStatus> list) {
        if (list == null || list.isEmpty()) return false;
        return (!TextUtils.equals(mLastId, mLastId = list.get(list.size() - 1).id));
    }

    @NonNull
    @Override
    protected UserKey[] getAccountKeys() {
        final UserKey[] accountKeys = Utils.getAccountKeys(getContext(), getArguments());
        if (accountKeys == null) return new UserKey[0];
        return accountKeys;
    }

    @Override
    protected Object createMessageBusCallback() {
        return new ParcelableStatusesBusCallback();
    }

    @NonNull
    @Override
    protected ListParcelableStatusesAdapter onCreateAdapter(final Context context, final boolean compact) {
        return new ListParcelableStatusesAdapter(context, compact);
    }

    @Override
    protected void onLoadingFinished() {
        showContent();
        setRefreshEnabled(true);
        setRefreshing(false);
        setLoadMoreIndicatorPosition(IndicatorPosition.NONE);
    }


    @Override
    public void onLoadMoreContents(int position) {
        // Only supports load from end, skip START flag
        if ((position & IndicatorPosition.START) != 0 || isRefreshing()) return;
        super.onLoadMoreContents(position);
        if (position == 0) return;
        final ParcelableStatusesAdapter adapter = getAdapter();
        // Load the last item
        final int idx = adapter.getStatusStartIndex() + adapter.getRawStatusCount() - 1;
        if (idx < 0) return;
        final ParcelableStatus status = adapter.getStatus(idx);
        UserKey[] accountKeys = {status.account_key};
        final String[] maxIds = {status.id};
        mPage += mPageDelta;
        final BaseRefreshTaskParam param = new BaseRefreshTaskParam(accountKeys, maxIds, null);
        param.setLoadingMore(true);
        getStatuses(param);
    }

    public final void replaceStatus(final ParcelableStatus status) {
        final List<ParcelableStatus> data = getAdapterData();
        if (status == null || data == null || data.isEmpty()) return;
        for (int i = 0, j = data.size(); i < j; i++) {
            if (status.equals(data.get(i))) {
                data.set(i, status);
            }
        }
        setAdapterData(data);
    }

    @Override
    public boolean triggerRefresh() {
        super.triggerRefresh();
        final IStatusesAdapter<List<ParcelableStatus>> adapter = getAdapter();
        final UserKey[] accountIds = getAccountKeys();
        if (adapter.getStatusCount() > 0) {
            final String[] sinceIds = new String[]{adapter.getStatus(0).id};
            getStatuses(new BaseRefreshTaskParam(accountIds, null, sinceIds));
        } else {
            getStatuses(new BaseRefreshTaskParam(accountIds, null, null));
        }
        return true;
    }

    @Override
    public boolean isRefreshing() {
        if (getContext() == null || isDetached()) return false;
        final LoaderManager lm = getLoaderManager();
        return lm.hasRunningLoaders();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_PAGE, mPage);
    }

    protected String[] getSavedStatusesFileArgs() {
        return null;
    }

    @Override
    protected void onHasMoreDataChanged(boolean hasMoreData) {
        mPageDelta = hasMoreData ? 1 : 0;
    }

    private void updateFavoritedStatus(ParcelableStatus status) {
        final Context context = getActivity();
        if (context == null) return;
        replaceStatus(status);
    }

    private void updateRetweetedStatuses(ParcelableStatus status) {
        final List<ParcelableStatus> data = getAdapterData();
        if (status == null || status.retweet_id == null || data == null) return;
        for (int i = 0, j = data.size(); i < j; i++) {
            final ParcelableStatus orig = data.get(i);
            if (orig.account_key.equals(status.account_key) && TextUtils.equals(orig.id, status.retweet_id)) {
                orig.my_retweet_id = status.my_retweet_id;
                orig.retweet_count = status.retweet_count;
            }
        }
        setAdapterData(data);
    }

    protected class ParcelableStatusesBusCallback {

        @Subscribe
        public void notifyFavoriteTask(FavoriteTaskEvent event) {
            if (event.isSucceeded()) {
                updateFavoritedStatus(event.getStatus());
            }
        }


        @Subscribe
        public void notifyStatusDestroyed(StatusDestroyedEvent event) {
            deleteStatus(event.status.id);
        }

        @Subscribe
        public void notifyStatusListChanged(StatusListChangedEvent event) {
            getAdapter().notifyDataSetChanged();
        }

        @Subscribe
        public void notifyStatusRetweeted(StatusRetweetedEvent event) {
            updateRetweetedStatuses(event.status);
        }

    }

}

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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AbsActivitiesAdapter;
import org.mariotaku.twidere.adapter.AbsActivitiesAdapter.ActivityAdapterListener;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.RecyclerViewNavigationHelper;
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback;
import org.mariotaku.twidere.view.holder.ActivityTitleSummaryViewHolder;
import org.mariotaku.twidere.view.holder.GapViewHolder;

/**
 * Created by mariotaku on 14/11/5.
 */
public abstract class AbsActivitiesFragment<Data> extends AbsContentRecyclerViewFragment<AbsActivitiesAdapter<Data>>
        implements LoaderCallbacks<Data>, OnRefreshListener, DrawerCallback, RefreshScrollTopInterface,
        ActivityAdapterListener, KeyboardShortcutsHandler.KeyboardShortcutCallback {

    private final Object mStatusesBusCallback;
    private SharedPreferences mPreferences;
    private RecyclerViewNavigationHelper mNavigationHelper;

    protected AbsActivitiesFragment() {
        mStatusesBusCallback = createMessageBusCallback();
    }

    @Override
    public void onGapClick(GapViewHolder holder, int position) {
        final ParcelableActivity activity = getAdapter().getActivity(position);
        final long[] accountIds = {activity.account_id};
        final long[] maxIds = {activity.min_position};
        getActivities(accountIds, maxIds, null);
    }

    @Override
    public void onActivityClick(ActivityTitleSummaryViewHolder holder, int position) {

    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        return mNavigationHelper.handleKeyboardShortcutSingle(handler, keyCode, event, metaState);
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, final int keyCode, final int repeatCount,
                                                @NonNull final KeyEvent event, int metaState) {
        return mNavigationHelper.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState);
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, final int keyCode, @NonNull final KeyEvent event, int metaState) {
        return mNavigationHelper.isKeyboardShortcutHandled(handler, keyCode, event, metaState);
    }

    public SharedPreferences getSharedPreferences() {
        if (mPreferences != null) return mPreferences;
        return mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public abstract int getActivities(long[] accountIds, long[] maxIds, long[] sinceIds);

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_recyclerview, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final AbsActivitiesAdapter<Data> adapter = getAdapter();
        mNavigationHelper = new RecyclerViewNavigationHelper(getRecyclerView(), getLayoutManager(),
                adapter, this);
        final View view = getView();
        if (view == null) throw new AssertionError();
        adapter.setListener(this);
        getLoaderManager().initLoader(0, getArguments(), this);
        showProgress();
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(mStatusesBusCallback);
    }

    @Override
    public void onStop() {
        mBus.unregister(mStatusesBusCallback);
        super.onStop();
    }

    @Override
    public void onLoadFinished(Loader<Data> loader, Data data) {
        setRefreshing(false);
        getAdapter().setData(data);
        showContent();
    }

    @Override
    public void onLoaderReset(Loader<Data> loader) {
    }

    @Override
    public void onRefresh() {
        triggerRefresh();
    }


    @Override
    public boolean scrollToStart() {
        final boolean result = super.scrollToStart();
        if (result) {
            final AsyncTwitterWrapper twitter = mTwitterWrapper;
            final int tabPosition = getTabPosition();
            if (twitter != null && tabPosition != -1) {
                twitter.clearUnreadCountAsync(tabPosition);
            }
        }
        return true;
    }

    protected abstract long[] getAccountIds();

    protected Data getAdapterData() {
        return getAdapter().getData();
    }

    protected void setAdapterData(Data data) {
        getAdapter().setData(data);
    }

    protected Object createMessageBusCallback() {
        return new StatusesBusCallback();
    }

    protected final class StatusesBusCallback {

        protected StatusesBusCallback() {
        }


    }
}

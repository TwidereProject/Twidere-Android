/*
 *                 Twidere - Twitter client for Android
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

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;

import com.desmond.asyncmanager.AsyncManager;
import com.desmond.asyncmanager.TaskRunnable;
import com.squareup.otto.Subscribe;

import org.mariotaku.library.objectcursor.ObjectCursor;
import org.mariotaku.sqliteqb.library.Columns.Column;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.RawItemArray;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.HomeActivity;
import org.mariotaku.twidere.adapter.AbsActivitiesAdapter;
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition;
import org.mariotaku.twidere.loader.support.ExtendedObjectCursorLoader;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableActivityCursorIndices;
import org.mariotaku.twidere.model.message.AccountChangedEvent;
import org.mariotaku.twidere.model.message.FavoriteTaskEvent;
import org.mariotaku.twidere.model.message.GetActivitiesTaskEvent;
import org.mariotaku.twidere.model.message.StatusDestroyedEvent;
import org.mariotaku.twidere.model.message.StatusListChangedEvent;
import org.mariotaku.twidere.model.message.StatusRetweetedEvent;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.ErrorInfoStore;

import java.util.List;

import static org.mariotaku.twidere.util.DataStoreUtils.getTableNameByUri;

/**
 * Created by mariotaku on 14/12/3.
 */
public abstract class CursorActivitiesFragment extends AbsActivitiesFragment<List<ParcelableActivity>> {

    @Override
    protected void onLoadingFinished() {
        final long[] accountIds = getAccountIds();
        final AbsActivitiesAdapter<List<ParcelableActivity>> adapter = getAdapter();
        if (adapter.getItemCount() > 0) {
            showContent();
        } else if (accountIds.length > 0) {
            final ErrorInfoStore.DisplayErrorInfo errorInfo = ErrorInfoStore.getErrorInfo(getContext(),
                    mErrorInfoStore.get(getErrorInfoKey(), accountIds[0]));
            if (errorInfo != null) {
                showEmpty(errorInfo.getIcon(), errorInfo.getMessage());
            } else {
                showEmpty(R.drawable.ic_info_refresh, getString(R.string.swipe_down_to_refresh));
            }
        } else {
            showError(R.drawable.ic_info_accounts, getString(R.string.no_account_selected));
        }
    }

    @NonNull
    protected abstract String getErrorInfoKey();

    private ContentObserver mContentObserver;

    public abstract Uri getContentUri();

    @Override
    protected Loader<List<ParcelableActivity>> onCreateStatusesLoader(final Context context,
                                                                      final Bundle args,
                                                                      final boolean fromUser) {
        final Uri uri = getContentUri();
        final String table = getTableNameByUri(uri);
        final String sortOrder = getSortOrder();
        final long[] accountIds = getAccountIds();
        final Expression accountWhere = Expression.in(new Column(Activities.ACCOUNT_ID),
                new RawItemArray(accountIds));
        final Expression filterWhere = getFiltersWhere(table), where;
        if (filterWhere != null) {
            where = Expression.and(accountWhere, filterWhere);
        } else {
            where = accountWhere;
        }
        final Where expression = processWhere(where, new String[0]);
        final String selection = expression.getSQL();
        final AbsActivitiesAdapter<List<ParcelableActivity>> adapter = getAdapter();
        adapter.setShowAccountsColor(accountIds.length > 1);
        final String[] projection = Activities.COLUMNS;
        return new CursorActivitiesLoader(context, uri, projection, selection, expression.whereArgs,
                sortOrder, fromUser);
    }

    @Override
    protected Object createMessageBusCallback() {
        return new CursorActivitiesBusCallback();
    }

    @Override
    protected long[] getAccountIds() {
        final Bundle args = getArguments();
        if (args != null && args.getLong(EXTRA_ACCOUNT_ID) > 0) {
            return new long[]{args.getLong(EXTRA_ACCOUNT_ID)};
        }
        final FragmentActivity activity = getActivity();
        if (activity instanceof HomeActivity) {
            return ((HomeActivity) activity).getActivatedAccountIds();
        }
        return DataStoreUtils.getActivatedAccountIds(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        final ContentResolver cr = getContentResolver();
        mContentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                reloadActivities();
            }
        };
        cr.registerContentObserver(Accounts.CONTENT_URI, true, mContentObserver);
        cr.registerContentObserver(Filters.CONTENT_URI, true, mContentObserver);
        updateRefreshState();
        reloadActivities();
    }

    protected void reloadActivities() {
        if (getActivity() == null || isDetached()) return;
        final Bundle args = new Bundle(), fragmentArgs = getArguments();
        if (fragmentArgs != null) {
            args.putAll(fragmentArgs);
            args.putBoolean(EXTRA_FROM_USER, true);
        }
        getLoaderManager().restartLoader(0, args, this);
    }

    @Override
    public void onStop() {
        final ContentResolver cr = getContentResolver();
        cr.unregisterContentObserver(mContentObserver);
        super.onStop();
    }

    @Override
    protected boolean hasMoreData(final List<ParcelableActivity> cursor) {
        return cursor != null && cursor.size() != 0;
    }

    @Override
    public void onLoaderReset(Loader<List<ParcelableActivity>> loader) {
        getAdapter().setData(null);
    }

    @Override
    public void onLoadMoreContents(@IndicatorPosition int position) {
        // Only supports load from end, skip START flag
        if ((position & IndicatorPosition.START) != 0) return;
        super.onLoadMoreContents(position);
        if (position == 0) return;
        AsyncManager.runBackgroundTask(new TaskRunnable<Object, long[][], CursorActivitiesFragment>() {
            @Override
            public long[][] doLongOperation(Object o) throws InterruptedException {
                final long[][] result = new long[3][];
                result[0] = getAccountIds();
                result[1] = getOldestActivityIds(result[0]);
                return result;
            }

            @Override
            public void callback(CursorActivitiesFragment fragment, long[][] result) {
                fragment.getActivities(result[0], result[1], result[2]);
            }
        }.setResultHandler(this));
    }

    @Override
    public boolean triggerRefresh() {
        super.triggerRefresh();
        AsyncManager.runBackgroundTask(new TaskRunnable<Object, long[][], CursorActivitiesFragment>() {
            @Override
            public long[][] doLongOperation(Object o) throws InterruptedException {
                final long[][] result = new long[3][];
                result[0] = getAccountIds();
                result[2] = getNewestActivityIds(result[0]);
                return result;
            }

            @Override
            public void callback(CursorActivitiesFragment fragment, long[][] result) {
                fragment.getActivities(result[0], result[1], result[2]);
            }
        }.setResultHandler(this));
        return true;
    }

    protected Expression getFiltersWhere(String table) {
        if (!isFilterEnabled()) return null;
        return DataStoreUtils.buildActivityFilterWhereClause(table, null);
    }

    protected long[] getNewestActivityIds(long[] accountIds) {
        return DataStoreUtils.getNewestActivityMaxPositions(getActivity(), getContentUri(), accountIds);
    }

    protected abstract int getNotificationType();

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            for (long accountId : getAccountIds()) {
                mTwitterWrapper.clearNotificationAsync(getNotificationType(), accountId);
            }
        }
    }

    protected long[] getOldestActivityIds(long[] accountIds) {
        return DataStoreUtils.getOldestActivityMaxPositions(getActivity(), getContentUri(), accountIds);
    }

    protected abstract boolean isFilterEnabled();

    @NonNull
    protected Where processWhere(@NonNull final Expression where, @NonNull final String[] whereArgs) {
        return new Where(where, whereArgs);
    }

    protected abstract void updateRefreshState();

    private String getSortOrder() {
        return Activities.DEFAULT_SORT_ORDER;
    }


    protected class CursorActivitiesBusCallback {

        @Subscribe
        public void notifyGetStatusesTaskChanged(GetActivitiesTaskEvent event) {
            if (!event.uri.equals(getContentUri())) return;
            setRefreshing(event.running);
            if (!event.running) {
                setLoadMoreIndicatorPosition(IndicatorPosition.NONE);
                setRefreshEnabled(true);
                onLoadingFinished();
            }
        }

        @Subscribe
        public void notifyFavoriteTask(FavoriteTaskEvent event) {

        }

        @Subscribe
        public void notifyStatusDestroyed(StatusDestroyedEvent event) {
        }

        @Subscribe
        public void notifyStatusListChanged(StatusListChangedEvent event) {
            getAdapter().notifyDataSetChanged();
        }

        @Subscribe
        public void notifyStatusRetweeted(StatusRetweetedEvent event) {
        }

        @Subscribe
        public void notifyAccountChanged(AccountChangedEvent event) {

        }

    }

    public static class Where {
        Expression where;
        String[] whereArgs;

        public Where(@NonNull Expression where, @Nullable String[] whereArgs) {
            this.where = where;
            this.whereArgs = whereArgs;
        }

        public String getSQL() {
            return where.getSQL();
        }
    }

    public static class CursorActivitiesLoader extends ExtendedObjectCursorLoader<ParcelableActivity> {
        public CursorActivitiesLoader(Context context, Uri uri, String[] projection,
                                      String selection, String[] selectionArgs, String sortOrder,
                                      boolean fromUser) {
            super(context, ParcelableActivityCursorIndices.class, uri, projection, selection, selectionArgs, sortOrder, fromUser);
        }

        @Override
        protected ObjectCursor<ParcelableActivity> createObjectCursor(Cursor cursor, ObjectCursor.CursorIndices<ParcelableActivity> indices) {
            return new ActivityCursor(cursor, indices, DataStoreUtils.getFilteredUserIds(getContext()));
        }

        public static class ActivityCursor extends ObjectCursor<ParcelableActivity> {

            private final long[] filteredUserIds;

            public ActivityCursor(Cursor cursor, CursorIndices<ParcelableActivity> indies, long[] filteredUserIds) {
                super(cursor, indies);
                this.filteredUserIds = filteredUserIds;
            }

            public long[] getFilteredUserIds() {
                return filteredUserIds;
            }
        }
    }
}

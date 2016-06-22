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

package org.mariotaku.twidere.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;

import com.squareup.otto.Subscribe;

import org.mariotaku.library.objectcursor.ObjectCursor;
import org.mariotaku.sqliteqb.library.ArgsArray;
import org.mariotaku.sqliteqb.library.Columns.Column;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter;
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition;
import org.mariotaku.twidere.loader.ExtendedObjectCursorLoader;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableActivityCursorIndices;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.SimpleRefreshTaskParam;
import org.mariotaku.twidere.model.UserKey;
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
import org.mariotaku.twidere.util.TwidereArrayUtils;
import org.mariotaku.twidere.util.Utils;

import java.util.List;

import static org.mariotaku.twidere.util.DataStoreUtils.getTableNameByUri;

/**
 * Displays statuses from database
 * Created by mariotaku on 14/12/3.
 */
public abstract class CursorActivitiesFragment extends AbsActivitiesFragment {

    @Override
    protected void onLoadingFinished() {
        final UserKey[] accountKeys = getAccountKeys();
        assert accountKeys != null;
        final ParcelableActivitiesAdapter adapter = getAdapter();
        if (adapter.getItemCount() > 0) {
            showContent();
        } else if (accountKeys.length > 0) {
            final ErrorInfoStore.DisplayErrorInfo errorInfo = ErrorInfoStore.getErrorInfo(getContext(),
                    mErrorInfoStore.get(getErrorInfoKey(), accountKeys[0]));
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
    protected Loader<List<ParcelableActivity>> onCreateActivitiesLoader(final Context context,
                                                                        final Bundle args,
                                                                        final boolean fromUser) {
        final Uri uri = getContentUri();
        final String table = getTableNameByUri(uri);
        final String sortOrder = getSortOrder();
        final UserKey[] accountKeys = getAccountKeys();
        assert accountKeys != null;
        final Expression accountWhere = Expression.in(new Column(Activities.ACCOUNT_KEY),
                new ArgsArray(accountKeys.length));
        final Expression filterWhere = getFiltersWhere(table), where;
        if (filterWhere != null) {
            where = Expression.and(accountWhere, filterWhere);
        } else {
            where = accountWhere;
        }
        final String[] accountSelectionArgs = TwidereArrayUtils.toStringArray(accountKeys, 0,
                accountKeys.length);
        final Where expression = processWhere(where, accountSelectionArgs);
        final String selection = expression.getSQL();
        final ParcelableActivitiesAdapter adapter = getAdapter();
        adapter.setShowAccountsColor(accountKeys.length > 1);
        final String[] projection = Activities.COLUMNS;
        return new CursorActivitiesLoader(context, uri, projection, selection, expression.whereArgs,
                sortOrder, fromUser);
    }

    @Override
    protected Object createMessageBusCallback() {
        return new CursorActivitiesBusCallback();
    }

    @Nullable
    @Override
    protected UserKey[] getAccountKeys() {
        final Context context = getContext();
        if (context == null) return null;
        final Bundle args = getArguments();
        final UserKey[] accountKeys = Utils.getAccountKeys(context, args);
        if (accountKeys != null) {
            return accountKeys;
        }
        if (context instanceof HomeActivity) {
            return ((HomeActivity) context).getActivatedAccountKeys();
        }
        return DataStoreUtils.getActivatedAccountKeys(context);
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
        if ((position & IndicatorPosition.START) != 0 || isRefreshing()) return;
        super.onLoadMoreContents(position);
        if (position == 0) return;
        getActivities(new SimpleRefreshTaskParam() {
            @NonNull
            @Override
            public UserKey[] getAccountKeysWorker() {
                final UserKey[] accountKeys = CursorActivitiesFragment.this.getAccountKeys();
                assert accountKeys != null;
                return accountKeys;
            }

            @Nullable
            @Override
            public String[] getMaxIds() {
                return getOldestActivityIds(getAccountKeys());
            }

            @Nullable
            @Override
            public long[] getMaxSortIds() {
                final Context context = getContext();
                if (context == null) return null;
                return DataStoreUtils.getOldestActivityMaxSortPositions(context,
                        getContentUri(), getAccountKeys());
            }

            @Override
            public boolean hasMaxIds() {
                return true;
            }

            @Override
            public boolean shouldAbort() {
                return getContext() == null;
            }
        });
    }

    @Override
    public boolean triggerRefresh() {
        super.triggerRefresh();
        getActivities(new SimpleRefreshTaskParam() {
            @NonNull
            @Override
            public UserKey[] getAccountKeysWorker() {
                final UserKey[] accountKeys = CursorActivitiesFragment.this.getAccountKeys();
                assert accountKeys != null;
                return accountKeys;
            }

            @Nullable
            @Override
            public String[] getSinceIds() {
                return getNewestActivityIds(getAccountKeys());
            }

            @Nullable
            @Override
            public long[] getSinceSortIds() {
                final Context context = getContext();
                if (context == null) return null;
                return DataStoreUtils.getNewestActivityMaxSortPositions(context,
                        getContentUri(), getAccountKeys());
            }

            @Override
            public boolean hasSinceIds() {
                return true;
            }

            @Override
            public boolean shouldAbort() {
                return getContext() == null;
            }
        });
        return true;
    }

    protected Expression getFiltersWhere(String table) {
        if (!isFilterEnabled()) return null;
        return DataStoreUtils.buildActivityFilterWhereClause(table, null);
    }

    protected String[] getNewestActivityIds(UserKey[] accountKeys) {
        final Context context = getContext();
        if (context == null) return null;
        return DataStoreUtils.getNewestActivityMaxPositions(context, getContentUri(), accountKeys);
    }

    protected abstract int getNotificationType();

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Context context = getContext();
        if (context != null && isVisibleToUser) {
            final UserKey[] accountKeys = getAccountKeys();
            assert accountKeys != null;
            for (UserKey accountKey : accountKeys) {
                mTwitterWrapper.clearNotificationAsync(getNotificationType(), accountKey);
            }
        }
    }

    protected String[] getOldestActivityIds(UserKey[] accountKeys) {
        final Context context = getContext();
        if (context == null) return null;
        return DataStoreUtils.getOldestActivityMaxPositions(context, getContentUri(), accountKeys);
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


    private void updateFavoritedStatus(ParcelableStatus status) {
        final Context context = getActivity();
        if (context == null) return;
        replaceStatusStates(status);
    }


    public final void replaceStatusStates(final ParcelableStatus result) {
        if (result == null) return;
        final LinearLayoutManager lm = getLayoutManager();
        final ParcelableActivitiesAdapter adapter = getAdapter();
        int rangeStart = Math.max(adapter.getActivityStartIndex(), lm.findFirstVisibleItemPosition());
        int rangeEnd = Math.min(lm.findLastVisibleItemPosition(), adapter.getActivityStartIndex()
                + adapter.getActivityCount() - 1);
        for (int i = rangeStart, j = rangeEnd + 1; i < j; i++) {
            ParcelableActivity activity = adapter.getActivity(i);
            if (result.account_key.equals(activity.account_key) && result.id.equals(activity.status_id)) {
                if (!result.id.equals(activity.status_id)) continue;
                ParcelableStatus[][] statusesMatrix = {activity.target_statuses,
                        activity.target_object_statuses};
                for (ParcelableStatus[] statusesArray : statusesMatrix) {
                    if (statusesArray == null) continue;
                    for (ParcelableStatus status : statusesArray) {
                        if (result.id.equals(status.id) || result.id.equals(status.retweet_id)
                                || result.id.equals(status.my_retweet_id)) {
                            status.is_favorite = result.is_favorite;
                            status.reply_count = result.reply_count;
                            status.retweet_count = result.retweet_count;
                            status.favorite_count = result.favorite_count;
                        }
                    }
                }
            }
        }
        adapter.notifyItemRangeChanged(rangeStart, rangeEnd);
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
            if (event.isSucceeded()) {
                updateFavoritedStatus(event.getStatus());
            }
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
                                      String selection, String[] selectionArgs,
                                      String sortOrder, boolean fromUser) {
            super(context, ParcelableActivityCursorIndices.class, uri, projection, selection,
                    selectionArgs, sortOrder, fromUser);
        }

        @Override
        protected ObjectCursor<ParcelableActivity> createObjectCursor(Cursor cursor, ObjectCursor.CursorIndices<ParcelableActivity> indices) {
            final UserKey[] filteredUserIds = DataStoreUtils.getFilteredUserIds(getContext());
            return new ActivityCursor(cursor, indices, filteredUserIds);
        }

        public static class ActivityCursor extends ObjectCursor<ParcelableActivity> {

            private final UserKey[] filteredUserIds;

            public ActivityCursor(Cursor cursor, CursorIndices<ParcelableActivity> indies,
                                  UserKey[] filteredUserIds) {
                super(cursor, indies);
                this.filteredUserIds = filteredUserIds;
            }

            public UserKey[] getFilteredUserIds() {
                return filteredUserIds;
            }

        }
    }
}

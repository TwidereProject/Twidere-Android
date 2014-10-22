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

package org.mariotaku.twidere.fragment.support;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.AbsListView;

import org.mariotaku.querybuilder.Columns.Column;
import org.mariotaku.querybuilder.RawItemArray;
import org.mariotaku.querybuilder.Where;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.HomeActivity;
import org.mariotaku.twidere.adapter.CursorStatusesAdapter;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.content.SupportFragmentReloadCursorObserver;

import static org.mariotaku.twidere.util.Utils.buildStatusFilterWhereClause;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getNewestStatusIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.getOldestStatusIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.getTableNameByUri;
import static org.mariotaku.twidere.util.Utils.shouldEnableFiltersForRTs;

public abstract class CursorStatusesListFragment extends BaseStatusesListFragment<Cursor> {

    private final SupportFragmentReloadCursorObserver mReloadContentObserver = new SupportFragmentReloadCursorObserver(
            this, 0, this);

    public HomeActivity getHomeActivity() {
        final Activity activity = getActivity();
        if (activity instanceof HomeActivity) return (HomeActivity) activity;
        return null;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListAdapter().setFiltersEnabled(isFiltersEnabled());
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final Context context = getActivity();
        final Uri uri = getContentUri();
        final String table = getTableNameByUri(uri);
        final String sortOrder = Statuses.DEFAULT_SORT_ORDER;
        final long account_id = getAccountId();
        final long[] accountIds = account_id > 0 ? new long[]{account_id} : getActivatedAccountIds(context);
        final boolean no_account_selected = accountIds.length == 0;
        setEmptyText(no_account_selected ? getString(R.string.no_account_selected) : null);
        if (!no_account_selected) {
            getListView().setEmptyView(null);
        }
        final Where accountWhere = Where.in(new Column(Statuses.ACCOUNT_ID), new RawItemArray(accountIds));
        final Where where;
        if (isFiltersEnabled()) {
            final Where filterWhere = new Where(buildStatusFilterWhereClause(table, null,
                    shouldEnableFiltersForRTs(context)));
            where = Where.and(accountWhere, filterWhere);
        } else {
            where = accountWhere;
        }
        final String selection = processWhere(where).getSQL();
        return new CursorLoader(context, uri, CursorStatusesAdapter.CURSOR_COLS, selection, null, sortOrder);
    }

    @Override
    public void onRefreshFromStart() {
        if (isRefreshing()) return;
        savePosition();
        new AsyncTask<Void, Void, long[][]>() {

            @Override
            protected long[][] doInBackground(final Void... params) {
                final long[][] result = new long[3][];
                final long account_id = getAccountId();
                result[0] = account_id > 0 ? new long[]{account_id} : getActivatedAccountIds(getActivity());
                result[2] = getNewestStatusIds();
                return result;
            }

            @Override
            protected void onPostExecute(final long[][] result) {
                getStatuses(result[0], result[1], result[2]);
            }

        }.execute();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        getLoaderManager().restartLoader(0, getArguments(), this);
    }

    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
        super.onScrollStateChanged(view, scrollState);
        switch (scrollState) {
            case SCROLL_STATE_FLING:
            case SCROLL_STATE_TOUCH_SCROLL: {
                break;
            }
            case SCROLL_STATE_IDLE: {
                savePosition();
                break;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        final ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(Filters.CONTENT_URI, true, mReloadContentObserver);
        if (getAccountId() <= 0) {
            resolver.registerContentObserver(Accounts.CONTENT_URI, true, mReloadContentObserver);
        }
    }

    @Override
    public void onStop() {
        savePosition();
        final ContentResolver resolver = getContentResolver();
        resolver.unregisterContentObserver(mReloadContentObserver);
        super.onStop();
    }

    protected long getAccountId() {
        final Bundle args = getArguments();
        return args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
    }

    protected abstract Uri getContentUri();

    @Override
    protected long[] getNewestStatusIds() {
        final long account_id = getAccountId();
        final long[] account_ids = account_id > 0 ? new long[]{account_id} : getActivatedAccountIds(getActivity());
        return getNewestStatusIdsFromDatabase(getActivity(), getContentUri(), account_ids);
    }

    protected abstract int getNotificationType();

    @Override
    protected long[] getOldestStatusIds() {
        final long account_id = getAccountId();
        final long[] account_ids = account_id > 0 ? new long[]{account_id} : getActivatedAccountIds(getActivity());
        return getOldestStatusIdsFromDatabase(getActivity(), getContentUri(), account_ids);
    }

    protected abstract boolean isFiltersEnabled();

    @Override
    protected void loadMoreStatuses() {
        if (isRefreshing()) return;
        savePosition();
        new AsyncTask<Void, Void, long[][]>() {

            @Override
            protected long[][] doInBackground(final Void... params) {
                final long[][] result = new long[3][];
                final long account_id = getAccountId();
                result[0] = account_id > 0 ? new long[]{account_id} : getActivatedAccountIds(getActivity());
                result[1] = getOldestStatusIds();
                return result;
            }

            @Override
            protected void onPostExecute(final long[][] result) {
                getStatuses(result[0], result[1], result[2]);
            }

        }.execute();
    }

    @Override
    protected CursorStatusesAdapter newAdapterInstance(final boolean compact, final boolean plain) {
        return new CursorStatusesAdapter(getActivity(), compact, plain);
    }

    @Override
    protected void onListTouched() {
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        if (twitter != null) {
            twitter.clearNotificationAsync(getNotificationType(), getAccountId());
        }
    }

    protected Where processWhere(final Where where) {
        return where;
    }

    @Override
    protected boolean shouldShowAccountColor() {
        return getAccountId() <= 0 && getActivatedAccountIds(getActivity()).length > 1;
    }
}

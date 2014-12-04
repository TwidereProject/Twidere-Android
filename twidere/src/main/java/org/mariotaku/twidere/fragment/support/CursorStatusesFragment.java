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

package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import org.mariotaku.querybuilder.Columns.Column;
import org.mariotaku.querybuilder.Expression;
import org.mariotaku.querybuilder.RawItemArray;
import org.mariotaku.twidere.adapter.CursorStatusesAdapter;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.util.Utils;

import static org.mariotaku.twidere.util.Utils.buildStatusFilterWhereClause;
import static org.mariotaku.twidere.util.Utils.getNewestStatusIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.getOldestStatusIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.getTableNameByUri;
import static org.mariotaku.twidere.util.Utils.shouldEnableFiltersForRTs;

/**
 * Created by mariotaku on 14/12/3.
 */
public abstract class CursorStatusesFragment extends AbsStatusesFragment<Cursor> {


    public abstract Uri getContentUri();

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final Context context = getActivity();
        final Uri uri = getContentUri();
        final String table = getTableNameByUri(uri);
        final String sortOrder = getSortOrder();
        final long[] accountIds = getAccountIds();
        final Expression accountWhere = Expression.in(new Column(Statuses.ACCOUNT_ID), new RawItemArray(accountIds));
        final Expression filterWhere = getFiltersWhere(table), where;
        if (filterWhere != null) {
            where = Expression.and(accountWhere, filterWhere);
        } else {
            where = accountWhere;
        }
        final String selection = processWhere(where).getSQL();
        return new CursorLoader(context, uri, Statuses.COLUMNS, selection, null, sortOrder);
    }

    @Override
    public boolean triggerRefresh() {
        new AsyncTask<Void, Void, long[][]>() {

            @Override
            protected long[][] doInBackground(final Void... params) {
                final long[][] result = new long[3][];
                result[0] = getAccountIds();
                result[2] = getNewestStatusIds(result[0]);
                return result;
            }

            @Override
            protected void onPostExecute(final long[][] result) {
                getStatuses(result[0], result[1], result[2]);
            }

        }.execute();
        return true;
    }

    @Override
    protected long[] getAccountIds() {
        final Bundle args = getArguments();
        if (args != null && args.getLong(EXTRA_ACCOUNT_ID) > 0) {
            return new long[]{args.getLong(EXTRA_ACCOUNT_ID)};
        }
        return Utils.getActivatedAccountIds(getActivity());
    }

    protected abstract int getNotificationType();

    @Override
    protected CursorStatusesAdapter onCreateAdapter(final Context context, final boolean compact) {
        return new CursorStatusesAdapter(context, compact);
    }

    @Override
    protected void onLoadMoreStatuses() {
        new AsyncTask<Void, Void, long[][]>() {

            @Override
            protected long[][] doInBackground(final Void... params) {
                final long[][] result = new long[3][];
                result[0] = getAccountIds();
                result[1] = getOldestStatusIds(result[0]);
                return result;
            }

            @Override
            protected void onPostExecute(final long[][] result) {
                getStatuses(result[0], result[1], result[2]);
            }

        }.execute();
    }

    protected Expression getFiltersWhere(String table) {
        if (!isFilterEnabled()) return null;
        return buildStatusFilterWhereClause(table, null, shouldEnableFiltersForRTs(getActivity()));
    }

    protected abstract boolean isFilterEnabled();

    protected long[] getNewestStatusIds(long[] accountIds) {
        return getNewestStatusIdsFromDatabase(getActivity(), getContentUri(), accountIds);
    }

    protected long[] getOldestStatusIds(long[] accountIds) {
        return getOldestStatusIdsFromDatabase(getActivity(), getContentUri(), accountIds);
    }

    protected Expression processWhere(final Expression where) {
        return where;
    }

    private String getSortOrder() {
        final SharedPreferences preferences = getSharedPreferences();
        final boolean sortById = preferences.getBoolean(KEY_SORT_TIMELINE_BY_ID, false);
        return sortById ? Statuses.SORT_ORDER_STATUS_ID_DESC : Statuses.SORT_ORDER_TIMESTAMP_DESC;
    }
}

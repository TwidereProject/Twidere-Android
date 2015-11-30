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

package org.mariotaku.twidere.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.SQLFunctions;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.util.content.ContentResolverUtils;

/**
 * Created by mariotaku on 15/11/28.
 */
public class DataStoreUtils {
    public static long[] getNewestMessageIdsFromDatabase(final Context context, final Uri uri) {
        final long[] accountIds = Utils.getActivatedAccountIds(context);
        return getNewestMessageIdsFromDatabase(context, uri, accountIds);
    }

    public static long[] getNewestMessageIdsFromDatabase(final Context context, final Uri uri, final long[] accountIds) {
        if (context == null || uri == null || accountIds == null) return null;
        final String[] cols = new String[]{TwidereDataStore.DirectMessages.MESSAGE_ID};
        final ContentResolver resolver = context.getContentResolver();
        final long[] messageIds = new long[accountIds.length];
        int idx = 0;
        for (final long accountId : accountIds) {
            final String where = Expression.equals(TwidereDataStore.DirectMessages.ACCOUNT_ID, accountId).getSQL();
            final Cursor cur = ContentResolverUtils.query(resolver, uri, cols, where, null,
                    TwidereDataStore.DirectMessages.DEFAULT_SORT_ORDER);
            if (cur == null) {
                continue;
            }

            if (cur.getCount() > 0) {
                cur.moveToFirst();
                messageIds[idx] = cur.getLong(cur.getColumnIndexOrThrow(TwidereDataStore.DirectMessages.MESSAGE_ID));
            }
            cur.close();
            idx++;
        }
        return messageIds;
    }

    public static long[] getNewestStatusIdsFromDatabase(final Context context, final Uri uri) {
        final long[] account_ids = Utils.getActivatedAccountIds(context);
        return getNewestStatusIdsFromDatabase(context, uri, account_ids);
    }

    public static long[] getNewestStatusIdsFromDatabase(final Context context, final Uri uri, final long[] accountIds) {
        if (context == null || uri == null || accountIds == null) return null;
        final String[] cols = new String[]{TwidereDataStore.Statuses.STATUS_ID};
        final ContentResolver resolver = context.getContentResolver();
        final long[] status_ids = new long[accountIds.length];
        int idx = 0;
        for (final long accountId : accountIds) {
            final String where = Expression.equals(TwidereDataStore.Statuses.ACCOUNT_ID, accountId).getSQL();
            final Cursor cur = ContentResolverUtils
                    .query(resolver, uri, cols, where, null, TwidereDataStore.Statuses.DEFAULT_SORT_ORDER);
            if (cur == null) {
                continue;
            }

            if (cur.getCount() > 0) {
                cur.moveToFirst();
                status_ids[idx] = cur.getLong(cur.getColumnIndexOrThrow(TwidereDataStore.Statuses.STATUS_ID));
            }
            cur.close();
            idx++;
        }
        return status_ids;
    }

    public static long[] getActivityMaxPositionsFromDatabase(final Context context, final Uri uri, final long[] accountIds) {
        if (context == null || uri == null || accountIds == null) return null;
        final String[] cols = new String[]{TwidereDataStore.Activities.MAX_POSITION};
        final ContentResolver resolver = context.getContentResolver();
        final long[] maxPositions = new long[accountIds.length];
        int idx = 0;
        for (final long accountId : accountIds) {
            final String where = Expression.equals(TwidereDataStore.Activities.ACCOUNT_ID, accountId).getSQL();
            final Cursor cur = ContentResolverUtils
                    .query(resolver, uri, cols, where, null, TwidereDataStore.Activities.DEFAULT_SORT_ORDER);
            if (cur == null) {
                continue;
            }

            if (cur.getCount() > 0) {
                cur.moveToFirst();
                maxPositions[idx] = cur.getLong(cur.getColumnIndexOrThrow(TwidereDataStore.Activities.MAX_POSITION));
            }
            cur.close();
            idx++;
        }
        return maxPositions;
    }

    public static long[] getOldestMessageIdsFromDatabase(final Context context, final Uri uri) {
        final long[] account_ids = Utils.getActivatedAccountIds(context);
        return getOldestMessageIdsFromDatabase(context, uri, account_ids);
    }

    public static long[] getOldestMessageIdsFromDatabase(final Context context, final Uri uri, final long[] accountIds) {
        if (context == null || uri == null) return null;
        final String[] cols = new String[]{TwidereDataStore.DirectMessages.MESSAGE_ID};
        final ContentResolver resolver = context.getContentResolver();
        final long[] status_ids = new long[accountIds.length];
        int idx = 0;
        for (final long accountId : accountIds) {
            final String where = Expression.equals(TwidereDataStore.DirectMessages.ACCOUNT_ID, accountId).getSQL();
            final Cursor cur = ContentResolverUtils.query(resolver, uri, cols, where, null, TwidereDataStore.DirectMessages.MESSAGE_ID);
            if (cur == null) {
                continue;
            }

            if (cur.getCount() > 0) {
                cur.moveToFirst();
                status_ids[idx] = cur.getLong(cur.getColumnIndexOrThrow(TwidereDataStore.DirectMessages.MESSAGE_ID));
            }
            cur.close();
            idx++;
        }
        return status_ids;
    }

    public static long[] getOldestStatusIdsFromDatabase(final Context context, final Uri uri) {
        final long[] account_ids = Utils.getActivatedAccountIds(context);
        return getOldestStatusIdsFromDatabase(context, uri, account_ids);
    }

    public static long[] getOldestStatusIdsFromDatabase(final Context context, final Uri uri, final long[] accountIds) {
        if (context == null || uri == null || accountIds == null) return null;
        final String[] cols = new String[]{TwidereDataStore.Statuses.STATUS_ID};
        final ContentResolver resolver = context.getContentResolver();
        final long[] status_ids = new long[accountIds.length];
        int idx = 0;
        for (final long accountId : accountIds) {
            final String where = Expression.equals(TwidereDataStore.Statuses.ACCOUNT_ID, accountId).getSQL();
            final Cursor cur = ContentResolverUtils.query(resolver, uri, cols, where, null, TwidereDataStore.Statuses.STATUS_ID);
            if (cur == null) {
                continue;
            }

            if (cur.getCount() > 0) {
                cur.moveToFirst();
                status_ids[idx] = cur.getLong(cur.getColumnIndexOrThrow(TwidereDataStore.Statuses.STATUS_ID));
            }
            cur.close();
            idx++;
        }
        return status_ids;
    }

    public static int getStatusCountInDatabase(final Context context, final Uri uri, final long accountId) {
        final String where = Expression.equals(TwidereDataStore.Statuses.ACCOUNT_ID, accountId).getSQL();
        return queryCount(context, uri, where, null);
    }

    public static int getActivityCountInDatabase(final Context context, final Uri uri, final long accountId) {
        final String where = Expression.equals(TwidereDataStore.Activities.ACCOUNT_ID, accountId).getSQL();
        return queryCount(context, uri, where, null);
    }

    public static int queryCount(final Context context, final Uri uri, final String selection, final String[] selectionArgs) {
        if (context == null) return -1;
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[]{SQLFunctions.COUNT()};
        final Cursor cur = ContentResolverUtils.query(resolver, uri, projection, selection, selectionArgs, null);
        if (cur == null) return -1;
        try {
            if (cur.moveToFirst()) {
                return cur.getInt(0);
            }
            return -1;
        } finally {
            cur.close();
        }
    }
}

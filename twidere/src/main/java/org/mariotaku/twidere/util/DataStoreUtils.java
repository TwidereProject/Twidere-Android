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
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.JsonMapper;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.commons.logansquare.LoganSquareMapperFinder;
import org.mariotaku.microblog.library.twitter.model.Activity;
import org.mariotaku.sqliteqb.library.ArgsArray;
import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Columns.Column;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.OrderBy;
import org.mariotaku.sqliteqb.library.SQLFunctions;
import org.mariotaku.sqliteqb.library.SQLQueryBuilder;
import org.mariotaku.sqliteqb.library.Table;
import org.mariotaku.sqliteqb.library.Tables;
import org.mariotaku.sqliteqb.library.query.SQLSelectQuery;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableActivityCursorIndices;
import org.mariotaku.twidere.model.ParcelableActivityValuesCreator;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableCredentialsCursorIndices;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserFollowState;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.tab.extra.HomeTabExtras;
import org.mariotaku.twidere.model.tab.extra.InteractionsTabExtras;
import org.mariotaku.twidere.model.tab.extra.TabExtras;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.AccountSupportColumns;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;
import org.mariotaku.twidere.provider.TwidereDataStore.CacheFiles;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedHashtags;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedImages;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedStatuses;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.provider.TwidereDataStore.DNS;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;
import org.mariotaku.twidere.provider.TwidereDataStore.Notifications;
import org.mariotaku.twidere.provider.TwidereDataStore.Permissions;
import org.mariotaku.twidere.provider.TwidereDataStore.Preferences;
import org.mariotaku.twidere.provider.TwidereDataStore.SavedSearches;
import org.mariotaku.twidere.provider.TwidereDataStore.SearchHistory;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.provider.TwidereDataStore.Suggestions;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;
import org.mariotaku.twidere.provider.TwidereDataStore.UnreadCounts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.provider.TwidereDataStore.ACTIVITIES_URIS;
import static org.mariotaku.twidere.provider.TwidereDataStore.CACHE_URIS;
import static org.mariotaku.twidere.provider.TwidereDataStore.DIRECT_MESSAGES_URIS;
import static org.mariotaku.twidere.provider.TwidereDataStore.STATUSES_URIS;

/**
 * Created by mariotaku on 15/11/28.
 */
public class DataStoreUtils implements Constants {
    static final UriMatcher CONTENT_PROVIDER_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static Map<UserKey, String> sAccountScreenNames = new HashMap<>();
    static Map<UserKey, String> sAccountNames = new HashMap<>();

    static {
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Accounts.CONTENT_PATH,
                TABLE_ID_ACCOUNTS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Statuses.CONTENT_PATH,
                TABLE_ID_STATUSES);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Activities.AboutMe.CONTENT_PATH,
                TABLE_ID_ACTIVITIES_ABOUT_ME);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Activities.ByFriends.CONTENT_PATH,
                TABLE_ID_ACTIVITIES_BY_FRIENDS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Drafts.CONTENT_PATH,
                TABLE_ID_DRAFTS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, CachedUsers.CONTENT_PATH,
                TABLE_ID_CACHED_USERS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Filters.Users.CONTENT_PATH,
                TABLE_ID_FILTERED_USERS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Filters.Keywords.CONTENT_PATH,
                TABLE_ID_FILTERED_KEYWORDS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Filters.Sources.CONTENT_PATH,
                TABLE_ID_FILTERED_SOURCES);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Filters.Links.CONTENT_PATH,
                TABLE_ID_FILTERED_LINKS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, DirectMessages.CONTENT_PATH,
                TABLE_ID_DIRECT_MESSAGES);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, DirectMessages.Inbox.CONTENT_PATH,
                TABLE_ID_DIRECT_MESSAGES_INBOX);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, DirectMessages.Outbox.CONTENT_PATH,
                TABLE_ID_DIRECT_MESSAGES_OUTBOX);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, DirectMessages.Conversation.CONTENT_PATH + "/*/*",
                TABLE_ID_DIRECT_MESSAGES_CONVERSATION);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, DirectMessages.Conversation.CONTENT_PATH_SCREEN_NAME + "/*/*",
                TABLE_ID_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, DirectMessages.ConversationEntries.CONTENT_PATH,
                TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRIES);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, CachedTrends.Local.CONTENT_PATH,
                TABLE_ID_TRENDS_LOCAL);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Tabs.CONTENT_PATH,
                TABLE_ID_TABS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, CachedStatuses.CONTENT_PATH,
                TABLE_ID_CACHED_STATUSES);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, CachedHashtags.CONTENT_PATH,
                TABLE_ID_CACHED_HASHTAGS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, CachedRelationships.CONTENT_PATH,
                TABLE_ID_CACHED_RELATIONSHIPS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, SavedSearches.CONTENT_PATH,
                TABLE_ID_SAVED_SEARCHES);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, SearchHistory.CONTENT_PATH,
                TABLE_ID_SEARCH_HISTORY);

        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Notifications.CONTENT_PATH,
                VIRTUAL_TABLE_ID_NOTIFICATIONS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Notifications.CONTENT_PATH + "/#",
                VIRTUAL_TABLE_ID_NOTIFICATIONS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Notifications.CONTENT_PATH + "/#/*",
                VIRTUAL_TABLE_ID_NOTIFICATIONS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Permissions.CONTENT_PATH,
                VIRTUAL_TABLE_ID_PERMISSIONS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, DNS.CONTENT_PATH + "/*",
                VIRTUAL_TABLE_ID_DNS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, CachedImages.CONTENT_PATH,
                VIRTUAL_TABLE_ID_CACHED_IMAGES);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, CacheFiles.CONTENT_PATH + "/*",
                VIRTUAL_TABLE_ID_CACHE_FILES);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Preferences.CONTENT_PATH,
                VIRTUAL_TABLE_ID_ALL_PREFERENCES);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Preferences.CONTENT_PATH + "/*",
                VIRTUAL_TABLE_ID_PREFERENCES);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, UnreadCounts.CONTENT_PATH,
                VIRTUAL_TABLE_ID_UNREAD_COUNTS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, UnreadCounts.CONTENT_PATH + "/#",
                VIRTUAL_TABLE_ID_UNREAD_COUNTS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, UnreadCounts.CONTENT_PATH + "/#/#/*",
                VIRTUAL_TABLE_ID_UNREAD_COUNTS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, UnreadCounts.ByType.CONTENT_PATH + "/*",
                VIRTUAL_TABLE_ID_UNREAD_COUNTS_BY_TYPE);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, TwidereDataStore.CONTENT_PATH_DATABASE_READY,
                VIRTUAL_TABLE_ID_DATABASE_READY);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, CachedUsers.CONTENT_PATH_WITH_RELATIONSHIP + "/*",
                VIRTUAL_TABLE_ID_CACHED_USERS_WITH_RELATIONSHIP);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, CachedUsers.CONTENT_PATH_WITH_SCORE + "/*",
                VIRTUAL_TABLE_ID_CACHED_USERS_WITH_SCORE);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Drafts.CONTENT_PATH_UNSENT,
                VIRTUAL_TABLE_ID_DRAFTS_UNSENT);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Drafts.CONTENT_PATH_NOTIFICATIONS,
                VIRTUAL_TABLE_ID_DRAFTS_NOTIFICATIONS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Drafts.CONTENT_PATH_NOTIFICATIONS,
                VIRTUAL_TABLE_ID_DRAFTS_NOTIFICATIONS);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Suggestions.AutoComplete.CONTENT_PATH,
                VIRTUAL_TABLE_ID_SUGGESTIONS_AUTO_COMPLETE);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Suggestions.Search.CONTENT_PATH,
                VIRTUAL_TABLE_ID_SUGGESTIONS_SEARCH);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, TwidereDataStore.CONTENT_PATH_EMPTY,
                VIRTUAL_TABLE_ID_EMPTY);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, TwidereDataStore.CONTENT_PATH_RAW_QUERY + "/*",
                VIRTUAL_TABLE_ID_RAW_QUERY);
    }

    @NonNull
    public static String[] getNewestMessageIds(@NonNull final Context context, @NonNull final Uri uri,
                                               @NonNull final UserKey[] accountKeys) {
        return getStringFieldArray(context, uri, accountKeys, DirectMessages.ACCOUNT_KEY,
                DirectMessages.MESSAGE_ID, new OrderBy(SQLFunctions.MAX(DirectMessages.MESSAGE_TIMESTAMP)));
    }

    @NonNull
    public static String[] getNewestStatusIds(@NonNull final Context context, @NonNull final Uri uri,
                                              @NonNull final UserKey[] accountKeys) {
        return getStringFieldArray(context, uri, accountKeys, Statuses.ACCOUNT_KEY,
                Statuses.STATUS_ID, new OrderBy(SQLFunctions.MAX(Statuses.STATUS_TIMESTAMP)));
    }


    @NonNull
    public static long[] getNewestStatusSortIds(@NonNull final Context context, @NonNull final Uri uri,
                                                @NonNull final UserKey[] accountKeys) {
        return getLongFieldArray(context, uri, accountKeys, Statuses.ACCOUNT_KEY,
                Statuses.SORT_ID, new OrderBy(SQLFunctions.MAX(Statuses.STATUS_TIMESTAMP)));
    }


    @NonNull
    public static String[] getOldestMessageIds(@NonNull final Context context, @NonNull final Uri uri,
                                               @NonNull final UserKey[] accountKeys) {
        return getStringFieldArray(context, uri, accountKeys, DirectMessages.ACCOUNT_KEY,
                DirectMessages.MESSAGE_ID, new OrderBy(SQLFunctions.MIN(DirectMessages.MESSAGE_TIMESTAMP)));
    }

    @NonNull
    public static String[] getOldestStatusIds(@NonNull final Context context, @NonNull final Uri uri,
                                              @NonNull final UserKey[] accountKeys) {
        return getStringFieldArray(context, uri, accountKeys, Statuses.ACCOUNT_KEY,
                Statuses.STATUS_ID, new OrderBy(SQLFunctions.MIN(Statuses.STATUS_TIMESTAMP)));
    }


    @NonNull
    public static long[] getOldestStatusSortIds(@NonNull final Context context, @NonNull final Uri uri,
                                                @NonNull final UserKey[] accountKeys) {
        return getLongFieldArray(context, uri, accountKeys, Statuses.ACCOUNT_KEY,
                Statuses.SORT_ID, new OrderBy(SQLFunctions.MIN(Statuses.STATUS_TIMESTAMP)));
    }

    @NonNull
    public static String[] getNewestActivityMaxPositions(final Context context, final Uri uri,
                                                         final UserKey[] accountKeys) {
        return getStringFieldArray(context, uri, accountKeys, Activities.ACCOUNT_KEY,
                Activities.MAX_REQUEST_POSITION, new OrderBy(SQLFunctions.MAX(Activities.TIMESTAMP)));
    }

    @NonNull
    public static String[] getOldestActivityMaxPositions(@NonNull final Context context,
                                                         @NonNull final Uri uri,
                                                         @NonNull final UserKey[] accountKeys) {
        return getStringFieldArray(context, uri, accountKeys, Activities.ACCOUNT_KEY,
                Activities.MAX_REQUEST_POSITION, new OrderBy(SQLFunctions.MIN(Activities.TIMESTAMP)));
    }

    @NonNull
    public static long[] getNewestActivityMaxSortPositions(final Context context, final Uri uri,
                                                           final UserKey[] accountKeys) {
        return getLongFieldArray(context, uri, accountKeys, Activities.ACCOUNT_KEY,
                Activities.MAX_SORT_POSITION, new OrderBy(SQLFunctions.MAX(Activities.TIMESTAMP)));
    }

    @NonNull
    public static long[] getOldestActivityMaxSortPositions(@NonNull final Context context,
                                                           @NonNull final Uri uri,
                                                           @NonNull final UserKey[] accountKeys) {
        return getLongFieldArray(context, uri, accountKeys, Activities.ACCOUNT_KEY,
                Activities.MAX_SORT_POSITION, new OrderBy(SQLFunctions.MIN(Activities.TIMESTAMP)));
    }

    public static int getStatusCount(final Context context, final Uri uri, final UserKey accountId) {
        final String where = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY).getSQL();
        final String[] whereArgs = {accountId.toString()};
        return queryCount(context, uri, where, whereArgs);
    }

    public static int getActivitiesCount(@NonNull final Context context, @NonNull final Uri uri,
                                         @NonNull final UserKey accountKey) {
        final String where = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY).getSQL();
        return queryCount(context, uri, where, new String[]{accountKey.toString()});
    }


    @NonNull
    public static UserKey[] getFilteredUserIds(Context context) {
        if (context == null) return new UserKey[0];
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = {Filters.Users.USER_KEY};
        final Cursor cur = resolver.query(Filters.Users.CONTENT_URI, projection, null, null, null);
        if (cur == null) return new UserKey[0];
        try {
            final UserKey[] ids = new UserKey[cur.getCount()];
            cur.moveToFirst();
            int i = 0;
            while (!cur.isAfterLast()) {
                ids[i] = UserKey.valueOf(cur.getString(0));
                cur.moveToNext();
                i++;
            }
            cur.close();
            return ids;
        } finally {
            cur.close();
        }
    }

    @NonNull
    public static Expression buildStatusFilterWhereClause(@NonNull final String table, final Expression extraSelection) {
        final SQLSelectQuery filteredUsersQuery = SQLQueryBuilder
                .select(new Column(new Table(Filters.Users.TABLE_NAME), Filters.Users.USER_KEY))
                .from(new Tables(Filters.Users.TABLE_NAME))
                .build();
        final Expression filteredUsersWhere = Expression.or(
                Expression.in(new Column(new Table(table), Statuses.USER_KEY), filteredUsersQuery),
                Expression.in(new Column(new Table(table), Statuses.RETWEETED_BY_USER_KEY), filteredUsersQuery),
                Expression.in(new Column(new Table(table), Statuses.QUOTED_USER_KEY), filteredUsersQuery)
        );
        final SQLSelectQuery.Builder filteredIdsQueryBuilder = SQLQueryBuilder
                .select(new Column(new Table(table), Statuses._ID))
                .from(new Tables(table))
                .where(filteredUsersWhere)
                .union()
                .select(new Columns(new Column(new Table(table), Statuses._ID)))
                .from(new Tables(table, Filters.Sources.TABLE_NAME))
                .where(Expression.or(
                        Expression.likeRaw(new Column(new Table(table), Statuses.SOURCE),
                                "'%>'||" + Filters.Sources.TABLE_NAME + "." + Filters.Sources.VALUE + "||'</a>%'"),
                        Expression.likeRaw(new Column(new Table(table), Statuses.QUOTED_SOURCE),
                                "'%>'||" + Filters.Sources.TABLE_NAME + "." + Filters.Sources.VALUE + "||'</a>%'")
                ))
                .union()
                .select(new Columns(new Column(new Table(table), Statuses._ID)))
                .from(new Tables(table, Filters.Keywords.TABLE_NAME))
                .where(Expression.or(
                        Expression.likeRaw(new Column(new Table(table), Statuses.TEXT_PLAIN),
                                "'%'||" + Filters.Keywords.TABLE_NAME + "." + Filters.Keywords.VALUE + "||'%'"),
                        Expression.likeRaw(new Column(new Table(table), Statuses.QUOTED_TEXT_PLAIN),
                                "'%'||" + Filters.Keywords.TABLE_NAME + "." + Filters.Keywords.VALUE + "||'%'")
                ))
                .union()
                .select(new Columns(new Column(new Table(table), Statuses._ID)))
                .from(new Tables(table, Filters.Links.TABLE_NAME))
                .where(Expression.or(
                        Expression.likeRaw(new Column(new Table(table), Statuses.SPANS),
                                "'%'||" + Filters.Links.TABLE_NAME + "." + Filters.Links.VALUE + "||'%'"),
                        Expression.likeRaw(new Column(new Table(table), Statuses.QUOTED_SPANS),
                                "'%'||" + Filters.Links.TABLE_NAME + "." + Filters.Links.VALUE + "||'%'")
                ));
        final Expression filterExpression = Expression.or(
                Expression.notIn(new Column(new Table(table), Statuses._ID), filteredIdsQueryBuilder.build()),
                Expression.equals(new Column(new Table(table), Statuses.IS_GAP), 1)
        );
        if (extraSelection != null) {
            return Expression.and(filterExpression, extraSelection);
        }
        return filterExpression;
    }


    public static String getAccountDisplayName(final Context context, final UserKey accountKey, final boolean nameFirst) {
        final String name;
        if (nameFirst) {
            name = getAccountName(context, accountKey);
        } else {
            name = String.format("@%s", getAccountScreenName(context, accountKey));
        }
        return name;
    }

    public static String getAccountName(final Context context, final UserKey accountKey) {
        if (context == null) return null;
        final String cached = sAccountNames.get(accountKey);
        if (!isEmpty(cached)) return cached;
        final String[] projection = {Accounts.SCREEN_NAME};
        final Cursor cur = getAccountCursor(context, projection, accountKey);
        if (cur == null) return null;
        try {
            if (cur.moveToFirst()) {
                final String name = cur.getString(0);
                sAccountNames.put(accountKey, name);
                return name;
            }
            return null;
        } finally {
            cur.close();
        }
    }

    public static String getAccountScreenName(final Context context, final UserKey accountKey) {
        if (context == null) return null;
        final String cached = sAccountScreenNames.get(accountKey);
        if (!isEmpty(cached)) return cached;
        final String[] projection = {Accounts.SCREEN_NAME};
        final Cursor cur = getAccountCursor(context, projection, accountKey);
        if (cur == null) return null;
        try {
            if (cur.moveToFirst()) {
                final String name = cur.getString(0);
                sAccountScreenNames.put(accountKey, name);
                return name;
            }
            return null;
        } finally {
            cur.close();
        }
    }

    public static String[] getAccountScreenNames(final Context context) {
        return getAccountScreenNames(context, null);
    }

    public static String[] getAccountScreenNames(@NonNull final Context context, @Nullable final UserKey[] accountKeys) {
        final String[] cols = new String[]{Accounts.SCREEN_NAME};
        final String where;
        final String[] whereArgs;
        if (accountKeys != null) {
            where = Expression.inArgs(new Column(Accounts.ACCOUNT_KEY), accountKeys.length).getSQL();
            whereArgs = TwidereArrayUtils.toStringArray(accountKeys);
        } else {
            where = null;
            whereArgs = null;
        }
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, where, whereArgs, null);
        if (cur == null) return new String[0];
        try {
            cur.moveToFirst();
            final String[] screen_names = new String[cur.getCount()];
            int i = 0;
            while (!cur.isAfterLast()) {
                screen_names[i++] = cur.getString(0);
                cur.moveToNext();
            }
            return screen_names;
        } finally {
            cur.close();
        }
    }

    @NonNull
    public static UserKey[] getActivatedAccountKeys(@NonNull final Context context) {
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
                new String[]{Accounts.ACCOUNT_KEY}, Accounts.IS_ACTIVATED + " = 1", null, null);
        if (cur == null) return new UserKey[0];
        try {
            cur.moveToFirst();
            final UserKey[] ids = new UserKey[cur.getCount()];
            int i = 0;
            while (!cur.isAfterLast()) {
                ids[i++] = UserKey.valueOf(cur.getString(0));
                cur.moveToNext();
            }
            return ids;
        } finally {
            cur.close();
        }
    }

    public static int getStatusesCount(@NonNull final Context context, final Uri uri,
                                       @Nullable final Bundle extraArgs, final long compare,
                                       String compareColumn, boolean greaterThan,
                                       @Nullable UserKey[] accountKeys) {
        if (accountKeys == null) {
            accountKeys = getActivatedAccountKeys(context);
        }

        List<Expression> expressions = new ArrayList<>();
        List<String> expressionArgs = new ArrayList<>();

        expressions.add(Expression.inArgs(new Column(Statuses.ACCOUNT_KEY), accountKeys.length));
        for (UserKey accountKey : accountKeys) {
            expressionArgs.add(accountKey.toString());
        }

        if (greaterThan) {
            expressions.add(Expression.greaterThanArgs(compareColumn));
        } else {
            expressions.add(Expression.lesserThanArgs(compareColumn));
        }
        expressionArgs.add(String.valueOf(compare));

        expressions.add(buildStatusFilterWhereClause(getTableNameByUri(uri), null));

        if (extraArgs != null) {
            Parcelable extras = extraArgs.getParcelable(EXTRA_EXTRAS);
            if (extras instanceof HomeTabExtras) {
                processTabExtras(expressions, expressionArgs, (HomeTabExtras) extras);
            }
        }

        Expression selection = Expression.and(expressions.toArray(new Expression[expressions.size()]));
        return queryCount(context, uri, selection.getSQL(), expressionArgs.toArray(new String[expressionArgs.size()]));
    }

    public static int getActivitiesCount(final Context context, final Uri uri, final long compare,
                                         String compareColumn, boolean greaterThan, UserKey... accountKeys) {
        if (context == null) return 0;
        if (accountKeys == null) {
            accountKeys = getActivatedAccountKeys(context);
        }
        final Expression selection = Expression.and(
                Expression.inArgs(new Column(Activities.ACCOUNT_KEY), accountKeys.length),
                greaterThan ? Expression.greaterThanArgs(compareColumn) : Expression.lesserThanArgs(compareColumn),
                buildActivityFilterWhereClause(getTableNameByUri(uri), null)
        );
        final String[] whereArgs = new String[accountKeys.length + 1];
        for (int i = 0; i < accountKeys.length; i++) {
            whereArgs[i] = accountKeys[i].toString();
        }
        whereArgs[accountKeys.length] = String.valueOf(compare);
        return queryCount(context, uri, selection.getSQL(), whereArgs);
    }

    public static int getActivitiesCount(@NonNull final Context context, final Uri uri,
                                         final Expression extraWhere, final String[] extraWhereArgs,
                                         final long since, String sinceColumn, boolean followingOnly,
                                         @Nullable UserKey[] accountKeys) {
        if (accountKeys == null) {
            accountKeys = getActivatedAccountKeys(context);
        }
        Expression[] expressions;
        if (extraWhere != null) {
            expressions = new Expression[4];
            expressions[3] = extraWhere;
        } else {
            expressions = new Expression[3];
        }
        expressions[0] = Expression.inArgs(new Column(Activities.ACCOUNT_KEY), accountKeys.length);
        expressions[1] = Expression.greaterThanArgs(sinceColumn);
        expressions[2] = buildActivityFilterWhereClause(getTableNameByUri(uri), null);
        final Expression selection = Expression.and(expressions);
        String[] selectionArgs;
        if (extraWhereArgs != null) {
            selectionArgs = new String[accountKeys.length + 1 + extraWhereArgs.length];
            System.arraycopy(extraWhereArgs, 0, selectionArgs, accountKeys.length + 1,
                    extraWhereArgs.length);
        } else {
            selectionArgs = new String[accountKeys.length + 1];
        }
        for (int i = 0; i < accountKeys.length; i++) {
            selectionArgs[i] = accountKeys[i].toString();
        }
        selectionArgs[accountKeys.length] = String.valueOf(since);
        // If followingOnly option is on, we have to iterate over items
        if (followingOnly) {
            final ContentResolver resolver = context.getContentResolver();
            final String[] projection = new String[]{Activities.SOURCES};
            final Cursor cur = resolver.query(uri, projection, selection.getSQL(), selectionArgs, null);
            if (cur == null) return -1;
            try {
                final JsonMapper<UserFollowState> mapper;
                try {
                    mapper = LoganSquareMapperFinder.mapperFor(UserFollowState.class);
                } catch (LoganSquareMapperFinder.ClassLoaderDeadLockException e) {
                    return -1;
                }
                int total = 0;
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    final String string = cur.getString(0);
                    if (TextUtils.isEmpty(string)) continue;
                    boolean hasFollowing = false;
                    try {
                        for (UserFollowState state : mapper.parseList(string)) {
                            if (state.is_following) {
                                hasFollowing = true;
                                break;
                            }
                        }
                    } catch (IOException e) {
                        continue;
                    }
                    if (hasFollowing) {
                        total++;
                    }
                    cur.moveToNext();
                }
                return total;
            } finally {
                cur.close();
            }
        }
        return queryCount(context, uri, selection.getSQL(), selectionArgs);
    }

    public static int getTableId(final Uri uri) {
        if (uri == null) return -1;
        return CONTENT_PROVIDER_URI_MATCHER.match(uri);
    }

    public static String getTableNameById(final int id) {
        switch (id) {
            case TwidereConstants.TABLE_ID_ACCOUNTS:
                return Accounts.TABLE_NAME;
            case TwidereConstants.TABLE_ID_STATUSES:
                return Statuses.TABLE_NAME;
            case TwidereConstants.TABLE_ID_ACTIVITIES_ABOUT_ME:
                return Activities.AboutMe.TABLE_NAME;
            case TwidereConstants.TABLE_ID_ACTIVITIES_BY_FRIENDS:
                return Activities.ByFriends.TABLE_NAME;
            case TwidereConstants.TABLE_ID_DRAFTS:
                return Drafts.TABLE_NAME;
            case TwidereConstants.TABLE_ID_FILTERED_USERS:
                return Filters.Users.TABLE_NAME;
            case TwidereConstants.TABLE_ID_FILTERED_KEYWORDS:
                return Filters.Keywords.TABLE_NAME;
            case TwidereConstants.TABLE_ID_FILTERED_SOURCES:
                return Filters.Sources.TABLE_NAME;
            case TwidereConstants.TABLE_ID_FILTERED_LINKS:
                return Filters.Links.TABLE_NAME;
            case TwidereConstants.TABLE_ID_DIRECT_MESSAGES_INBOX:
                return DirectMessages.Inbox.TABLE_NAME;
            case TwidereConstants.TABLE_ID_DIRECT_MESSAGES_OUTBOX:
                return DirectMessages.Outbox.TABLE_NAME;
            case TwidereConstants.TABLE_ID_DIRECT_MESSAGES:
                return DirectMessages.TABLE_NAME;
            case TwidereConstants.TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRIES:
                return DirectMessages.ConversationEntries.TABLE_NAME;
            case TwidereConstants.TABLE_ID_TRENDS_LOCAL:
                return CachedTrends.Local.TABLE_NAME;
            case TwidereConstants.TABLE_ID_TABS:
                return Tabs.TABLE_NAME;
            case TwidereConstants.TABLE_ID_CACHED_STATUSES:
                return CachedStatuses.TABLE_NAME;
            case TwidereConstants.TABLE_ID_CACHED_USERS:
                return CachedUsers.TABLE_NAME;
            case TwidereConstants.TABLE_ID_CACHED_HASHTAGS:
                return CachedHashtags.TABLE_NAME;
            case TwidereConstants.TABLE_ID_CACHED_RELATIONSHIPS:
                return CachedRelationships.TABLE_NAME;
            case TwidereConstants.TABLE_ID_SAVED_SEARCHES:
                return SavedSearches.TABLE_NAME;
            case TwidereConstants.TABLE_ID_SEARCH_HISTORY:
                return SearchHistory.TABLE_NAME;
            default:
                return null;
        }
    }

    public static String getTableNameByUri(final Uri uri) {
        if (uri == null) return null;
        return getTableNameById(getTableId(uri));
    }

    @NonNull
    public static Expression buildActivityFilterWhereClause(@NonNull final String table, final Expression extraSelection) {
        final SQLSelectQuery filteredUsersQuery = SQLQueryBuilder
                .select(new Column(new Table(Filters.Users.TABLE_NAME), Filters.Users.USER_KEY))
                .from(new Tables(Filters.Users.TABLE_NAME))
                .build();
        final Expression filteredUsersWhere = Expression.or(
                Expression.in(new Column(new Table(table), Activities.STATUS_USER_KEY), filteredUsersQuery),
                Expression.in(new Column(new Table(table), Activities.STATUS_RETWEETED_BY_USER_KEY), filteredUsersQuery),
                Expression.in(new Column(new Table(table), Activities.STATUS_QUOTED_USER_KEY), filteredUsersQuery)
        );
        final SQLSelectQuery.Builder filteredIdsQueryBuilder = SQLQueryBuilder
                .select(new Column(new Table(table), Activities._ID))
                .from(new Tables(table))
                .where(filteredUsersWhere)
                .union()
                .select(new Columns(new Column(new Table(table), Activities._ID)))
                .from(new Tables(table, Filters.Sources.TABLE_NAME))
                .where(Expression.or(
                        Expression.likeRaw(new Column(new Table(table), Activities.STATUS_SOURCE),
                                "'%>'||" + Filters.Sources.TABLE_NAME + "." + Filters.Sources.VALUE + "||'</a>%'"),
                        Expression.likeRaw(new Column(new Table(table), Activities.STATUS_QUOTE_SOURCE),
                                "'%>'||" + Filters.Sources.TABLE_NAME + "." + Filters.Sources.VALUE + "||'</a>%'")
                ))
                .union()
                .select(new Columns(new Column(new Table(table), Activities._ID)))
                .from(new Tables(table, Filters.Keywords.TABLE_NAME))
                .where(Expression.or(
                        Expression.likeRaw(new Column(new Table(table), Activities.STATUS_TEXT_PLAIN),
                                "'%'||" + Filters.Keywords.TABLE_NAME + "." + Filters.Keywords.VALUE + "||'%'"),
                        Expression.likeRaw(new Column(new Table(table), Activities.STATUS_QUOTE_TEXT_PLAIN),
                                "'%'||" + Filters.Keywords.TABLE_NAME + "." + Filters.Keywords.VALUE + "||'%'")
                ))
                .union()
                .select(new Columns(new Column(new Table(table), Activities._ID)))
                .from(new Tables(table, Filters.Links.TABLE_NAME))
                .where(Expression.or(
                        Expression.likeRaw(new Column(new Table(table), Activities.STATUS_SPANS),
                                "'%'||" + Filters.Links.TABLE_NAME + "." + Filters.Links.VALUE + "||'%'"),
                        Expression.likeRaw(new Column(new Table(table), Activities.STATUS_QUOTE_SPANS),
                                "'%'||" + Filters.Links.TABLE_NAME + "." + Filters.Links.VALUE + "||'%'")
                ));
        final Expression filterExpression = Expression.or(
                Expression.notIn(new Column(new Table(table), Activities._ID), filteredIdsQueryBuilder.build()),
                Expression.equals(new Column(new Table(table), Activities.IS_GAP), 1)
        );
        if (extraSelection != null) {
            return Expression.and(filterExpression, extraSelection);
        }
        return filterExpression;
    }

    @NonNull
    public static int[] getAccountColors(@NonNull final Context context, @NonNull final UserKey[] accountKeys) {
        final String[] cols = new String[]{Accounts.ACCOUNT_KEY, Accounts.COLOR};
        final String where = Expression.inArgs(new Column(Accounts.ACCOUNT_KEY), accountKeys.length).getSQL();
        final String[] whereArgs = TwidereArrayUtils.toStringArray(accountKeys);
        final int[] colors = new int[accountKeys.length];
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, where,
                whereArgs, null);
        if (cur == null) return colors;
        try {
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                final int idx = ArrayUtils.indexOf(accountKeys, UserKey.valueOf(cur.getString(0)));
                if (idx >= 0) {
                    colors[idx] = cur.getInt(1);
                }
                cur.moveToNext();
            }
            return colors;
        } finally {
            cur.close();
        }
    }

    public static UserKey findAccountKeyByScreenName(@NonNull final Context context, @NonNull final String screenName) {
        final String[] projection = {Accounts.ACCOUNT_KEY};
        final String where = Expression.equalsArgs(Accounts.SCREEN_NAME).getSQL();
        final String[] whereArgs = {screenName};
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, projection,
                where, whereArgs, null);
        if (cur == null) return null;
        try {
            if (cur.moveToFirst()) {
                return UserKey.valueOf(cur.getString(0));
            }
            return null;
        } finally {
            cur.close();
        }
    }

    public static UserKey findAccountKey(@NonNull final Context context, final String accountId) {
        final String[] projection = {Accounts.ACCOUNT_KEY};
        final Cursor cur = findAccountCursorsById(context, projection, accountId);
        if (cur == null) return null;
        try {
            if (cur.moveToFirst()) {
                return UserKey.valueOf(cur.getString(0));
            }
            return null;
        } finally {
            cur.close();
        }
    }

    @NonNull
    public static UserKey[] getAccountKeys(final Context context) {
        if (context == null) return new UserKey[0];
        final String[] projection = {Accounts.ACCOUNT_KEY};
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, projection,
                null, null, null);
        if (cur == null) return new UserKey[0];
        try {
            cur.moveToFirst();
            final UserKey[] ids = new UserKey[cur.getCount()];
            int i = 0;
            while (!cur.isAfterLast()) {
                ids[i++] = UserKey.valueOf(cur.getString(0));
                cur.moveToNext();
            }
            return ids;
        } finally {
            cur.close();
        }
    }

    public static boolean hasAccount(final Context context) {
        if (context == null) return false;
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, new String[]{SQLFunctions.COUNT()}, null, null, null);
        if (cur == null) return false;
        try {
            cur.moveToFirst();
            return cur.getInt(0) > 0;
        } finally {
            cur.close();
        }
    }

    public static synchronized void cleanDatabasesByItemLimit(final Context context) {
        if (context == null) return;
        final ContentResolver resolver = context.getContentResolver();
        final int itemLimit = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(
                KEY_DATABASE_ITEM_LIMIT, DEFAULT_DATABASE_ITEM_LIMIT);

        for (final UserKey accountKey : getAccountKeys(context)) {
            // Clean statuses.
            for (final Uri uri : STATUSES_URIS) {
                if (CachedStatuses.CONTENT_URI.equals(uri)) {
                    continue;
                }
                final String table = getTableNameByUri(uri);
                final SQLSelectQuery.Builder qb = new SQLSelectQuery.Builder();
                qb.select(new Column(Statuses._ID))
                        .from(new Tables(table))
                        .where(Expression.equalsArgs(Statuses.ACCOUNT_KEY))
                        .orderBy(new OrderBy(Statuses.POSITION_KEY, false))
                        .limit(itemLimit);
                final Expression where = Expression.and(
                        Expression.notIn(new Column(Statuses._ID), qb.build()),
                        Expression.equalsArgs(Statuses.ACCOUNT_KEY)
                );
                final String[] whereArgs = {String.valueOf(accountKey), String.valueOf(accountKey)};
                resolver.delete(uri, where.getSQL(), whereArgs);
            }
            for (final Uri uri : ACTIVITIES_URIS) {
                final String table = getTableNameByUri(uri);
                final SQLSelectQuery.Builder qb = new SQLSelectQuery.Builder();
                qb.select(new Column(Activities._ID))
                        .from(new Tables(table))
                        .where(Expression.equalsArgs(Activities.ACCOUNT_KEY))
                        .orderBy(new OrderBy(Activities.TIMESTAMP, false))
                        .limit(itemLimit);
                final Expression where = Expression.and(
                        Expression.notIn(new Column(Activities._ID), qb.build()),
                        Expression.equalsArgs(Activities.ACCOUNT_KEY)
                );
                final String[] whereArgs = {String.valueOf(accountKey), String.valueOf(accountKey)};
                resolver.delete(uri, where.getSQL(), whereArgs);
            }
            for (final Uri uri : DIRECT_MESSAGES_URIS) {
                final String table = getTableNameByUri(uri);
                final Expression accountWhere = Expression.equalsArgs(DirectMessages.ACCOUNT_KEY);
                final SQLSelectQuery.Builder qb = new SQLSelectQuery.Builder();
                qb.select(new Column(DirectMessages._ID))
                        .from(new Tables(table))
                        .where(accountWhere)
                        .orderBy(new OrderBy(DirectMessages.MESSAGE_ID, false))
                        .limit(itemLimit * 10);
                final Expression where = Expression.and(
                        Expression.notIn(new Column(DirectMessages._ID), qb.build()),
                        Expression.equalsArgs(DirectMessages.ACCOUNT_KEY)
                );
                final String[] whereArgs = {String.valueOf(accountKey), String.valueOf(accountKey)};
                resolver.delete(uri, where.getSQL(), whereArgs);
            }
        }
        // Clean cached values.
        for (final Uri uri : CACHE_URIS) {
            final String table = getTableNameByUri(uri);
            if (table == null) continue;
            final SQLSelectQuery.Builder qb = new SQLSelectQuery.Builder();
            qb.select(new Column(BaseColumns._ID))
                    .from(new Tables(table))
                    .orderBy(new OrderBy(BaseColumns._ID, false))
                    .limit(itemLimit * 20);
            final Expression where = Expression.notIn(new Column(BaseColumns._ID), qb.build());
            resolver.delete(uri, where.getSQL(), null);
        }
    }

    public static void clearAccountName() {
        sAccountScreenNames.clear();
    }

    public static boolean isFilteringUser(Context context, UserKey userKey) {
        return isFilteringUser(context, userKey.toString());
    }

    public static boolean isFilteringUser(Context context, String userKey) {
        final ContentResolver cr = context.getContentResolver();
        final Expression where = Expression.equalsArgs(Filters.Users.USER_KEY);
        final Cursor c = cr.query(Filters.Users.CONTENT_URI, new String[]{SQLFunctions.COUNT()},
                where.getSQL(), new String[]{userKey}, null);
        if (c == null) return false;
        try {
            if (c.moveToFirst()) {
                return c.getLong(0) > 0;
            }
        } finally {
            c.close();
        }
        return false;
    }

    @NonNull
    static String[] getStringFieldArray(@NonNull Context context, @NonNull Uri uri,
                                        @NonNull UserKey[] keys, @NonNull String keyField,
                                        @NonNull String valueField, @Nullable OrderBy sortExpression) {
        return getFieldArray(context, uri, keys, keyField, valueField, sortExpression, new FieldArrayCreator<String[]>() {
            @Override
            public String[] newArray(int size) {
                return new String[size];
            }

            @Override
            public void assign(String[] array, int arrayIdx, Cursor cur, int colIdx) {
                array[arrayIdx] = cur.getString(colIdx);
            }
        });
    }

    @NonNull
    static long[] getLongFieldArray(@NonNull Context context, @NonNull Uri uri,
                                    @NonNull UserKey[] keys, @NonNull String keyField,
                                    @NonNull String valueField, @Nullable OrderBy sortExpression) {
        return getFieldArray(context, uri, keys, keyField, valueField, sortExpression, new FieldArrayCreator<long[]>() {
            @Override
            public long[] newArray(int size) {
                return new long[size];
            }

            @Override
            public void assign(long[] array, int arrayIdx, Cursor cur, int colIdx) {
                array[arrayIdx] = cur.getLong(colIdx);
            }
        });
    }

    @NonNull
    static <T> T getFieldArray(@NonNull Context context, @NonNull Uri uri,
                               @NonNull UserKey[] keys, @NonNull String keyField,
                               @NonNull String valueField, @Nullable OrderBy sortExpression,
                               @NonNull FieldArrayCreator<T> creator) {
        final ContentResolver resolver = context.getContentResolver();
        final T messageIds = creator.newArray(keys.length);
        final String[] selectionArgs = TwidereArrayUtils.toStringArray(keys);
        final SQLSelectQuery.Builder builder = SQLQueryBuilder.select(new Columns(keyField, valueField))
                .from(new Table(getTableNameByUri(uri)))
                .groupBy(new Column(keyField))
                .having(Expression.in(new Column(keyField), new ArgsArray(keys.length)));
        if (sortExpression != null) {
            builder.orderBy(sortExpression);
        }
        final Cursor cur = resolver.query(Uri.withAppendedPath(TwidereDataStore.CONTENT_URI_RAW_QUERY,
                builder.buildSQL()), null, null, selectionArgs, null);
        if (cur == null) return messageIds;
        try {
            while (cur.moveToNext()) {
                final UserKey accountKey = UserKey.valueOf(cur.getString(0));
                int idx = ArrayUtils.indexOf(keys, accountKey);
                if (idx < 0) continue;
                creator.assign(messageIds, idx, cur, 1);
            }
            return messageIds;
        } finally {
            cur.close();
        }
    }

    public static void deleteStatus(@NonNull ContentResolver cr, @NonNull UserKey accountKey,
                                    @NonNull String statusId, @Nullable ParcelableStatus status) {

        final String host = accountKey.getHost();
        final String deleteWhere, updateWhere;
        final String[] deleteWhereArgs, updateWhereArgs;
        if (host != null) {
            deleteWhere = Expression.and(
                    Expression.likeRaw(new Column(Statuses.ACCOUNT_KEY), "'%@'||?"),
                    Expression.or(
                            Expression.equalsArgs(Statuses.STATUS_ID),
                            Expression.equalsArgs(Statuses.RETWEET_ID)
                    )).getSQL();
            deleteWhereArgs = new String[]{host, statusId, statusId};
            updateWhere = Expression.and(
                    Expression.likeRaw(new Column(Statuses.ACCOUNT_KEY), "'%@'||?"),
                    Expression.equalsArgs(Statuses.MY_RETWEET_ID)
            ).getSQL();
            updateWhereArgs = new String[]{host, statusId};
        } else {
            deleteWhere = Expression.or(
                    Expression.equalsArgs(Statuses.STATUS_ID),
                    Expression.equalsArgs(Statuses.RETWEET_ID)
            ).getSQL();
            deleteWhereArgs = new String[]{statusId, statusId};
            updateWhere = Expression.equalsArgs(Statuses.MY_RETWEET_ID).getSQL();
            updateWhereArgs = new String[]{statusId};
        }
        for (final Uri uri : STATUSES_URIS) {
            cr.delete(uri, deleteWhere, deleteWhereArgs);
            if (status != null) {
                final ContentValues values = new ContentValues();
                values.putNull(Statuses.MY_RETWEET_ID);
                values.put(Statuses.RETWEET_COUNT, status.retweet_count - 1);
                cr.update(uri, values, updateWhere, updateWhereArgs);
            }
        }
    }

    public static void deleteActivityStatus(@NonNull ContentResolver cr, @NonNull UserKey accountKey,
                                            @NonNull final String statusId, @Nullable final ParcelableStatus result) {

        final String host = accountKey.getHost();
        final String deleteWhere, updateWhere;
        final String[] deleteWhereArgs, updateWhereArgs;
        if (host != null) {
            deleteWhere = Expression.and(
                    Expression.likeRaw(new Column(Activities.ACCOUNT_KEY), "'%@'||?"),
                    Expression.or(
                            Expression.equalsArgs(Activities.STATUS_ID),
                            Expression.equalsArgs(Activities.STATUS_RETWEET_ID)
                    )).getSQL();
            deleteWhereArgs = new String[]{host, statusId, statusId};
            updateWhere = Expression.and(
                    Expression.likeRaw(new Column(Activities.ACCOUNT_KEY), "'%@'||?"),
                    Expression.equalsArgs(Activities.STATUS_MY_RETWEET_ID)
            ).getSQL();
            updateWhereArgs = new String[]{host, statusId};
        } else {
            deleteWhere = Expression.or(
                    Expression.equalsArgs(Activities.STATUS_ID),
                    Expression.equalsArgs(Activities.STATUS_RETWEET_ID)
            ).getSQL();
            deleteWhereArgs = new String[]{statusId, statusId};
            updateWhere = Expression.equalsArgs(Activities.STATUS_MY_RETWEET_ID).getSQL();
            updateWhereArgs = new String[]{statusId};
        }
        for (final Uri uri : ACTIVITIES_URIS) {
            cr.delete(uri, deleteWhere, deleteWhereArgs);
            updateActivity(cr, uri, updateWhere, updateWhereArgs, new UpdateActivityAction() {

                @Override
                public void process(ParcelableActivity activity) {
                    activity.status_my_retweet_id = null;
                    ParcelableStatus[][] statusesMatrix = {activity.target_statuses,
                            activity.target_object_statuses};
                    for (ParcelableStatus[] statusesArray : statusesMatrix) {
                        if (statusesArray == null) continue;
                        for (ParcelableStatus status : statusesArray) {
                            if (statusId.equals(status.id) || statusId.equals(status.retweet_id)
                                    || statusId.equals(status.my_retweet_id)) {
                                status.my_retweet_id = null;
                                if (result != null) {
                                    status.reply_count = result.reply_count;
                                    status.retweet_count = result.retweet_count - 1;
                                    status.favorite_count = result.favorite_count;
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    @WorkerThread
    public static void updateActivity(ContentResolver cr, Uri uri, String where, String[] whereArgs,
                                      UpdateActivityAction action) {
        final Cursor c = cr.query(uri, Activities.COLUMNS, where, whereArgs, null);
        if (c == null) return;
        LongSparseArray<ContentValues> values = new LongSparseArray<>();
        try {
            ParcelableActivityCursorIndices ci = new ParcelableActivityCursorIndices(c);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                final ParcelableActivity activity = ci.newObject(c);
                action.process(activity);
                values.put(activity._id, ParcelableActivityValuesCreator.create(activity));
                c.moveToNext();
            }
        } finally {
            c.close();
        }
        String updateWhere = Expression.equalsArgs(Activities._ID).getSQL();
        String[] updateWhereArgs = new String[1];
        for (int i = 0, j = values.size(); i < j; i++) {
            updateWhereArgs[0] = String.valueOf(values.keyAt(i));
            cr.update(uri, values.valueAt(i), updateWhere, updateWhereArgs);
        }
    }

    static void updateActivityStatus(ContentResolver resolver, UserKey accountKey, String statusId, UpdateActivityAction action) {
        final String activityWhere = Expression.and(
                Expression.equalsArgs(Activities.ACCOUNT_KEY),
                Expression.or(
                        Expression.equalsArgs(Activities.STATUS_ID),
                        Expression.equalsArgs(Activities.STATUS_RETWEET_ID)
                )
        ).getSQL();
        final String[] activityWhereArgs = {accountKey.toString(), statusId, statusId};
        for (final Uri uri : ACTIVITIES_URIS) {
            updateActivity(resolver, uri, activityWhere, activityWhereArgs, action);
        }
    }

    public static void processTabExtras(List<Expression> expressions, List<String> expressionArgs, HomeTabExtras extras) {
        if (extras.isHideRetweets()) {
            expressions.add(Expression.equalsArgs(Statuses.IS_RETWEET));
            expressionArgs.add("0");
        }
        if (extras.isHideQuotes()) {
            expressions.add(Expression.equalsArgs(Statuses.IS_QUOTE));
            expressionArgs.add("0");
        }
        if (extras.isHideReplies()) {
            expressions.add(Expression.isNull(new Column(Statuses.IN_REPLY_TO_STATUS_ID)));
        }
    }

    interface FieldArrayCreator<T> {
        T newArray(int size);

        void assign(T array, int arrayIdx, Cursor cur, int colIdx);
    }

    public static int queryCount(@NonNull final Context context, @NonNull final Uri uri,
                          @Nullable final String selection, @Nullable final String[] selectionArgs) {
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[]{SQLFunctions.COUNT()};
        final Cursor cur = resolver.query(uri, projection, selection, selectionArgs, null);
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

    public static List<ParcelableAccount> getAccountsList(final Context context, final boolean activatedOnly) {
        return getAccountsList(context, activatedOnly, false);
    }

    public static List<ParcelableAccount> getAccountsList(final Context context, final boolean activatedOnly,
                                                          final boolean officialKeyOnly) {
        if (context == null) return Collections.emptyList();
        final ArrayList<ParcelableAccount> accounts = new ArrayList<>();
        final String selection = activatedOnly ? Accounts.IS_ACTIVATED + " = 1" : null;
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, Accounts.COLUMNS, selection, null, Accounts.SORT_POSITION);
        if (cur == null) return accounts;
        final ParcelableCredentialsCursorIndices indices = new ParcelableCredentialsCursorIndices(cur);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            if (!officialKeyOnly) {
                accounts.add(indices.newObject(cur));
            } else {
                final String consumerKey = cur.getString(indices.consumer_key);
                final String consumerSecret = cur.getString(indices.consumer_secret);
                if (TwitterContentUtils.isOfficialKey(context, consumerKey, consumerSecret)) {
                    accounts.add(indices.newObject(cur));
                }
            }
            cur.moveToNext();
        }
        cur.close();
        return accounts;
    }

    @Nullable
    public static Cursor getAccountCursor(@NonNull final Context context, final String[] columns,
                                          @NonNull final UserKey accountKey) {
        final ContentResolver cr = context.getContentResolver();
        final String accountId = accountKey.getId();
        final String accountHost = accountKey.getHost();
        final String where;
        final String[] whereArgs;
        if (TextUtils.isEmpty(accountHost)) {
            where = Expression.or(Expression.equalsArgs(Accounts.ACCOUNT_KEY),
                    Expression.likeRaw(new Column(Accounts.ACCOUNT_KEY), "?||\'@%\'")).getSQL();
            whereArgs = new String[]{accountId, accountId};
        } else {
            where = Expression.or(Expression.equalsArgs(Accounts.ACCOUNT_KEY),
                    Expression.equalsArgs(Accounts.ACCOUNT_KEY)).getSQL();
            whereArgs = new String[]{String.valueOf(accountKey.toString()),
                    accountId};
        }
        return cr.query(Accounts.CONTENT_URI, columns, where, whereArgs, null);
    }

    @Nullable
    public static Cursor findAccountCursorsById(@NonNull final Context context, final String[] columns,
                                                final String... ids) {
        if (ids == null) return null;
        final ContentResolver cr = context.getContentResolver();
        Expression[] expressions = new Expression[ids.length + 1];
        for (int i = 0, j = ids.length; i < j; i++) {
            expressions[i] = Expression.likeRaw(new Column(Accounts.ACCOUNT_KEY), "?||\'@%\'");
        }
        expressions[ids.length] = Expression.inArgs(new Column(Accounts.ACCOUNT_KEY), ids.length);
        final String where = Expression.or(expressions).getSQL();
        final String[] whereArgs = new String[ids.length * 2];
        System.arraycopy(TwidereArrayUtils.toStringArray(ids), 0, whereArgs, 0, ids.length);
        System.arraycopy(whereArgs, 0, whereArgs, ids.length, ids.length);
        return cr.query(Accounts.CONTENT_URI, columns, where, whereArgs, null);
    }

    public static String getAccountType(@NonNull final Context context, @NonNull final UserKey accountKey) {
        final String[] projection = {Accounts.ACCOUNT_TYPE};
        final Cursor cur = getAccountCursor(context, projection, accountKey);
        if (cur == null) return null;
        try {
            if (cur.moveToFirst()) {
                return cur.getString(0);
            }
        } finally {
            cur.close();
        }
        return null;
    }

    public static List<ParcelableCredentials> getCredentialsList(final Context context, final boolean activatedOnly) {
        return getCredentialsList(context, activatedOnly, false);
    }

    public static ParcelableCredentials[] getCredentialsArray(final Context context, final boolean activatedOnly,
                                                              final boolean officialKeyOnly) {
        final List<ParcelableCredentials> credentialsList = getCredentialsList(context, activatedOnly, officialKeyOnly);
        return credentialsList.toArray(new ParcelableCredentials[credentialsList.size()]);
    }

    public static List<ParcelableCredentials> getCredentialsList(final Context context, final boolean activatedOnly,
                                                                 final boolean officialKeyOnly) {
        if (context == null) return Collections.emptyList();
        final ArrayList<ParcelableCredentials> accounts = new ArrayList<>();
        final String selection = activatedOnly ? Accounts.IS_ACTIVATED + " = 1" : null;
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, Accounts.COLUMNS, selection, null, Accounts.SORT_POSITION);
        if (cur == null) return accounts;
        ParcelableCredentialsCursorIndices indices = new ParcelableCredentialsCursorIndices(cur);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            if (officialKeyOnly) {
                final String consumerKey = cur.getString(indices.consumer_key);
                final String consumerSecret = cur.getString(indices.consumer_secret);
                if (TwitterContentUtils.isOfficialKey(context, consumerKey, consumerSecret)) {
                    accounts.add(indices.newObject(cur));
                }
            } else {
                accounts.add(indices.newObject(cur));
            }
            cur.moveToNext();
        }
        cur.close();
        return accounts;
    }

    public static int getInteractionsCount(@NonNull final Context context, @Nullable final Bundle extraArgs,
                                           final UserKey[] accountIds, final long since, final String sinceColumn) {
        Expression extraWhere = null;
        String[] extraWhereArgs = null;
        boolean followingOnly = false;
        if (extraArgs != null) {
            final TabExtras extras = extraArgs.getParcelable(EXTRA_EXTRAS);
            if (extras instanceof InteractionsTabExtras) {
                InteractionsTabExtras ite = ((InteractionsTabExtras) extras);
                if (ite.isMentionsOnly()) {
                    extraWhere = Expression.inArgs(Activities.ACTION, 3);
                    extraWhereArgs = new String[]{Activity.Action.MENTION,
                            Activity.Action.REPLY, Activity.Action.QUOTE};
                }
                if (ite.isMyFollowingOnly()) {
                    followingOnly = true;
                }
            }
        }
        return getActivitiesCount(context, Activities.AboutMe.CONTENT_URI, extraWhere, extraWhereArgs,
                since, sinceColumn, followingOnly, accountIds);
    }

    public interface UpdateActivityAction {

        void process(ParcelableActivity activity);
    }
}

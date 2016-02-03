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
import android.content.UriMatcher;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.sqliteqb.library.ArgsArray;
import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Columns.Column;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.OrderBy;
import org.mariotaku.sqliteqb.library.RawItemArray;
import org.mariotaku.sqliteqb.library.SQLFunctions;
import org.mariotaku.sqliteqb.library.SQLQueryBuilder;
import org.mariotaku.sqliteqb.library.Table;
import org.mariotaku.sqliteqb.library.Tables;
import org.mariotaku.sqliteqb.library.query.SQLSelectQuery;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.provider.TwidereDataStore;
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
import org.mariotaku.twidere.util.content.ContentResolverUtils;

import java.util.Arrays;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.provider.TwidereDataStore.CACHE_URIS;
import static org.mariotaku.twidere.provider.TwidereDataStore.DIRECT_MESSAGES_URIS;
import static org.mariotaku.twidere.provider.TwidereDataStore.STATUSES_URIS;

/**
 * Created by mariotaku on 15/11/28.
 */
public class DataStoreUtils implements Constants {
    static final UriMatcher CONTENT_PROVIDER_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static LongSparseArray<Integer> sAccountColors = new LongSparseArray<>();
    static LongSparseArray<String> sAccountScreenNames = new LongSparseArray<>();
    static LongSparseArray<String> sAccountNames = new LongSparseArray<>();

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
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, DirectMessages.Conversation.CONTENT_PATH + "/#/#",
                TABLE_ID_DIRECT_MESSAGES_CONVERSATION);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, DirectMessages.Conversation.CONTENT_PATH_SCREEN_NAME + "/#/*",
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
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, Notifications.CONTENT_PATH + "/#/#",
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
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, CachedUsers.CONTENT_PATH_WITH_RELATIONSHIP + "/#",
                VIRTUAL_TABLE_ID_CACHED_USERS_WITH_RELATIONSHIP);
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, CachedUsers.CONTENT_PATH_WITH_SCORE + "/#",
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
        CONTENT_PROVIDER_URI_MATCHER.addURI(TwidereDataStore.AUTHORITY, TwidereDataStore.CONTENT_PATH_RAW_QUERY,
                VIRTUAL_TABLE_ID_RAW_QUERY);
    }

    @NonNull
    public static long[] getNewestMessageIds(final Context context, final Uri uri, final long[] accountIds) {
        return getLongFieldArray(context, uri, accountIds, DirectMessages.ACCOUNT_ID, DirectMessages.MESSAGE_ID,
                new OrderBy(SQLFunctions.MAX(DirectMessages.MESSAGE_TIMESTAMP)));
    }

    @NonNull
    public static long[] getNewestStatusIds(final Context context, final Uri uri, final long[] accountIds) {
        return getLongFieldArray(context, uri, accountIds, Statuses.ACCOUNT_ID, Statuses.STATUS_ID,
                new OrderBy(SQLFunctions.MAX(Statuses.STATUS_TIMESTAMP)));
    }


    @NonNull
    public static long[] getOldestMessageIds(@NonNull final Context context, @NonNull final Uri uri,
                                             @NonNull final long[] accountIds) {
        return getLongFieldArray(context, uri, accountIds, DirectMessages.ACCOUNT_ID,
                DirectMessages.MESSAGE_ID, new OrderBy(SQLFunctions.MIN(DirectMessages.MESSAGE_TIMESTAMP)));
    }

    @NonNull
    public static long[] getOldestStatusIds(@NonNull final Context context, @NonNull final Uri uri,
                                            @NonNull final long[] accountIds) {
        return getLongFieldArray(context, uri, accountIds, Statuses.ACCOUNT_ID, Statuses.STATUS_ID,
                new OrderBy(SQLFunctions.MIN(Statuses.STATUS_TIMESTAMP)));
    }

    @NonNull
    public static long[] getNewestActivityMaxPositions(final Context context, final Uri uri, final long[] accountIds) {
        return getLongFieldArray(context, uri, accountIds, Activities.ACCOUNT_ID,
                Activities.MAX_POSITION, new OrderBy(SQLFunctions.MAX(Activities.TIMESTAMP)));
    }

    @NonNull
    public static long[] getOldestActivityMaxPositions(@NonNull final Context context,
                                                       @NonNull final Uri uri,
                                                       @NonNull final long[] accountIds) {
        return getLongFieldArray(context, uri, accountIds, Activities.ACCOUNT_ID,
                Activities.MAX_POSITION, new OrderBy(SQLFunctions.MIN(Activities.TIMESTAMP)));
    }

    public static int getStatusCount(final Context context, final Uri uri, final long accountId) {
        final String where = Expression.equals(Statuses.ACCOUNT_ID, accountId).getSQL();
        return queryCount(context, uri, where, null);
    }

    public static int getActivitiesCount(final Context context, final Uri uri, final long accountId) {
        final String where = Expression.equals(Activities.ACCOUNT_ID, accountId).getSQL();
        return queryCount(context, uri, where, null);
    }


    @NonNull
    public static long[] getFilteredUserIds(Context context) {
        if (context == null) return new long[0];
        final ContentResolver resolver = context.getContentResolver();
        final Cursor cur = ContentResolverUtils.query(resolver, Filters.Users.CONTENT_URI,
                new String[]{Filters.Users.USER_ID}, null, null, null);
        if (cur == null) return new long[0];
        try {
            final long[] ids = new long[cur.getCount()];
            cur.moveToFirst();
            int i = 0;
            while (!cur.isAfterLast()) {
                ids[i] = cur.getLong(0);
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
                .select(new Column(new Table(Filters.Users.TABLE_NAME), Filters.Users.USER_ID))
                .from(new Tables(Filters.Users.TABLE_NAME))
                .build();
        final Expression filteredUsersWhere = Expression.or(
                Expression.in(new Column(new Table(table), Statuses.USER_ID), filteredUsersQuery),
                Expression.in(new Column(new Table(table), Statuses.RETWEETED_BY_USER_ID), filteredUsersQuery),
                Expression.in(new Column(new Table(table), Statuses.QUOTED_USER_ID), filteredUsersQuery)
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
                        Expression.likeRaw(new Column(new Table(table), Statuses.TEXT_HTML),
                                "'%>%'||" + Filters.Links.TABLE_NAME + "." + Filters.Links.VALUE + "||'%</a>%'"),
                        Expression.likeRaw(new Column(new Table(table), Statuses.QUOTED_TEXT_HTML),
                                "'%>%'||" + Filters.Links.TABLE_NAME + "." + Filters.Links.VALUE + "||'%</a>%'")
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

    public static String getAccountScreenName(final Context context, final long accountId) {
        if (context == null) return null;
        final String cached = sAccountScreenNames.get(accountId);
        if (!isEmpty(cached)) return cached;
        final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
                new String[]{Accounts.SCREEN_NAME}, Accounts.ACCOUNT_ID + " = " + accountId, null, null);
        if (cur == null) return null;
        try {
            if (cur.getCount() > 0 && cur.moveToFirst()) {
                final String name = cur.getString(0);
                sAccountScreenNames.put(accountId, name);
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

    public static String[] getAccountScreenNames(final Context context, final long[] accountIds) {
        if (context == null) return new String[0];
        final String[] cols = new String[]{Accounts.SCREEN_NAME};
        final String where = accountIds != null ? Expression.in(new Column(Accounts.ACCOUNT_ID),
                new RawItemArray(accountIds)).getSQL() : null;
        final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI, cols, where,
                null, null);
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

    public static long[] getActivatedAccountIds(final Context context) {
        if (context == null) return new long[0];
        final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
                new String[]{Accounts.ACCOUNT_ID}, Accounts.IS_ACTIVATED + " = 1", null, null);
        if (cur == null) return new long[0];
        try {
            cur.moveToFirst();
            final long[] ids = new long[cur.getCount()];
            int i = 0;
            while (!cur.isAfterLast()) {
                ids[i++] = cur.getLong(0);
                cur.moveToNext();
            }
            return ids;
        } finally {
            cur.close();
        }
    }

    public static int getStatusesCount(final Context context, final Uri uri, final long sinceId, final long... accountIds) {
        if (context == null) return 0;
        final RawItemArray idsIn;
        if (accountIds == null || accountIds.length == 0 || (accountIds.length == 1 && accountIds[0] < 0)) {
            idsIn = new RawItemArray(getActivatedAccountIds(context));
        } else {
            idsIn = new RawItemArray(accountIds);
        }
        final Expression selection = Expression.and(
                Expression.in(new Column(Statuses.ACCOUNT_ID), idsIn),
                Expression.greaterThan(Statuses.STATUS_ID, sinceId),
                buildStatusFilterWhereClause(getTableNameByUri(uri), null)
        );
        return queryCount(context, uri, selection.getSQL(), null);
    }

    public static int getActivitiesCount(final Context context, final Uri uri,
                                         final Expression extraWhere, final String[] extraWhereArgs,
                                         final long sinceTimestamp, final long... accountIds) {
        if (context == null) return 0;
        final RawItemArray idsIn;
        if (accountIds == null || accountIds.length == 0 || (accountIds.length == 1 && accountIds[0] < 0)) {
            idsIn = new RawItemArray(getActivatedAccountIds(context));
        } else {
            idsIn = new RawItemArray(accountIds);
        }
        Expression[] expressions;
        if (extraWhere != null) {
            expressions = new Expression[4];
            expressions[3] = extraWhere;
        } else {
            expressions = new Expression[3];
        }
        expressions[0] = Expression.in(new Column(Activities.ACCOUNT_ID), idsIn);
        expressions[1] = Expression.greaterThan(Activities.TIMESTAMP, sinceTimestamp);
        expressions[2] = buildActivityFilterWhereClause(getTableNameByUri(uri), null);
        final Expression selection = Expression.and(expressions);
        return queryCount(context, uri, selection.getSQL(), extraWhereArgs);
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
                .select(new Column(new Table(Filters.Users.TABLE_NAME), Filters.Users.USER_ID))
                .from(new Tables(Filters.Users.TABLE_NAME))
                .build();
        final Expression filteredUsersWhere = Expression.or(
                Expression.in(new Column(new Table(table), Activities.STATUS_USER_ID), filteredUsersQuery),
                Expression.in(new Column(new Table(table), Activities.STATUS_RETWEETED_BY_USER_ID), filteredUsersQuery),
                Expression.in(new Column(new Table(table), Activities.STATUS_QUOTED_USER_ID), filteredUsersQuery)
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
                        Expression.likeRaw(new Column(new Table(table), Activities.STATUS_TEXT_HTML),
                                "'%>%'||" + Filters.Links.TABLE_NAME + "." + Filters.Links.VALUE + "||'%</a>%'"),
                        Expression.likeRaw(new Column(new Table(table), Activities.STATUS_QUOTE_TEXT_HTML),
                                "'%>%'||" + Filters.Links.TABLE_NAME + "." + Filters.Links.VALUE + "||'%</a>%'")
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

    public static int getAccountColor(final Context context, final long accountId) {
        if (context == null) return Color.TRANSPARENT;
        final Integer cached = sAccountColors.get(accountId);
        if (cached != null) return cached;
        final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
                new String[]{Accounts.COLOR}, Expression.equals(Accounts.ACCOUNT_ID, accountId).getSQL(),
                null, null);
        if (cur == null) return Color.TRANSPARENT;
        try {
            if (cur.getCount() > 0 && cur.moveToFirst()) {
                final int color = cur.getInt(0);
                sAccountColors.put(accountId, color);
                return color;
            }
            return Color.TRANSPARENT;
        } finally {
            cur.close();
        }
    }

    public static int[] getAccountColors(final Context context, final long[] accountIds) {
        if (context == null || accountIds == null) return new int[0];
        final String[] cols = new String[]{Accounts.ACCOUNT_ID, Accounts.COLOR};
        final String where = Expression.in(new Column(Accounts.ACCOUNT_ID), new RawItemArray(accountIds)).getSQL();
        final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI, cols, where,
                null, null);
        if (cur == null) return new int[0];
        try {
            final int[] colors = new int[cur.getCount()];
            for (int i = 0, j = cur.getCount(); i < j; i++) {
                cur.moveToPosition(i);
                colors[ArrayUtils.indexOf(accountIds, cur.getLong(0))] = cur.getInt(1);
            }
            return colors;
        } finally {
            cur.close();
        }
    }

    public static String getAccountDisplayName(final Context context, final long accountId, final boolean nameFirst) {
        final String name;
        if (nameFirst) {
            name = getAccountName(context, accountId);
        } else {
            name = String.format("@%s", getAccountScreenName(context, accountId));
        }
        return name;
    }

    public static long getAccountId(final Context context, final String screenName) {
        if (context == null || isEmpty(screenName)) return -1;
        final Cursor cur = ContentResolverUtils
                .query(context.getContentResolver(), Accounts.CONTENT_URI, new String[]{Accounts.ACCOUNT_ID},
                        Expression.equalsArgs(Accounts.SCREEN_NAME).getSQL(), new String[]{screenName}, null);
        if (cur == null) return -1;
        try {
            if (cur.getCount() > 0 && cur.moveToFirst()) return cur.getLong(0);
            return -1;
        } finally {
            cur.close();
        }
    }

    @NonNull
    public static long[] getAccountIds(final Context context) {
        if (context == null) return new long[0];
        final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
                new String[]{Accounts.ACCOUNT_ID}, null, null, null);
        if (cur == null) return new long[0];
        try {
            cur.moveToFirst();
            final long[] ids = new long[cur.getCount()];
            int i = 0;
            while (!cur.isAfterLast()) {
                ids[i++] = cur.getLong(0);
                cur.moveToNext();
            }
            return ids;
        } finally {
            cur.close();
        }
    }

    public static boolean hasAccount(final Context context) {
        if (context == null) return false;
        final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
                new String[]{SQLFunctions.COUNT()}, null, null, null);
        if (cur == null) return false;
        try {
            cur.moveToFirst();
            return cur.getInt(0) > 0;
        } finally {
            cur.close();
        }
    }

    public static String getAccountName(final Context context, final long accountId) {
        if (context == null) return null;
        final String cached = sAccountNames.get(accountId);
        if (!isEmpty(cached)) return cached;
        final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
                new String[]{Accounts.NAME}, Accounts.ACCOUNT_ID + " = " + accountId, null, null);
        if (cur == null) return null;
        try {
            if (cur.getCount() > 0 && cur.moveToFirst()) {
                final String name = cur.getString(0);
                sAccountNames.put(accountId, name);
                return name;
            }
            return null;
        } finally {
            cur.close();
        }
    }

    public static synchronized void cleanDatabasesByItemLimit(final Context context) {
        if (context == null) return;
        final ContentResolver resolver = context.getContentResolver();
        final int itemLimit = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(
                KEY_DATABASE_ITEM_LIMIT, DEFAULT_DATABASE_ITEM_LIMIT);

        for (final long accountId : getAccountIds(context)) {
            // Clean statuses.
            for (final Uri uri : STATUSES_URIS) {
                if (CachedStatuses.CONTENT_URI.equals(uri)) {
                    continue;
                }
                final String table = getTableNameByUri(uri);
                final Expression account_where = new Expression(Statuses.ACCOUNT_ID + " = " + accountId);
                final SQLSelectQuery.Builder qb = new SQLSelectQuery.Builder();
                qb.select(new Column(Statuses._ID)).from(new Tables(table));
                qb.where(Expression.equals(Statuses.ACCOUNT_ID, accountId));
                qb.orderBy(new OrderBy(Statuses.STATUS_ID, false));
                qb.limit(itemLimit);
                final Expression where = Expression.and(Expression.notIn(new Column(Statuses._ID), qb.build()), account_where);
                resolver.delete(uri, where.getSQL(), null);
            }
            for (final Uri uri : DIRECT_MESSAGES_URIS) {
                final String table = getTableNameByUri(uri);
                final Expression account_where = new Expression(DirectMessages.ACCOUNT_ID + " = " + accountId);
                final SQLSelectQuery.Builder qb = new SQLSelectQuery.Builder();
                qb.select(new Column(DirectMessages._ID)).from(new Tables(table));
                qb.where(Expression.equals(DirectMessages.ACCOUNT_ID, accountId));
                qb.orderBy(new OrderBy(DirectMessages.MESSAGE_ID, false));
                qb.limit(itemLimit * 10);
                final Expression where = Expression.and(Expression.notIn(new Column(DirectMessages._ID), qb.build()), account_where);
                resolver.delete(uri, where.getSQL(), null);
            }
        }
        // Clean cached values.
        for (final Uri uri : CACHE_URIS) {
            final String table = getTableNameByUri(uri);
            if (table == null) continue;
            final SQLSelectQuery.Builder qb = new SQLSelectQuery.Builder();
            qb.select(new Column(BaseColumns._ID));
            qb.from(new Tables(table));
            qb.orderBy(new OrderBy(BaseColumns._ID, false));
            qb.limit(itemLimit * 20);
            final Expression where = Expression.notIn(new Column(BaseColumns._ID), qb.build());
            resolver.delete(uri, where.getSQL(), null);
        }
    }

    public static void clearAccountColor() {
        sAccountColors.clear();
    }

    public static void clearAccountName() {
        sAccountScreenNames.clear();
    }

    public static boolean isFilteringUser(Context context, long userId) {
        final ContentResolver cr = context.getContentResolver();
        final Expression where = Expression.equals(Filters.Users.USER_ID, userId);
        final Cursor c = cr.query(Filters.Users.CONTENT_URI, new String[0], where.getSQL(), null, null);
        if (c == null) return false;
        try {
            return c.getCount() > 0;
        } finally {
            c.close();
        }
    }

    @NonNull
    static long[] getLongFieldArray(@NonNull Context context, @NonNull Uri uri, @NonNull long[] keys,
                                    @NonNull String keyField, @NonNull String valueField,
                                    @Nullable OrderBy sortExpression) {
        final ContentResolver resolver = context.getContentResolver();
        final long[] messageIds = new long[keys.length];
        Arrays.fill(messageIds, -1);
        final String[] selectionArgs = TwidereArrayUtils.toStringArray(keys);
        final SQLSelectQuery.Builder builder = SQLQueryBuilder.select(new Columns(keyField, valueField))
                .from(new Table(getTableNameByUri(uri)))
                .groupBy(new Column(keyField))
                .having(Expression.in(new Column(keyField), new ArgsArray(keys.length)));
        if (sortExpression != null) {
            builder.orderBy(sortExpression);
        }
        final Cursor cur = ContentResolverUtils.query(resolver,
                Uri.withAppendedPath(TwidereDataStore.CONTENT_URI_DATABASE_READY, builder.buildSQL()),
                null, null, selectionArgs, null);
        if (cur == null) return messageIds;
        while (cur.moveToNext()) {
            final long accountId = cur.getLong(0);
            int idx = ArrayUtils.indexOf(keys, accountId);
            if (idx < 0) continue;
            messageIds[idx] = cur.getLong(1);
        }
        return messageIds;
    }

    static int queryCount(@NonNull final Context context, @NonNull final Uri uri,
                          @Nullable final String selection, @Nullable final String[] selectionArgs) {
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

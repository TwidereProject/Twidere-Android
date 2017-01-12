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

package org.mariotaku.twidere.util.content;

import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Columns.Column;
import org.mariotaku.sqliteqb.library.Constraint;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.NewColumn;
import org.mariotaku.sqliteqb.library.OnConflict;
import org.mariotaku.sqliteqb.library.RawSQLLang;
import org.mariotaku.sqliteqb.library.SQLQuery;
import org.mariotaku.sqliteqb.library.SQLQueryBuilder;
import org.mariotaku.sqliteqb.library.SetValue;
import org.mariotaku.sqliteqb.library.Table;
import org.mariotaku.sqliteqb.library.query.SQLCreateIndexQuery;
import org.mariotaku.sqliteqb.library.query.SQLCreateTableQuery;
import org.mariotaku.sqliteqb.library.query.SQLCreateTriggerQuery.Event;
import org.mariotaku.sqliteqb.library.query.SQLCreateTriggerQuery.Type;
import org.mariotaku.sqliteqb.library.query.SQLDeleteQuery;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.annotation.CustomTabType;
import org.mariotaku.twidere.model.Tab;
import org.mariotaku.twidere.model.TabValuesCreator;
import org.mariotaku.twidere.model.tab.TabConfiguration;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedHashtags;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedStatuses;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;
import org.mariotaku.twidere.provider.TwidereDataStore.SavedSearches;
import org.mariotaku.twidere.provider.TwidereDataStore.SearchHistory;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;
import org.mariotaku.twidere.util.AccountMigratorKt;
import org.mariotaku.twidere.util.TwidereQueryBuilder.ConversationsEntryQueryBuilder;
import org.mariotaku.twidere.util.TwidereQueryBuilder.DirectMessagesQueryBuilder;

import java.io.IOException;
import java.util.HashMap;

import static org.mariotaku.twidere.util.content.DatabaseUpgradeHelper.safeUpgrade;

public final class TwidereSQLiteOpenHelper extends SQLiteOpenHelper implements Constants {

    private final Context mContext;

    public TwidereSQLiteOpenHelper(final Context context, final String name, final int version) {
        super(context, name, null, version);
        mContext = context;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.beginTransaction();
        db.execSQL(createTable(Statuses.TABLE_NAME, Statuses.COLUMNS, Statuses.TYPES, true));
        db.execSQL(createTable(Activities.AboutMe.TABLE_NAME, Activities.AboutMe.COLUMNS, Activities.AboutMe.TYPES, true));
        db.execSQL(createTable(Activities.ByFriends.TABLE_NAME, Activities.ByFriends.COLUMNS, Activities.ByFriends.TYPES, true));
        db.execSQL(createTable(Drafts.TABLE_NAME, Drafts.COLUMNS, Drafts.TYPES, true));
        db.setTransactionSuccessful();
        db.endTransaction();

        db.beginTransaction();
        db.execSQL(createTable(CachedUsers.TABLE_NAME, CachedUsers.COLUMNS, CachedUsers.TYPES, true,
                createConflictReplaceConstraint(CachedUsers.USER_KEY)));
        db.execSQL(createTable(CachedStatuses.TABLE_NAME, CachedStatuses.COLUMNS, CachedStatuses.TYPES, true));
        db.execSQL(createTable(CachedTrends.Local.TABLE_NAME, CachedTrends.Local.COLUMNS, CachedTrends.Local.TYPES,
                true));
        db.execSQL(createTable(CachedHashtags.TABLE_NAME, CachedHashtags.COLUMNS, CachedHashtags.TYPES, true));
        db.execSQL(createTable(CachedRelationships.TABLE_NAME, CachedRelationships.COLUMNS, CachedRelationships.TYPES, true,
                createConflictReplaceConstraint(CachedRelationships.ACCOUNT_KEY, CachedRelationships.USER_KEY)));
        db.setTransactionSuccessful();
        db.endTransaction();


        db.beginTransaction();
        db.execSQL(createTable(Filters.Users.TABLE_NAME, Filters.Users.COLUMNS, Filters.Users.TYPES, true));
        db.execSQL(createTable(Filters.Keywords.TABLE_NAME, Filters.Keywords.COLUMNS, Filters.Keywords.TYPES, true));
        db.execSQL(createTable(Filters.Sources.TABLE_NAME, Filters.Sources.COLUMNS, Filters.Sources.TYPES, true));
        db.execSQL(createTable(Filters.Links.TABLE_NAME, Filters.Links.COLUMNS, Filters.Links.TYPES, true));
        db.execSQL(createTable(Filters.Subscriptions.TABLE_NAME, Filters.Subscriptions.COLUMNS, Filters.Subscriptions.TYPES, true));
        db.setTransactionSuccessful();
        db.endTransaction();

        db.beginTransaction();
        db.execSQL(createTable(DirectMessages.Inbox.TABLE_NAME, DirectMessages.Inbox.COLUMNS,
                DirectMessages.Inbox.TYPES, true));
        db.execSQL(createTable(DirectMessages.Outbox.TABLE_NAME, DirectMessages.Outbox.COLUMNS,
                DirectMessages.Outbox.TYPES, true));
        db.execSQL(createTable(Tabs.TABLE_NAME, Tabs.COLUMNS, Tabs.TYPES, true));
        db.execSQL(createTable(SavedSearches.TABLE_NAME, SavedSearches.COLUMNS, SavedSearches.TYPES, true));
        db.execSQL(createTable(SearchHistory.TABLE_NAME, SearchHistory.COLUMNS, SearchHistory.TYPES, true));
        db.setTransactionSuccessful();
        db.endTransaction();

        db.beginTransaction();
        createViews(db);
        createTriggers(db);
        createIndices(db);
        db.setTransactionSuccessful();
        db.endTransaction();

        setupDefaultTabs(db);
    }

    private void setupDefaultTabs(SQLiteDatabase db) {
        db.beginTransaction();
        @CustomTabType
        String[] tabTypes = {CustomTabType.HOME_TIMELINE, CustomTabType.NOTIFICATIONS_TIMELINE,
                CustomTabType.TRENDS_SUGGESTIONS, CustomTabType.DIRECT_MESSAGES};
        for (int i = 0, j = tabTypes.length; i < j; i++) {
            @CustomTabType
            final String tabType = tabTypes[i];
            final TabConfiguration conf = TabConfiguration.ofType(tabType);
            final Tab tab = new Tab();
            tab.setType(tabType);
            tab.setIcon(conf.getIcon().getPersistentKey());
            tab.setPosition(i);
            try {
                db.insert(Tabs.TABLE_NAME, null, TabValuesCreator.create(tab));
            } catch (IOException e) {
                // Ignore
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private Constraint createConflictReplaceConstraint(String... columns) {
        return Constraint.unique(new Columns(columns), OnConflict.IGNORE);
    }

    private void createIndices(SQLiteDatabase db) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        db.execSQL(createIndex("statuses_index", Statuses.TABLE_NAME, new String[]{Statuses.ACCOUNT_KEY}, true));
        db.execSQL(createIndex("messages_inbox_index", DirectMessages.Inbox.TABLE_NAME, new String[]{DirectMessages.ACCOUNT_KEY}, true));
        db.execSQL(createIndex("messages_outbox_index", DirectMessages.Outbox.TABLE_NAME, new String[]{DirectMessages.ACCOUNT_KEY}, true));
    }

    private void createViews(SQLiteDatabase db) {
        db.execSQL(SQLQueryBuilder.dropView(true, DirectMessages.TABLE_NAME).getSQL());
        db.execSQL(SQLQueryBuilder.dropView(true, DirectMessages.ConversationEntries.TABLE_NAME).getSQL());

        db.execSQL(SQLQueryBuilder.createView(true, DirectMessages.TABLE_NAME)
                .as(DirectMessagesQueryBuilder.build()).buildSQL());
        db.execSQL(SQLQueryBuilder.createView(true, DirectMessages.ConversationEntries.TABLE_NAME)
                .as(ConversationsEntryQueryBuilder.build()).buildSQL());
    }

    private void createTriggers(SQLiteDatabase db) {
        db.execSQL(SQLQueryBuilder.dropTrigger(true, "delete_old_statuses").getSQL());
        db.execSQL(SQLQueryBuilder.dropTrigger(true, "delete_old_cached_statuses").getSQL());
        db.execSQL(SQLQueryBuilder.dropTrigger(true, "delete_old_received_messages").getSQL());
        db.execSQL(SQLQueryBuilder.dropTrigger(true, "delete_old_sent_messages").getSQL());
        db.execSQL(SQLQueryBuilder.dropTrigger(true, "on_user_cache_update_trigger").getSQL());
        db.execSQL(SQLQueryBuilder.dropTrigger(true, "delete_old_cached_hashtags").getSQL());
        db.execSQL(createDeleteDuplicateStatusTrigger("delete_old_statuses", Statuses.TABLE_NAME).getSQL());
        db.execSQL(createDeleteDuplicateStatusTrigger("delete_old_cached_statuses", CachedStatuses.TABLE_NAME).getSQL());
        db.execSQL(createDeleteDuplicateMessageTrigger("delete_old_received_messages", DirectMessages.Inbox.TABLE_NAME).getSQL());
        db.execSQL(createDeleteDuplicateMessageTrigger("delete_old_sent_messages", DirectMessages.Outbox.TABLE_NAME).getSQL());

        // Update user info in filtered users
        final Table cachedUsersTable = new Table(CachedUsers.TABLE_NAME);
        final Table filteredUsersTable = new Table(Filters.Users.TABLE_NAME);
        db.execSQL(SQLQueryBuilder.createTrigger(false, true, "on_user_cache_update_trigger")
                .type(Type.BEFORE)
                .event(Event.INSERT)
                .on(cachedUsersTable)
                .forEachRow(true)
                .actions(SQLQueryBuilder.update(OnConflict.REPLACE, filteredUsersTable)
                        .set(new SetValue(new Column(Filters.Users.NAME), new Column(Table.NEW, CachedUsers.NAME)),
                                new SetValue(new Column(Filters.Users.SCREEN_NAME), new Column(Table.NEW, CachedUsers.SCREEN_NAME)))
                        .where(Expression.equals(new Column(Filters.Users.USER_KEY), new Column(Table.NEW, CachedUsers.USER_KEY)))
                        .build())
                .buildSQL());

        // Delete duplicated hashtags ignoring case
        final Table cachedHashtagsTable = new Table(CachedHashtags.TABLE_NAME);
        db.execSQL(SQLQueryBuilder.createTrigger(false, true, "delete_old_cached_hashtags")
                .type(Type.BEFORE)
                .event(Event.INSERT)
                .on(cachedHashtagsTable)
                .forEachRow(true)
                .actions(SQLQueryBuilder.deleteFrom(cachedHashtagsTable)
                        .where(Expression.like(new Column(CachedHashtags.NAME), new Column(Table.NEW, CachedHashtags.NAME)))
                        .build())
                .buildSQL());

    }

    private SQLQuery createDeleteDuplicateStatusTrigger(String triggerName, String tableName) {
        final Table table = new Table(tableName);
        final SQLDeleteQuery deleteOld = SQLQueryBuilder.deleteFrom(table).where(Expression.and(
                Expression.equals(new Column(Statuses.ACCOUNT_KEY), new Column(Table.NEW, Statuses.ACCOUNT_KEY)),
                Expression.equals(new Column(Statuses.STATUS_ID), new Column(Table.NEW, Statuses.STATUS_ID))
        )).build();
        return SQLQueryBuilder.createTrigger(false, true, triggerName)
                .type(Type.BEFORE).event(Event.INSERT).on(table).forEachRow(true)
                .actions(deleteOld).build();
    }


    private SQLQuery createDeleteDuplicateMessageTrigger(String triggerName, String tableName) {
        final Table table = new Table(tableName);
        final SQLDeleteQuery deleteOld = SQLQueryBuilder.deleteFrom(table).where(Expression.and(
                Expression.equals(new Column(DirectMessages.ACCOUNT_KEY), new Column(Table.NEW, DirectMessages.ACCOUNT_KEY)),
                Expression.equals(new Column(DirectMessages.MESSAGE_ID), new Column(Table.NEW, DirectMessages.MESSAGE_ID))
        )).build();
        return SQLQueryBuilder.createTrigger(false, true, triggerName)
                .type(Type.BEFORE).event(Event.INSERT).on(table).forEachRow(true)
                .actions(deleteOld).build();
    }


    @Override
    public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        handleVersionChange(db, oldVersion, newVersion);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        handleVersionChange(db, oldVersion, newVersion);
        if (oldVersion <= 43 && newVersion >= 44 && newVersion <= 153) {
            final ContentValues values = new ContentValues();
            final SharedPreferences prefs = mContext
                    .getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            // Here I use old consumer key/secret because it's default key for
            // older versions
            final String prefConsumerKey = prefs.getString(KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY_LEGACY);
            final String prefConsumerSecret = prefs.getString(KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET_LEGACY);
            values.put(Accounts.CONSUMER_KEY, prefConsumerKey.trim());
            values.put(Accounts.CONSUMER_SECRET, prefConsumerSecret.trim());
            db.update(Accounts.TABLE_NAME, values, null, null);
        }
    }

    private void handleVersionChange(final SQLiteDatabase db, final int oldVersion, final int newVersion) {

        if (oldVersion <= 153) {
            migrateLegacyAccounts(db);
            if (newVersion > 153) {
                AccountMigratorKt.migrateAccounts(AccountManager.get(mContext), db);
                db.execSQL(SQLQueryBuilder.dropTable(true, Accounts.TABLE_NAME).getSQL());
            }
        }

        safeUpgrade(db, Statuses.TABLE_NAME, Statuses.COLUMNS, Statuses.TYPES, true, null);
        safeUpgrade(db, Activities.AboutMe.TABLE_NAME, Activities.AboutMe.COLUMNS,
                Activities.AboutMe.TYPES, true, null);
        safeUpgrade(db, Activities.ByFriends.TABLE_NAME, Activities.ByFriends.COLUMNS,
                Activities.ByFriends.TYPES, true, null);
        migrateDrafts(db);
        safeUpgrade(db, CachedUsers.TABLE_NAME, CachedUsers.COLUMNS, CachedUsers.TYPES, true, null,
                createConflictReplaceConstraint(CachedUsers.USER_KEY));
        safeUpgrade(db, CachedStatuses.TABLE_NAME, CachedStatuses.COLUMNS, CachedStatuses.TYPES, true, null);
        safeUpgrade(db, CachedHashtags.TABLE_NAME, CachedHashtags.COLUMNS, CachedHashtags.TYPES, true, null);
        safeUpgrade(db, CachedRelationships.TABLE_NAME, CachedRelationships.COLUMNS, CachedRelationships.TYPES, true, null,
                createConflictReplaceConstraint(CachedRelationships.ACCOUNT_KEY, CachedRelationships.USER_KEY));

        migrateFilters(db, oldVersion);
        safeUpgrade(db, DirectMessages.Inbox.TABLE_NAME, DirectMessages.Inbox.COLUMNS,
                DirectMessages.Inbox.TYPES, true, null);
        safeUpgrade(db, DirectMessages.Outbox.TABLE_NAME, DirectMessages.Outbox.COLUMNS,
                DirectMessages.Outbox.TYPES, true, null);
        safeUpgrade(db, CachedTrends.Local.TABLE_NAME, CachedTrends.Local.COLUMNS,
                CachedTrends.Local.TYPES, true, null);
        safeUpgrade(db, Tabs.TABLE_NAME, Tabs.COLUMNS, Tabs.TYPES, false, null);
        safeUpgrade(db, SavedSearches.TABLE_NAME, SavedSearches.COLUMNS, SavedSearches.TYPES, true, null);
        safeUpgrade(db, SearchHistory.TABLE_NAME, SearchHistory.COLUMNS, SearchHistory.TYPES, true, null);

        if (oldVersion < 131) {
            migrateFilteredUsers(db);
        }

        db.beginTransaction();
        db.execSQL(SQLQueryBuilder.dropTable(true, "network_usages").getSQL());
        db.execSQL(SQLQueryBuilder.dropTable(true, "mentions").getSQL());
        createViews(db);
        createTriggers(db);
        createIndices(db);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void migrateDrafts(SQLiteDatabase db) {
        final HashMap<String, String> draftsAlias = new HashMap<>();
        draftsAlias.put(Drafts.MEDIA, "medias");
        safeUpgrade(db, Drafts.TABLE_NAME, Drafts.COLUMNS, Drafts.TYPES, false, draftsAlias);
    }

    private void migrateFilters(SQLiteDatabase db, int oldVersion) {
        safeUpgrade(db, Filters.Users.TABLE_NAME, Filters.Users.COLUMNS, Filters.Users.TYPES,
                oldVersion < 49, null);

        final HashMap<String, String> filtersAlias = new HashMap<>();
        safeUpgrade(db, Filters.Keywords.TABLE_NAME, Filters.Keywords.COLUMNS, Filters.Keywords.TYPES,
                oldVersion < 49, filtersAlias);
        safeUpgrade(db, Filters.Sources.TABLE_NAME, Filters.Sources.COLUMNS, Filters.Sources.TYPES,
                oldVersion < 49, filtersAlias);
        safeUpgrade(db, Filters.Links.TABLE_NAME, Filters.Links.COLUMNS, Filters.Links.TYPES,
                oldVersion < 49, filtersAlias);
        safeUpgrade(db, Filters.Subscriptions.TABLE_NAME, Filters.Subscriptions.COLUMNS,
                Filters.Subscriptions.TYPES, false, null);
    }

    private void migrateLegacyAccounts(SQLiteDatabase db) {
        final HashMap<String, String> accountsAlias = new HashMap<>();
        accountsAlias.put(Accounts.SCREEN_NAME, "username");
        accountsAlias.put(Accounts.NAME, "username");
        accountsAlias.put(Accounts.ACCOUNT_KEY, "user_id");
        accountsAlias.put(Accounts.COLOR, "user_color");
        accountsAlias.put(Accounts.OAUTH_TOKEN_SECRET, "token_secret");
        accountsAlias.put(Accounts.API_URL_FORMAT, "rest_base_url");
        safeUpgrade(db, Accounts.TABLE_NAME, Accounts.COLUMNS, Accounts.TYPES, false, accountsAlias);
    }

    private void migrateFilteredUsers(SQLiteDatabase db) {
        db.execSQL(SQLQueryBuilder.update(OnConflict.REPLACE, Filters.Users.TABLE_NAME)
                        .set(new SetValue(Filters.Users.USER_KEY, new RawSQLLang(Filters.Users.USER_KEY + "||?")))
                        .where(Expression.notLikeArgs(new Column(Filters.Users.USER_KEY)))
                        .buildSQL(),
                new Object[]{"@twitter.com", "%@%"});
    }

    private static String createTable(final String tableName, final String[] columns, final String[] types,
                                      final boolean createIfNotExists, final Constraint... constraints) {
        final SQLCreateTableQuery.Builder qb = SQLQueryBuilder.createTable(createIfNotExists, tableName);
        qb.columns(NewColumn.createNewColumns(columns, types));
        qb.constraint(constraints);
        return qb.buildSQL();
    }

    private static String createIndex(final String indexName, final String tableName, final String[] columns,
                                      final boolean createIfNotExists) {
        final SQLCreateIndexQuery.Builder qb = SQLQueryBuilder.createIndex(false, createIfNotExists);
        qb.name(indexName);
        qb.on(new Table(tableName), new Columns(columns));
        return qb.buildSQL();
    }

}

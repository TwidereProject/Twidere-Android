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

package org.mariotaku.twidere.util.content

import android.accounts.AccountManager
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import org.mariotaku.kpreferences.get
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.sqliteqb.library.*
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.sqliteqb.library.query.SQLCreateTriggerQuery.Event
import org.mariotaku.sqliteqb.library.query.SQLCreateTriggerQuery.Type
import org.mariotaku.twidere.TwidereConstants.SHARED_PREFERENCES_NAME
import org.mariotaku.twidere.annotation.CustomTabType
import org.mariotaku.twidere.constant.defaultAPIConfigKey
import org.mariotaku.twidere.model.Tab
import org.mariotaku.twidere.model.tab.TabConfiguration
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.util.content.DatabaseUpgradeHelper.safeUpgrade
import org.mariotaku.twidere.util.migrateAccounts
import java.util.*

class TwidereSQLiteOpenHelper(
        private val context: Context,
        name: String,
        version: Int
) : SQLiteOpenHelper(context, name, null, version) {

    override fun onCreate(db: SQLiteDatabase) {
        db.beginTransaction()
        db.execSQL(createTable(Statuses.TABLE_NAME, Statuses.COLUMNS, Statuses.TYPES, true))
        db.execSQL(createTable(Activities.AboutMe.TABLE_NAME, Activities.AboutMe.COLUMNS, Activities.AboutMe.TYPES, true))
        db.execSQL(createTable(Drafts.TABLE_NAME, Drafts.COLUMNS, Drafts.TYPES, true))
        db.setTransactionSuccessful()
        db.endTransaction()

        db.beginTransaction()
        db.execSQL(createTable(CachedUsers.TABLE_NAME, CachedUsers.COLUMNS, CachedUsers.TYPES, true,
                createConflictReplaceConstraint(CachedUsers.USER_KEY)))
        db.execSQL(createTable(CachedStatuses.TABLE_NAME, CachedStatuses.COLUMNS, CachedStatuses.TYPES, true))
        db.execSQL(createTable(CachedTrends.Local.TABLE_NAME, CachedTrends.Local.COLUMNS, CachedTrends.Local.TYPES,
                true))
        db.execSQL(createTable(CachedHashtags.TABLE_NAME, CachedHashtags.COLUMNS, CachedHashtags.TYPES, true))
        db.execSQL(createTable(CachedRelationships.TABLE_NAME, CachedRelationships.COLUMNS, CachedRelationships.TYPES, true,
                createConflictReplaceConstraint(CachedRelationships.ACCOUNT_KEY, CachedRelationships.USER_KEY)))
        db.setTransactionSuccessful()
        db.endTransaction()


        db.beginTransaction()
        db.execSQL(createTable(Filters.Users.TABLE_NAME, Filters.Users.COLUMNS, Filters.Users.TYPES, true))
        db.execSQL(createTable(Filters.Keywords.TABLE_NAME, Filters.Keywords.COLUMNS, Filters.Keywords.TYPES, true))
        db.execSQL(createTable(Filters.Sources.TABLE_NAME, Filters.Sources.COLUMNS, Filters.Sources.TYPES, true))
        db.execSQL(createTable(Filters.Links.TABLE_NAME, Filters.Links.COLUMNS, Filters.Links.TYPES, true))
        db.execSQL(createTable(Filters.Subscriptions.TABLE_NAME, Filters.Subscriptions.COLUMNS, Filters.Subscriptions.TYPES, true))
        db.setTransactionSuccessful()
        db.endTransaction()

        db.beginTransaction()
        db.execSQL(createTable(Messages.TABLE_NAME, Messages.COLUMNS, Messages.TYPES, true,
                messagesConstraint()))
        db.execSQL(createTable(Conversations.TABLE_NAME, Conversations.COLUMNS, Conversations.TYPES,
                true, messageConversationsConstraint()))
        db.setTransactionSuccessful()
        db.endTransaction()

        db.beginTransaction()
        db.execSQL(createTable(Tabs.TABLE_NAME, Tabs.COLUMNS, Tabs.TYPES, true))
        db.execSQL(createTable(SavedSearches.TABLE_NAME, SavedSearches.COLUMNS, SavedSearches.TYPES, true))
        db.execSQL(createTable(SearchHistory.TABLE_NAME, SearchHistory.COLUMNS, SearchHistory.TYPES, true))
        db.setTransactionSuccessful()
        db.endTransaction()

        db.beginTransaction()
        createTriggers(db)
        createIndices(db)
        db.setTransactionSuccessful()
        db.endTransaction()

        setupDefaultTabs(db)
    }


    private fun setupDefaultTabs(db: SQLiteDatabase) {
        val creator = ObjectCursor.valuesCreatorFrom(Tab::class.java)
        db.beginTransaction()
        @CustomTabType
        val tabTypes = arrayOf(CustomTabType.HOME_TIMELINE, CustomTabType.NOTIFICATIONS_TIMELINE,
                CustomTabType.TRENDS_SUGGESTIONS, CustomTabType.DIRECT_MESSAGES)
        for (i in tabTypes.indices) {
            @CustomTabType
            val tabType = tabTypes[i]
            val conf = TabConfiguration.ofType(tabType) ?: continue
            val tab = Tab().apply {
                this.type = tabType
                this.icon = conf.icon.persistentKey
                this.position = i
            }
            db.insert(Tabs.TABLE_NAME, null, creator.create(tab))
        }
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    private fun createConflictReplaceConstraint(vararg columns: String): Constraint {
        return Constraint.unique(Columns(*columns), OnConflict.IGNORE)
    }

    private fun createIndices(db: SQLiteDatabase) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
        db.execSQL(createIndex("statuses_index", Statuses.TABLE_NAME, arrayOf(Statuses.ACCOUNT_KEY), true))
    }

    private fun createTriggers(db: SQLiteDatabase) {
        db.execSQL(SQLQueryBuilder.dropTrigger(true, "delete_old_statuses").sql)
        db.execSQL(SQLQueryBuilder.dropTrigger(true, "delete_old_cached_statuses").sql)
        db.execSQL(SQLQueryBuilder.dropTrigger(true, "on_user_cache_update_trigger").sql)
        db.execSQL(SQLQueryBuilder.dropTrigger(true, "delete_old_cached_hashtags").sql)

        db.execSQL(createDeleteDuplicateStatusTrigger("delete_old_statuses", Statuses.TABLE_NAME).sql)
        db.execSQL(createDeleteDuplicateStatusTrigger("delete_old_cached_statuses", CachedStatuses.TABLE_NAME).sql)

        // Update user info in filtered users
        val cachedUsersTable = Table(CachedUsers.TABLE_NAME)
        val filteredUsersTable = Table(Filters.Users.TABLE_NAME)
        db.execSQL(SQLQueryBuilder.createTrigger(false, true, "on_user_cache_update_trigger")
                .type(Type.BEFORE)
                .event(Event.INSERT)
                .on(cachedUsersTable)
                .forEachRow(true)
                .actions(SQLQueryBuilder.update(OnConflict.REPLACE, filteredUsersTable)
                        .set(SetValue(Column(Filters.Users.NAME), Column(Table.NEW, CachedUsers.NAME)),
                                SetValue(Column(Filters.Users.SCREEN_NAME), Column(Table.NEW, CachedUsers.SCREEN_NAME)))
                        .where(Expression.equals(Column(Filters.Users.USER_KEY), Column(Table.NEW, CachedUsers.USER_KEY)))
                        .build())
                .buildSQL())

        // Delete duplicated hashtags ignoring case
        val cachedHashtagsTable = Table(CachedHashtags.TABLE_NAME)
        db.execSQL(SQLQueryBuilder.createTrigger(false, true, "delete_old_cached_hashtags")
                .type(Type.BEFORE)
                .event(Event.INSERT)
                .on(cachedHashtagsTable)
                .forEachRow(true)
                .actions(SQLQueryBuilder.deleteFrom(cachedHashtagsTable)
                        .where(Expression.like(Column(CachedHashtags.NAME), Column(Table.NEW, CachedHashtags.NAME)))
                        .build())
                .buildSQL())

    }

    private fun createDeleteDuplicateStatusTrigger(triggerName: String, tableName: String): SQLQuery {
        val table = Table(tableName)
        val deleteOld = SQLQueryBuilder.deleteFrom(table).where(Expression.and(
                Expression.equals(Column(Statuses.ACCOUNT_KEY), Column(Table.NEW, Statuses.ACCOUNT_KEY)),
                Expression.equals(Column(Statuses.ID), Column(Table.NEW, Statuses.ID))
        )).build()
        return SQLQueryBuilder.createTrigger(false, true, triggerName)
                .type(Type.BEFORE).event(Event.INSERT).on(table).forEachRow(true)
                .actions(deleteOld).build()
    }


    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        handleVersionChange(db, oldVersion, newVersion)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        handleVersionChange(db, oldVersion, newVersion)
        if (oldVersion <= 43 && newVersion >= 44 && newVersion <= 153) {
            val values = ContentValues()
            val prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            // Here I use old consumer key/secret because it's default key for
            // older versions
            val defaultAPIConfig = prefs[defaultAPIConfigKey]
            values.put(Accounts.CONSUMER_KEY, defaultAPIConfig.consumerKey)
            values.put(Accounts.CONSUMER_SECRET, defaultAPIConfig.consumerSecret)
            db.update(Accounts.TABLE_NAME, values, null, null)
        }
    }

    private fun handleVersionChange(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion <= 153) {
            migrateLegacyAccounts(db)
            if (newVersion > 153) {
                migrateAccounts(AccountManager.get(context), db)
                db.execSQL(SQLQueryBuilder.dropTable(true, Accounts.TABLE_NAME).sql)
            }
        }
        if (oldVersion <= 164) {
            try {
                db.execSQL(SQLQueryBuilder.dropView(true, "messages").sql)
            } catch (e: IllegalArgumentException) {
                // Ignore http://crashes.to/s/5f46822a382
            }
            db.execSQL(SQLQueryBuilder.dropView(true, "messages_conversation_entries").sql)
            db.execSQL(SQLQueryBuilder.dropTrigger(true, "delete_old_received_messages").sql)
            db.execSQL(SQLQueryBuilder.dropTrigger(true, "delete_old_sent_messages").sql)

            db.execSQL(SQLQueryBuilder.dropTable(true, "messages_inbox").sql)
            db.execSQL(SQLQueryBuilder.dropTable(true, "messages_outbox").sql)

            db.execSQL(SQLQueryBuilder.dropIndex(true, "messages_inbox_index").sql)
            db.execSQL(SQLQueryBuilder.dropIndex(true, "messages_outbox_index").sql)
        }

        safeUpgrade(db, Statuses.TABLE_NAME, Statuses.COLUMNS, Statuses.TYPES, true, null)
        safeUpgrade(db, Activities.AboutMe.TABLE_NAME, Activities.AboutMe.COLUMNS,
                Activities.AboutMe.TYPES, true, null)
        migrateDrafts(db)
        safeUpgrade(db, CachedUsers.TABLE_NAME, CachedUsers.COLUMNS, CachedUsers.TYPES, true, null,
                createConflictReplaceConstraint(CachedUsers.USER_KEY))
        safeUpgrade(db, CachedStatuses.TABLE_NAME, CachedStatuses.COLUMNS, CachedStatuses.TYPES, true, null)
        safeUpgrade(db, CachedHashtags.TABLE_NAME, CachedHashtags.COLUMNS, CachedHashtags.TYPES, true, null)
        safeUpgrade(db, CachedRelationships.TABLE_NAME, CachedRelationships.COLUMNS, CachedRelationships.TYPES, true, null,
                createConflictReplaceConstraint(CachedRelationships.ACCOUNT_KEY, CachedRelationships.USER_KEY))

        migrateFilters(db, oldVersion)
        safeUpgrade(db, CachedTrends.Local.TABLE_NAME, CachedTrends.Local.COLUMNS,
                CachedTrends.Local.TYPES, true, null)
        safeUpgrade(db, Tabs.TABLE_NAME, Tabs.COLUMNS, Tabs.TYPES, false, null)
        safeUpgrade(db, SavedSearches.TABLE_NAME, SavedSearches.COLUMNS, SavedSearches.TYPES, true, null)

        // DM columns
        safeUpgrade(db, Messages.TABLE_NAME, Messages.COLUMNS, Messages.TYPES, true, null,
                messagesConstraint())
        safeUpgrade(db, Conversations.TABLE_NAME, Conversations.COLUMNS,
                Conversations.TYPES, true, null, messageConversationsConstraint())

        if (oldVersion < 131) {
            migrateFilteredUsers(db)
        }

        db.beginTransaction()
        db.execSQL(SQLQueryBuilder.dropTable(true, "network_usages").sql)
        db.execSQL(SQLQueryBuilder.dropTable(true, "mentions").sql)
        db.execSQL(SQLQueryBuilder.dropTable(true, "activities_by_friends").sql)
        createTriggers(db)
        createIndices(db)
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    private fun migrateDrafts(db: SQLiteDatabase) {
        val draftsAlias = HashMap<String, String>()
        draftsAlias[Drafts.MEDIA] = "medias"
        safeUpgrade(db, Drafts.TABLE_NAME, Drafts.COLUMNS, Drafts.TYPES, false, draftsAlias)
    }

    private fun migrateFilters(db: SQLiteDatabase, oldVersion: Int) {
        safeUpgrade(db, Filters.Users.TABLE_NAME, Filters.Users.COLUMNS, Filters.Users.TYPES,
                oldVersion < 49, null)

        val filtersAlias = HashMap<String, String>()
        safeUpgrade(db, Filters.Keywords.TABLE_NAME, Filters.Keywords.COLUMNS, Filters.Keywords.TYPES,
                oldVersion < 49, filtersAlias)
        safeUpgrade(db, Filters.Sources.TABLE_NAME, Filters.Sources.COLUMNS, Filters.Sources.TYPES,
                oldVersion < 49, filtersAlias)
        safeUpgrade(db, Filters.Links.TABLE_NAME, Filters.Links.COLUMNS, Filters.Links.TYPES,
                oldVersion < 49, filtersAlias)
        safeUpgrade(db, Filters.Subscriptions.TABLE_NAME, Filters.Subscriptions.COLUMNS,
                Filters.Subscriptions.TYPES, false, null)
    }

    private fun migrateLegacyAccounts(db: SQLiteDatabase) {
        val alias = HashMap<String, String>()
        alias[Accounts.SCREEN_NAME] = "username"
        alias[Accounts.NAME] = "username"
        alias[Accounts.ACCOUNT_KEY] = "user_id"
        alias[Accounts.COLOR] = "user_color"
        alias[Accounts.OAUTH_TOKEN_SECRET] = "token_secret"
        alias[Accounts.API_URL_FORMAT] = "rest_base_url"
        safeUpgrade(db, Accounts.TABLE_NAME, Accounts.COLUMNS, Accounts.TYPES, false, alias)
    }

    private fun migrateFilteredUsers(db: SQLiteDatabase) {
        db.execSQL(SQLQueryBuilder.update(OnConflict.REPLACE, Filters.Users.TABLE_NAME)
                .set(SetValue(Filters.Users.USER_KEY, RawSQLLang(Filters.Users.USER_KEY + "||?")))
                .where(Expression.notLikeArgs(Column(Filters.Users.USER_KEY)))
                .buildSQL(),
                arrayOf<Any>("@twitter.com", "%@%"))
    }

    private fun createTable(tableName: String, columns: Array<String>, types: Array<String>,
            createIfNotExists: Boolean, vararg constraints: Constraint): String {
        val qb = SQLQueryBuilder.createTable(createIfNotExists, tableName)
        qb.columns(*NewColumn.createNewColumns(columns, types))
        qb.constraint(*constraints)
        return qb.buildSQL()
    }

    private fun createIndex(indexName: String, tableName: String, columns: Array<String>,
            createIfNotExists: Boolean): String {
        val qb = SQLQueryBuilder.createIndex(false, createIfNotExists)
        qb.name(indexName)
        qb.on(Table(tableName), Columns(*columns))
        return qb.buildSQL()
    }

    private fun messagesConstraint(): Constraint {
        return Constraint.unique("unique_message", Columns(Messages.ACCOUNT_KEY, Messages.CONVERSATION_ID,
                Messages.MESSAGE_ID), OnConflict.REPLACE)
    }

    private fun messageConversationsConstraint(): Constraint {
        return Constraint.unique("unique_message_conversations", Columns(Conversations.ACCOUNT_KEY,
                Conversations.CONVERSATION_ID), OnConflict.REPLACE)
    }
}

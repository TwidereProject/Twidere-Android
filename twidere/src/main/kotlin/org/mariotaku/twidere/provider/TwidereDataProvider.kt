/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.provider

import android.app.PendingIntent
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteFullException
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.BaseColumns
import android.support.v4.app.NotificationCompat
import android.support.v4.text.BidiFormatter
import com.nostra13.universalimageloader.core.ImageLoader
import com.squareup.otto.Bus
import org.apache.commons.lang3.ArrayUtils
import org.mariotaku.ktextension.isNullOrEmpty
import org.mariotaku.ktextension.toNulls
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.RawItemArray
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.annotation.CustomTabType
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.model.AccountPreferences
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.model.DraftCursorIndices
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.UnreadCountUpdatedEvent
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.service.LengthyOperationsService
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.SQLiteDatabaseWrapper.LazyLoadCallback
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.util.database.CachedUsersQueryBuilder
import org.mariotaku.twidere.util.database.SuggestionsCursorCreator
import org.mariotaku.twidere.util.net.TwidereDns
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

class TwidereDataProvider : ContentProvider(), LazyLoadCallback {
    @Inject
    lateinit internal var readStateManager: ReadStateManager
    @Inject
    lateinit internal var twitterWrapper: AsyncTwitterWrapper
    @Inject
    lateinit internal var mediaLoader: ImageLoader
    @Inject
    lateinit internal var notificationManager: NotificationManagerWrapper
    @Inject
    lateinit internal var preferences: SharedPreferencesWrapper
    @Inject
    lateinit internal var dns: TwidereDns
    @Inject
    lateinit internal var bus: Bus
    @Inject
    lateinit internal var userColorNameManager: UserColorNameManager
    @Inject
    lateinit internal var bidiFormatter: BidiFormatter
    @Inject
    lateinit internal var permissionsManager: PermissionsManager
    @Inject
    lateinit internal var contentNotificationManager: ContentNotificationManager

    private lateinit var databaseWrapper: SQLiteDatabaseWrapper

    private var handler: Handler? = null
    private var backgroundExecutor: Executor? = null


    override fun onCreate(): Boolean {
        val context = context!!
        GeneralComponentHelper.build(context).inject(this)
        handler = Handler(Looper.getMainLooper())
        databaseWrapper = SQLiteDatabaseWrapper(this)
        backgroundExecutor = Executors.newSingleThreadExecutor()
        // final GetWritableDatabaseTask task = new
        // GetWritableDatabaseTask(context, helper, mDatabaseWrapper);
        // task.executeTask();
        return true
    }

    override fun onCreateSQLiteDatabase(): SQLiteDatabase {
        val app = TwidereApplication.getInstance(context!!)
        val helper = app.sqLiteOpenHelper
        return helper.writableDatabase
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        try {
            return insertInternal(uri, values)
        } catch (e: SQLException) {
            if (handleSQLException(e)) {
                try {
                    return insertInternal(uri, values)
                } catch (e1: SQLException) {
                    throw IllegalStateException(e1)
                }

            }
            throw IllegalStateException(e)
        }

    }

    override fun bulkInsert(uri: Uri, valuesArray: Array<ContentValues>): Int {
        try {
            return bulkInsertInternal(uri, valuesArray)
        } catch (e: SQLException) {
            if (handleSQLException(e)) {
                try {
                    return bulkInsertInternal(uri, valuesArray)
                } catch (e1: SQLException) {
                    throw IllegalStateException(e1)
                }

            }
            throw IllegalStateException(e)
        }

    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?,
            sortOrder: String?): Cursor? {
        try {
            val tableId = DataStoreUtils.getTableId(uri)
            val table = DataStoreUtils.getTableNameById(tableId)
            when (tableId) {
                VIRTUAL_TABLE_ID_DATABASE_PREPARE -> {
                    databaseWrapper.prepare()
                    return MatrixCursor(projection ?: arrayOfNulls<String>(0))
                }
                VIRTUAL_TABLE_ID_PERMISSIONS -> {
                    val context = context ?: return null
                    val c = MatrixCursor(Permissions.MATRIX_COLUMNS)
                    val pm = context.packageManager
                    if (Binder.getCallingUid() == Process.myUid()) {
                        val map = permissionsManager.all
                        for ((key, value) in map) {
                            c.addRow(arrayOf<Any>(key, value))
                        }
                    } else {
                        val map = permissionsManager.all
                        val callingPackages = pm.getPackagesForUid(Binder.getCallingUid())
                        for ((key, value) in map) {
                            if (ArrayUtils.contains(callingPackages, key)) {
                                c.addRow(arrayOf<Any>(key, value))
                            }
                        }
                    }
                    return c
                }
                VIRTUAL_TABLE_ID_CACHED_USERS_WITH_RELATIONSHIP -> {
                    val accountKey = UserKey.valueOf(uri.lastPathSegment)
                    val query = CachedUsersQueryBuilder.withRelationship(projection,
                            selection, selectionArgs, sortOrder, accountKey)
                    val c = databaseWrapper.rawQuery(query.first.sql, query.second)
                    c?.setNotificationUri(context.contentResolver, CachedUsers.CONTENT_URI)
                    return c
                }
                VIRTUAL_TABLE_ID_CACHED_USERS_WITH_SCORE -> {
                    val accountKey = UserKey.valueOf(uri.lastPathSegment)
                    val query = CachedUsersQueryBuilder.withScore(projection,
                            selection, selectionArgs, sortOrder, accountKey, 0)
                    val c = databaseWrapper.rawQuery(query.first.sql, query.second)
                    c?.setNotificationUri(context.contentResolver, CachedUsers.CONTENT_URI)
                    return c
                }
                VIRTUAL_TABLE_ID_DRAFTS_UNSENT -> {
                    val twitter = twitterWrapper
                    val sendingIds = RawItemArray(twitter.getSendingDraftIds())
                    val where: Expression
                    if (selection != null) {
                        where = Expression.and(Expression(selection),
                                Expression.notIn(Column(Drafts._ID), sendingIds))
                    } else {
                        where = Expression.and(Expression.notIn(Column(Drafts._ID), sendingIds))
                    }
                    val c = databaseWrapper.query(Drafts.TABLE_NAME, projection,
                            where.sql, selectionArgs, null, null, sortOrder)
                    c?.setNotificationUri(context.contentResolver, uri)
                    return c
                }
                VIRTUAL_TABLE_ID_SUGGESTIONS_AUTO_COMPLETE -> {
                    return SuggestionsCursorCreator.forAutoComplete(databaseWrapper,
                            userColorNameManager, uri, projection)
                }
                VIRTUAL_TABLE_ID_SUGGESTIONS_SEARCH -> {
                    return SuggestionsCursorCreator.forSearch(databaseWrapper,
                            userColorNameManager, uri, projection)
                }
                VIRTUAL_TABLE_ID_NULL -> {
                    return null
                }
                VIRTUAL_TABLE_ID_EMPTY -> {
                    return MatrixCursor(projection ?: arrayOfNulls<String>(0))
                }
                VIRTUAL_TABLE_ID_RAW_QUERY -> {
                    if (projection != null || selection != null || sortOrder != null) {
                        throw IllegalArgumentException()
                    }
                    val c = databaseWrapper.rawQuery(uri.lastPathSegment, selectionArgs)
                    val uri = uri.getQueryParameter(QUERY_PARAM_NOTIFY_URI)?.let(Uri::parse)
                    if (uri != null) {
                        c?.setNotificationUri(context.contentResolver, uri)
                    }
                    return c
                }
            }
            if (table == null) return null
            val c = databaseWrapper.query(table, projection, selection, selectionArgs,
                    null, null, sortOrder)
            c?.setNotificationUri(context.contentResolver, uri)
            return c
        } catch (e: SQLException) {
            throw IllegalStateException(e)
        }

    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        try {
            return updateInternal(uri, values, selection, selectionArgs)
        } catch (e: SQLException) {
            if (handleSQLException(e)) {
                try {
                    return updateInternal(uri, values, selection, selectionArgs)
                } catch (e1: SQLException) {
                    throw IllegalStateException(e1)
                }

            }
            throw IllegalStateException(e)
        }

    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        try {
            return deleteInternal(uri, selection, selectionArgs)
        } catch (e: SQLException) {
            if (handleSQLException(e)) {
                try {
                    return deleteInternal(uri, selection, selectionArgs)
                } catch (e1: SQLException) {
                    throw IllegalStateException(e1)
                }

            }
            throw IllegalStateException(e)
        }

    }

    override fun getType(uri: Uri): String? {
        return null
    }

    private fun handleSQLException(e: SQLException): Boolean {
        try {
            if (e is SQLiteFullException) {
                // Drop cached databases
                databaseWrapper.delete(CachedUsers.TABLE_NAME, null, null)
                databaseWrapper.delete(CachedStatuses.TABLE_NAME, null, null)
                databaseWrapper.delete(CachedHashtags.TABLE_NAME, null, null)
                databaseWrapper.execSQL("VACUUM")
                return true
            }
        } catch (ee: SQLException) {
            throw IllegalStateException(ee)
        }

        throw IllegalStateException(e)
    }

    private fun bulkInsertInternal(uri: Uri, valuesArray: Array<ContentValues>): Int {
        val tableId = DataStoreUtils.getTableId(uri)
        val table = DataStoreUtils.getTableNameById(tableId)
        var result = 0
        val newIds = LongArray(valuesArray.size)
        if (table != null && valuesArray.isNotEmpty()) {
            databaseWrapper.beginTransaction()
            if (tableId == TABLE_ID_CACHED_USERS) {
                for (values in valuesArray) {
                    val where = Expression.equalsArgs(CachedUsers.USER_KEY)
                    databaseWrapper.update(table, values, where.sql, arrayOf(values.getAsString(CachedUsers.USER_KEY)))
                    newIds[result++] = databaseWrapper.insertWithOnConflict(table, null,
                            values, SQLiteDatabase.CONFLICT_REPLACE)
                }
            } else if (tableId == TABLE_ID_SEARCH_HISTORY) {
                for (values in valuesArray) {
                    values.put(SearchHistory.RECENT_QUERY, System.currentTimeMillis())
                    val where = Expression.equalsArgs(SearchHistory.QUERY)
                    val args = arrayOf(values.getAsString(SearchHistory.QUERY))
                    databaseWrapper.update(table, values, where.sql, args)
                    newIds[result++] = databaseWrapper.insertWithOnConflict(table, null,
                            values, SQLiteDatabase.CONFLICT_IGNORE)
                }
            } else {
                val conflictAlgorithm = getConflictAlgorithm(tableId)
                if (conflictAlgorithm != SQLiteDatabase.CONFLICT_NONE) {
                    for (values in valuesArray) {
                        newIds[result++] = databaseWrapper.insertWithOnConflict(table, null,
                                values, conflictAlgorithm)
                    }
                } else {
                    for (values in valuesArray) {
                        newIds[result++] = databaseWrapper.insert(table, null, values)
                    }
                }
            }
            databaseWrapper.setTransactionSuccessful()
            databaseWrapper.endTransaction()
        }
        if (result > 0) {
            onDatabaseUpdated(tableId, uri)
        }
        onNewItemsInserted(uri, tableId, valuesArray.toNulls())
        return result
    }

    private fun deleteInternal(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val tableId = DataStoreUtils.getTableId(uri)
        val table = DataStoreUtils.getTableNameById(tableId) ?: return 0
        val result = databaseWrapper.delete(table, selection, selectionArgs)
        if (result > 0) {
            onDatabaseUpdated(tableId, uri)
        }
        return result
    }

    private fun showDraftNotification(values: ContentValues?): Long {
        val context = context
        if (values == null || context == null) return -1
        val draftId = values.getAsLong(BaseColumns._ID) ?: return -1
        val where = Expression.equals(Drafts._ID, draftId)
        val c = context.contentResolver.query(Drafts.CONTENT_URI, Drafts.COLUMNS, where.sql, null, null) ?: return -1
        val i = DraftCursorIndices(c)
        val item: Draft
        try {
            if (!c.moveToFirst()) return -1
            item = i.newObject(c)
        } catch (e: IOException) {
            return -1
        } finally {
            c.close()
        }
        val title = context.getString(R.string.status_not_updated)
        val message = context.getString(R.string.status_not_updated_summary)
        val intent = Intent()
        intent.`package` = BuildConfig.APPLICATION_ID
        val uriBuilder = Uri.Builder()
        uriBuilder.scheme(SCHEME_TWIDERE)
        uriBuilder.authority(AUTHORITY_DRAFTS)
        intent.data = uriBuilder.build()
        val nb = NotificationCompat.Builder(context)
        nb.setTicker(message)
        nb.setContentTitle(title)
        nb.setContentText(item.text)
        nb.setAutoCancel(true)
        nb.setWhen(System.currentTimeMillis())
        nb.setSmallIcon(R.drawable.ic_stat_draft)
        val discardIntent = Intent(context, LengthyOperationsService::class.java)
        discardIntent.action = INTENT_ACTION_DISCARD_DRAFT
        val draftUri = Uri.withAppendedPath(Drafts.CONTENT_URI, draftId.toString())
        discardIntent.data = draftUri
        nb.addAction(R.drawable.ic_action_delete, context.getString(R.string.discard), PendingIntent.getService(context, 0,
                discardIntent, PendingIntent.FLAG_ONE_SHOT))

        val sendIntent = Intent(context, LengthyOperationsService::class.java)
        sendIntent.action = INTENT_ACTION_SEND_DRAFT
        sendIntent.data = draftUri
        nb.addAction(R.drawable.ic_action_send, context.getString(R.string.action_send),
                PendingIntent.getService(context, 0, sendIntent, PendingIntent.FLAG_ONE_SHOT))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        nb.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT))
        notificationManager.notify(draftUri.toString(), NOTIFICATION_ID_DRAFTS,
                nb.build())
        return draftId
    }


    private fun insertInternal(uri: Uri, values: ContentValues?): Uri? {
        val tableId = DataStoreUtils.getTableId(uri)
        val table = DataStoreUtils.getTableNameById(tableId)
        var rowId: Long = -1
        when (tableId) {
            TABLE_ID_CACHED_USERS -> {
                if (values != null) {
                    val where = Expression.equalsArgs(CachedUsers.USER_KEY)
                    val whereArgs = arrayOf(values.getAsString(CachedUsers.USER_KEY))
                    databaseWrapper.update(table, values, where.sql, whereArgs)
                }
                rowId = databaseWrapper.insertWithOnConflict(table, null, values,
                        SQLiteDatabase.CONFLICT_IGNORE)
            }
            TABLE_ID_SEARCH_HISTORY -> {
                if (values != null) {
                    values.put(SearchHistory.RECENT_QUERY, System.currentTimeMillis())
                    val where = Expression.equalsArgs(SearchHistory.QUERY)
                    val args = arrayOf(values.getAsString(SearchHistory.QUERY))
                    databaseWrapper.update(table, values, where.sql, args)
                }
                rowId = databaseWrapper.insertWithOnConflict(table, null, values,
                        SQLiteDatabase.CONFLICT_IGNORE)
            }
            TABLE_ID_CACHED_RELATIONSHIPS -> {
                var updated = false
                if (values != null) {
                    val accountKey = values.getAsString(CachedRelationships.ACCOUNT_KEY)
                    val userId = values.getAsString(CachedRelationships.USER_KEY)
                    val where = Expression.and(Expression.equalsArgs(CachedRelationships.ACCOUNT_KEY),
                            Expression.equalsArgs(CachedRelationships.USER_KEY))
                    if (databaseWrapper.update(table, values, where.sql, arrayOf(accountKey, userId)) > 0) {
                        val projection = arrayOf(CachedRelationships._ID)
                        val c = databaseWrapper.query(table, projection, where.sql, null,
                                null, null, null)
                        if (c.moveToFirst()) {
                            rowId = c.getLong(0)
                        }
                        c.close()
                        updated = true
                    }
                }
                if (!updated) {
                    rowId = databaseWrapper.insertWithOnConflict(table, null, values,
                            SQLiteDatabase.CONFLICT_IGNORE)
                }
            }
            VIRTUAL_TABLE_ID_DRAFTS_NOTIFICATIONS -> {
                rowId = showDraftNotification(values)
            }
            else -> {
                val conflictAlgorithm = getConflictAlgorithm(tableId)
                if (conflictAlgorithm != SQLiteDatabase.CONFLICT_NONE) {
                    rowId = databaseWrapper.insertWithOnConflict(table, null, values,
                            conflictAlgorithm)
                } else if (table != null) {
                    rowId = databaseWrapper.insert(table, null, values)
                } else {
                    return null
                }
            }
        }
        onDatabaseUpdated(tableId, uri)
        onNewItemsInserted(uri, tableId, arrayOf(values))
        return Uri.withAppendedPath(uri, rowId.toString())
    }

    private fun updateInternal(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val tableId = DataStoreUtils.getTableId(uri)
        val table = DataStoreUtils.getTableNameById(tableId)
        var result = 0
        if (table != null) {
            result = databaseWrapper.update(table, values, selection, selectionArgs)
        }
        if (result > 0) {
            onDatabaseUpdated(tableId, uri)
        }
        return result
    }

    private fun notifyContentObserver(uri: Uri) {
        if (!uri.getBooleanQueryParameter(QUERY_PARAM_NOTIFY, true)) return
        handler!!.post(Runnable {
            context?.contentResolver?.notifyChange(uri, null)
        })
    }

    private fun notifyUnreadCountChanged(position: Int) {
        handler!!.post { bus.post(UnreadCountUpdatedEvent(position)) }
    }

    private fun onDatabaseUpdated(tableId: Int, uri: Uri?) {
        if (uri == null) return
        notifyContentObserver(uri)
    }

    private fun onNewItemsInserted(uri: Uri, tableId: Int, valuesArray: Array<ContentValues?>?) {
        val context = context ?: return
        if (valuesArray.isNullOrEmpty()) return
        when (tableId) {
            TABLE_ID_STATUSES -> {
                backgroundExecutor!!.execute {
                    val prefs = AccountPreferences.getNotificationEnabledPreferences(context,
                            DataStoreUtils.getAccountKeys(context))
                    prefs.filter(AccountPreferences::isHomeTimelineNotificationEnabled).forEach {
                        val positionTag = getPositionTag(CustomTabType.HOME_TIMELINE, it.accountKey)
                        contentNotificationManager.showTimeline(it, positionTag)
                    }
                    notifyUnreadCountChanged(NOTIFICATION_ID_HOME_TIMELINE)
                }
            }
            TABLE_ID_ACTIVITIES_ABOUT_ME -> {
                backgroundExecutor!!.execute {
                    val prefs = AccountPreferences.getNotificationEnabledPreferences(context,
                            DataStoreUtils.getAccountKeys(context))
                    prefs.filter(AccountPreferences::isInteractionsNotificationEnabled).forEach {
                        contentNotificationManager.showInteractions(it, getPositionTag(ReadPositionTag.ACTIVITIES_ABOUT_ME,
                                it.accountKey))
                    }
                    notifyUnreadCountChanged(NOTIFICATION_ID_INTERACTIONS_TIMELINE)
                }
            }
            TABLE_ID_MESSAGES_CONVERSATIONS -> {
                val prefs = AccountPreferences.getNotificationEnabledPreferences(context,
                        DataStoreUtils.getAccountKeys(context))
                prefs.filter(AccountPreferences::isDirectMessagesNotificationEnabled).forEach {
                    contentNotificationManager.showMessages(it)
                }
                notifyUnreadCountChanged(NOTIFICATION_ID_DIRECT_MESSAGES)
            }
            TABLE_ID_DRAFTS -> {
            }
        }
    }

    private fun getPositionTag(tag: String, accountKey: UserKey): Long {
        val position = readStateManager.getPosition(Utils.getReadPositionTagWithAccount(tag,
                accountKey))
        if (position != -1L) return position
        return readStateManager.getPosition(tag)
    }

    companion object {

        private fun getConflictAlgorithm(tableId: Int): Int {
            when (tableId) {
                TABLE_ID_CACHED_HASHTAGS, TABLE_ID_CACHED_STATUSES, TABLE_ID_CACHED_USERS,
                TABLE_ID_CACHED_RELATIONSHIPS, TABLE_ID_SEARCH_HISTORY, TABLE_ID_MESSAGES,
                TABLE_ID_MESSAGES_CONVERSATIONS -> {
                    return SQLiteDatabase.CONFLICT_REPLACE
                }
                TABLE_ID_FILTERED_USERS, TABLE_ID_FILTERED_KEYWORDS, TABLE_ID_FILTERED_SOURCES,
                TABLE_ID_FILTERED_LINKS -> {
                    return SQLiteDatabase.CONFLICT_IGNORE
                }
            }
            return SQLiteDatabase.CONFLICT_NONE
        }
    }


}

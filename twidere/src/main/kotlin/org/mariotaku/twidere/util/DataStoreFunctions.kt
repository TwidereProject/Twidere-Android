package org.mariotaku.twidere.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import androidx.annotation.WorkerThread
import androidx.collection.LongSparseArray
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.ktextension.toStringArray
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.sqliteqb.library.*
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.twidere.extension.queryReference
import org.mariotaku.twidere.extension.rawQueryReference
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.util.DataStoreUtils.ACTIVITIES_URIS
import java.io.IOException


fun Context.deleteDrafts(draftIds: LongArray): Int {
    val where = Expression.inArgs(Drafts._ID, draftIds.size).sql
    val whereArgs = draftIds.mapToArray(Long::toString)

    contentResolver.queryReference(Drafts.CONTENT_URI, Drafts.COLUMNS, where, whereArgs, null)?.use { (cursor) ->
        val indices = ObjectCursor.indicesFrom(cursor, Draft::class.java)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val draft = indices.newObject(cursor)
            draft.media?.forEach { item ->
                Utils.deleteMedia(this, Uri.parse(item.uri))
            }
            cursor.moveToNext()
        }
    }
    return contentResolver.delete(Drafts.CONTENT_URI, where, whereArgs)
}

fun ContentResolver.deleteAccountData(accountKey: UserKey) {
    val where = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY).sql
    val whereArgs = arrayOf(accountKey.toString())
    // Also delete tweets related to the account we previously
    // deleted.
    delete(Statuses.CONTENT_URI, where, whereArgs)
    delete(Activities.AboutMe.CONTENT_URI, where, whereArgs)
    delete(Messages.CONTENT_URI, where, whereArgs)
    delete(Conversations.CONTENT_URI, where, whereArgs)
}


fun ContentResolver.deleteActivityStatus(accountKey: UserKey, statusId: String,
        result: ParcelableStatus?) {

    val host = accountKey.host
    val deleteWhere: String
    val updateWhere: String
    val deleteWhereArgs: Array<String>
    val updateWhereArgs: Array<String>
    if (host != null) {
        deleteWhere = Expression.and(
                Expression.likeRaw(Column(Activities.ACCOUNT_KEY), "'%@'||?"),
                Expression.or(
                        Expression.equalsArgs(Activities.ID),
                        Expression.equalsArgs(Activities.RETWEET_ID)
                )).sql
        deleteWhereArgs = arrayOf(host, statusId, statusId)
        updateWhere = Expression.and(
                Expression.likeRaw(Column(Activities.ACCOUNT_KEY), "'%@'||?"),
                Expression.equalsArgs(Activities.MY_RETWEET_ID)
        ).sql
        updateWhereArgs = arrayOf(host, statusId)
    } else {
        deleteWhere = Expression.or(
                Expression.equalsArgs(Activities.ID),
                Expression.equalsArgs(Activities.RETWEET_ID)
        ).sql
        deleteWhereArgs = arrayOf(statusId, statusId)
        updateWhere = Expression.equalsArgs(Activities.MY_RETWEET_ID).sql
        updateWhereArgs = arrayOf(statusId)
    }
    for (uri in ACTIVITIES_URIS) {
        delete(uri, deleteWhere, deleteWhereArgs)
        updateItems(uri, Activities.COLUMNS, updateWhere, updateWhereArgs,
                ParcelableActivity::class.java) { activity ->
            activity.my_retweet_id = null
            if (statusId == activity.id || statusId == activity.retweet_id ||
                    statusId == activity.my_retweet_id) {
                activity.my_retweet_id = null
                if (result != null) {
                    activity.reply_count = result.reply_count
                    activity.retweet_count = result.retweet_count - 1
                    activity.favorite_count = result.favorite_count
                }
            }
            return@updateItems activity
        }
    }
}

fun <T : ParcelableStatus> ContentResolver.updateStatusInfo(uris: Array<Uri>, columns: Array<String>?,
        accountKey: UserKey, statusId: String, cls: Class<T>, action: (T) -> T) {
    val activityWhere = Expression.and(
            Expression.equalsArgs(Activities.ACCOUNT_KEY),
            Expression.or(
                    Expression.equalsArgs(Activities.ID),
                    Expression.equalsArgs(Activities.RETWEET_ID)
            )
    ).sql
    val activityWhereArgs = arrayOf(accountKey.toString(), statusId, statusId)
    for (uri in uris) {
        updateItems(uri, columns, activityWhere, activityWhereArgs, cls, action)
    }
}

@WorkerThread
fun <T> ContentResolver.updateItems(uri: Uri, columns: Array<String>?, where: String?,
        whereArgs: Array<String>?, cls: Class<T>, action: (T) -> T) {
    val values = LongSparseArray<ContentValues>()

    queryReference(uri, columns, where, whereArgs, null)?.use { (c) ->
        val ci = ObjectCursor.indicesFrom(c, cls)
        val vc = ObjectCursor.valuesCreatorFrom(cls)
        c.moveToFirst()
        try {
            while (!c.isAfterLast) {
                val item = action(ci.newObject(c))
                values.put(c.getLong(ci[BaseColumns._ID]), vc.create(item))
                c.moveToNext()
            }

        } catch (e: IOException) {
            return
        }
    }
    for (i in 0 until values.size()) {
        val updateWhere = Expression.equals(BaseColumns._ID, values.keyAt(i)).sql
        update(uri, values.valueAt(i), updateWhere, null)
    }
}

fun ContentResolver.getUnreadMessagesEntriesCursorReference(projection: Array<Column>,
        accountKeys: Array<UserKey>, extraWhere: Expression? = null,
        extraWhereArgs: Array<String>? = null, extraHaving: Expression? = null,
        extraHavingArgs: Array<String>? = null): CursorReference<Cursor>? {
    val qb = SQLQueryBuilder.select(Columns(*projection))
    qb.from(Table(Conversations.TABLE_NAME))
    qb.join(Join(false, Join.Operation.LEFT_OUTER, Table(Messages.TABLE_NAME),
            Expression.and(
                    Expression.equals(
                            Column(Table(Conversations.TABLE_NAME), Conversations.CONVERSATION_ID),
                            Column(Table(Messages.TABLE_NAME), Messages.CONVERSATION_ID)
                    ),
                    Expression.equals(
                            Column(Table(Conversations.TABLE_NAME), Conversations.ACCOUNT_KEY),
                            Column(Table(Messages.TABLE_NAME), Messages.ACCOUNT_KEY)
                    )
            )
    ))
    val whereConditions = arrayOf(
            Expression.inArgs(Column(Table(Conversations.TABLE_NAME), Conversations.ACCOUNT_KEY),
                    accountKeys.size),
            Expression.notEqualsArgs(Column(Table(Conversations.TABLE_NAME), Conversations.IS_OUTGOING)),
            Expression.lesserThan(Column(Table(Conversations.TABLE_NAME), Conversations.LAST_READ_TIMESTAMP),
                    Column(Table(Conversations.TABLE_NAME), Conversations.LOCAL_TIMESTAMP))
    )
    if (extraWhere != null) {
        qb.where(Expression.and(*(whereConditions + extraWhere)))
    } else {
        qb.where(Expression.and(*whereConditions))
    }
    qb.groupBy(Column(Table(Messages.TABLE_NAME), Messages.CONVERSATION_ID))
    if (extraHaving != null) {
        qb.having(extraHaving)
    }
    qb.orderBy(OrderBy(arrayOf(Column(Table(Conversations.TABLE_NAME), Conversations.LOCAL_TIMESTAMP),
            Column(Table(Conversations.TABLE_NAME), Conversations.SORT_ID)), booleanArrayOf(false, false)))

    var selectionArgs = accountKeys.toStringArray() + "1"
    if (extraWhereArgs != null) {
        selectionArgs += extraWhereArgs
    }
    if (extraHavingArgs != null) {
        selectionArgs += extraHavingArgs
    }
    return rawQueryReference(qb.buildSQL(), selectionArgs)
}
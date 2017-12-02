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

package org.mariotaku.twidere.extension

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import android.support.annotation.WorkerThread
import android.support.v4.util.LongSparseArray
import org.mariotaku.ktextension.map
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.RawItemArray
import org.mariotaku.sqliteqb.library.SQLFunctions
import org.mariotaku.twidere.TwidereConstants.QUERY_PARAM_LIMIT
import org.mariotaku.twidere.model.CursorReference
import org.mariotaku.twidere.util.TwidereQueryBuilder
import org.mariotaku.twidere.util.content.ContentResolverUtils.MAX_BULK_COUNT
import java.io.FileNotFoundException

fun ContentResolver.query(uri: Uri, projection: Array<String>? = null,
        selection: String? = null, selectionArgs: Array<String>? = null, sortOrder: String? = null,
        limit: String? = null): Cursor? {
    return if (limit != null) {
        query(uri.buildUpon().appendQueryParameter(QUERY_PARAM_LIMIT, limit.toString()).build(),
                projection, selection, selectionArgs, sortOrder)
    } else {
        query(uri, projection, selection, selectionArgs, sortOrder)
    }
}

fun ContentResolver.queryReference(uri: Uri, projection: Array<String>? = null,
        selection: String? = null, selectionArgs: Array<String>? = null, sortOrder: String? = null,
        limit: String? = null): CursorReference<Cursor>? {
    return CursorReference.get(query(uri, projection, selection, selectionArgs, sortOrder, limit))
}

@SuppressLint("Recycle")
fun ContentResolver.rawQuery(sql: String, selectionArgs: Array<String>?, notifyUri: Uri? = null): Cursor? {
    val rawUri = TwidereQueryBuilder.rawQuery(sql, notifyUri)
    return query(rawUri, null, null, selectionArgs, null)
}

@SuppressLint("Recycle")
fun ContentResolver.rawQueryReference(sql: String, selectionArgs: Array<String>?, notifyUri: Uri? = null): CursorReference<Cursor>? {
    val rawUri = TwidereQueryBuilder.rawQuery(sql, notifyUri)
    return queryReference(rawUri, null, null, selectionArgs, null)
}


fun <T> ContentResolver.queryOne(uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String? = null, cls: Class<T>): T? {
    return queryReference(uri, projection, selection, selectionArgs, sortOrder, "1")?.use { (cur) ->
        if (!cur.moveToFirst()) return@use null
        val indices = ObjectCursor.indicesFrom(cur, cls)
        return@use indices.newObject(cur)
    }
}

fun <T> ContentResolver.queryAll(uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String? = null, limit: String? = null,
        cls: Class<T>): List<T>? {
    return queryReference(uri, projection, selection, selectionArgs, sortOrder, limit)?.use { (cur) ->
        return@use cur.map(ObjectCursor.indicesFrom(cur, cls))
    }
}

fun ContentResolver.queryCount(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
    val projection = arrayOf(SQLFunctions.COUNT())
    return queryReference(uri, projection, selection, selectionArgs, null)?.use { (cur) ->
        if (cur.moveToFirst()) {
            return@use cur.getInt(0)
        }
        return@use -1
    } ?: -1
}

fun ContentResolver.queryLong(uri: Uri, field: String, selection: String?, selectionArgs: Array<String>?, def: Long = -1): Long {
    val projection = arrayOf(field)
    return queryReference(uri, projection, selection, selectionArgs, null, "1")?.use { (cur) ->
        if (cur.moveToFirst()) {
            return@use cur.getLong(0)
        }
        return@use def
    } ?: def
}

fun <T : Any> ContentResolver.insert(uri: Uri, obj: T, cls: Class<T> = obj.javaClass): Uri? {
    return this.insert(uri, ObjectCursor.valuesCreatorFrom(cls).create(obj))
}

fun <T : Any> ContentResolver.bulkInsert(uri: Uri, collection: Collection<T>, cls: Class<T>): Int {
    if (collection.isEmpty()) return 0
    val creator = ObjectCursor.valuesCreatorFrom(cls)
    var rowsInserted = 0
    val block = ArrayList<ContentValues>(MAX_BULK_COUNT)
    collection.forEachIndexed { index, item ->
        block.add(creator.create(item))
        if (index == collection.size - 1 || block.size >= MAX_BULK_COUNT) {
            rowsInserted += bulkInsert(uri, block.toTypedArray())
            block.clear()
        }
    }
    return rowsInserted
}


fun ContentResolver.blockBulkInsert(uri: Uri, collection: Collection<ContentValues>): Int {
    if (collection.isEmpty()) return 0
    var rowsInserted = 0
    val block = ArrayList<ContentValues>(MAX_BULK_COUNT)
    collection.forEachIndexed { index, item ->
        block += item
        if (index == collection.size - 1 || block.size >= MAX_BULK_COUNT) {
            rowsInserted += bulkInsert(uri, block.toTypedArray())
            block.clear()
        }
    }
    return rowsInserted
}

@WorkerThread
fun <T> ContentResolver.update(uri: Uri, columns: Array<String>?, where: String?,
        whereArgs: Array<String>?, cls: Class<T>, action: (T) -> T): Int {
    val values = LongSparseArray<ContentValues>()

    queryReference(uri, columns, where, whereArgs, null)?.use { (c) ->
        val ci = ObjectCursor.indicesFrom(c, cls)
        val vc = ObjectCursor.valuesCreatorFrom(cls)
        c.moveToFirst()
        while (!c.isAfterLast) {
            val item = action(ci.newObject(c))
            values.put(c.getLong(ci[BaseColumns._ID]), vc.create(item))
            c.moveToNext()
        }
    }
    var numbersUpdated = 0
    for (i in 0 until values.size()) {
        val updateWhere = Expression.equals(BaseColumns._ID, values.keyAt(i)).sql
        numbersUpdated += update(uri, values.valueAt(i), updateWhere, null)
    }
    return numbersUpdated
}

fun ContentResolver.copyStream(src: Uri, dest: Uri) {
    openOutputStream(dest)?.use { os ->
        openInputStream(src)?.use { st ->
            st.copyTo(os)
        } ?: throw FileNotFoundException("Unable to open $src")
    } ?: throw FileNotFoundException("Unable to open $dest")
}

fun ContentResolver.delete(uri: Uri, rowId: Long): Int {
    val where = Expression.equals(BaseColumns._ID, rowId).sql
    return delete(uri, where, null)
}

fun ContentResolver.delete(uri: Uri, rowIds: LongArray): Int {
    if (rowIds.isEmpty()) return 0
    val idColumn = Columns.Column(BaseColumns._ID)
    return (rowIds.indices step MAX_BULK_COUNT).sumBy { i ->
        val bulkIds = rowIds.sliceArray(i..(i + MAX_BULK_COUNT - 1).coerceAtMost(rowIds.lastIndex))
        return@sumBy delete(uri, Expression.`in`(idColumn, RawItemArray(bulkIds)).sql, null)
    }
}

fun ContentResolver.bulkDelete(uri: Uri, column: String, values: Array<String>?): Int {
    if (values == null || values.isEmpty()) return 0
    return (values.indices step MAX_BULK_COUNT).sumBy { i ->
        val bulk = values.sliceArray(i..(i + MAX_BULK_COUNT - 1).coerceAtMost(values.lastIndex))
        return@sumBy delete(uri, Expression.inArgs(column, bulk.size).sql, bulk)
    }
}
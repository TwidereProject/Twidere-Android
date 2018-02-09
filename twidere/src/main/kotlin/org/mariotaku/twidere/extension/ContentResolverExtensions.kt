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
import android.database.Cursor
import android.net.Uri
import org.mariotaku.ktextension.map
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.sqliteqb.library.SQLFunctions
import org.mariotaku.twidere.TwidereConstants.QUERY_PARAM_LIMIT
import org.mariotaku.twidere.model.CursorReference
import org.mariotaku.twidere.util.TwidereQueryBuilder
import org.mariotaku.twidere.util.content.ContentResolverUtils

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
        selectionArgs: Array<String>?, sortOrder: String? = null, cls: Class<T>): List<T> {
    return queryReference(uri, projection, selection, selectionArgs, sortOrder)?.use { (cur) ->
        return@use cur.map(ObjectCursor.indicesFrom(cur, cls))
    } ?: emptyList()
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
    val creator = ObjectCursor.valuesCreatorFrom(cls)
    return ContentResolverUtils.bulkInsert(this, uri, collection.map(creator::create))
}
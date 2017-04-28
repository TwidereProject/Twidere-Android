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
import org.mariotaku.ktextension.useCursor
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.twidere.util.TwidereQueryBuilder

@SuppressLint("Recycle")
fun ContentResolver.rawQuery(sql: String, selectionArgs: Array<String>?, notifyUri: Uri? = null): Cursor? {
    val rawUri = TwidereQueryBuilder.rawQuery(sql, notifyUri)
    return query(rawUri, null, null, selectionArgs, null)
}

fun <T> ContentResolver.queryOne(uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String? = null, cls: Class<T>): T? {
    val cursor = this.query(uri, projection, selection, selectionArgs, sortOrder)
    if (!cursor.moveToFirst()) return null
    return cursor.useCursor {
        val indices = ObjectCursor.indicesFrom(cursor, cls)
        return@useCursor indices.newObject(cursor)
    }
}
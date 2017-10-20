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

package org.mariotaku.twidere.data.source

import android.arch.paging.TiledDataSource
import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import org.mariotaku.ktextension.weak
import org.mariotaku.twidere.extension.queryAll
import org.mariotaku.twidere.extension.queryCount

class CursorObjectTiledDataSource<T>(
        private val resolver: ContentResolver,
        val uri: Uri,
        val projection: Array<String>? = null,
        val selection: String? = null,
        val selectionArgs: Array<String>? = null,
        val sortOrder: String? = null,
        val cls: Class<T>
) : TiledDataSource<T>() {

    init {
        val weakThis = weak()
        val observer = object : ContentObserver(MainHandler) {
            override fun onChange(selfChange: Boolean) {
                weakThis.get()?.invalidate()
            }
        }
        addInvalidatedCallback cb@ {
            resolver.unregisterContentObserver(observer)
        }
        resolver.registerContentObserver(uri, false, observer)
    }

    override fun countItems() = resolver.queryCount(uri, selection, selectionArgs)

    override fun loadRange(startPosition: Int, count: Int): List<T> {
        return resolver.queryAll(uri, projection, selection, selectionArgs, sortOrder,
                "$startPosition,$count", cls)
    }

    private object MainHandler : Handler(Looper.getMainLooper())

}

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

import android.arch.paging.LivePagedListProvider
import android.content.ContentResolver
import android.net.Uri

/**
 * Created by mariotaku on 2017/10/13.
 */

class CursorObjectLivePagedListProvider<T>(
        private val resolver: ContentResolver,
        val uri: Uri,
        val projection: Array<String>? = null,
        val selection: String? = null,
        val selectionArgs: Array<String>? = null,
        val sortOrder: String? = null,
        val cls: Class<T>
) : LivePagedListProvider<Int, T>() {

    override fun createDataSource() = CursorObjectTiledDataSource(resolver, uri, projection,
            selection, selectionArgs, sortOrder, cls)

}

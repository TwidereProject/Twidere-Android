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

package org.mariotaku.twidere.data

import android.content.ContentResolver
import android.net.Uri
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.twidere.extension.queryAll

/**
 * Created by mariotaku on 2017/10/9.
 */

class ObjectCursorLiveData<T>(
        val resolver: ContentResolver,
        val uri: Uri,
        val projection: Array<String>? = null,
        val selection: String? = null,
        val selectionArgs: Array<String>? = null,
        val orderBy: String? = null,
        val cls: Class<out T>
) : ReloadableLiveData<List<T>?>() {

    override fun onLoadData(callback: (List<T>?) -> Unit) {
        task {
            return@task resolver.queryAll(uri, projection, selection, selectionArgs, cls = cls)
        }.successUi { data ->
            callback(data)
        }
    }

}

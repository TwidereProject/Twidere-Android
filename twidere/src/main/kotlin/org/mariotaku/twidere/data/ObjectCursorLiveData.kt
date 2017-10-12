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
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.ktextension.weak
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.twidere.extension.queryReference

class ObjectCursorLiveData<T>(
        val resolver: ContentResolver,
        val uri: Uri,
        val projection: Array<String>? = null,
        val selection: String? = null,
        val selectionArgs: Array<String>? = null,
        val orderBy: String? = null,
        val cls: Class<out T>
) : ReloadableLiveData<List<T>?>() {

    private val reloadObserver = object : ContentObserver(MainThreadHandler) {
        override fun onChange(selfChange: Boolean) {
            if (hasActiveObservers()) {
                loadData()
            }
        }
    }

    override fun onLoadData(callback: (List<T>?) -> Unit) {
        val weakThis = weak()
        task {
            val (c) = resolver.queryReference(uri, projection, selection, selectionArgs) ?:
                    throw NullPointerException()
            c.registerContentObserver(reloadObserver)
            val i = ObjectCursor.indicesFrom(c, cls)
            return@task ObjectCursor(c, i)
        }.successUi { data ->
            val ld = weakThis.get()
            val oldValue = ld?.value
            if (oldValue is ObjectCursor<*>) {
                oldValue.close()
            }
            callback(data)
        }
    }

    override fun onInactive() {
        val oldValue = this.value
        if (oldValue is ObjectCursor<*>) {
            oldValue.cursor.unregisterContentObserver(reloadObserver)
            oldValue.close()
        }
        value = null
    }

    object MainThreadHandler : Handler(Looper.getMainLooper())
}

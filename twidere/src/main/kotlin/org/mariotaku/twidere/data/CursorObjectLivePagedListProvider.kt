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

import android.arch.paging.DataSource
import android.arch.paging.TiledDataSource
import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.support.annotation.WorkerThread
import org.mariotaku.ktextension.toWeak
import org.mariotaku.twidere.data.processor.DataSourceItemProcessor
import org.mariotaku.twidere.extension.queryAll
import org.mariotaku.twidere.extension.queryCount
import org.mariotaku.twidere.util.DebugLog

class CursorObjectLivePagedListProvider<T : Any>(
        private val resolver: ContentResolver,
        val uri: Uri,
        val projection: Array<String>? = null,
        val selection: String? = null,
        val selectionArgs: Array<String>? = null,
        val sortOrder: String? = null,
        val cls: Class<T>,
        val processor: DataSourceItemProcessor<T>? = null
) : ExtendedPagedListProvider<Int, T>() {

    @WorkerThread
    override fun onCreateDataSource(): DataSource<Int, T> {
        return CursorObjectTiledDataSource(resolver, uri, projection,
                selection, selectionArgs, sortOrder, cls, processor)
    }

    private class CursorObjectTiledDataSource<T : Any>(
            val resolver: ContentResolver,
            val uri: Uri,
            val projection: Array<String>? = null,
            val selection: String? = null,
            val selectionArgs: Array<String>? = null,
            val sortOrder: String? = null,
            val cls: Class<T>,
            val processor: DataSourceItemProcessor<T>?
    ) : TiledDataSource<T>() {

        private val lazyCount: Int by lazy { resolver.queryCount(uri, selection, selectionArgs) }
        private val filterStates: BooleanArray by lazy { BooleanArray(lazyCount) }

        init {
            val weakThis = toWeak()
            val observer: ContentObserver = object : ContentObserver(MainHandler) {
                override fun onChange(selfChange: Boolean) {
                    resolver.unregisterContentObserver(this)
                    weakThis.get()?.invalidate()
                }
            }
            resolver.registerContentObserver(uri, false, observer)
            processor?.init(resolver)
        }

        override fun countItems() = lazyCount

        override fun loadRange(startPosition: Int, count: Int): List<T> {
            if (processor == null) {
                val start = System.currentTimeMillis()
                val result = resolver.queryAll(uri, projection, selection, selectionArgs, sortOrder,
                        "$startPosition,$count", cls)
                DebugLog.d(msg = "Querying $uri took ${System.currentTimeMillis() - start} ms.")
                return result.orEmpty()
            }
            val result = ArrayList<T>()
            var offset = filterStates.actualIndex(startPosition, startPosition)
            var limit = count
            do {
                val start = System.currentTimeMillis()
                val list = resolver.queryAll(uri, projection, selection, selectionArgs, sortOrder,
                        "$offset,$limit", cls).orEmpty()
                DebugLog.d(msg = "Querying $uri took ${System.currentTimeMillis() - start} ms.")
                val reachedEnd = list.size < count
                list.mapIndexedNotNullTo(result) lambda@ { index, item ->
                    val processed = processor.process(item)
                    filterStates[offset + index] = processed != null
                    return@lambda processed
                }
                if (reachedEnd) break
                offset += limit
                limit = count - result.size
            } while (result.size < count)
            return result
        }

        override fun invalidate() {
            processor?.invalidate()
            super.invalidate()
        }

        private fun BooleanArray.actualIndex(index: Int, def: Int): Int {
            var actual = -1
            forEachIndexed { i, v ->
                if (v) {
                    actual += 1
                }
                if (index == actual) return i
            }
            return def
        }

        private object MainHandler : Handler(Looper.getMainLooper())

    }

}

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
import android.arch.paging.PositionalDataSource
import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.support.annotation.WorkerThread
import org.mariotaku.ktextension.weak
import org.mariotaku.twidere.data.processor.DataSourceItemProcessor
import org.mariotaku.twidere.extension.queryAll
import org.mariotaku.twidere.extension.queryCount
import org.mariotaku.twidere.util.DebugLog

class CursorObjectDataSourceFactory<T : Any>(
        private val resolver: ContentResolver,
        val uri: Uri,
        val projection: Array<String>? = null,
        val selection: String? = null,
        val selectionArgs: Array<String>? = null,
        val sortOrder: String? = null,
        val cls: Class<T>,
        val processor: DataSourceItemProcessor<T>? = null
) : DataSource.Factory<Int, T>() {

    private val observerInfo = mutableListOf<ObserverInfo>()

    @WorkerThread
    override fun create(): DataSource<Int, T> {
        return CursorObjectDataSource(resolver, uri, projection, selection, selectionArgs,
                sortOrder, cls, processor, observerInfo)
    }

    fun registerContentObserver(uri: Uri, notifyForDescendants: Boolean) {
        observerInfo.add(ObserverInfo(uri, notifyForDescendants))
    }

    private data class ObserverInfo(val uri: Uri, val notifyForDescendants: Boolean)

    private class CursorObjectDataSource<T : Any>(
            val resolver: ContentResolver,
            val uri: Uri,
            val projection: Array<String>? = null,
            val selection: String? = null,
            val selectionArgs: Array<String>? = null,
            val sortOrder: String? = null,
            val cls: Class<T>,
            val processor: DataSourceItemProcessor<T>?,
            val observerInfo: List<ObserverInfo>
    ) : PositionalDataSource<T>() {

        private val totalCount: Int by lazy { resolver.queryCount(uri, selection, selectionArgs) }
        private val filterStates: BooleanArray by lazy { BooleanArray(totalCount) }

        init {
            val weakThis by weak(this)
            val observer: ContentObserver = object : ContentObserver(MainHandler) {
                override fun onChange(selfChange: Boolean) {
                    resolver.unregisterContentObserver(this)
                    observerInfo.forEach {
                        resolver.unregisterContentObserver(this)
                    }
                    weakThis?.invalidate()
                }
            }
            resolver.registerContentObserver(uri, false, observer)
            observerInfo.forEach {
                resolver.registerContentObserver(it.uri, it.notifyForDescendants, observer)
            }
            processor?.init(resolver)
        }

        override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<T>) {
            val totalCount = this.totalCount
            val firstLoadPosition = computeInitialLoadPosition(params, totalCount)
            val firstLoadSize = computeInitialLoadSize(params, firstLoadPosition, totalCount)
            val valid = loadRange(firstLoadPosition, firstLoadSize) { data ->
                callback.onResult(data, firstLoadPosition, totalCount)
            }
            if (!valid) {
                invalidate()
            }
        }

        override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<T>) {
            val valid = loadRange(params.startPosition, params.loadSize, callback::onResult)
            if (!valid) {
                invalidate()
            }
        }

        override fun invalidate() {
            processor?.invalidate()
            super.invalidate()
        }

        private fun loadRange(startPosition: Int, count: Int, callback: (List<T>) -> Unit): Boolean {
            if (processor == null) {
                val start = System.currentTimeMillis()
                val result = resolver.queryAll(uri, projection, selection, selectionArgs, sortOrder,
                        "$startPosition,$count", cls) ?: return false
                DebugLog.d(msg = "Querying $uri:$startPosition,$count took ${System.currentTimeMillis() - start} ms.")
                callback(result)
                return true
            }
            val result = ArrayList<T>()
            var offset = filterStates.actualIndex(startPosition, startPosition)
            var limit = count
            do {
                val start = System.currentTimeMillis()
                val list = resolver.queryAll(uri, projection, selection, selectionArgs, sortOrder,
                        "$offset,$limit", cls) ?: return false
                DebugLog.d(msg = "Querying $uri:$startPosition,$count took ${System.currentTimeMillis() - start} ms.")
                val reachedEnd = list.size < count
                list.mapIndexedNotNullTo(result) lambda@{ index, item ->
                    val processed = processor.process(item)
                    filterStates[offset + index] = processed != null
                    return@lambda processed
                }
                if (reachedEnd) break
                offset += limit
                limit = count - result.size
            } while (result.size < count)
            callback(result)
            return true
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

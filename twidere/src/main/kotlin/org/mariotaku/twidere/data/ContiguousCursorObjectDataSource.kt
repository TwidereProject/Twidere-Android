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

import android.arch.paging.PageKeyedDataSource
import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import org.mariotaku.ktextension.weak
import org.mariotaku.twidere.data.processor.DataSourceItemProcessor
import org.mariotaku.twidere.extension.queryAll
import org.mariotaku.twidere.util.DebugLog

internal class ContiguousCursorObjectDataSource<T : Any>(
        val resolver: ContentResolver,
        val uri: Uri,
        val projection: Array<String>? = null,
        val selection: String? = null,
        val selectionArgs: Array<String>? = null,
        val sortOrder: String? = null,
        val cls: Class<T>,
        val processor: DataSourceItemProcessor<T>?
) : PageKeyedDataSource<Int, T>() {

    init {
        val weakThis by weak(this)
        val observer: ContentObserver = object : ContentObserver(MainHandler) {
            override fun onChange(selfChange: Boolean) {
                resolver.unregisterContentObserver(this)
                weakThis?.invalidate()
            }
        }
        resolver.registerContentObserver(uri, false, observer)
        processor?.init(resolver)
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, T>) {
        val result = loadRange(0, params.requestedLoadSize)
        if (result == null) {
            invalidate()
            return
        }
        callback.onResult(result.data, null, params.requestedLoadSize)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, T>) {
        val result = loadRange(params.key, params.requestedLoadSize)
        if (result == null) {
            invalidate()
            return
        }
        if (params.key == 0) {
            callback.onResult(result.data, null)
        } else {
            callback.onResult(result.data, (params.key - params.requestedLoadSize).coerceAtLeast(0))
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, T>) {
        val result = loadRange(params.key, params.requestedLoadSize)
        if (result == null) {
            invalidate()
            return
        }
        callback.onResult(result.data, params.key + params.requestedLoadSize)
    }

    private fun loadRange(startPosition: Int, count: Int): RangeResult<T>? {
        val start = System.currentTimeMillis()
        val result = resolver.queryAll(uri, projection, selection, selectionArgs, sortOrder,
                "$startPosition,$count", cls) ?: return null
        DebugLog.d(msg = "Querying $uri:$startPosition,$count took ${System.currentTimeMillis() - start} ms.")
        if (processor != null) {
            return RangeResult(result.mapNotNull(processor::process), result.size)
        }
        return RangeResult(result, result.size)
    }

    override fun invalidate() {
        processor?.invalidate()
        super.invalidate()
    }

    private object MainHandler : Handler(Looper.getMainLooper())

    private data class RangeResult<out T>(val data: List<T>, val unfilteredCount: Int)
}


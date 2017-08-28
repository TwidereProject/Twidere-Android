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

package org.mariotaku.twidere.util.cache

import com.bumptech.glide.disklrucache.DiskLruCache
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.util.DebugLog
import org.mariotaku.twidere.util.JsonSerializer
import java.io.File
import java.io.IOException

/**
 * Created by mariotaku on 2017/3/1.
 */

class JsonCache(cacheDir: File) {

    private val cache: DiskLruCache? by lazy {
        try {
            return@lazy DiskLruCache.open(cacheDir, BuildConfig.VERSION_CODE, 1, 100 * 1048576)
        } catch (e: IOException) {
            DebugLog.w(tr = e)
            return@lazy null
        }
    }

    fun <T> getList(key: String, cls: Class<T>): List<T>? {
        val value = cache?.get(key) ?: return null
        return try {
            value.getFile(0)?.inputStream()?.use {
                JsonSerializer.parseList(it, cls)
            }
        } catch (e: IOException) {
            DebugLog.w(tr = e)
            null
        }
    }

    fun <T> saveList(key: String, list: List<T>?, cls: Class<T>) {
        if (list == null) {
            cache?.remove(key)
            return
        }
        val editor = cache?.edit(key) ?: return
        try {
            editor.getFile(0)?.outputStream()?.use {
                JsonSerializer.serialize(list, it, cls)
            }
            editor.commit()
        } catch (e: IOException) {
            DebugLog.w(tr = e)
        } finally {
            editor.abortUnlessCommitted()
        }
    }
}

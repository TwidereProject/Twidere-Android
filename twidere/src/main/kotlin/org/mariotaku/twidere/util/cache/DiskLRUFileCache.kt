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

import android.net.Uri
import com.bumptech.glide.disklrucache.DiskLruCache
import okio.ByteString.Companion.encode
import org.mariotaku.mediaviewer.library.FileCache
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.provider.CacheProvider
import java.io.File
import java.io.IOException
import java.io.InputStream

class DiskLRUFileCache(val cacheDir: File) : FileCache {

    private val cache = try {
        DiskLruCache.open(cacheDir, BuildConfig.VERSION_CODE, 2, 100 * 1048576)
    } catch (e: IOException) {
        null
    }

    override fun fromUri(uri: Uri): String {
        return CacheProvider.getCacheKey(uri)
    }

    override fun toUri(key: String): Uri {
        return CacheProvider.getCacheUri(key, null)
    }

    override fun get(key: String): File? {
        return cache?.get(hash(key))?.getFile(0)
    }

    override fun getExtra(key: String): ByteArray? {
        return cache?.get(hash(key))?.getFile(1)?.readBytes()
    }

    override fun remove(key: String) {
        cache?.remove(hash(key))
    }

    @Throws(IOException::class)
    override fun save(key: String, stream: InputStream, extra: ByteArray?, listener: FileCache.CopyListener?) {
        val hashedKey = hash(key)
        val editor = cache?.edit(hashedKey) ?: throw IOException("Unable to open cache for $key")

        try {
            editor.getFile(0).outputStream().use {
                var bytesCopied = 0
                val buffer = ByteArray(8192)
                var bytes = stream.read(buffer)
                while (bytes >= 0) {
                    it.write(buffer, 0, bytes)
                    bytesCopied += bytes
                    if (listener != null && !listener.onCopied(bytesCopied)) {
                        return
                    }
                    bytes = stream.read(buffer)
                }
            }
            if (extra != null) {
                editor.getFile(1).writeBytes(extra)
            }
            editor.commit()
        } finally {
            editor.abortUnlessCommitted()
        }
    }

    private fun hash(key: String): String {
        return key.encode(Charsets.UTF_8).sha256().hex()
    }
}
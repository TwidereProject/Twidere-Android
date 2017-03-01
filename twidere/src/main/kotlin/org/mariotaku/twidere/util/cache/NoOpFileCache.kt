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
import org.mariotaku.mediaviewer.library.FileCache
import org.mariotaku.twidere.provider.CacheProvider
import java.io.File
import java.io.InputStream

class NoOpFileCache : FileCache {
    override fun fromUri(uri: Uri): String {
        return CacheProvider.getCacheKey(uri)
    }

    override fun toUri(key: String): Uri {
        return CacheProvider.getCacheUri(key, null)
    }

    override fun get(key: String): File? {
        return null
    }

    override fun remove(key: String) {
    }

    override fun save(key: String, stream: InputStream, metadata: ByteArray?, listener: FileCache.CopyListener?) {
    }

}
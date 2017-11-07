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

package org.mariotaku.twidere.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import org.mariotaku.ktextension.weak
import org.mariotaku.twidere.extension.copyStream
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableMediaUpdate
import org.mariotaku.twidere.model.util.ParcelableMediaUtils
import java.io.File
import java.io.IOException

/**
 * Created by mariotaku on 2017/11/6.
 */
fun Context.obtainMedia(sources: Array<Uri>, types: IntArray?, copySrc: Boolean = false,
        deleteSrc: Boolean = false): Promise<List<ParcelableMediaUpdate>, Exception> {
    val weakThis by weak(this)
    return task {
        val context = weakThis ?: throw InterruptedException()
        val resolver = context.contentResolver
        return@task sources.mapIndexedNotNull map@ { index, source ->
            try {
                val mimeTypeMap = MimeTypeMap.getSingleton()
                val sourceMimeType = resolver.getType(source) ?: mimeTypeMap.getMimeTypeFromExtension(
                        source.lastPathSegment.substringAfterLast('.', "tmp"))
                val mediaType = types?.get(index) ?: sourceMimeType?.let {
                    return@let ParcelableMediaUtils.inferMediaType(it)
                } ?: ParcelableMedia.Type.IMAGE
                val extension = sourceMimeType?.let { mimeType ->
                    mimeTypeMap.getExtensionFromMimeType(mimeType)
                } ?: "tmp"
                if (copySrc) {
                    val dest = context.createTempImageUri(index, extension)
                    resolver.copyStream(source, dest)
                    if (deleteSrc) {
                        Utils.deleteMedia(context, source)
                    }
                    // File is copied locally, so delete on success
                    return@map ParcelableMediaUpdate(dest.toString(), mediaType).apply {
                        delete_on_success = true
                    }
                } else {
                    return@map ParcelableMediaUpdate(source.toString(), mediaType).apply {
                        delete_on_success = true
                    }
                }
            } catch (e: IOException) {
                DebugLog.w(tr = e)
                return@map null
            }
        }
    }
}

private fun Context.createTempImageUri(extraNum: Int, ext: String): Uri {
    val file = File(cacheDir, "tmp_media_${System.currentTimeMillis()}_$extraNum.$ext")
    return Uri.fromFile(file)
}
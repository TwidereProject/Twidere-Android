/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.task

import android.app.Activity
import android.content.ContentValues
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.CacheFileType
import org.mariotaku.twidere.provider.CacheProvider
import java.io.File

/**
 * Created by mariotaku on 15/12/28.
 */
class SaveMediaToGalleryTask(
        activity: Activity,
        private val fileInfo: FileInfo,
        destination: File
) : ProgressSaveFileTask(activity, destination, fileInfo) {

    override fun onFileSaved(savedFile: File, mimeType: String?) {
        val context = context ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val type = (fileInfo as? CacheProvider.CacheFileTypeSupport)?.cacheFileType
            val path = when (type) {
                CacheFileType.VIDEO -> {
                    Environment.DIRECTORY_MOVIES
                }
                CacheFileType.IMAGE -> {
                    Environment.DIRECTORY_PICTURES
                }
                else -> {
                    Environment.DIRECTORY_DOWNLOADS
                }
            }
            val url = when (type) {
                CacheFileType.VIDEO -> {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                CacheFileType.IMAGE -> {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                else -> {
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI
                }
            }
            val contentValues = ContentValues()
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileInfo.fileName)
            contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, fileInfo.mimeType)
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "$path/Twidere")
            context.contentResolver.insert(url, contentValues)?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use {
                    savedFile.inputStream().use { fileInputStream ->
                        fileInputStream.copyTo(it)
                    }
                }
                MediaScannerConnection.scanFile(context, arrayOf(uri.path),
                        arrayOf(fileInfo.mimeType), null)
            }
            savedFile.delete()
        } else {
            MediaScannerConnection.scanFile(context, arrayOf(savedFile.path),
                    arrayOf(fileInfo.mimeType), null)
        }
        Toast.makeText(context, R.string.message_toast_saved_to_gallery, Toast.LENGTH_SHORT).show()
    }

    override fun onFileSaveFailed() {
        val context = context ?: return
        Toast.makeText(context, R.string.message_toast_error_occurred, Toast.LENGTH_SHORT).show()
    }

}

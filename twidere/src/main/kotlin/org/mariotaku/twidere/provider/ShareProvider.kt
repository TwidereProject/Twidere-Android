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

package org.mariotaku.twidere.provider

import android.Manifest
import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore.MediaColumns
import android.support.v4.content.ContextCompat

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class ShareProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
            selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val file = try {
            getFile(uri)
        } catch (e: IOException) {
            return null
        }
        val realProjection = projection ?: defaultColumns
        val cursor = MatrixCursor(realProjection, 1)
        val values = arrayOfNulls<Any>(realProjection.size)
        writeValue(realProjection, values, MediaColumns.DATA, file.absolutePath)
        cursor.addRow(values)
        return cursor
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r") throw IllegalArgumentException()
        val file = getFile(uri)
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0

    private fun writeValue(columns: Array<String>, values: Array<Any?>, column: String, value: Any) {
        val idx = columns.indexOf(column)
        if (idx >= 0) {
            values[idx] = value
        }
    }

    @Throws(FileNotFoundException::class)
    private fun getFile(uri: Uri): File {
        val lastPathSegment = uri.lastPathSegment ?: throw FileNotFoundException(uri.toString())
        return File(getFilesDir(context), lastPathSegment)
    }

    companion object {
        private val defaultColumns = arrayOf(MediaColumns.DATA, MediaColumns.DISPLAY_NAME, MediaColumns.SIZE, MediaColumns.MIME_TYPE)

        fun getFilesDir(context: Context?): File? {
            var cacheDir: File? = context!!.cacheDir
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                val externalCacheDir = context.externalCacheDir
                if (externalCacheDir != null && externalCacheDir.canWrite()) {
                    cacheDir = externalCacheDir
                }
            }
            return if (cacheDir == null) null else File(cacheDir, "shared_files")
        }

        fun getUriForFile(context: Context, authority: String, file: File): Uri? {
            val filesDir = getFilesDir(context) ?: return null
            return if (filesDir != file.parentFile) null else Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(authority).appendPath(file.name).build()
        }

        fun clearTempFiles(context: Context): Boolean {
            val externalCacheDir = context.externalCacheDir ?: return false
            val files = externalCacheDir.listFiles()
            for (file in files) {
                if (file.isFile) {

                    file.delete()
                }
            }
            return true
        }
    }
}

/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.content.Context
import android.os.AsyncTask
import android.text.TextUtils.isEmpty
import android.util.Log
import android.webkit.MimeTypeMap
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.util.*

abstract class SaveFileTask(
        context: Context,
        private val destination: File,
        private val fileInfo: FileInfo
) : AsyncTask<Any, Any, SaveFileTask.SaveFileResult>() {

    private val contextRef = WeakReference(context)

    override fun doInBackground(vararg args: Any): SaveFileResult? {
        return saveFile(fileInfo, destination, requiresValidExtension)
    }

    override fun onCancelled() {
        dismissProgress()
    }

    override fun onPreExecute() {
        showProgress()
    }

    override fun onPostExecute(result: SaveFileResult?) {
        dismissProgress()
        if (result?.savedFile != null) {
            onFileSaved(result.savedFile!!, result.mimeType)
        } else {
            onFileSaveFailed()
        }
    }

    protected abstract fun onFileSaved(savedFile: File, mimeType: String?)

    protected abstract fun onFileSaveFailed()

    protected abstract fun showProgress()

    protected abstract fun dismissProgress()

    open val requiresValidExtension: Boolean = false

    protected val context: Context?
        get() = contextRef.get()

    interface FileInfo : Closeable {
        val fileName: String?

        val mimeType: String?

        val fileExtension: String? get() {
            val typeLowered = mimeType?.toLowerCase(Locale.US) ?: return null
            return when (typeLowered) {
            // Hack for fanfou image type
                "image/jpg" -> "jpg"
                else -> MimeTypeMap.getSingleton().getExtensionFromMimeType(typeLowered)
            }
        }

        val specialCharacter: Char

        fun inputStream(): InputStream
    }

    class SaveFileResult(savedFile: File, mimeType: String) {
        var savedFile: File? = null
            internal set
        var mimeType: String
            internal set

        init {
            this.savedFile = savedFile
            this.mimeType = mimeType
        }
    }

    companion object {

        fun saveFile(fileInfo: FileInfo, destinationDir: File, requiresValidExtension: Boolean) = try {
            fileInfo.use {
                var name: String = it.fileName ?: return null
                if (isEmpty(name)) return null
                if (name.length > 32) {
                    name = name.substring(0, 32)
                }
                val mimeType = it.mimeType ?: return null
                val extension = it.fileExtension
                if (requiresValidExtension && extension == null) {
                    return null
                }
                if (!destinationDir.isDirectory && !destinationDir.mkdirs()) return null
                var nameToSave = getFileNameWithExtension(name, extension,
                        it.specialCharacter, null)
                var saveFile = File(destinationDir, nameToSave)
                if (saveFile.exists()) {
                    nameToSave = getFileNameWithExtension(name, extension,
                            it.specialCharacter,
                            System.currentTimeMillis().toString())
                    saveFile = File(destinationDir, nameToSave)
                }
                saveFile.outputStream().use { output ->
                    it.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
                return@use SaveFileResult(saveFile, mimeType)
            }
        } catch (e: IOException) {
            Log.w(LOGTAG, "Failed to save file", e)
            null
        }

        internal fun getFileNameWithExtension(name: String, extension: String?,
                specialCharacter: Char, suffix: String?): String {
            val sb = StringBuilder()
            if (extension != null) {
                sb.append(name
                        .removeSuffix(extension)
                        .removeSuffix(".")
                        .removeSuffix(specialCharacter.toString())
                        .takeLastWhile { it != specialCharacter })
            } else {
                sb.append(name)
            }
            if (suffix != null) {
                sb.append(suffix)
            }
            if (extension != null) {
                sb.append('.')
                sb.append(extension)
            }
            return sb.toString()
        }
    }

}

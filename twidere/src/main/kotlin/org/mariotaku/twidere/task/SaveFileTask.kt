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
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.model.SaveFileInfo
import org.mariotaku.twidere.model.SaveFileResult
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference

abstract class SaveFileTask(
        context: Context,
        private val destination: File,
        private val fileInfo: SaveFileInfo
) : AsyncTask<Any, Any, SaveFileResult>() {

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
        if (result != null) {
            onFileSaved(result.savedFile, result.mimeType)
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

    companion object {

        fun saveFile(fileInfo: SaveFileInfo, destinationDir: File, requiresValidExtension: Boolean) = try {
            fileInfo.use {
                var name: String = it.name
                if (isEmpty(name)) return null
                if (name.length > 32) {
                    name = name.substring(0, 32)
                }
                val mimeType = it.mimeType ?: return null
                val extension = it.extension
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
                    it.stream().use { input ->
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
            var end = name.length
            if (extension != null) {
                if (name.endsWith(extension)) {
                    for (i in end - extension.length - 1 downTo 0) {
                        if (name[i] != specialCharacter) {
                            end = i + 1
                            break
                        }
                    }
                }
            }
            sb.append(name, 0, end)
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

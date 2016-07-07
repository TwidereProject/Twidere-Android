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
import android.net.Uri
import android.os.AsyncTask
import android.text.TextUtils.isEmpty
import android.util.Log
import okio.BufferedSink
import okio.Okio
import okio.Source
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.util.Utils
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference

abstract class SaveFileTask(context: Context, private val source: Uri,
                            private val destination: File, private val getMimeType: SaveFileTask.FileInfoCallback) : AsyncTask<Any, Any, SaveFileTask.SaveFileResult>() {

    private val contextRef: WeakReference<Context>

    init {
        this.contextRef = WeakReference(context)
    }

    override fun doInBackground(vararg args: Any): SaveFileResult? {
        val context = contextRef.get() ?: return null
        return saveFile(context, source, getMimeType, destination)
    }

    override fun onCancelled() {
        dismissProgress()
    }

    override fun onPreExecute() {
        showProgress()
    }

    override fun onPostExecute(result: SaveFileResult?) {
        dismissProgress()
        if (result != null && result.savedFile != null) {
            onFileSaved(result.savedFile!!, result.mimeType)
        } else {
            onFileSaveFailed()
        }
    }

    protected abstract fun onFileSaved(savedFile: File, mimeType: String?)

    protected abstract fun onFileSaveFailed()

    protected abstract fun showProgress()

    protected abstract fun dismissProgress()


    protected val context: Context
        get() = contextRef.get()

    interface FileInfoCallback {
        fun getFilename(source: Uri): String?

        fun getMimeType(source: Uri): String?

        fun getExtension(mimeType: String?): String?

        val specialCharacter: Char
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

        fun saveFile(context: Context, source: Uri,
                     fileInfoCallback: FileInfoCallback,
                     destinationDir: File): SaveFileResult? {
            val cr = context.contentResolver
            var ioSrc: Source? = null
            var sink: BufferedSink? = null
            try {
                var name: String = fileInfoCallback.getFilename(source) ?: return null
                if (isEmpty(name)) return null
                if (name.length > 32) {
                    name = name.substring(0, 32)
                }
                val mimeType = fileInfoCallback.getMimeType(source) ?: return null
                val extension = fileInfoCallback.getExtension(mimeType)
                if (!destinationDir.isDirectory && !destinationDir.mkdirs()) return null
                var nameToSave = getFileNameWithExtension(name, extension,
                        fileInfoCallback.specialCharacter, null)
                var saveFile = File(destinationDir, nameToSave)
                if (saveFile.exists()) {
                    nameToSave = getFileNameWithExtension(name, extension,
                            fileInfoCallback.specialCharacter,
                            System.currentTimeMillis().toString())
                    saveFile = File(destinationDir, nameToSave)
                }
                val `in` = cr.openInputStream(source) ?: return null
                ioSrc = Okio.source(`in`)
                sink = Okio.buffer(Okio.sink(saveFile))
                sink!!.writeAll(ioSrc)
                sink.flush()
                return SaveFileResult(saveFile, mimeType)
            } catch (e: IOException) {
                Log.w(LOGTAG, "Failed to save file", e)
                return null
            } finally {
                Utils.closeSilently(sink)
                Utils.closeSilently(ioSrc)
            }
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

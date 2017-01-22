package org.mariotaku.twidere.util.sync.google

import android.content.Context
import android.content.SharedPreferences
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import org.mariotaku.twidere.extension.newPullParser
import org.mariotaku.twidere.extension.newSerializer
import org.mariotaku.twidere.util.io.DirectByteArrayOutputStream
import org.mariotaku.twidere.util.sync.FileBasedPreferencesValuesSyncAction
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.*

internal class GoogleDrivePreferencesValuesSyncAction(
        context: Context,
        val drive: Drive,
        preferences: SharedPreferences,
        processor: Processor,
        val fileName: String
) : FileBasedPreferencesValuesSyncAction<CloseableAny<File>,
        GoogleDriveUploadSession<Map<String, String>>>(context, preferences, processor) {

    private lateinit var commonFolderId: String

    private val files = drive.files()

    override fun newLoadFromRemoteSession(): CloseableAny<File> {
        val file = drive.getFileOrNull(name = fileName, mimeType = xmlMimeType,
                parent = commonFolderId, spaces = appDataFolderSpace,
                conflictResolver = ::resolveFilesConflict) ?: run {
            throw FileNotFoundException()
        }
        return CloseableAny(file)
    }

    override fun CloseableAny<File>.getRemoteLastModified(): Long {
        return obj.modifiedTime?.value ?: throw IOException("Modified time should not be null")
    }

    override fun CloseableAny<File>.loadFromRemote(): MutableMap<String, String> {
        val data = HashMap<String, String>()
        data.parse(files.get(obj.id).executeMediaAsInputStream().newPullParser())
        return data
    }

    override fun newSaveToRemoteSession(): GoogleDriveUploadSession<Map<String, String>> {
        return object : GoogleDriveUploadSession<Map<String, String>>(fileName, commonFolderId, xmlMimeType, drive) {
            override fun Map<String, String>.toInputStream(): InputStream {
                val os = DirectByteArrayOutputStream()
                this.serialize(os.newSerializer(charset = Charsets.UTF_8, indent = true))
                return os.inputStream(true)
            }
        }
    }

    override fun GoogleDriveUploadSession<Map<String, String>>.saveToRemote(data: MutableMap<String, String>): Boolean {
        return this.uploadData(data)
    }


    override fun GoogleDriveUploadSession<Map<String, String>>.setRemoteLastModified(lastModified: Long) {
        this.localModifiedTime = lastModified
    }

    override fun setup(): Boolean {
        commonFolderId = drive.getFileOrCreate(name = commonFolderName, mimeType = folderMimeType,
                parent = appDataFolderName, spaces = appDataFolderSpace,
                conflictResolver = ::resolveFoldersConflict).id
        return true
    }
}
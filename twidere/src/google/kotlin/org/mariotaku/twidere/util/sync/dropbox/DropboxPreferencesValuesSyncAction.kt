package org.mariotaku.twidere.util.sync.dropbox

import android.content.Context
import android.content.SharedPreferences
import com.dropbox.core.DbxDownloader
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.UploadUploader
import org.mariotaku.twidere.extension.newPullParser
import org.mariotaku.twidere.extension.newSerializer
import org.mariotaku.twidere.util.sync.FileBasedPreferencesValuesSyncAction
import java.util.*

internal class DropboxPreferencesValuesSyncAction(
        context: Context,
        val client: DbxClientV2,
        preferences: SharedPreferences,
        processor: Processor,
        val filePath: String
) : FileBasedPreferencesValuesSyncAction<DbxDownloader<FileMetadata>,
        DropboxUploadSession<Map<String, String>>>(context, preferences, processor) {
    override fun DbxDownloader<FileMetadata>.getRemoteLastModified(): Long {
        return result.clientModified.time
    }

    override fun DbxDownloader<FileMetadata>.loadFromRemote(): MutableMap<String, String> {
        val data = HashMap<String, String>()
        data.parse(inputStream.newPullParser())
        return data
    }

    override fun newLoadFromRemoteSession(): DbxDownloader<FileMetadata> {
        return client.newDownloader(filePath)
    }

    override fun newSaveToRemoteSession(): DropboxUploadSession<Map<String, String>> {
        return object : DropboxUploadSession<Map<String, String>>(filePath, client) {
            override fun performUpload(uploader: UploadUploader, data: Map<String, String>) {
                data.serialize(uploader.outputStream.newSerializer(charset = Charsets.UTF_8,
                        indent = true))
            }
        }
    }

    override fun DropboxUploadSession<Map<String, String>>.saveToRemote(data: MutableMap<String, String>): Boolean {
        return this.uploadData(data)
    }

    override fun DropboxUploadSession<Map<String, String>>.setRemoteLastModified(lastModified: Long) {
        this.localModifiedTime = lastModified
    }
}
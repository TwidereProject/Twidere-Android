package org.mariotaku.twidere.util.sync.dropbox

import android.content.Context
import com.dropbox.core.DbxDownloader
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.UploadUploader
import org.mariotaku.twidere.extension.model.initFields
import org.mariotaku.twidere.extension.model.parse
import org.mariotaku.twidere.extension.model.serialize
import org.mariotaku.twidere.extension.newPullParser
import org.mariotaku.twidere.extension.newSerializer
import org.mariotaku.twidere.model.FiltersData
import org.mariotaku.twidere.util.sync.FileBasedFiltersDataSyncAction

internal class DropboxFiltersDataSyncAction(
        context: Context,
        val client: DbxClientV2
) : FileBasedFiltersDataSyncAction<DbxDownloader<FileMetadata>, DropboxUploadSession<FiltersData>>(context) {
    override fun DbxDownloader<FileMetadata>.getRemoteLastModified(): Long {
        return result.clientModified.time
    }

    private val filePath = "/Common/filters.xml"

    override fun newLoadFromRemoteSession(): DbxDownloader<FileMetadata> {
        return client.newDownloader(filePath)
    }

    override fun DbxDownloader<FileMetadata>.loadFromRemote(): FiltersData {
        val data = FiltersData()
        data.parse(inputStream.newPullParser(charset = Charsets.UTF_8))
        data.initFields()
        return data
    }

    override fun DropboxUploadSession<FiltersData>.setRemoteLastModified(lastModified: Long) {
        this.localModifiedTime = lastModified
    }

    override fun DropboxUploadSession<FiltersData>.saveToRemote(data: FiltersData): Boolean {
        return this.uploadData(data)
    }

    override fun newSaveToRemoteSession(): DropboxUploadSession<FiltersData> {
        return object : DropboxUploadSession<FiltersData>(filePath, client) {
            override fun performUpload(uploader: UploadUploader, data: FiltersData) {
                data.serialize(uploader.outputStream.newSerializer(charset = Charsets.UTF_8, indent = true))
            }
        }
    }

}
package org.mariotaku.twidere.util.sync.google

import android.content.Context
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import org.mariotaku.twidere.extension.model.initFields
import org.mariotaku.twidere.extension.model.parse
import org.mariotaku.twidere.extension.model.serialize
import org.mariotaku.twidere.extension.newPullParser
import org.mariotaku.twidere.extension.newSerializer
import org.mariotaku.twidere.model.FiltersData
import org.mariotaku.twidere.util.sync.FileBasedFiltersDataSyncAction
import org.mariotaku.twidere.util.tempFileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

internal class GoogleDriveFiltersDataSyncAction(
        context: Context,
        val drive: Drive
) : FileBasedFiltersDataSyncAction<CloseableAny<File>, GoogleDriveUploadSession<FiltersData>>(context) {

    private val fileName = "filters.xml"

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

    override fun CloseableAny<File>.loadFromRemote(): FiltersData {
        val data = FiltersData()
        data.parse(files.get(obj.id).executeMediaAsInputStream().newPullParser(charset = Charsets.UTF_8))
        data.initFields()
        return data
    }

    override fun GoogleDriveUploadSession<FiltersData>.setRemoteLastModified(lastModified: Long) {
        this.localModifiedTime = lastModified
    }

    override fun GoogleDriveUploadSession<FiltersData>.saveToRemote(data: FiltersData): Boolean {
        return this.uploadData(data)
    }

    override fun newSaveToRemoteSession(): GoogleDriveUploadSession<FiltersData> {
        return object : GoogleDriveUploadSession<FiltersData>(fileName, commonFolderId, xmlMimeType, drive) {
            override fun FiltersData.toInputStream(): InputStream {
                return tempFileInputStream(context) {
                    this.serialize(it.newSerializer(charset = Charsets.UTF_8, indent = true))
                }
            }
        }
    }


    override fun setup(): Boolean {
        commonFolderId = drive.getFileOrCreate(name = commonFolderName, mimeType = folderMimeType,
                parent = appDataFolderName, spaces = appDataFolderSpace,
                conflictResolver = ::resolveFoldersConflict).id
        return true
    }

}
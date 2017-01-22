package org.mariotaku.twidere.util.sync.google

import android.content.Context
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import org.mariotaku.twidere.extension.model.filename
import org.mariotaku.twidere.extension.model.readMimeMessageFrom
import org.mariotaku.twidere.extension.model.writeMimeMessageTo
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.util.io.DirectByteArrayOutputStream
import org.mariotaku.twidere.util.sync.FileBasedDraftsSyncAction
import java.io.IOException
import java.util.*


internal class GoogleDriveDraftsSyncAction(
        context: Context,
        val drive: Drive
) : FileBasedDraftsSyncAction<DriveFileInfo>(context) {

    val draftsDirName = "Drafts"
    val draftMimeType = "message/rfc822"

    private lateinit var folderId: String
    private val files = drive.files()

    @Throws(IOException::class)
    override fun Draft.saveToRemote(): DriveFileInfo {
        val os = DirectByteArrayOutputStream()
        this.writeMimeMessageTo(context, os)
        val driveId = this.remote_extras
        val `is` = os.inputStream(true)
        val fileConfig: (File) -> Unit = {
            it.modifiedTime = DateTime(timestamp)
        }
        val file = if (driveId != null) {
            drive.files().performUpdate(driveId, filename, draftMimeType, stream = `is`, fileConfig = fileConfig)
        } else {
            drive.updateOrCreate(filename, draftMimeType, folderId, stream = `is`, fileConfig = fileConfig)
        }
        return DriveFileInfo(file.id, file.name, Date(file.modifiedTime.value))
    }

    @Throws(IOException::class)
    override fun Draft.loadFromRemote(info: DriveFileInfo): Boolean {
        val get = files.get(info.fileId)
        get.executeMediaAsInputStream().use {
            val parsed = this.readMimeMessageFrom(context, it)
            if (parsed) {
                this.timestamp = info.draftTimestamp
                this.unique_id = info.draftFileName.substringBeforeLast(".eml")
                this.remote_extras = info.fileId
            }
            return parsed
        }
    }

    @Throws(IOException::class)
    override fun removeDrafts(list: List<DriveFileInfo>): Boolean {
        val batch = drive.batch()
        val callback = SimpleJsonBatchCallback<Void>()
        list.forEach { info ->
            files.delete(info.fileId).queue(batch, callback)
        }
        batch.execute()
        return true
    }

    @Throws(IOException::class)
    override fun removeDraft(info: DriveFileInfo): Boolean {
        files.delete(info.fileId).execute()
        return true
    }

    override val DriveFileInfo.draftTimestamp: Long get() = this.modifiedDate.time

    override val DriveFileInfo.draftFileName: String get() = this.name

    override val DriveFileInfo.draftRemoteExtras: String? get() = this.fileId

    @Throws(IOException::class)
    override fun listRemoteDrafts(): List<DriveFileInfo> {
        val result = ArrayList<DriveFileInfo>()
        var pageToken: String?
        do {
            val listResult = files.list().apply {
                fields = "files($requiredRequestFields)"
                q = "'$folderId' in parents and mimeType = '$draftMimeType' and trashed = false"
            }.execute()
            listResult.files.filter { file ->
                file.mimeType == draftMimeType
            }.mapTo(result) { file ->
                DriveFileInfo(file.id, file.name, Date(file.modifiedTime.value))
            }
            pageToken = listResult.nextPageToken
        } while (pageToken != null)
        return result
    }

    override fun setup(): Boolean {
        folderId = drive.getFileOrCreate(draftsDirName, folderMimeType, conflictResolver = ::resolveFoldersConflict).id
        return true
    }

}
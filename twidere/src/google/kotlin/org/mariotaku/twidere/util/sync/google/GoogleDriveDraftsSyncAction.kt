package org.mariotaku.twidere.util.sync.google

import android.content.Context
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
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
        val file = files.updateOrCreate(filename, draftMimeType, folderId, stream = os.inputStream(true), fileConfig = {
            it.modifiedTime = DateTime(timestamp)
        })
        return DriveFileInfo(file.id, file.name, Date(timestamp))
    }

    @Throws(IOException::class)
    override fun Draft.loadFromRemote(info: DriveFileInfo): Boolean {
        val get = files.get(info.fileId)
        get.executeAsInputStream().use {
            val parsed = this.readMimeMessageFrom(context, it)
            if (parsed) {
                this.timestamp = info.draftTimestamp
                this.unique_id = info.draftFileName.substringBeforeLast(".eml")
            }
            return parsed
        }
    }

    @Throws(IOException::class)
    override fun removeDrafts(list: List<DriveFileInfo>): Boolean {
        list.forEach { info ->
            files.delete(info.fileId).execute()
        }
        return true
    }

    @Throws(IOException::class)
    override fun removeDraft(info: DriveFileInfo): Boolean {
        files.delete(info.fileId).execute()
        return true
    }

    override val DriveFileInfo.draftTimestamp: Long get() = this.modifiedDate.time

    override val DriveFileInfo.draftFileName: String get() = this.name

    @Throws(IOException::class)
    override fun listRemoteDrafts(): List<DriveFileInfo> {
        val result = ArrayList<DriveFileInfo>()
        var pageToken: String?
        do {
            val executeResult = files.list().apply {
                q = "'$folderId' in parents and mimeType = '$draftMimeType' and trashed = false"
            }.execute()
            executeResult.files.filter { file ->
                file.mimeType == draftMimeType
            }.mapTo(result) { file ->
                val lastModified = file.modifiedTime ?: file.createdTime
                DriveFileInfo(file.id, file.name, Date(lastModified?.value ?: 0))
            }
            pageToken = executeResult.nextPageToken
        } while (pageToken != null)
        return result
    }

    override fun setup(): Boolean {
        folderId = files.getOrCreate(draftsDirName, folderMimeType).id
        return true
    }

}
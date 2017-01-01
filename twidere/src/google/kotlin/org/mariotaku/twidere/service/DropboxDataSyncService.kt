package org.mariotaku.twidere.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.NotificationCompat
import android.util.Log
import android.util.Xml
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.NetworkIOException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.DeleteArg
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.core.v2.files.WriteMode
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.dropboxAuthTokenKey
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.model.FiltersData
import org.mariotaku.twidere.util.sync.FileBasedDraftsSyncHelper
import org.mariotaku.twidere.util.sync.FileBasedFiltersDataSyncHelper
import org.mariotaku.twidere.util.sync.LOGTAG_SYNC
import java.io.IOException
import java.util.*

/**
 * Created by mariotaku on 2016/12/7.
 */

class DropboxDataSyncService : BaseIntentService("dropbox_data_sync") {
    private val NOTIFICATION_ID_SYNC_DATA = 302

    override fun onHandleIntent(intent: Intent?) {
        val authToken = preferences[dropboxAuthTokenKey] ?: return
        val nb = NotificationCompat.Builder(this)
        nb.setSmallIcon(R.drawable.ic_stat_refresh)
        nb.setOngoing(true)
        nb.setContentTitle("Syncing data")
        nb.setContentText("Syncing using Dropbox")
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID_SYNC_DATA, nb.build())
        val requestConfig = DbxRequestConfig.newBuilder("twidere-android/${BuildConfig.VERSION_NAME}")
                .build()
        val client = DbxClientV2(requestConfig, authToken)
        syncFilters(client)
        uploadDrafts(client)
        nm.cancel(NOTIFICATION_ID_SYNC_DATA)
    }

    private fun uploadDrafts(client: DbxClientV2) {
        val helper = DropboxDraftsSyncHelper(this, client)
        try {
            helper.performSync()
        } catch (e: IOException) {
            Log.w(LOGTAG_SYNC, e)
        }
    }

    @Throws(IOException::class)
    private fun syncFilters(client: DbxClientV2) {
        val helper = DropboxFiltersDataSyncHelper(this, client)
        try {
            helper.performSync()
        } catch (e: IOException) {
            Log.w(LOGTAG_SYNC, e)
        }
    }

    class DropboxDraftsSyncHelper(context: Context, val client: DbxClientV2) : FileBasedDraftsSyncHelper<FileMetadata>(context) {
        @Throws(IOException::class)
        override fun Draft.saveToRemote(): FileMetadata {
            try {
                client.newUploader("/Drafts/$filename", this.timestamp).use {
                    this.writeMimeMessageTo(context, it.outputStream)
                    return it.finish()
                }
            } catch (e: NetworkIOException) {
                throw IOException(e)
            }
        }

        @Throws(IOException::class)
        override fun Draft.loadFromRemote(info: FileMetadata): Boolean {
            try {
                client.files().download(info.pathLower).use {
                    val parsed = this.readMimeMessageFrom(context, it.inputStream)
                    if (parsed) {
                        this.timestamp = info.draftTimestamp
                        this.unique_id = info.draftFileName.substringBeforeLast(".eml")
                    }
                    return parsed
                }
            } catch (e: NetworkIOException) {
                throw IOException(e)
            }
        }

        override fun removeDrafts(list: List<FileMetadata>): Boolean {
            return client.files().deleteBatch(list.map { DeleteArg(it.pathLower) }) != null
        }

        override fun removeDraft(info: FileMetadata): Boolean {
            return client.files().delete(info.pathLower) != null
        }

        override val FileMetadata.draftTimestamp: Long get() = this.clientModified.time

        override val FileMetadata.draftFileName: String get() = this.name

        override fun listRemoteDrafts(): List<FileMetadata> {
            val result = ArrayList<FileMetadata>()
            var listResult: ListFolderResult = client.files().listFolder("/Drafts/")
            while (true) {
                // Do something with files
                listResult.entries.mapNotNullTo(result) { it as? FileMetadata }
                if (!listResult.hasMore) break
                listResult = client.files().listFolderContinue(listResult.cursor)
            }
            return result
        }

    }

    class DropboxFiltersDataSyncHelper(context: Context, val client: DbxClientV2) : FileBasedFiltersDataSyncHelper(context) {
        override fun FiltersData.loadFromRemote(snapshotModifiedMillis: Long): Boolean {
            client.newDownloader("/Common/filters.xml").use { downloader ->
                // Local file is the same with remote version
                if (Math.abs(downloader.result.clientModified.time - snapshotModifiedMillis) < 1000) {
                    return false
                }
                val parser = Xml.newPullParser()
                parser.setInput(downloader.inputStream, "UTF-8")
                this.parse(parser)
                this.initFields()
                return true
            }
        }

        override fun FiltersData.saveToRemote(localModifiedTime: Long): Boolean {
            client.newUploader("/Common/filters.xml", localModifiedTime).use { uploader ->
                val serializer = Xml.newSerializer()
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
                serializer.setOutput(uploader.outputStream, "UTF-8")
                this.serialize(serializer)
                uploader.finish()
                return true
            }
        }

    }

    companion object {
        private fun DbxClientV2.newUploader(path: String, clientModified: Long) = files().uploadBuilder(path)
                .withMode(WriteMode.OVERWRITE).withMute(true).withClientModified(Date(clientModified)).start()

        private fun DbxClientV2.newDownloader(path: String) = files().downloadBuilder(path).start()
    }
}


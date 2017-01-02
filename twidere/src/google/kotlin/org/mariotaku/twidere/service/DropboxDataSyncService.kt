package org.mariotaku.twidere.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.NotificationCompat
import android.util.Log
import android.util.Xml
import com.dropbox.core.DbxDownloader
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.NetworkIOException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.*
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.dropboxAuthTokenKey
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.model.FiltersData
import org.mariotaku.twidere.util.sync.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import java.io.*
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
        arrayOf(
                DropboxDraftsSyncHelper(this, client),
                DropboxFiltersDataSyncHelper(this, client),
                DropboxPreferencesValuesSyncHelper(this, client, userColorNameManager.colorPreferences,
                        UserColorsSyncProcessor, "/Common/user_colors.xml"),
                DropboxPreferencesValuesSyncHelper(this, client, userColorNameManager.nicknamePreferences,
                        UserNicknamesSyncProcessor, "/Common/user_nicknames.xml")
        ).forEach { helper ->
            try {
                helper.performSync()
            } catch (e: IOException) {
                Log.w(LOGTAG_SYNC, e)
            }
        }
        nm.cancel(NOTIFICATION_ID_SYNC_DATA)
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

    internal class DropboxFiltersDataSyncHelper(
            context: Context,
            val client: DbxClientV2
    ) : FileBasedFiltersDataSyncHelper<DbxDownloader<FileMetadata>, DropboxUploadSession<FiltersData>>(context) {
        override fun DbxDownloader<FileMetadata>.getRemoteLastModified(): Long {
            return result.clientModified.time
        }

        private val filePath = "/Common/filters.xml"

        override fun newLoadFromRemoteSession(): DbxDownloader<FileMetadata> {
            return client.newDownloader(filePath)
        }

        override fun DbxDownloader<FileMetadata>.loadFromRemote(): FiltersData {
            val data = FiltersData()
            data.parse(inputStream.newPullParser())
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
                    data.serialize(uploader.outputStream.newSerializer(true))
                }
            }
        }

    }


    internal class DropboxPreferencesValuesSyncHelper(
            context: Context,
            val client: DbxClientV2,
            preferences: SharedPreferences,
            processor: FileBasedPreferencesValuesSyncHelper.Processor,
            val filePath: String
    ) : FileBasedPreferencesValuesSyncHelper<DbxDownloader<FileMetadata>,
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
                    data.serialize(uploader.outputStream.newSerializer(true))
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

    abstract internal class DropboxUploadSession<in Data>(val fileName: String, val client: DbxClientV2) : Closeable {
        private var uploader: UploadUploader? = null

        var localModifiedTime: Long = 0

        override fun close() {
            uploader?.close()
        }

        abstract fun performUpload(uploader: UploadUploader, data: Data)

        fun uploadData(data: Data): Boolean {
            uploader = client.newUploader(fileName, localModifiedTime).apply {
                performUpload(this, data)
                this.finish()
            }
            return true
        }

    }

    companion object {

        private fun InputStream.newPullParser(): XmlPullParser {
            val parser = Xml.newPullParser()
            parser.setInput(this, "UTF-8")
            return parser
        }

        private fun OutputStream.newSerializer(indent: Boolean = true): XmlSerializer {
            val serializer = Xml.newSerializer()
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", indent)
            serializer.setOutput(this, "UTF-8")
            return serializer
        }

        private fun DbxClientV2.newUploader(path: String, clientModified: Long) = files().uploadBuilder(path)
                .withMode(WriteMode.OVERWRITE).withMute(true).withClientModified(Date(clientModified)).start()

        private fun DbxClientV2.newDownloader(path: String): DbxDownloader<FileMetadata> {
            try {
                return files().downloadBuilder(path).start()
            } catch (e: DownloadErrorException) {
                if (e.errorValue?.pathValue?.isNotFound ?: false) {
                    throw FileNotFoundException(path)
                }
                throw IOException(e)
            }
        }
    }
}


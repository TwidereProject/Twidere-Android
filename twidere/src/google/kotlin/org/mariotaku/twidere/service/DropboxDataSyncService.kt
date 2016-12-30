package org.mariotaku.twidere.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.NotificationCompat
import android.util.Xml
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.map
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.dropboxAuthTokenKey
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.model.DraftCursorIndices
import org.mariotaku.twidere.model.FiltersData
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts
import java.io.*

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
        val cur = contentResolver.query(Drafts.CONTENT_URI, Drafts.COLUMNS, null, null, null) ?: return
        cur.map(DraftCursorIndices(cur)).forEach { draft ->
            client.newUploader("/Drafts/${draft.timestamp}.eml").use {
                draft.writeMimeMessageTo(this, it.outputStream)
                it.finish()
            }
        }
        cur.close()
    }

    @Throws(IOException::class)
    private fun syncFilters(client: DbxClientV2) {
        val helper = DropboxFiltersDataSyncHelper(this, client)
        helper.sync()
    }

    abstract class FileBasedFiltersDataSyncHelper(val context: Context) {
        @Throws(IOException::class)
        protected abstract fun loadFromRemote(): FiltersData

        @Throws(IOException::class)
        protected abstract fun saveToRemote(data: FiltersData)

        fun sync() {
            val remoteFilters: FiltersData = loadFromRemote()

            val filters: FiltersData = FiltersData().apply {
                read(context.contentResolver)
                initFields()
            }

            val syncDataDir: File = context.syncDataDir.apply {
                if (!exists()) {
                    mkdirs()
                }
            }
            val snapshotFile = File(syncDataDir, "filters.xml")
            val deletedFilters: FiltersData? = try {
                FileReader(snapshotFile).use {
                    val result = FiltersData()
                    val parser = Xml.newPullParser()
                    parser.setInput(it)
                    result.parse(parser)
                    result.removeAll(filters)
                    return@use result
                }
            } catch (e: FileNotFoundException) {
                null
            }

            filters.addAll(remoteFilters, true)

            if (deletedFilters != null) {
                filters.removeAll(deletedFilters)
            }

            filters.write(context.contentResolver)

            saveToRemote(filters)
            try {
                FileWriter(snapshotFile).use {
                    val serializer = Xml.newSerializer()
                    serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
                    serializer.setOutput(it)
                    filters.serialize(serializer)
                }
            } catch (e: FileNotFoundException) {
                // Ignore
            }
        }

        private val Context.syncDataDir: File
            get() = File(filesDir, "sync_data")
    }

    class DropboxFiltersDataSyncHelper(context: Context, val client: DbxClientV2) : FileBasedFiltersDataSyncHelper(context) {
        override fun loadFromRemote(): FiltersData = client.newDownloader("/Common/filters.xml").use { downloader ->
            val result = FiltersData()
            val parser = Xml.newPullParser()
            parser.setInput(downloader.inputStream, "UTF-8")
            result.parse(parser)
            result.initFields()
            return@use result
        }

        override fun saveToRemote(data: FiltersData) {
            client.newUploader("/Common/filters.xml").use { uploader ->
                val serializer = Xml.newSerializer()
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
                serializer.setOutput(uploader.outputStream, "UTF-8")
                data.serialize(serializer)
                uploader.finish()
            }
        }

    }

}


private fun DbxClientV2.newUploader(path: String) = files().uploadBuilder(path).withMode(WriteMode.OVERWRITE).withMute(true).start()
private fun DbxClientV2.newDownloader(path: String) = files().downloadBuilder(path).start()

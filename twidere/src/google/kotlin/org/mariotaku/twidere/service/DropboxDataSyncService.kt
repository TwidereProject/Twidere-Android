package org.mariotaku.twidere.service

import android.content.Intent
import android.util.Xml
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.map
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.dropboxAuthTokenKey
import org.mariotaku.twidere.extension.model.read
import org.mariotaku.twidere.extension.model.serialize
import org.mariotaku.twidere.extension.model.writeMimeMessageTo
import org.mariotaku.twidere.model.DraftCursorIndices
import org.mariotaku.twidere.model.FiltersData
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts

/**
 * Created by mariotaku on 2016/12/7.
 */

class DropboxDataSyncService : BaseIntentService("dropbox_data_sync") {

    override fun onHandleIntent(intent: Intent?) {
        val authToken = preferences[dropboxAuthTokenKey] ?: return
        val requestConfig = DbxRequestConfig.newBuilder("twidere-android/${BuildConfig.VERSION_NAME}")
                .build()
        val client = DbxClientV2(requestConfig, authToken)
        uploadFilters(client)
        uploadDrafts(client)
    }

    private fun DbxClientV2.newUploader(path: String) = files().uploadBuilder(path).withMode(WriteMode.OVERWRITE).withMute(true).start()

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

    private fun uploadFilters(client: DbxClientV2) {
        val uploader = client.newUploader("/Common/filters.xml")
        val filters = FiltersData()
        filters.read(contentResolver)
        val serializer = Xml.newSerializer()
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
        uploader.use {
            serializer.setOutput(it.outputStream, "UTF-8")
            filters.serialize(serializer)
            it.finish()
        }
    }

}


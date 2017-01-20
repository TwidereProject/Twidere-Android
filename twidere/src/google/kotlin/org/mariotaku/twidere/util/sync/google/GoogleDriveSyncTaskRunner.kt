package org.mariotaku.twidere.util.sync.google

import android.content.Context
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.twidere.util.TaskServiceRunner
import org.mariotaku.twidere.util.sync.ISyncAction
import org.mariotaku.twidere.util.sync.SyncTaskRunner


/**
 * Created by mariotaku on 2017/1/6.
 */

class GoogleDriveSyncTaskRunner(context: Context, val accessToken: String) : SyncTaskRunner(context) {
    override fun onRunningTask(action: String, callback: (Boolean) -> Unit): Boolean {
        val transport = NetHttpTransport.Builder().build()
        val credential = GoogleCredential().setAccessToken(accessToken)
        val drive = Drive.Builder(transport, JacksonFactory.getDefaultInstance(), credential).build()
        val syncAction: ISyncAction = when (action) {
            TaskServiceRunner.ACTION_SYNC_DRAFTS -> GoogleDriveDraftsSyncAction(context, drive)
            else -> null
        } ?: return false
        task {
            val about = drive.about().get().execute()
            println(about)
            syncAction.execute()
        }.successUi {
            callback(true)
        }.failUi {
            callback(false)
        }
        return true
    }

}

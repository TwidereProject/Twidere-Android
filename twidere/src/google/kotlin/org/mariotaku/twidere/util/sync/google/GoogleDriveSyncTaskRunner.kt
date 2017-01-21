package org.mariotaku.twidere.util.sync.google

import android.content.Context
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.twidere.model.sync.GoogleDriveSyncProviderInfo
import org.mariotaku.twidere.util.TaskServiceRunner
import org.mariotaku.twidere.util.sync.ISyncAction
import org.mariotaku.twidere.util.sync.SyncTaskRunner
import org.mariotaku.twidere.util.sync.UserColorsSyncProcessor
import org.mariotaku.twidere.util.sync.UserNicknamesSyncProcessor
import org.mariotaku.twidere.util.sync.dropbox.DropboxPreferencesValuesSyncAction


/**
 * Created by mariotaku on 2017/1/6.
 */

class GoogleDriveSyncTaskRunner(context: Context, val refreshToken: String) : SyncTaskRunner(context) {
    override fun onRunningTask(action: String, callback: (Boolean) -> Unit): Boolean {
        val httpTransport = NetHttpTransport.Builder().build()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        val credential = GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setClientSecrets(GoogleDriveSyncProviderInfo.WEB_CLIENT_ID, GoogleDriveSyncProviderInfo.WEB_CLIENT_SECRET)
                .build()
        credential.refreshToken = refreshToken
        val drive = Drive.Builder(httpTransport, JacksonFactory.getDefaultInstance(), credential).build()
        val syncAction: ISyncAction = when (action) {
            TaskServiceRunner.ACTION_SYNC_DRAFTS -> GoogleDriveDraftsSyncAction(context, drive)
            TaskServiceRunner.ACTION_SYNC_FILTERS -> GoogleDriveFiltersDataSyncAction(context, drive)
            TaskServiceRunner.ACTION_SYNC_USER_COLORS -> GoogleDrivePreferencesValuesSyncAction(context,
                    drive, userColorNameManager.colorPreferences, UserColorsSyncProcessor,
                    "user_colors.xml")
            TaskServiceRunner.ACTION_SYNC_USER_NICKNAMES -> GoogleDrivePreferencesValuesSyncAction(context,
                    drive, userColorNameManager.nicknamePreferences, UserNicknamesSyncProcessor,
                    "user_nicknames.xml")
            else -> null
        } ?: return false
        task {
            syncAction.execute()
        }.successUi {
            callback(true)
        }.failUi {
            callback(false)
        }
        return true
    }

}

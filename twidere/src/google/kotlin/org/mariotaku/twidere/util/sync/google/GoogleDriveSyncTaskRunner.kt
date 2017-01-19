package org.mariotaku.twidere.util.sync.google

import android.content.ComponentCallbacks
import android.content.Context
import android.os.Bundle
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.util.TaskServiceRunner
import org.mariotaku.twidere.util.sync.ISyncAction
import org.mariotaku.twidere.util.sync.SyncTaskRunner
import org.mariotaku.twidere.util.sync.UserColorsSyncProcessor
import org.mariotaku.twidere.util.sync.UserNicknamesSyncProcessor
import org.mariotaku.twidere.util.sync.dropbox.DropboxDraftsSyncAction
import org.mariotaku.twidere.util.sync.dropbox.DropboxFiltersDataSyncAction
import org.mariotaku.twidere.util.sync.dropbox.DropboxPreferencesValuesSyncAction
import java.net.ConnectException

/**
 * Created by mariotaku on 2017/1/6.
 */

class GoogleDriveSyncTaskRunner(context: Context) : SyncTaskRunner(context) {
    override fun onRunningTask(action: String, callback: (Boolean) -> Unit): Boolean {
        val client = GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .build()
        val syncAction: ISyncAction = when (action) {
            TaskServiceRunner.ACTION_SYNC_DRAFTS -> GoogleDriveDraftsSyncAction(context, client)
            else -> null
        } ?: return false
        task {
            val connResult = client.blockingConnect()
            if (!connResult.isSuccess) {
                throw ConnectException()
            }
            syncAction.execute()
        }.successUi {
            callback(true)
        }.failUi {
            callback(false)
        }.always {
            client.disconnect()
        }
        return true
    }

}

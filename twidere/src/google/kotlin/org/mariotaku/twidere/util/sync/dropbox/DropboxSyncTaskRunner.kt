package org.mariotaku.twidere.util.sync.dropbox;

import android.content.Context
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.util.TaskServiceRunner
import org.mariotaku.twidere.util.sync.ISyncAction
import org.mariotaku.twidere.util.sync.SyncTaskRunner
import org.mariotaku.twidere.util.sync.UserColorsSyncProcessor
import org.mariotaku.twidere.util.sync.UserNicknamesSyncProcessor

/**
 * Created by mariotaku on 2017/1/6.
 */

class DropboxSyncTaskRunner(context: Context, val authToken: String) : SyncTaskRunner(context) {

    override fun onRunningTask(action: String, callback: (Boolean) -> Unit): Boolean {
        val requestConfig = DbxRequestConfig.newBuilder("twidere-android/${BuildConfig.VERSION_NAME}")
                .build()
        val client = DbxClientV2(requestConfig, authToken)
        val syncAction: ISyncAction = when (action) {
            TaskServiceRunner.ACTION_SYNC_DRAFTS -> DropboxDraftsSyncAction(context, client)
            TaskServiceRunner.ACTION_SYNC_FILTERS -> DropboxFiltersDataSyncAction(context, client)
            TaskServiceRunner.ACTION_SYNC_USER_COLORS -> DropboxPreferencesValuesSyncAction(context,
                    client, userColorNameManager.colorPreferences, UserColorsSyncProcessor,
                    "/Common/user_colors.xml")
            TaskServiceRunner.ACTION_SYNC_USER_NICKNAMES -> DropboxPreferencesValuesSyncAction(context,
                    client, userColorNameManager.nicknamePreferences, UserNicknamesSyncProcessor,
                    "/Common/user_nicknames.xml")
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

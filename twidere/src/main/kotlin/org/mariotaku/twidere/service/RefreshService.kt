/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.ktextension.convert
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.annotation.AutoRefreshType
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.model.AccountPreferences
import org.mariotaku.twidere.model.SimpleRefreshTaskParam
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.task.GetActivitiesAboutMeTask
import org.mariotaku.twidere.task.GetHomeTimelineTask
import org.mariotaku.twidere.task.GetReceivedDirectMessagesTask
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.SharedPreferencesWrapper
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import javax.inject.Inject

class RefreshService : Service() {

    @Inject
    internal lateinit var preferences: SharedPreferencesWrapper

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        GeneralComponentHelper.build(this).inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOGTAG, "onStartCommand ${intent?.action}")
        val task = run {
            val type = intent?.action?.convert { getRefreshType(it) } ?: return@run null
            return@run createJobTask(this, type)
        } ?: run {
            stopSelfResult(startId)
            return START_NOT_STICKY
        }
        task.callback = {
            stopSelfResult(startId)
        }
        return START_NOT_STICKY
    }

    class AutoRefreshTaskParam(
            val context: Context,
            val refreshable: (AccountPreferences) -> Boolean,
            val getSinceIds: (Array<UserKey>) -> Array<String?>?
    ) : SimpleRefreshTaskParam() {

        override fun getAccountKeysWorker(): Array<UserKey> {
            val prefs = AccountPreferences.getAccountPreferences(context,
                    DataStoreUtils.getAccountKeys(context)).filter(AccountPreferences::isAutoRefreshEnabled)
            return prefs.filter(refreshable)
                    .map(AccountPreferences::getAccountKey).toTypedArray()
        }

        override val sinceIds: Array<String?>?
            get() = getSinceIds(accountKeys)
    }

    companion object {

        fun createJobTask(context: Context, @AutoRefreshType refreshType: String): AbstractTask<*, *, () -> Unit>? {
            when (refreshType) {
                AutoRefreshType.HOME_TIMELINE -> {
                    val task = GetHomeTimelineTask(context)
                    task.params = RefreshService.AutoRefreshTaskParam(context, AccountPreferences::isAutoRefreshHomeTimelineEnabled) { accountKeys ->
                        DataStoreUtils.getNewestStatusIds(context, Statuses.CONTENT_URI, accountKeys)
                    }
                    return task
                }
                AutoRefreshType.INTERACTIONS_TIMELINE -> {
                    val task = GetActivitiesAboutMeTask(context)
                    task.params = RefreshService.AutoRefreshTaskParam(context, AccountPreferences::isAutoRefreshMentionsEnabled) { accountKeys ->
                        DataStoreUtils.getNewestActivityMaxPositions(context, Activities.AboutMe.CONTENT_URI, accountKeys)
                    }
                    return task
                }
                AutoRefreshType.DIRECT_MESSAGES -> {
                    val task = GetReceivedDirectMessagesTask(context)
                    task.params = RefreshService.AutoRefreshTaskParam(context, AccountPreferences::isAutoRefreshDirectMessagesEnabled) { accountKeys ->
                        DataStoreUtils.getNewestMessageIds(context, DirectMessages.Inbox.CONTENT_URI, accountKeys)
                    }
                    return task
                }
            }
            return null
        }

        @AutoRefreshType
        fun getRefreshType(action: String): String? = when (action) {
            ACTION_REFRESH_HOME_TIMELINE -> AutoRefreshType.HOME_TIMELINE
            ACTION_REFRESH_NOTIFICATIONS -> AutoRefreshType.INTERACTIONS_TIMELINE
            ACTION_REFRESH_DIRECT_MESSAGES -> AutoRefreshType.DIRECT_MESSAGES
            else -> null
        }

        fun getRefreshAction(@AutoRefreshType type: String): String? = when (type) {
            AutoRefreshType.HOME_TIMELINE -> ACTION_REFRESH_HOME_TIMELINE
            AutoRefreshType.INTERACTIONS_TIMELINE -> ACTION_REFRESH_NOTIFICATIONS
            AutoRefreshType.DIRECT_MESSAGES -> ACTION_REFRESH_DIRECT_MESSAGES
            else -> null
        }

    }
}

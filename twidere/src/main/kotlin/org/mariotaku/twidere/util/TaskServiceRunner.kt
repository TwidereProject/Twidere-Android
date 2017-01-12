package org.mariotaku.twidere.util

import android.content.Context
import android.support.annotation.StringDef
import com.squareup.otto.Bus
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.twidere.constant.IntentConstants.INTENT_PACKAGE_PREFIX
import org.mariotaku.twidere.constant.dataSyncProviderInfoKey
import org.mariotaku.twidere.constant.stopAutoRefreshWhenBatteryLowKey
import org.mariotaku.twidere.model.AccountPreferences
import org.mariotaku.twidere.model.SimpleRefreshTaskParam
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.task.GetActivitiesAboutMeTask
import org.mariotaku.twidere.task.GetHomeTimelineTask
import org.mariotaku.twidere.task.GetReceivedDirectMessagesTask
import org.mariotaku.twidere.task.filter.RefreshFiltersSubscriptionsTask

/**
 * Created by mariotaku on 2017/1/6.
 */

class TaskServiceRunner(
        val context: Context,
        val preferences: KPreferences,
        val bus: Bus
) {

    fun runTask(@Action action: String, callback: (Boolean) -> Unit): Boolean {
        when (action) {
            ACTION_REFRESH_HOME_TIMELINE, ACTION_REFRESH_NOTIFICATIONS,
            ACTION_REFRESH_DIRECT_MESSAGES, ACTION_REFRESH_FILTERS_SUBSCRIPTIONS -> {
                val task = createRefreshTask(action) ?: return false
                task.callback = callback
                TaskStarter.execute(task)
                return true
            }
            ACTION_SYNC_DRAFTS, ACTION_SYNC_FILTERS, ACTION_SYNC_USER_NICKNAMES, ACTION_SYNC_USER_COLORS -> {
                val runner = preferences[dataSyncProviderInfoKey]?.newSyncTaskRunner(context) ?: return false
                return runner.runTask(action, callback)
            }
        }
        return false
    }

    fun createRefreshTask(@Action action: String): AbstractTask<*, *, (Boolean) -> Unit>? {
        if (!Utils.isBatteryOkay(context) && preferences[stopAutoRefreshWhenBatteryLowKey]) {
            // Low battery, don't refresh
            return null
        }
        when (action) {
            ACTION_REFRESH_HOME_TIMELINE -> {
                val task = GetHomeTimelineTask(context)
                task.params = AutoRefreshTaskParam(context, AccountPreferences::isAutoRefreshHomeTimelineEnabled) { accountKeys ->
                    DataStoreUtils.getNewestStatusIds(context, Statuses.CONTENT_URI, accountKeys)
                }
                return task
            }
            ACTION_REFRESH_NOTIFICATIONS -> {
                val task = GetActivitiesAboutMeTask(context)
                task.params = AutoRefreshTaskParam(context, AccountPreferences::isAutoRefreshMentionsEnabled) { accountKeys ->
                    DataStoreUtils.getNewestActivityMaxPositions(context, Activities.AboutMe.CONTENT_URI, accountKeys)
                }
                return task
            }
            ACTION_REFRESH_DIRECT_MESSAGES -> {
                val task = GetReceivedDirectMessagesTask(context)
                task.params = AutoRefreshTaskParam(context, AccountPreferences::isAutoRefreshDirectMessagesEnabled) { accountKeys ->
                    DataStoreUtils.getNewestMessageIds(context, DirectMessages.Inbox.CONTENT_URI, accountKeys)
                }
                return task
            }
            ACTION_REFRESH_FILTERS_SUBSCRIPTIONS -> {
                return RefreshFiltersSubscriptionsTask(context)
            }
        }
        return null
    }

    @StringDef(ACTION_REFRESH_HOME_TIMELINE, ACTION_REFRESH_NOTIFICATIONS, ACTION_REFRESH_DIRECT_MESSAGES,
            ACTION_REFRESH_FILTERS_SUBSCRIPTIONS, ACTION_SYNC_DRAFTS, ACTION_SYNC_FILTERS,
            ACTION_SYNC_USER_NICKNAMES, ACTION_SYNC_USER_COLORS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Action

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
        @Action
        const val ACTION_REFRESH_HOME_TIMELINE = INTENT_PACKAGE_PREFIX + "REFRESH_HOME_TIMELINE"
        @Action
        const val ACTION_REFRESH_NOTIFICATIONS = INTENT_PACKAGE_PREFIX + "REFRESH_NOTIFICATIONS"
        @Action
        const val ACTION_REFRESH_DIRECT_MESSAGES = INTENT_PACKAGE_PREFIX + "REFRESH_DIRECT_MESSAGES"
        @Action
        const val ACTION_REFRESH_FILTERS_SUBSCRIPTIONS = INTENT_PACKAGE_PREFIX + "REFRESH_FILTERS_SUBSCRIPTIONS"
        @Action
        const val ACTION_SYNC_DRAFTS = INTENT_PACKAGE_PREFIX + "SYNC_DRAFTS"
        @Action
        const val ACTION_SYNC_FILTERS = INTENT_PACKAGE_PREFIX + "SYNC_FILTERS"
        @Action
        const val ACTION_SYNC_USER_NICKNAMES = INTENT_PACKAGE_PREFIX + "SYNC_USER_NICKNAMES"
        @Action
        const val ACTION_SYNC_USER_COLORS = INTENT_PACKAGE_PREFIX + "SYNC_USER_COLORS"

        val ACTIONS_SYNC = arrayOf(ACTION_SYNC_DRAFTS, ACTION_SYNC_FILTERS, ACTION_SYNC_USER_COLORS,
                ACTION_SYNC_USER_NICKNAMES)
    }

    data class SyncFinishedEvent(val syncType: String, val success: Boolean)

}


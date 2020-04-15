package org.mariotaku.twidere.util

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringDef
import android.util.Log
import com.squareup.otto.Bus
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.ktextension.toNulls
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.constant.IntentConstants.INTENT_PACKAGE_PREFIX
import org.mariotaku.twidere.constant.dataSyncProviderInfoKey
import org.mariotaku.twidere.constant.stopAutoRefreshWhenBatteryLowKey
import org.mariotaku.twidere.model.AccountPreferences
import org.mariotaku.twidere.model.RefreshTaskParam
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.Pagination
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.provider.TwidereDataStore.Activities
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.filter.RefreshFiltersSubscriptionsTask
import org.mariotaku.twidere.task.filter.RefreshLaunchPresentationsTask
import org.mariotaku.twidere.task.twitter.GetActivitiesAboutMeTask
import org.mariotaku.twidere.task.twitter.GetHomeTimelineTask
import org.mariotaku.twidere.task.twitter.message.GetMessagesTask

/**
 * Created by mariotaku on 2017/1/6.
 */

class TaskServiceRunner(
        val context: Context,
        val preferences: SharedPreferences,
        val activityTracker: ActivityTracker,
        val bus: Bus
) {

    fun runTask(@Action action: String, callback: (Boolean) -> Unit): Boolean {
        Log.d(LOGTAG, "TaskServiceRunner run task $action")
        when (action) {
            ACTION_REFRESH_HOME_TIMELINE, ACTION_REFRESH_NOTIFICATIONS,
            ACTION_REFRESH_DIRECT_MESSAGES, ACTION_REFRESH_FILTERS_SUBSCRIPTIONS,
            ACTION_REFRESH_LAUNCH_PRESENTATIONS -> {
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

    private fun createRefreshTask(@Action action: String): AbstractTask<*, *, (Boolean) -> Unit>? {
        if (!Utils.isBatteryOkay(context) && preferences[stopAutoRefreshWhenBatteryLowKey]) {
            // Low battery, don't refresh
            return null
        }
        when (action) {
            ACTION_REFRESH_HOME_TIMELINE -> {
                val task = GetHomeTimelineTask(context)
                task.params = AutoRefreshTaskParam(context, preferences, activityTracker.isEmpty,
                        AccountPreferences::isAutoRefreshHomeTimelineEnabled) { accountKeys ->
                    DataStoreUtils.getNewestStatusIds(context, Statuses.CONTENT_URI,
                            accountKeys.toNulls())
                }
                return task
            }
            ACTION_REFRESH_NOTIFICATIONS -> {
                val task = GetActivitiesAboutMeTask(context)
                task.params = AutoRefreshTaskParam(context, preferences, activityTracker.isEmpty,
                        AccountPreferences::isAutoRefreshMentionsEnabled) { accountKeys ->
                    DataStoreUtils.getRefreshNewestActivityMaxPositions(context,
                            Activities.AboutMe.CONTENT_URI, accountKeys.toNulls())
                }
                return task
            }
            ACTION_REFRESH_DIRECT_MESSAGES -> {
                val task = GetMessagesTask(context)
                task.params = object : GetMessagesTask.RefreshNewTaskParam(context) {

                    override val isBackground: Boolean = activityTracker.isEmpty

                    override val accountKeys: Array<UserKey> by lazy {
                        AccountPreferences.getAccountPreferences(context, preferences,
                                DataStoreUtils.getAccountKeys(context)).filter {
                            it.isAutoRefreshEnabled && it.isAutoRefreshDirectMessagesEnabled
                        }.mapToArray(AccountPreferences::accountKey)
                    }
                }
                return task
            }
            ACTION_REFRESH_FILTERS_SUBSCRIPTIONS -> {
                return RefreshFiltersSubscriptionsTask(context)
            }
            ACTION_REFRESH_LAUNCH_PRESENTATIONS -> {
                return RefreshLaunchPresentationsTask(context)
            }
        }
        return null
    }

    @StringDef(ACTION_REFRESH_HOME_TIMELINE, ACTION_REFRESH_NOTIFICATIONS, ACTION_REFRESH_DIRECT_MESSAGES,
            ACTION_REFRESH_FILTERS_SUBSCRIPTIONS, ACTION_REFRESH_LAUNCH_PRESENTATIONS,
            ACTION_SYNC_DRAFTS, ACTION_SYNC_FILTERS, ACTION_SYNC_USER_NICKNAMES, ACTION_SYNC_USER_COLORS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Action

    class AutoRefreshTaskParam(
            val context: Context,
            val preferences: SharedPreferences,
            override val isBackground: Boolean,
            val refreshable: (AccountPreferences) -> Boolean,
            val getSinceIds: (Array<UserKey>) -> Array<String?>?
    ) : RefreshTaskParam {

        override val accountKeys: Array<UserKey> by lazy {
            return@lazy AccountPreferences.getAccountPreferences(context, preferences,
                    DataStoreUtils.getAccountKeys(context)).filter {
                it.isAutoRefreshEnabled && refreshable(it)
            }.mapToArray(AccountPreferences::accountKey)
        }

        override val pagination: Array<Pagination?>?
            get() = getSinceIds(accountKeys)?.mapToArray { sinceId ->
                SinceMaxPagination().also { it.sinceId = sinceId }
            }

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
        const val ACTION_REFRESH_LAUNCH_PRESENTATIONS = INTENT_PACKAGE_PREFIX + "REFRESH_LAUNCH_PRESENTATIONS"
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


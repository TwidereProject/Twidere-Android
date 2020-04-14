package org.mariotaku.twidere.service

import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.net.ConnectivityManagerCompat
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.addOnAccountsUpdatedListenerSafe
import org.mariotaku.ktextension.removeOnAccountsUpdatedListenerSafe
import org.mariotaku.ktextension.toLongOr
import org.mariotaku.ktextension.toNulls
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.TwitterUserStream
import org.mariotaku.microblog.library.twitter.annotation.StreamWith
import org.mariotaku.microblog.library.twitter.model.*
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.streamingEnabledKey
import org.mariotaku.twidere.constant.streamingNonMeteredNetworkKey
import org.mariotaku.twidere.constant.streamingPowerSavingKey
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.extension.model.api.key
import org.mariotaku.twidere.extension.model.api.microblog.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.queryCount
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.notification.NotificationChannelSpec
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.task.twitter.GetActivitiesAboutMeTask
import org.mariotaku.twidere.task.twitter.message.GetMessagesTask
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.DebugLog
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.dagger.DependencyHolder
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.util.streaming.TwitterTimelineStreamCallback
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class StreamingService : BaseService() {

    internal lateinit var threadPoolExecutor: ExecutorService
    internal lateinit var handler: Handler

    private val submittedTasks: MutableMap<UserKey, StreamingRunnable<*>> = WeakHashMap()

    private val accountChangeObserver = OnAccountsUpdateListener {
        if (!setupStreaming()) {
            stopSelf()
        }
    }

    override fun onCreate() {
        super.onCreate()
        GeneralComponent.get(this).inject(this)
        threadPoolExecutor = Executors.newCachedThreadPool { runnable ->
            val thread = Thread(runnable)
            thread.priority = Thread.NORM_PRIORITY - 1
            if (runnable is StreamingRunnable<*>) {
                thread.name = "twidere-streaming-${runnable.account.key}"
            }
            return@newCachedThreadPool thread
        }
        handler = Handler(Looper.getMainLooper())
        AccountManager.get(this).addOnAccountsUpdatedListenerSafe(accountChangeObserver, updateImmediately = false)
    }

    override fun onDestroy() {
        submittedTasks.forEach {
            // NOTE: IMPORTANT!!! Before Nougat, forEach { k, v -> } will crash because referenced
            // BiConsumer, which is introduced in Java 8
            val (_, v) = it
            v.cancel()
        }
        threadPoolExecutor.shutdown()
        submittedTasks.clear()
        removeNotification()
        AccountManager.get(this).removeOnAccountsUpdatedListenerSafe(accountChangeObserver)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (setupStreaming()) {
            return START_STICKY
        }
        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent) = throw UnsupportedOperationException()

    /**
     * @return True if there're enabled accounts, false if request not met and service should be stopped
     */
    private fun setupStreaming(): Boolean {
        if (!preferences[streamingEnabledKey]) {
            return false
        }
        if (!activityTracker.isHomeActivityLaunched) {
            return false
        }
        // Quit if no connection
        if (connectivityManager.activeNetworkInfo?.isAvailable != true) {
            return false
        }
        // Quit if connection metered (with preference)
        val isNetworkMetered = ConnectivityManagerCompat.isActiveNetworkMetered(connectivityManager)
        if (preferences[streamingNonMeteredNetworkKey] && isNetworkMetered) {
            return false
        }
        // Quit if not charging (with preference)
        val isCharging = Utils.isCharging(this)
        if (preferences[streamingPowerSavingKey] && !isCharging) {
            return false
        }
        // Quit if no streaming instance available
        if (!updateStreamingInstances()) {
            return false
        }
        showNotification()
        return true
    }

    private fun updateStreamingInstances(): Boolean {
        val am = AccountManager.get(this)
        val supportedAccounts = AccountUtils.getAllAccountDetails(am, true).filter { it.isStreamingSupported }
        val supportedPrefs = supportedAccounts.map { AccountPreferences(this, preferences, it.key) }
        val enabledAccounts = supportedAccounts.filter { account ->
            return@filter supportedPrefs.any {
                account.key == it.accountKey && it.isStreamingEnabled
            }
        }

        if (enabledAccounts.isEmpty()) return false

        // Remove all disabled instances
        submittedTasks.forEach {
            // NOTE: IMPORTANT!!! Before Nougat, forEach { k, v -> } will crash because referenced
            // BiConsumer, which is introduced in Java 8
            val (k, v) = it
            if (enabledAccounts.none { k == it.key } && !v.cancelled) {
                v.cancel()
            }
        }
        // Add instances if not running
        enabledAccounts.forEach { account ->
            val existing = submittedTasks[account.key]
            if (existing == null || existing.cancelled) {
                val runnable = newStreamingRunnable(account, supportedPrefs.first {
                    it.accountKey == account.key
                }) ?: return@forEach
                threadPoolExecutor.submit(runnable)
                submittedTasks[account.key] = runnable
            }
        }
        return true
    }

    private fun showNotification() {
        val intent = IntentUtils.settings("streaming")
        val contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val contentTitle = getString(R.string.app_name)
        val contentText = getString(R.string.timeline_streaming_running)
        val builder = NotificationChannelSpec.serviceStatuses.notificationBuilder(this)
        builder.setOngoing(true)
        builder.setSmallIcon(R.drawable.ic_stat_streaming)
        builder.setContentTitle(contentTitle)
        builder.setContentText(contentText)
        builder.setContentIntent(contentIntent)
        builder.setCategory(NotificationCompat.CATEGORY_STATUS)
        builder.priority = NotificationCompat.PRIORITY_MIN
        startForeground(NOTIFICATION_SERVICE_STARTED, builder.build())
    }

    private fun removeNotification() {
        stopForeground(true)
    }

    private fun newStreamingRunnable(account: AccountDetails, preferences: AccountPreferences): StreamingRunnable<*>? {
        when (account.type) {
            AccountType.TWITTER -> {
                return TwitterStreamingRunnable(this, account, preferences)
            }
        }
        return null
    }

    internal abstract class StreamingRunnable<T>(
            val context: Context,
            val account: AccountDetails,
            val accountPreferences: AccountPreferences
    ) : Runnable {

        var cancelled: Boolean = false
            private set

        override fun run() {
            val instance = createStreamingInstance()
            while (!cancelled && !Thread.currentThread().isInterrupted) {
                try {
                    instance.beginStreaming()
                } catch (e: MicroBlogException) {
                    DebugLog.w(LOGTAG, msg = "Can't stream for ${account.key}", tr = e)
                }
                Thread.sleep(TimeUnit.MINUTES.toMillis(1))
            }
        }

        fun cancel(): Boolean {
            if (cancelled) return false
            cancelled = true
            onCancelled()
            return true
        }

        abstract fun createStreamingInstance(): T

        abstract fun T.beginStreaming()

        abstract fun onCancelled()
    }

    internal inner class TwitterStreamingRunnable(
            context: Context,
            account: AccountDetails,
            accountPreferences: AccountPreferences
    ) : StreamingRunnable<TwitterUserStream>(context, account, accountPreferences) {

        private val profileImageSize = context.getString(R.string.profile_image_size)
        private val isOfficial = account.isOfficial(context)

        private var canGetInteractions: Boolean = true
        private var canGetMessages: Boolean = true

        private val interactionsTimeoutRunnable = Runnable {
            canGetInteractions = true
        }

        private val messagesTimeoutRunnable = Runnable {
            canGetMessages = true
        }

        val callback = object : TwitterTimelineStreamCallback(account.key.id) {

            private var lastStatusTimestamps = LongArray(2)

            private var homeInsertGap = false
            private var interactionsInsertGap = false

            private var lastActivityAboutMe: ParcelableActivity? = null

            override fun onConnected(): Boolean {
                homeInsertGap = true
                interactionsInsertGap = true
                return true
            }

            override fun onHomeTimeline(status: Status): Boolean {
                if (!accountPreferences.isStreamHomeTimelineEnabled) {
                    homeInsertGap = true
                    return false
                }
                val parcelableStatus = status.toParcelable(account, profileImageSize = profileImageSize)
                parcelableStatus.is_gap = homeInsertGap

                val currentTimeMillis = System.currentTimeMillis()
                if (lastStatusTimestamps[0] >= parcelableStatus.timestamp) {
                    val extraValue = (currentTimeMillis - lastStatusTimestamps[1]).coerceAtMost(499)
                    parcelableStatus.position_key = parcelableStatus.timestamp + extraValue
                } else {
                    parcelableStatus.position_key = parcelableStatus.timestamp
                }
                parcelableStatus.inserted_date = currentTimeMillis

                lastStatusTimestamps[0] = parcelableStatus.position_key
                lastStatusTimestamps[1] = parcelableStatus.inserted_date

                val values = ObjectCursor.valuesCreatorFrom(ParcelableStatus::class.java)
                        .create(parcelableStatus)
                context.contentResolver.insert(Statuses.CONTENT_URI, values)
                homeInsertGap = false
                return true
            }

            override fun onActivityAboutMe(activity: Activity): Boolean {
                if (!accountPreferences.isStreamInteractionsEnabled) {
                    interactionsInsertGap = true
                    return false
                }
                if (isOfficial) {
                    // Wait for 30 seconds to avoid rate limit
                    if (canGetInteractions) {
                        handler.post { getInteractions() }
                        canGetInteractions = false
                        handler.postDelayed(interactionsTimeoutRunnable, TimeUnit.SECONDS.toMillis(30))
                    }
                } else {
                    val insertGap: Boolean
                    if (activity.action in Activity.Action.MENTION_ACTIONS) {
                        insertGap = interactionsInsertGap
                        interactionsInsertGap = false
                    } else {
                        insertGap = false
                    }
                    val curActivity = activity.toParcelable(account, insertGap, profileImageSize)
                    curActivity.account_color = account.color
                    curActivity.position_key = curActivity.timestamp
                    var updateId = -1L
                    if (curActivity.action !in Activity.Action.MENTION_ACTIONS) {
                        /* Merge two activities if:
                         * * Not mention/reply/quote
                         * * Same action
                         * * Same source or target or target object
                         */
                        val lastActivity = this.lastActivityAboutMe
                        if (lastActivity != null && curActivity.action == lastActivity.action) {
                            if (curActivity.reachedCountLimit) {
                                // Skip if more than 10 sources/targets/target_objects
                            } else if (curActivity.isSameSources(lastActivity)) {
                                curActivity.prependTargets(lastActivity)
                                curActivity.prependTargetObjects(lastActivity)
                                updateId = lastActivity._id
                            } else if (curActivity.isSameTarget(lastActivity)) {
                                curActivity.prependSources(lastActivity)
                                curActivity.prependTargets(lastActivity)
                                updateId = lastActivity._id
                            } else if (curActivity.isSameTargetObject(lastActivity)) {
                                curActivity.prependSources(lastActivity)
                                curActivity.prependTargets(lastActivity)
                                updateId = lastActivity._id
                            }
                            if (updateId > 0) {
                                curActivity.min_position = lastActivity.min_position
                                curActivity.min_sort_position = lastActivity.min_sort_position
                            }
                        }
                    }
                    val values = ObjectCursor.valuesCreatorFrom(ParcelableActivity::class.java)
                            .create(curActivity)
                    val resolver = context.contentResolver
                    if (updateId > 0) {
                        val where = Expression.equals(Activities._ID, updateId).sql
                        resolver.update(Activities.AboutMe.CONTENT_URI, values, where, null)
                        curActivity._id = updateId
                    } else {
                        val uri = resolver.insert(Activities.AboutMe.CONTENT_URI, values)
                        if (uri != null) {
                            curActivity._id = uri.lastPathSegment.toLongOr(-1L)
                        }
                    }
                    lastActivityAboutMe = curActivity
                }
                return true
            }

            @WorkerThread
            override fun onDirectMessage(directMessage: DirectMessage): Boolean {
                if (!accountPreferences.isStreamDirectMessagesEnabled) {
                    return false
                }
                if (canGetMessages) {
                    handler.post { getMessages() }
                    canGetMessages = false
                    val timeout = TimeUnit.SECONDS.toMillis(if (isOfficial) 30 else 90)
                    handler.postDelayed(messagesTimeoutRunnable, timeout)
                }
                return true
            }

            override fun onAllStatus(status: Status) {
                if (!accountPreferences.isStreamNotificationUsersEnabled) {
                    return
                }
                val user = status.user ?: return
                val userKey = user.key
                val where = Expression.and(Expression.equalsArgs(CachedRelationships.ACCOUNT_KEY),
                        Expression.equalsArgs(CachedRelationships.USER_KEY),
                        Expression.equals(CachedRelationships.NOTIFICATIONS_ENABLED, 1)).sql
                val whereArgs = arrayOf(account.key.toString(), userKey.toString())
                if (context.contentResolver.queryCount(CachedRelationships.CONTENT_URI,
                        where, whereArgs) <= 0) return

                contentNotificationManager.showUserNotification(account.key, status, userKey)
            }

            override fun onStatusDeleted(event: DeletionEvent): Boolean {
                val deleteWhere = Expression.and(Expression.likeRaw(Columns.Column(Statuses.ACCOUNT_KEY), "'%@'||?"),
                        Expression.equalsArgs(Columns.Column(Statuses.ID))).sql
                val deleteWhereArgs = arrayOf(account.key.host, event.id)
                context.contentResolver.delete(Statuses.CONTENT_URI, deleteWhere, deleteWhereArgs)
                return true
            }

            override fun onDisconnectNotice(code: Int, reason: String?): Boolean {
                disconnect()
                return true
            }

            override fun onException(ex: Throwable): Boolean {
                DebugLog.w(LOGTAG, msg = "Exception for ${account.key}", tr = ex)
                return true
            }

            override fun onUnhandledEvent(obj: TwitterStreamObject, json: String) {
                DebugLog.d(LOGTAG, msg = "Unhandled event ${obj.determine()} for ${account.key}: $json")
            }

            @UiThread
            private fun getInteractions() {
                val task = GetActivitiesAboutMeTask(context)
                task.params = object : RefreshTaskParam {
                    override val accountKeys: Array<UserKey> = arrayOf(account.key)

                    override val pagination by lazy {
                        val keys = accountKeys.toNulls()
                        val sinceIds = DataStoreUtils.getRefreshNewestActivityMaxPositions(context,
                                Activities.AboutMe.CONTENT_URI, keys)
                        val sinceSortIds = DataStoreUtils.getRefreshNewestActivityMaxSortPositions(context,
                                Activities.AboutMe.CONTENT_URI, keys)
                        return@lazy Array(keys.size) { idx ->
                            SinceMaxPagination.sinceId(sinceIds[idx], sinceSortIds[idx])
                        }
                    }

                }
                TaskStarter.execute(task)
            }

            @UiThread
            private fun getMessages() {
                val task = GetMessagesTask(context)
                task.params = object : GetMessagesTask.RefreshMessagesTaskParam(context) {
                    override val accountKeys: Array<UserKey> = arrayOf(account.key)
                }
                TaskStarter.execute(task)
            }
        }

        override fun createStreamingInstance(): TwitterUserStream {
            return account.newMicroBlogInstance(context, cls = TwitterUserStream::class.java)
        }

        override fun TwitterUserStream.beginStreaming() {
            getUserStream(StreamWith.USER, callback)
        }

        override fun onCancelled() {
            callback.disconnect()
        }

    }

    companion object {

        private val NOTIFICATION_SERVICE_STARTED = 1

        fun startOrStopService(context: Context) {
            val streamingIntent = Intent(context, StreamingService::class.java)
            val holder = DependencyHolder.get(context)
            try {
                if (holder.activityTracker.isHomeActivityLaunched) {
                    context.startService(streamingIntent)
                } else {
                    context.stopService(streamingIntent)
                }
            } catch (e: IllegalStateException) {
                // This shouldn't happen, catch it.
            }
        }
    }

}


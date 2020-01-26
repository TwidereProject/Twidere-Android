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


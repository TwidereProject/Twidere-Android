package org.mariotaku.twidere.service

import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.util.SimpleArrayMap
import android.util.Log
import org.mariotaku.ktextension.addOnAccountsUpdatedListenerSafe
import org.mariotaku.ktextension.removeOnAccountsUpdatedListenerSafe
import org.mariotaku.microblog.library.twitter.TwitterUserStream
import org.mariotaku.microblog.library.twitter.annotation.StreamWith
import org.mariotaku.microblog.library.twitter.callback.UserStreamCallback
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.microblog.library.twitter.model.DirectMessage
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.activity.SettingsActivity
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.AccountPreferences
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.account.cred.OAuthCredentials
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.DebugLog
import org.mariotaku.twidere.util.TwidereArrayUtils
import org.mariotaku.twidere.util.streaming.TwitterTimelineStreamCallback

class StreamingService : Service() {

    private val callbacks = SimpleArrayMap<UserKey, UserStreamCallback>()

    private var notificationManager: NotificationManager? = null

    private var accountKeys: Array<UserKey>? = null

    private val accountChangeObserver = OnAccountsUpdateListener {
        if (!TwidereArrayUtils.contentMatch(accountKeys, DataStoreUtils.getActivatedAccountKeys(this@StreamingService))) {
            initStreaming()
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        DebugLog.d(LOGTAG, "Stream service started.")
        initStreaming()
        AccountManager.get(this).addOnAccountsUpdatedListenerSafe(accountChangeObserver, updateImmediately = false)
    }

    override fun onDestroy() {
        clearTwitterInstances()
        AccountManager.get(this).removeOnAccountsUpdatedListenerSafe(accountChangeObserver)
        DebugLog.d(LOGTAG, "Stream service stopped.")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun clearTwitterInstances() {
        var i = 0
        val j = callbacks.size()
        while (i < j) {
            Thread(ShutdownStreamTwitterRunnable(callbacks.valueAt(i))).start()
            i++
        }
        callbacks.clear()
        notificationManager!!.cancel(NOTIFICATION_SERVICE_STARTED)
    }

    private fun initStreaming() {
        if (!BuildConfig.DEBUG) return
        setTwitterInstances()
        updateStreamState()
    }

    private fun setTwitterInstances(): Boolean {
        val accountsList = AccountUtils.getAllAccountDetails(AccountManager.get(this), true).filter { it.credentials is OAuthCredentials }
        val accountKeys = accountsList.map { it.key }.toTypedArray()
        val activatedPreferences = AccountPreferences.getAccountPreferences(this, accountKeys)
        DebugLog.d(LOGTAG, "Setting up twitter stream instances")
        this.accountKeys = accountKeys
        clearTwitterInstances()
        var result = false
        accountsList.forEachIndexed { i, account ->
            val preferences = activatedPreferences[i]
            if (!preferences.isStreamingEnabled) {
                return@forEachIndexed
            }
            val twitter = account.newMicroBlogInstance(context = this, cls = TwitterUserStream::class.java)
            val callback = TwidereUserStreamCallback(this, account)
            callbacks.put(account.key, callback)
            object : Thread() {
                override fun run() {
                    twitter.getUserStream(StreamWith.USER, callback)
                    Log.d(LOGTAG, String.format("Stream %s disconnected", account.key))
                    callbacks.remove(account.key)
                    updateStreamState()
                }
            }.start()
            result = result or true
        }
        return result
    }

    private fun updateStreamState() {
        if (callbacks.size() > 0) {
            val intent = Intent(this, SettingsActivity::class.java)
            val contentIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            val contentTitle = getString(R.string.app_name)
            val contentText = getString(R.string.timeline_streaming_running)
            val builder = NotificationCompat.Builder(this)
            builder.setOngoing(true)
            builder.setSmallIcon(R.drawable.ic_stat_refresh)
            builder.setContentTitle(contentTitle)
            builder.setContentText(contentText)
            builder.setContentIntent(contentIntent)
            notificationManager!!.notify(NOTIFICATION_SERVICE_STARTED, builder.build())
        } else {
            notificationManager!!.cancel(NOTIFICATION_SERVICE_STARTED)
        }
    }

    internal class ShutdownStreamTwitterRunnable(private val callback: UserStreamCallback?) : Runnable {

        override fun run() {
            if (callback == null) return
            Log.d(LOGTAG, "Disconnecting stream")
            callback.disconnect()
        }

    }

    internal class TwidereUserStreamCallback(
            private val context: Context,
            private val account: AccountDetails
    ) : TwitterTimelineStreamCallback(account.key.id) {
        override fun onHomeTimeline(status: Status): Boolean = true

        override fun onActivityAboutMe(activity: Activity): Boolean = true

        override fun onDirectMessage(directMessage: DirectMessage): Boolean = true

        private var statusStreamStarted: Boolean = false
        private val mentionsStreamStarted: Boolean = false

    }

    companion object {

        private val NOTIFICATION_SERVICE_STARTED = 1

    }

}

package org.mariotaku.twidere.service

import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.util.SimpleArrayMap
import android.util.Log
import org.mariotaku.ktextension.addOnAccountsUpdatedListenerSafe
import org.mariotaku.ktextension.removeOnAccountsUpdatedListenerSafe
import org.mariotaku.microblog.library.twitter.TwitterUserStream
import org.mariotaku.microblog.library.twitter.UserStreamCallback
import org.mariotaku.microblog.library.twitter.model.DeletionEvent
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.microblog.library.twitter.model.Warning
import org.mariotaku.sqliteqb.library.Expression
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
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.DebugLog
import org.mariotaku.twidere.util.TwidereArrayUtils
import java.io.IOException

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
                    twitter.getUserStream(callback)
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
    ) : UserStreamCallback() {

        private var statusStreamStarted: Boolean = false
        private val mentionsStreamStarted: Boolean = false

        override fun onConnected() = true

        override fun onBlock(source: User, blockedUser: User): Boolean {
            val message = String.format("%s blocked %s", source.screenName, blockedUser.screenName)
            Log.d(LOGTAG, message)
            return true
        }

        override fun onDirectMessageDeleted(event: DeletionEvent): Boolean {
            val where = Expression.equalsArgs(Messages.MESSAGE_ID).sql
            val whereArgs = arrayOf(event.id)
            context.contentResolver.delete(Messages.CONTENT_URI, where, whereArgs)
            return true
        }

        override fun onStatusDeleted(event: DeletionEvent): Boolean {
            val statusId = event.id
            context.contentResolver.delete(Statuses.CONTENT_URI, Expression.equalsArgs(Statuses.STATUS_ID).sql,
                    arrayOf(statusId))
            context.contentResolver.delete(Activities.AboutMe.CONTENT_URI, Expression.equalsArgs(Activities.STATUS_ID).sql,
                    arrayOf(statusId))
            return true
        }

        override fun onFavorite(source: User, target: User, targetStatus: Status): Boolean {
            val message = String.format("%s favorited %s's tweet: %s", source.screenName,
                    target.screenName, targetStatus.extendedText)
            Log.d(LOGTAG, message)
            return true
        }

        override fun onFollow(source: User, followedUser: User): Boolean {
            val message = String
                    .format("%s followed %s", source.screenName, followedUser.screenName)
            Log.d(LOGTAG, message)
            return true
        }

        override fun onFriendList(friendIds: Array<String>): Boolean {
            return true
        }

        override fun onScrubGeo(userId: String, upToStatusId: String): Boolean {
            val resolver = context.contentResolver

            val where = Expression.and(Expression.equalsArgs(Statuses.USER_KEY),
                    Expression.greaterEqualsArgs(Statuses.SORT_ID)).sql
            val whereArgs = arrayOf(userId, upToStatusId)
            val values = ContentValues()
            values.putNull(Statuses.LOCATION)
            resolver.update(Statuses.CONTENT_URI, values, where, whereArgs)
            return true
        }

        override fun onStallWarning(warn: Warning): Boolean {
            return true
        }

        @Throws(IOException::class)
        override fun onStatus(status: Status): Boolean {
            return true
        }

        override fun onUnblock(source: User, unblockedUser: User): Boolean {
            val message = String.format("%s unblocked %s", source.screenName,
                    unblockedUser.screenName)
            Log.d(LOGTAG, message)
            return true
        }

        override fun onUnfavorite(source: User, target: User, targetStatus: Status): Boolean {
            val message = String.format("%s unfavorited %s's tweet: %s", source.screenName,
                    target.screenName, targetStatus.extendedText)
            Log.d(LOGTAG, message)
            return true
        }

    }

    companion object {

        private val NOTIFICATION_SERVICE_STARTED = 1

    }

}

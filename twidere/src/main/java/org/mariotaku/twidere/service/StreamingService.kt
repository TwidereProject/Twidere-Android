package org.mariotaku.twidere.service

import android.accounts.AccountManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.util.SimpleArrayMap
import android.text.TextUtils
import android.util.Log
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.TwitterUserStream
import org.mariotaku.microblog.library.twitter.UserStreamCallback
import org.mariotaku.microblog.library.twitter.model.*
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.TwidereConstants.QUERY_PARAM_NOTIFY
import org.mariotaku.twidere.activity.SettingsActivity
import org.mariotaku.twidere.extension.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.AccountPreferences
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.account.cred.OAuthCredentials
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.util.ContentValuesCreator
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.TwidereArrayUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset

class StreamingService : Service() {

    private val mCallbacks = SimpleArrayMap<UserKey, UserStreamCallback>()
    private var mResolver: ContentResolver? = null

    private var mNotificationManager: NotificationManager? = null

    private var mAccountKeys: Array<UserKey>? = null

    private val mAccountChangeObserver = object : ContentObserver(Handler()) {

        override fun onChange(selfChange: Boolean) {
            onChange(selfChange, null)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            if (!TwidereArrayUtils.contentMatch(mAccountKeys, DataStoreUtils.getActivatedAccountKeys(this@StreamingService))) {
                initStreaming()
            }
        }

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mResolver = contentResolver
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (BuildConfig.DEBUG) {
            Log.d(LOGTAG, "Stream service started.")
        }
        initStreaming()
        mResolver!!.registerContentObserver(Accounts.CONTENT_URI, true, mAccountChangeObserver)
    }

    override fun onDestroy() {
        clearTwitterInstances()
        mResolver!!.unregisterContentObserver(mAccountChangeObserver)
        if (BuildConfig.DEBUG) {
            Log.d(LOGTAG, "Stream service stopped.")
        }
        super.onDestroy()
    }

    private fun clearTwitterInstances() {
        var i = 0
        val j = mCallbacks.size()
        while (i < j) {
            Thread(ShutdownStreamTwitterRunnable(mCallbacks.valueAt(i))).start()
            i++
        }
        mCallbacks.clear()
        mNotificationManager!!.cancel(NOTIFICATION_SERVICE_STARTED)
    }

    private fun initStreaming() {
        if (!BuildConfig.DEBUG) return
        setTwitterInstances()
        updateStreamState()
    }

    private fun setTwitterInstances(): Boolean {
        val accountsList = AccountUtils.getAllAccountDetails(AccountManager.get(this)).filter { it.credentials is OAuthCredentials }
        val accountKeys = accountsList.map { it.key }.toTypedArray()
        val activatedPreferences = AccountPreferences.getAccountPreferences(this, accountKeys)
        if (BuildConfig.DEBUG) {
            Log.d(LOGTAG, "Setting up twitter stream instances")
        }
        mAccountKeys = accountKeys
        clearTwitterInstances()
        var result = false
        accountsList.forEachIndexed { i, account ->
            val preferences = activatedPreferences[i]
            if (!preferences.isStreamingEnabled) {
                return@forEachIndexed
            }
            val twitter = account.credentials.newMicroBlogInstance(context = this, cls = TwitterUserStream::class.java)
            val callback = TwidereUserStreamCallback(this, account)
            mCallbacks.put(account.key, callback)
            object : Thread() {
                override fun run() {
                    twitter.getUserStream(callback)
                    Log.d(LOGTAG, String.format("Stream %s disconnected", account.key))
                    mCallbacks.remove(account.key)
                    updateStreamState()
                }
            }.start()
            result = result or true
        }
        return result
    }

    private fun updateStreamState() {
        if (mCallbacks.size() > 0) {
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
            mNotificationManager!!.notify(NOTIFICATION_SERVICE_STARTED, builder.build())
        } else {
            mNotificationManager!!.cancel(NOTIFICATION_SERVICE_STARTED)
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
        private val resolver: ContentResolver

        private var statusStreamStarted: Boolean = false
        private val mentionsStreamStarted: Boolean = false

        init {
            resolver = context.contentResolver
        }

        override fun onConnected() {

        }

        override fun onBlock(source: User, blockedUser: User) {
            val message = String.format("%s blocked %s", source.screenName, blockedUser.screenName)
            Log.d(LOGTAG, message)
        }

        override fun onDirectMessageDeleted(event: DeletionEvent) {
            val where = Expression.equalsArgs(DirectMessages.MESSAGE_ID).sql
            val whereArgs = arrayOf(event.id)
            for (uri in MESSAGES_URIS) {
                resolver.delete(uri, where, whereArgs)
            }
        }

        override fun onStatusDeleted(event: DeletionEvent) {
            val statusId = event.id
            resolver.delete(Statuses.CONTENT_URI, Expression.equalsArgs(Statuses.STATUS_ID).sql,
                    arrayOf(statusId))
            resolver.delete(Activities.AboutMe.CONTENT_URI, Expression.equalsArgs(Activities.STATUS_ID).sql,
                    arrayOf(statusId))
        }

        @Throws(IOException::class)
        override fun onDirectMessage(directMessage: DirectMessage?) {
            if (directMessage == null || directMessage.id == null) return
            val where = Expression.and(Expression.equalsArgs(DirectMessages.ACCOUNT_KEY),
                    Expression.equalsArgs(DirectMessages.MESSAGE_ID)).sql
            val whereArgs = arrayOf(account.key.toString(), directMessage.id)
            for (uri in MESSAGES_URIS) {
                resolver.delete(uri, where, whereArgs)
            }
            val sender = directMessage.sender
            val recipient = directMessage.recipient
            if (TextUtils.equals(sender.id, account.key.id)) {
                val values = ContentValuesCreator.createDirectMessage(directMessage,
                        account.key, true)
                if (values != null) {
                    resolver.insert(DirectMessages.Outbox.CONTENT_URI, values)
                }
            }
            if (TextUtils.equals(recipient.id, account.key.id)) {
                val values = ContentValuesCreator.createDirectMessage(directMessage,
                        account.key, false)
                val builder = DirectMessages.Inbox.CONTENT_URI.buildUpon()
                builder.appendQueryParameter(QUERY_PARAM_NOTIFY, "true")
                if (values != null) {
                    resolver.insert(builder.build(), values)
                }
            }

        }

        override fun onException(ex: Throwable) {
            if (ex is MicroBlogException) {
                Log.w(LOGTAG, String.format("Error %d", ex.statusCode), ex)
                val response = ex.httpResponse
                if (response != null) {
                    try {
                        val body = response.body
                        if (body != null) {
                            val os = ByteArrayOutputStream()
                            body.writeTo(os)
                            val charsetName: String
                            val contentType = body.contentType()
                            if (contentType != null) {
                                val charset = contentType.charset
                                if (charset != null) {
                                    charsetName = charset.name()
                                } else {
                                    charsetName = Charset.defaultCharset().name()
                                }
                            } else {
                                charsetName = Charset.defaultCharset().name()
                            }
                            Log.w(LOGTAG, os.toString(charsetName))
                        }
                    } catch (e: IOException) {
                        Log.w(LOGTAG, e)
                    }

                }
            } else {
                Log.w(LOGTAG, ex)
            }
        }

        override fun onFavorite(source: User, target: User, targetStatus: Status) {
            val message = String.format("%s favorited %s's tweet: %s", source.screenName,
                    target.screenName, targetStatus.extendedText)
            Log.d(LOGTAG, message)
        }

        override fun onFollow(source: User, followedUser: User) {
            val message = String
                    .format("%s followed %s", source.screenName, followedUser.screenName)
            Log.d(LOGTAG, message)
        }

        override fun onFriendList(friendIds: LongArray) {

        }

        override fun onScrubGeo(userId: Long, upToStatusId: Long) {
            val where = Expression.and(Expression.equalsArgs(Statuses.USER_KEY),
                    Expression.greaterEqualsArgs(Statuses.SORT_ID)).sql
            val whereArgs = arrayOf(userId.toString(), upToStatusId.toString())
            val values = ContentValues()
            values.putNull(Statuses.LOCATION)
            resolver.update(Statuses.CONTENT_URI, values, where, whereArgs)
        }

        override fun onStallWarning(warn: Warning) {

        }

        @Throws(IOException::class)
        override fun onStatus(status: Status) {
            val values = ContentValuesCreator.createStatus(status, account.key)
            if (!statusStreamStarted) {
                statusStreamStarted = true
                values.put(Statuses.IS_GAP, true)
            }
            val where = Expression.and(Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY),
                    Expression.equalsArgs(Statuses.STATUS_ID)).sql
            val whereArgs = arrayOf(account.key.toString(), status.id.toString())
            resolver.delete(Statuses.CONTENT_URI, where, whereArgs)
            resolver.delete(Mentions.CONTENT_URI, where, whereArgs)
            resolver.insert(Statuses.CONTENT_URI, values)
            val rt = status.retweetedStatus
            if (rt != null && rt.extendedText.contains("@" + account.user.screen_name) || rt == null && status.extendedText.contains("@" + account.user.screen_name)) {
                resolver.insert(Mentions.CONTENT_URI, values)
            }
        }

        override fun onTrackLimitationNotice(numberOfLimitedStatuses: Int) {

        }

        override fun onUnblock(source: User, unblockedUser: User) {
            val message = String.format("%s unblocked %s", source.screenName,
                    unblockedUser.screenName)
            Log.d(LOGTAG, message)
        }

        override fun onUnfavorite(source: User, target: User, targetStatus: Status) {
            val message = String.format("%s unfavorited %s's tweet: %s", source.screenName,
                    target.screenName, targetStatus.extendedText)
            Log.d(LOGTAG, message)
        }

        override fun onUserListCreation(listOwner: User, list: UserList) {

        }

        override fun onUserListDeletion(listOwner: User, list: UserList) {

        }

        override fun onUserListMemberAddition(addedMember: User, listOwner: User, list: UserList) {

        }

        override fun onUserListMemberDeletion(deletedMember: User, listOwner: User, list: UserList) {

        }

        override fun onUserListSubscription(subscriber: User, listOwner: User, list: UserList) {

        }

        override fun onUserListUnsubscription(subscriber: User, listOwner: User, list: UserList) {

        }

        override fun onUserListUpdate(listOwner: User, list: UserList) {

        }

        override fun onUserProfileUpdate(updatedUser: User) {

        }
    }

    companion object {

        private val NOTIFICATION_SERVICE_STARTED = 1

        private val MESSAGES_URIS = arrayOf(DirectMessages.Inbox.CONTENT_URI, DirectMessages.Outbox.CONTENT_URI)
    }

}

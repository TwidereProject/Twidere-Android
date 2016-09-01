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

import android.app.IntentService
import android.app.Notification
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.BaseColumns
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationCompat.Builder
import android.text.TextUtils
import android.util.Log
import android.util.Pair
import android.widget.Toast
import com.twitter.Extractor
import edu.tsinghua.hotmobi.HotMobiLogger
import edu.tsinghua.hotmobi.model.TimelineType
import edu.tsinghua.hotmobi.model.TweetEvent
import org.mariotaku.abstask.library.ManualTaskStarter
import org.mariotaku.ktextension.configure
import org.mariotaku.ktextension.toLong
import org.mariotaku.ktextension.toTypedArray
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.TwitterUpload
import org.mariotaku.microblog.library.twitter.model.MediaUploadResponse
import org.mariotaku.microblog.library.twitter.model.MediaUploadResponse.ProcessingInfo
import org.mariotaku.restfu.http.ContentType
import org.mariotaku.restfu.http.mime.Body
import org.mariotaku.restfu.http.mime.FileBody
import org.mariotaku.restfu.http.mime.SimpleBody
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.draft.SendDirectMessageActionExtra
import org.mariotaku.twidere.model.util.ParcelableAccountUtils
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils
import org.mariotaku.twidere.model.util.ParcelableDirectMessageUtils
import org.mariotaku.twidere.model.util.ParcelableStatusUpdateUtils
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts
import org.mariotaku.twidere.task.twitter.UpdateStatusTask
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.util.io.ContentLengthInputStream.ReadListener
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BackgroundOperationService : IntentService("background_operation"), Constants {


    private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    @Inject
    lateinit var twitterWrapper: AsyncTwitterWrapper
    @Inject
    lateinit var notificationManager: NotificationManagerWrapper
    @Inject
    lateinit var validator: TwidereValidator
    @Inject
    lateinit var extractor: Extractor
    @Inject
    lateinit var mediaLoader: MediaLoaderWrapper


    override fun onCreate() {
        super.onCreate()
        GeneralComponentHelper.build(this).inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

    fun showErrorMessage(message: CharSequence, longMessage: Boolean) {
        handler.post { Utils.showErrorMessage(this@BackgroundOperationService, message, longMessage) }
    }

    fun showErrorMessage(actionRes: Int, e: Exception?, longMessage: Boolean) {
        handler.post { Utils.showErrorMessage(this@BackgroundOperationService, actionRes, e, longMessage) }
    }

    fun showErrorMessage(actionRes: Int, message: String, longMessage: Boolean) {
        handler.post { Utils.showErrorMessage(this@BackgroundOperationService, actionRes, message, longMessage) }
    }

    fun showOkMessage(messageRes: Int, longMessage: Boolean) {
        showToast(getString(messageRes), longMessage)
    }

    private fun showToast(message: CharSequence, longMessage: Boolean) {
        handler.post { Toast.makeText(this@BackgroundOperationService, message, if (longMessage) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show() }
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return
        when (action) {
            INTENT_ACTION_UPDATE_STATUS -> {
                handleUpdateStatusIntent(intent)
            }
            INTENT_ACTION_SEND_DIRECT_MESSAGE -> {
                handleSendDirectMessageIntent(intent)
            }
            INTENT_ACTION_DISCARD_DRAFT -> {
                handleDiscardDraftIntent(intent)
            }
            INTENT_ACTION_SEND_DRAFT -> {
                handleSendDraftIntent(intent)
            }
        }
    }

    private fun handleSendDraftIntent(intent: Intent) {
        val uri = intent.data ?: return
        notificationManager.cancel(uri.toString(), NOTIFICATION_ID_DRAFTS)
        val draftId = uri.lastPathSegment.toLong(-1)
        if (draftId == -1L) return
        val where = Expression.equals(Drafts._ID, draftId)
        val cr = contentResolver
        val c = cr.query(Drafts.CONTENT_URI, Drafts.COLUMNS, where.sql, null, null) ?: return
        val i = DraftCursorIndices(c)
        val item: Draft
        try {
            if (!c.moveToFirst()) return
            item = i.newObject(c)
        } finally {
            c.close()
        }
        cr.delete(Drafts.CONTENT_URI, where.sql, null)
        if (TextUtils.isEmpty(item.action_type)) {
            item.action_type = Draft.Action.UPDATE_STATUS
        }
        when (item.action_type) {
            Draft.Action.UPDATE_STATUS_COMPAT_1, Draft.Action.UPDATE_STATUS_COMPAT_2, Draft.Action.UPDATE_STATUS, Draft.Action.REPLY, Draft.Action.QUOTE -> {
                updateStatuses(item.action_type, ParcelableStatusUpdateUtils.fromDraftItem(this, item))
            }
            Draft.Action.SEND_DIRECT_MESSAGE_COMPAT, Draft.Action.SEND_DIRECT_MESSAGE -> {
                val recipientId = (item.action_extras as? SendDirectMessageActionExtra)?.recipientId ?: return
                if (item.account_keys?.isEmpty() ?: true) {
                    return
                }
                val accountKey = item.account_keys!!.first()
                val imageUri = item.media.firstOrNull()?.uri
                sendMessage(accountKey, recipientId, item.text, imageUri)
            }
        }
    }

    private fun handleDiscardDraftIntent(intent: Intent) {
        val data = intent.data ?: return
        notificationManager.cancel(data.toString(), NOTIFICATION_ID_DRAFTS)
        val id = data.lastPathSegment.toLong(-1)
        val where = Expression.equals(Drafts._ID, id)
        contentResolver.delete(Drafts.CONTENT_URI, where.sql, null)
    }

    private fun handleSendDirectMessageIntent(intent: Intent) {
        val accountId = intent.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)
        val recipientId = intent.getStringExtra(EXTRA_RECIPIENT_ID)
        val text = intent.getStringExtra(EXTRA_TEXT)
        val imageUri = intent.getStringExtra(EXTRA_IMAGE_URI)
        if (accountId == null || recipientId == null || text == null) return
        sendMessage(accountId, recipientId, text, imageUri)
    }

    private fun sendMessage(accountId: UserKey, recipientId: String,
                            text: String, imageUri: String?) {
        val title = getString(R.string.sending_direct_message)
        val builder = Builder(this)
        builder.setSmallIcon(R.drawable.ic_stat_send)
        builder.setProgress(100, 0, true)
        builder.setTicker(title)
        builder.setContentTitle(title)
        builder.setContentText(text)
        builder.setCategory(NotificationCompat.CATEGORY_PROGRESS)
        builder.setOngoing(true)
        val notification = builder.build()
        startForeground(NOTIFICATION_ID_SEND_DIRECT_MESSAGE, notification)
        val result = sendDirectMessage(builder, accountId,
                recipientId, text, imageUri)

        val resolver = contentResolver
        if (result.hasData()) {
            val message = result.data
            val values = ContentValuesCreator.createDirectMessage(message)
            val deleteWhere = Expression.and(Expression.equalsArgs(DirectMessages.ACCOUNT_KEY),
                    Expression.equalsArgs(DirectMessages.MESSAGE_ID)).sql
            val deleteWhereArgs = arrayOf(message!!.account_key.toString(), message.id)
            resolver.delete(DirectMessages.Outbox.CONTENT_URI, deleteWhere, deleteWhereArgs)
            resolver.insert(DirectMessages.Outbox.CONTENT_URI, values)
            showOkMessage(R.string.direct_message_sent, false)
        } else {
            val values = ContentValuesCreator.createMessageDraft(accountId, recipientId, text, imageUri)
            resolver.insert(Drafts.CONTENT_URI, values)
            showErrorMessage(R.string.action_sending_direct_message, result.exception, true)
        }
        stopForeground(false)
        notificationManager.cancel(NOTIFICATION_ID_SEND_DIRECT_MESSAGE)
    }

    private fun handleUpdateStatusIntent(intent: Intent) {
        val status = intent.getParcelableExtra<ParcelableStatusUpdate>(EXTRA_STATUS)
        val statusParcelables = intent.getParcelableArrayExtra(EXTRA_STATUSES)
        val statuses: Array<ParcelableStatusUpdate>
        if (statusParcelables != null) {
            statuses = statusParcelables.toTypedArray(ParcelableStatusUpdate.CREATOR)
        } else if (status != null) {
            statuses = arrayOf(status)
        } else
            return
        @Draft.Action
        val actionType = intent.getStringExtra(EXTRA_ACTION)
        updateStatuses(actionType, *statuses)
    }

    private fun updateStatuses(@Draft.Action actionType: String, vararg statuses: ParcelableStatusUpdate) {
        val context = this
        val builder = Builder(context)
        startForeground(NOTIFICATION_ID_UPDATE_STATUS, updateUpdateStatusNotification(context,
                builder, 0, null))
        for (item in statuses) {
            val task = UpdateStatusTask(context, object : UpdateStatusTask.StateCallback {

                @WorkerThread
                override fun onStartUploadingMedia() {
                    startForeground(NOTIFICATION_ID_UPDATE_STATUS, updateUpdateStatusNotification(context,
                            builder, 0, item))
                }

                @WorkerThread
                override fun onUploadingProgressChanged(index: Int, current: Long, total: Long) {
                    val progress = (current * 100 / total).toInt()
                    startForeground(NOTIFICATION_ID_UPDATE_STATUS, updateUpdateStatusNotification(context,
                            builder, progress, item))
                }

                @WorkerThread
                override fun onShorteningStatus() {
                    startForeground(NOTIFICATION_ID_UPDATE_STATUS, updateUpdateStatusNotification(context,
                            builder, 0, item))
                }

                @WorkerThread
                override fun onUpdatingStatus() {
                    startForeground(NOTIFICATION_ID_UPDATE_STATUS, updateUpdateStatusNotification(context,
                            builder, 0, item))
                }

                @UiThread
                override fun afterExecute(handler: Context?, result: UpdateStatusTask.UpdateStatusResult) {
                    var failed = false
                    val exception = result.exception
                    val exceptions = result.exceptions
                    if (exception != null) {
                        Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                        failed = true
                        Log.w(LOGTAG, exception)
                    } else for (e in exceptions) {
                        if (e != null) {
                            // Show error
                            var errorMessage = Utils.getErrorMessage(context, e)
                            if (TextUtils.isEmpty(errorMessage)) {
                                errorMessage = context.getString(R.string.status_not_updated)
                            }
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            failed = true
                            break
                        }
                    }
                    if (!failed) {
                        Toast.makeText(context, R.string.status_updated, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun beforeExecute() {

                }
            })
            task.callback = this
            task.params = Pair.create(actionType, item)
            handler.post { ManualTaskStarter.invokeBeforeExecute(task) }

            val result = ManualTaskStarter.invokeExecute(task)
            handler.post { ManualTaskStarter.invokeAfterExecute(task, result) }

            if (!result.succeed) {
                contentResolver.insert(Drafts.CONTENT_URI_NOTIFICATIONS, configure(ContentValues()) {
                    put(BaseColumns._ID, result.draftId)
                })
            }
            for (status in result.statuses) {
                if (status == null) continue
                val event = TweetEvent.create(context, status, TimelineType.OTHER)
                event.setAction(TweetEvent.Action.TWEET)
                HotMobiLogger.getInstance(context).log(status.account_key, event)
            }
        }
        if (preferences.getBoolean(KEY_REFRESH_AFTER_TWEET)) {
            handler.post { twitterWrapper.refreshAll() }
        }
        stopForeground(false)
        notificationManager.cancel(NOTIFICATION_ID_UPDATE_STATUS)
    }


    private fun sendDirectMessage(builder: NotificationCompat.Builder,
                                  accountKey: UserKey,
                                  recipientId: String,
                                  text: String,
                                  imageUri: String?): SingleResponse<ParcelableDirectMessage> {
        val credentials = ParcelableCredentialsUtils.getCredentials(this,
                accountKey) ?: return SingleResponse.getInstance<ParcelableDirectMessage>()
        val twitter = MicroBlogAPIFactory.getInstance(this, credentials, true, true)
        val twitterUpload = MicroBlogAPIFactory.getInstance(this, credentials,
                true, true, TwitterUpload::class.java)
        if (twitter == null || twitterUpload == null) return SingleResponse.getInstance<ParcelableDirectMessage>()
        try {
            val directMessage: ParcelableDirectMessage
            when (ParcelableAccountUtils.getAccountType(credentials)) {
                ParcelableAccount.Type.FANFOU -> {
                    if (imageUri != null) {
                        throw MicroBlogException("Can't send image DM on Fanfou")
                    }
                    val dm = twitter.sendFanfouDirectMessage(recipientId, text)
                    directMessage = ParcelableDirectMessageUtils.fromDirectMessage(dm, accountKey, true)
                }
                else -> {
                    if (imageUri != null) {
                        val mediaUri = Uri.parse(imageUri)
                        var body: FileBody? = null
                        try {
                            body = UpdateStatusTask.getBodyFromMedia(this, mediaLoader,
                                    mediaUri, null, ParcelableMedia.Type.IMAGE,
                                    MessageMediaUploadListener(this, notificationManager,
                                            builder, text))
                            val uploadResp = uploadMedia(twitterUpload, body)
                            val response = twitter.sendDirectMessage(recipientId,
                                    text, uploadResp.id)
                            directMessage = ParcelableDirectMessageUtils.fromDirectMessage(response,
                                    accountKey, true)
                        } finally {
                            Utils.closeSilently(body)
                        }
                        val path = Utils.getImagePathFromUri(this, mediaUri)
                        if (path != null) {
                            val file = File(path)
                            if (!file.delete()) {
                                Log.d(LOGTAG, String.format("unable to delete %s", path))
                            }
                        }
                    } else {
                        val response = twitter.sendDirectMessage(recipientId, text)
                        directMessage = ParcelableDirectMessageUtils.fromDirectMessage(response,
                                accountKey, true)
                    }
                }
            }
            Utils.setLastSeen(this, UserKey(recipientId, accountKey.host),
                    System.currentTimeMillis())

            return SingleResponse.getInstance(directMessage)
        } catch (e: IOException) {
            return SingleResponse.getInstance<ParcelableDirectMessage>(e)
        } catch (e: MicroBlogException) {
            return SingleResponse.getInstance<ParcelableDirectMessage>(e)
        }

    }


    @Throws(IOException::class, MicroBlogException::class)
    private fun uploadMedia(upload: TwitterUpload, body: Body): MediaUploadResponse {
        val mediaType = body.contentType().contentType
        val length = body.length()
        val stream = body.stream()
        var response = upload.initUploadMedia(mediaType, length, null)
        val segments = if (length == 0L) 0 else (length / BULK_SIZE + 1).toInt()
        for (segmentIndex in 0..segments - 1) {
            val currentBulkSize = Math.min(BULK_SIZE, length - segmentIndex * BULK_SIZE).toInt()
            val bulk = SimpleBody(ContentType.OCTET_STREAM, null, currentBulkSize.toLong(),
                    stream)
            upload.appendUploadMedia(response.id, segmentIndex, bulk)
        }
        response = upload.finalizeUploadMedia(response.id)
        run {
            var info: ProcessingInfo? = response.processingInfo
            while (info != null && shouldWaitForProcess(info)) {
                val checkAfterSecs = info.checkAfterSecs
                if (checkAfterSecs <= 0) {
                    break
                }
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(checkAfterSecs))
                } catch (e: InterruptedException) {
                    break
                }

                response = upload.getUploadMediaStatus(response.id)
                info = response.processingInfo
            }
        }
        val info = response.processingInfo
        if (info != null && ProcessingInfo.State.FAILED == info.state) {
            val exception = MicroBlogException()
            val errorInfo = info.error
            if (errorInfo != null) {
                exception.errors = arrayOf(errorInfo)
            }
            throw exception
        }
        return response
    }

    private fun shouldWaitForProcess(info: ProcessingInfo): Boolean {
        when (info.state) {
            ProcessingInfo.State.PENDING, ProcessingInfo.State.IN_PROGRESS -> return true
            else -> return false
        }
    }

    internal class MessageMediaUploadListener(private val context: Context, private val manager: NotificationManagerWrapper,
                                              builder: NotificationCompat.Builder, private val message: String) : ReadListener {

        var percent: Int = 0

        private val builder: Builder

        init {
            this.builder = builder
        }

        override fun onRead(length: Long, position: Long) {
            val percent = if (length > 0) (position * 100 / length).toInt() else 0
            if (this.percent != percent) {
                manager.notify(NOTIFICATION_ID_SEND_DIRECT_MESSAGE,
                        updateSendDirectMessageNotification(context, builder, percent, message))
            }
            this.percent = percent
        }
    }

    companion object {
        private val BULK_SIZE = (128 * 1024).toLong() // 128KiB

        private fun updateSendDirectMessageNotification(context: Context,
                                                        builder: NotificationCompat.Builder,
                                                        progress: Int, message: String?): Notification {
            builder.setContentTitle(context.getString(R.string.sending_direct_message))
            if (message != null) {
                builder.setContentText(message)
            }
            builder.setSmallIcon(R.drawable.ic_stat_send)
            builder.setProgress(100, progress, progress >= 100 || progress <= 0)
            builder.setOngoing(true)
            return builder.build()
        }

        private fun updateUpdateStatusNotification(context: Context,
                                                   builder: NotificationCompat.Builder,
                                                   progress: Int,
                                                   status: ParcelableStatusUpdate?): Notification {
            builder.setContentTitle(context.getString(R.string.updating_status_notification))
            if (status != null) {
                builder.setContentText(status.text)
            }
            builder.setSmallIcon(R.drawable.ic_stat_send)
            builder.setProgress(100, progress, progress >= 100 || progress <= 0)
            builder.setOngoing(true)
            return builder.build()
        }

        fun updateStatusesAsync(context: Context, @Draft.Action action: String,
                                vararg statuses: ParcelableStatusUpdate) {
            val intent = Intent(context, BackgroundOperationService::class.java)
            intent.action = INTENT_ACTION_UPDATE_STATUS
            intent.putExtra(EXTRA_STATUSES, statuses)
            intent.putExtra(EXTRA_ACTION, action)
            context.startService(intent)
        }
    }

}

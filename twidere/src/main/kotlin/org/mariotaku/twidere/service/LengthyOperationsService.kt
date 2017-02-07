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

import android.accounts.AccountManager
import android.annotation.SuppressLint
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
import android.widget.Toast
import edu.tsinghua.hotmobi.HotMobiLogger
import edu.tsinghua.hotmobi.model.TimelineType
import edu.tsinghua.hotmobi.model.TweetEvent
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.abstask.library.ManualTaskStarter
import org.mariotaku.ktextension.configure
import org.mariotaku.ktextension.toLong
import org.mariotaku.ktextension.toTypedArray
import org.mariotaku.ktextension.useCursor
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.TwitterUpload
import org.mariotaku.microblog.library.twitter.model.MediaUploadResponse
import org.mariotaku.microblog.library.twitter.model.MediaUploadResponse.ProcessingInfo
import org.mariotaku.restfu.http.ContentType
import org.mariotaku.restfu.http.mime.Body
import org.mariotaku.restfu.http.mime.SimpleBody
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.draft.SendDirectMessageActionExtras
import org.mariotaku.twidere.model.draft.StatusObjectExtras
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.AccountUtils.getAccountDetails
import org.mariotaku.twidere.model.util.ParcelableDirectMessageUtils
import org.mariotaku.twidere.model.util.ParcelableStatusUpdateUtils
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts
import org.mariotaku.twidere.task.twitter.UpdateStatusTask
import org.mariotaku.twidere.util.ContentValuesCreator
import org.mariotaku.twidere.util.NotificationManagerWrapper
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.deleteDrafts
import org.mariotaku.twidere.util.io.ContentLengthInputStream.ReadListener
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Intent service for lengthy operations like update status/send DM.
 */
class LengthyOperationsService : BaseIntentService("lengthy_operations") {

    private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
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

    private fun showErrorMessage(actionRes: Int, e: Exception?, longMessage: Boolean) {
        handler.post { Utils.showErrorMessage(this@LengthyOperationsService, actionRes, e, longMessage) }
    }

    private fun showOkMessage(message: Int, longMessage: Boolean) {
        handler.post { Toast.makeText(this@LengthyOperationsService, message, if (longMessage) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show() }
    }

    private fun handleSendDraftIntent(intent: Intent) {
        val uri = intent.data ?: return
        notificationManager.cancel(uri.toString(), NOTIFICATION_ID_DRAFTS)
        val draftId = uri.lastPathSegment.toLong(-1)
        if (draftId == -1L) return
        val where = Expression.equals(Drafts._ID, draftId)
        @SuppressLint("Recycle")
        val draft: Draft = contentResolver.query(Drafts.CONTENT_URI, Drafts.COLUMNS, where.sql, null, null)?.useCursor {
            val i = DraftCursorIndices(it)
            if (!it.moveToFirst()) return@useCursor null
            return@useCursor i.newObject(it)
        } ?: return

        contentResolver.delete(Drafts.CONTENT_URI, where.sql, null)
        if (TextUtils.isEmpty(draft.action_type)) {
            draft.action_type = Draft.Action.UPDATE_STATUS
        }
        when (draft.action_type) {
            Draft.Action.UPDATE_STATUS_COMPAT_1, Draft.Action.UPDATE_STATUS_COMPAT_2,
            Draft.Action.UPDATE_STATUS, Draft.Action.REPLY, Draft.Action.QUOTE -> {
                updateStatuses(draft.action_type, ParcelableStatusUpdateUtils.fromDraftItem(this, draft))
            }
            Draft.Action.SEND_DIRECT_MESSAGE_COMPAT, Draft.Action.SEND_DIRECT_MESSAGE -> {
                val recipientId = (draft.action_extras as? SendDirectMessageActionExtras)?.recipientId ?: return
                val accountKey = draft.account_keys?.firstOrNull() ?: return
                val imageUri = draft.media.firstOrNull()?.uri
                sendMessage(accountKey, recipientId, draft.text, imageUri)
            }
            Draft.Action.FAVORITE -> {
                performStatusAction(draft) { microBlog, account, status ->
                    if (account.type == AccountType.FANFOU) {
                        microBlog.createFanfouFavorite(status.id)
                    } else {
                        microBlog.createFavorite(status.id)
                    }
                    return@performStatusAction true
                }
            }
            Draft.Action.RETWEET -> {
                performStatusAction(draft) { microBlog, account, status ->
                    microBlog.retweetStatus(status.id)
                    return@performStatusAction true
                }
            }
        }
    }

    @SuppressLint("Recycle")
    private fun handleDiscardDraftIntent(intent: Intent) {
        val data = intent.data ?: return
        task {
            if (deleteDrafts(this, longArrayOf(data.lastPathSegment.toLong(-1))) < 1) {
                throw IOException()
            }
            return@task data
        }.successUi { uri ->
            notificationManager.cancel(data.toString(), NOTIFICATION_ID_DRAFTS)
        }
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
            val message = result.data!!
            val values = ContentValuesCreator.createDirectMessage(message)
            val deleteWhere = Expression.and(Expression.equalsArgs(DirectMessages.ACCOUNT_KEY),
                    Expression.equalsArgs(DirectMessages.MESSAGE_ID)).sql
            val deleteWhereArgs = arrayOf(message.account_key.toString(), message.id)
            resolver.delete(DirectMessages.Outbox.CONTENT_URI, deleteWhere, deleteWhereArgs)
            resolver.insert(DirectMessages.Outbox.CONTENT_URI, values)
            showOkMessage(R.string.message_direct_message_sent, false)
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
                override fun afterExecute(result: UpdateStatusTask.UpdateStatusResult) {
                    var failed = false
                    val exception = result.exception
                    val exceptions = result.exceptions
                    if (exception != null) {
                        val cause = exception.cause
                        if (cause is MicroBlogException) {
                            Toast.makeText(context, cause.errors?.firstOrNull()?.message ?: cause.message,
                                    Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                        }
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
                        Toast.makeText(context, R.string.message_toast_status_updated, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun beforeExecute() {

                }
            })
            task.callback = this
            task.params = Pair(actionType, item)
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
                event.action = TweetEvent.Action.TWEET
                if (item.in_reply_to_status != null && item.in_reply_to_status.user_is_protected) {
                    event.inReplyToId = item.in_reply_to_status.id
                }
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
        val details = AccountUtils.getAccountDetails(AccountManager.get(this),
                accountKey, true) ?: return SingleResponse.getInstance()
        val twitter = details.newMicroBlogInstance(context = this, cls = MicroBlog::class.java)
        val twitterUpload = details.newMicroBlogInstance(context = this, cls = TwitterUpload::class.java)
        try {
            val directMessage: ParcelableDirectMessage
            when (details.type) {
                AccountType.FANFOU -> {
                    if (imageUri != null) {
                        throw MicroBlogException("Can't send image DM on Fanfou")
                    }
                    val dm = twitter.sendFanfouDirectMessage(recipientId, text)
                    directMessage = ParcelableDirectMessageUtils.fromDirectMessage(dm, accountKey, true)
                }
                else -> {
                    if (imageUri != null) {
                        val mediaUri = Uri.parse(imageUri)
                        val listener = MessageMediaUploadListener(this, notificationManager,
                                builder, text)
                        val chucked = details.type == AccountType.TWITTER
                        val uploadResp = UpdateStatusTask.getBodyFromMedia(this, mediaLoader,
                                mediaUri, null, ParcelableMedia.Type.IMAGE, chucked, listener).use { body ->
                            val resp = uploadMedia(twitterUpload, body.body)
                            body.deleteOnSuccess?.forEach { item ->
                                item.delete(this)
                            }
                            return@use resp
                        }
                        val response = twitter.sendDirectMessage(recipientId,
                                text, uploadResp.id)
                        directMessage = ParcelableDirectMessageUtils.fromDirectMessage(response,
                                accountKey, true)
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

    private fun performStatusAction(draft: Draft, action: (MicroBlog, AccountDetails, ParcelableStatus) -> Boolean): Boolean {
        val accountKey = draft.account_keys?.firstOrNull() ?: return false
        val extras = draft.action_extras as? StatusObjectExtras ?: return false
        val account = getAccountDetails(AccountManager.get(this), accountKey, true) ?: return false
        val microBlog = account.newMicroBlogInstance(this, cls = MicroBlog::class.java)
        try {
            return action(microBlog, account, extras.status)
        } catch (e: MicroBlogException) {
            return false
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
            val intent = Intent(context, LengthyOperationsService::class.java)
            intent.action = INTENT_ACTION_UPDATE_STATUS
            intent.putExtra(EXTRA_STATUSES, statuses)
            intent.putExtra(EXTRA_ACTION, action)
            context.startService(intent)
        }
    }

}

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
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.ManualTaskStarter
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.getNullableTypedArrayExtra
import org.mariotaku.ktextension.toLongOr
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.TwitterUpload
import org.mariotaku.microblog.library.twitter.model.MediaUploadResponse
import org.mariotaku.microblog.library.twitter.model.MediaUploadResponse.ProcessingInfo
import org.mariotaku.restfu.http.mime.Body
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.constant.refreshAfterTweetKey
import org.mariotaku.twidere.extension.getErrorMessage
import org.mariotaku.twidere.extension.model.notificationBuilder
import org.mariotaku.twidere.extension.queryOne
import org.mariotaku.twidere.extension.withAppendedPath
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.draft.SendDirectMessageActionExtras
import org.mariotaku.twidere.model.draft.StatusObjectActionExtras
import org.mariotaku.twidere.model.notification.NotificationChannelSpec
import org.mariotaku.twidere.model.schedule.ScheduleInfo
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableStatusUpdateUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts
import org.mariotaku.twidere.task.CreateFavoriteTask
import org.mariotaku.twidere.task.RetweetStatusTask
import org.mariotaku.twidere.task.twitter.UpdateStatusTask
import org.mariotaku.twidere.task.twitter.message.SendMessageTask
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.deleteDrafts
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.min

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

    private fun showToast(e: Exception, longMessage: Boolean) {
        handler.post {
            Toast.makeText(this, e.getErrorMessage(this), if (longMessage) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
        }
    }

    private fun showToast(message: Int, longMessage: Boolean) {
        handler.post { Toast.makeText(this, message, if (longMessage) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show() }
    }

    private fun handleSendDraftIntent(intent: Intent) {
        val uri = intent.data ?: return
        notificationManager.cancel(uri.toString(), NOTIFICATION_ID_DRAFTS)
        val draftId = uri.lastPathSegment.toLongOr(-1L)
        if (draftId == -1L) return
        val where = Expression.equals(Drafts._ID, draftId)
        val draft: Draft = contentResolver.queryOne(Drafts.CONTENT_URI, Drafts.COLUMNS, where.sql,
                null, null, cls = Draft::class.java) ?: return

        contentResolver.delete(Drafts.CONTENT_URI, where.sql, null)
        if (TextUtils.isEmpty(draft.action_type)) {
            draft.action_type = Draft.Action.UPDATE_STATUS
        }
        when (draft.action_type) {
            Draft.Action.UPDATE_STATUS_COMPAT_1, Draft.Action.UPDATE_STATUS_COMPAT_2,
            Draft.Action.UPDATE_STATUS, Draft.Action.REPLY, Draft.Action.QUOTE -> {
                updateStatuses(arrayOf(ParcelableStatusUpdateUtils.fromDraftItem(this, draft)))
            }
            Draft.Action.SEND_DIRECT_MESSAGE_COMPAT, Draft.Action.SEND_DIRECT_MESSAGE -> {
                val extras = draft.action_extras as? SendDirectMessageActionExtras ?: return
                val message = ParcelableNewMessage().apply {
                    this.account = draft.account_keys?.firstOrNull()?.let { key ->
                        val am = AccountManager.get(this@LengthyOperationsService)
                        return@let AccountUtils.getAccountDetails(am, key, true)
                    }
                    this.text = draft.text
                    this.media = draft.media
                    this.recipient_ids = extras.recipientIds
                    this.conversation_id = extras.conversationId
                }
                sendMessage(message)
            }
            Draft.Action.FAVORITE -> {
                performStatusAction(draft) { accountKey, status ->
                    CreateFavoriteTask(this, accountKey, status)
                }
            }
            Draft.Action.RETWEET -> {
                performStatusAction(draft) { accountKey, status ->
                    RetweetStatusTask(this, accountKey, status)
                }
            }
        }
    }

    private fun handleDiscardDraftIntent(intent: Intent) {
        val data = intent.data ?: return
        task {
            if (deleteDrafts(longArrayOf(data.lastPathSegment.toLongOr(-1L))) < 1) {
                throw IOException()
            }
            return@task data
        }.successUi { uri ->
            notificationManager.cancel(uri.toString(), NOTIFICATION_ID_DRAFTS)
        }
    }

    private fun handleSendDirectMessageIntent(intent: Intent) {
        val message = intent.getParcelableExtra<ParcelableNewMessage>(EXTRA_MESSAGE) ?: return
        sendMessage(message)
    }

    private fun sendMessage(message: ParcelableNewMessage) {
        val title = getString(R.string.sending_direct_message)
        val builder = NotificationChannelSpec.backgroundProgresses.notificationBuilder(this)
        builder.setSmallIcon(R.drawable.ic_stat_send)
        builder.setProgress(100, 0, true)
        builder.setTicker(title)
        builder.setContentTitle(title)
        builder.setContentText(message.text)
        builder.setCategory(NotificationCompat.CATEGORY_PROGRESS)
        builder.setOngoing(true)
        val notification = builder.build()
        startForeground(NOTIFICATION_ID_SEND_DIRECT_MESSAGE, notification)
        val task = SendMessageTask(this)
        task.params = message
        invokeBeforeExecute(task)
        val result = ManualTaskStarter.invokeExecute(task)
        invokeAfterExecute(task, result)

        if (result.hasData()) {
            showToast(R.string.message_direct_message_sent, false)
        } else {
            UpdateStatusTask.saveDraft(this, Draft.Action.SEND_DIRECT_MESSAGE) {
                account_keys = arrayOf(message.account.key)
                text = message.text
                media = message.media
                action_extras = SendDirectMessageActionExtras().apply {
                    recipientIds = message.recipient_ids
                    conversationId = message.conversation_id
                }
            }
            val exception = result.exception
            if (exception != null) {
                showToast(exception, true)
            }
        }
        stopForeground(false)
        notificationManager.cancel(NOTIFICATION_ID_SEND_DIRECT_MESSAGE)
    }

    private fun handleUpdateStatusIntent(intent: Intent) {
        val status = intent.getParcelableExtra<ParcelableStatusUpdate>(EXTRA_STATUS)
        val scheduleInfo = intent.getParcelableExtra<ScheduleInfo>(EXTRA_SCHEDULE_INFO)
        val statuses: Array<ParcelableStatusUpdate>
        statuses = intent.getNullableTypedArrayExtra(EXTRA_STATUSES)
            ?: if (status != null) {
                arrayOf(status)
            } else
                return
        @Draft.Action
        val actionType = intent.getStringExtra(EXTRA_ACTION)
        statuses.forEach { it.draft_action = actionType }
        updateStatuses(statuses, scheduleInfo)
    }

    private fun updateStatuses(statuses: Array<ParcelableStatusUpdate>, scheduleInfo: ScheduleInfo? = null) {
        val context = this
        val builder = NotificationChannelSpec.backgroundProgresses.notificationBuilder(context)
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
                            val errorMessage = e.getErrorMessage(context)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            failed = true
                            break
                        }
                    }
                    if (!failed) {
                        if (scheduleInfo != null) {
                            Toast.makeText(context, R.string.message_toast_status_scheduled,
                                    Toast.LENGTH_SHORT).show()
                        } else if (item.repost_status_id != null ||
                                item.draft_action == Draft.Action.QUOTE) {
                            Toast.makeText(context, R.string.message_toast_status_retweeted,
                                    Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, R.string.message_toast_status_updated,
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun beforeExecute() {

                }
            })
            task.callback = this
            task.params = Pair(item, scheduleInfo)
            invokeBeforeExecute(task)

            val result = ManualTaskStarter.invokeExecute(task)
            invokeAfterExecute(task, result)

            if (!result.succeed) {
                contentResolver.insert(Drafts.CONTENT_URI_NOTIFICATIONS.withAppendedPath(result.draftId.toString()), null)
            }
        }
        if (preferences[refreshAfterTweetKey]) {
            handler.post { twitterWrapper.refreshAll() }
        }
        stopForeground(false)
        notificationManager.cancel(NOTIFICATION_ID_UPDATE_STATUS)
    }

    @Throws(IOException::class, MicroBlogException::class)
    private fun uploadMedia(upload: TwitterUpload, body: Body): MediaUploadResponse {
        val mediaType = body.contentType().contentType
        val length = body.length()
        val stream = body.stream()
        var response = upload.initUploadMedia(mediaType, length, null, null)
        run {
                var streamReadLength = 0
                var segmentIndex = 0
                while (streamReadLength < length) {
                    val currentBulkSize = min(BULK_SIZE, length - streamReadLength).toInt()
                    val output = ByteArrayOutputStream()
                    Utils.copyStream(stream, output, currentBulkSize)
                    val data = Base64.encodeToString(output.toByteArray(), Base64.DEFAULT)
                    upload.appendUploadMedia(response.id, segmentIndex, data)
                    output.close()
                    segmentIndex++
                    streamReadLength += currentBulkSize
                }
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
        return when (info.state) {
            ProcessingInfo.State.PENDING, ProcessingInfo.State.IN_PROGRESS -> true
            else -> false
        }
    }

    private fun <T> performStatusAction(draft: Draft, action: (accountKey: UserKey, status: ParcelableStatus) -> AbstractTask<*, T, *>): Boolean {
        val accountKey = draft.account_keys?.firstOrNull() ?: return false
        val status = (draft.action_extras as? StatusObjectActionExtras)?.status ?: return false
        val task = action(accountKey, status)
        invokeBeforeExecute(task)
        val result = ManualTaskStarter.invokeExecute(task)
        invokeAfterExecute(task, result)
        return true
    }

    private fun invokeBeforeExecute(task: AbstractTask<*, *, *>) {
        handler.post { ManualTaskStarter.invokeBeforeExecute(task) }
    }

    private fun <T> invokeAfterExecute(task: AbstractTask<*, T, *>, result: T) {
        handler.post { ManualTaskStarter.invokeAfterExecute(task, result) }
    }

    companion object {
        private const val BULK_SIZE = (128 * 1024).toLong() // 128KiB

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
                vararg statuses: ParcelableStatusUpdate, scheduleInfo: ScheduleInfo? = null) {
            val intent = Intent(context, LengthyOperationsService::class.java)
            intent.action = INTENT_ACTION_UPDATE_STATUS
            intent.putExtra(EXTRA_STATUSES, statuses)
            intent.putExtra(EXTRA_SCHEDULE_INFO, scheduleInfo)
            intent.putExtra(EXTRA_ACTION, action)
            context.startService(intent)
        }

        fun sendMessageAsync(context: Context, message: ParcelableNewMessage) {
            val intent = Intent(context, LengthyOperationsService::class.java)
            intent.action = INTENT_ACTION_SEND_DIRECT_MESSAGE
            intent.putExtra(EXTRA_MESSAGE, message)
            context.startService(intent)
        }
    }

}

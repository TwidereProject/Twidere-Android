/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.task.twitter.message

import android.content.Context
import org.mariotaku.ktextension.isNotNullOrEmpty
import org.mariotaku.microblog.library.*
import org.mariotaku.microblog.library.annotation.twitter.MediaCategory
import org.mariotaku.microblog.library.model.microblog.DirectMessage
import org.mariotaku.microblog.library.model.twitter.dm.NewDm
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.api.*
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.ParcelableNewMessage
import org.mariotaku.twidere.model.event.SendMessageTaskEvent
import org.mariotaku.twidere.model.util.ParcelableMessageUtils
import org.mariotaku.twidere.promise.UpdateStatusPromise
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.task.ExceptionHandlingAbstractTask
import org.mariotaku.twidere.task.twitter.message.GetMessagesTask.Companion.addConversation
import org.mariotaku.twidere.task.twitter.message.GetMessagesTask.Companion.addLocalConversations

/**
 * Created by mariotaku on 2017/2/8.
 */
class SendMessageTask(
        context: Context
) : ExceptionHandlingAbstractTask<ParcelableNewMessage, SendMessageTask.SendMessageResult,
        MicroBlogException, Unit>(context) {

    private val profileImageSize = context.getString(R.string.profile_image_size)

    override val exceptionClass = MicroBlogException::class.java

    override fun onExecute(params: ParcelableNewMessage): SendMessageResult {
        val account = params.account ?: throw MicroBlogException("No account")
        val updateData = requestSendMessage(account, params)
        if (params.is_temp_conversation && params.conversation_id != null) {
            val deleteTempWhere = Expression.and(Expression.equalsArgs(Conversations.ACCOUNT_KEY),
                    Expression.equalsArgs(Conversations.CONVERSATION_ID)).sql
            val deleteTempWhereArgs = arrayOf(account.key.toString(), params.conversation_id)
            context.contentResolver.delete(Conversations.CONTENT_URI, deleteTempWhere,
                    deleteTempWhereArgs)
        }
        GetMessagesTask.storeMessages(context, updateData, account)
        return SendMessageResult(updateData.conversations.map { it.id })
    }

    override fun onException(callback: Unit?, exception: MicroBlogException) {
        bus.post(SendMessageTaskEvent(params.account.key, params.conversation_id, null, false))
    }

    override fun onSucceed(callback: Unit?, result: SendMessageResult) {
        bus.post(SendMessageTaskEvent(params.account.key, params.conversation_id,
                result.conversationIds.singleOrNull(), true))
    }

    private fun requestSendMessage(account: AccountDetails, message: ParcelableNewMessage): GetMessagesTask.DatabaseUpdateData {
        when (account.type) {
            AccountType.TWITTER -> {
                val twitter = account.newMicroBlogInstance(context, cls = Twitter::class.java)
                if (account.isOfficial(context)) {
                    return sendTwitterOfficialDM(twitter, account, message)
                } else {
                    return sendTwitterMessageEvent(twitter, account, message)
                }
            }
            AccountType.FANFOU -> {
                val fanfou = account.newMicroBlogInstance(context, cls = Fanfou::class.java)
                return sendFanfouDM(fanfou, account, message)
            }
        }
        val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        return sendDefaultDM(microBlog, account, message)
    }

    private fun sendTwitterOfficialDM(twitter: Twitter, account: AccountDetails,
            message: ParcelableNewMessage): GetMessagesTask.DatabaseUpdateData {
        var deleteOnSuccess: List<UpdateStatusPromise.MediaDeletionItem>? = null
        var deleteAlways: List<UpdateStatusPromise.MediaDeletionItem>? = null
        val sendResponse = try {
            val conversationId = message.conversation_id
            val tempConversation = message.is_temp_conversation

            val newDm = NewDm()
            if (!tempConversation && conversationId != null) {
                newDm.setConversationId(conversationId)
            } else {
                newDm.setRecipientIds(message.recipient_ids)
            }
            newDm.setText(message.text)

            if (message.media.isNotNullOrEmpty()) {
                val upload = account.newMicroBlogInstance(context, cls = TwitterUpload::class.java)
                val uploadResult = UpdateStatusPromise.uploadMicroBlogMediaShared(context,
                        upload, account, message.media, null, null, true, null)
                newDm.setMediaId(uploadResult.ids[0])
                deleteAlways = uploadResult.deleteAlways
                deleteOnSuccess = uploadResult.deleteOnSuccess
            }
            twitter.sendDm(newDm)
        } catch (e: UpdateStatusPromise.UploadException) {
            e.deleteAlways?.forEach {
                it.delete(context)
            }
            throw MicroBlogException(e)
        } finally {
            deleteAlways?.forEach { it.delete(context) }
        }
        deleteOnSuccess?.forEach { it.delete(context) }
        val conversationId = sendResponse.entries?.firstOrNull {
            it.message != null
        }?.message?.conversationId
        val response = twitter.getDmConversation(conversationId, null).conversationTimeline
        return GetMessagesTask.createDatabaseUpdateData(context, account, response, profileImageSize)
    }

    private fun sendTwitterMessageEvent(twitter: Twitter, account: AccountDetails,
            message: ParcelableNewMessage): GetMessagesTask.DatabaseUpdateData {
        val recipientId = message.recipient_ids.singleOrNull() ?: throw MicroBlogException("No recipient")
        val category = when (message.media?.firstOrNull()?.type) {
            ParcelableMedia.Type.IMAGE -> MediaCategory.DM_IMAGE
            ParcelableMedia.Type.VIDEO -> MediaCategory.DM_VIDEO
            ParcelableMedia.Type.ANIMATED_GIF -> MediaCategory.DM_GIF
            else -> null
        }
        val response = uploadMediaThen(account, message, category) { mediaId ->
            val obj = DirectMessageEventObject {
                type = "message_create"
                messageCreate {
                    target { this.recipientId = recipientId }
                    messageData {
                        text = message.text
                        if (mediaId != null) {
                            attachment {
                                type = "media"
                                media {
                                    id = mediaId
                                }
                            }
                        }
                    }
                }
            }
            return@uploadMediaThen twitter.newDirectMessageEvent(obj)
        }
        return createDatabaseUpdateData(account, twitter.showDirectMessage(response.event.id))
    }

    private fun sendFanfouDM(fanfou: Fanfou, account: AccountDetails, message: ParcelableNewMessage): GetMessagesTask.DatabaseUpdateData {
        val recipientId = message.recipient_ids.singleOrNull() ?: throw MicroBlogException("No recipient")
        val response = fanfou.sendFanfouDirectMessage(recipientId, message.text)
        return createDatabaseUpdateData(account, response)
    }

    private fun sendDefaultDM(microBlog: MicroBlog, account: AccountDetails, message: ParcelableNewMessage): GetMessagesTask.DatabaseUpdateData {
        val recipientId = message.recipient_ids.singleOrNull() ?: throw MicroBlogException("No recipient")
        val response = uploadMediaThen(account, message) { mediaId ->
            if (mediaId != null) {
                microBlog.sendDirectMessage(recipientId, message.text, mediaId)
            } else {
                microBlog.sendDirectMessage(recipientId, message.text)
            }
        }
        return createDatabaseUpdateData(account, response)
    }

    private fun <T> uploadMediaThen(account: AccountDetails, message: ParcelableNewMessage,
            category: String? = null, action: (mediaId: String?) -> T): T {
        var deleteOnSuccess: List<UpdateStatusPromise.MediaDeletionItem>? = null
        var deleteAlways: List<UpdateStatusPromise.MediaDeletionItem>? = null
        try {
            var mediaId: String? = null
            if (message.media.isNotNullOrEmpty()) {
                val upload = account.newMicroBlogInstance(context, cls = TwitterUpload::class.java)
                val uploadResult = UpdateStatusPromise.uploadMicroBlogMediaShared(context,
                        upload, account, message.media, category, null, true, null)
                mediaId = uploadResult.ids[0]
                deleteAlways = uploadResult.deleteAlways
                deleteOnSuccess = uploadResult.deleteOnSuccess
            }
            val result = action(mediaId)
            deleteOnSuccess?.forEach { it.delete(context) }
            return result
        } catch (e: UpdateStatusPromise.UploadException) {
            e.deleteAlways?.forEach {
                it.delete(context)
            }
            throw MicroBlogException(e)
        } finally {
            deleteAlways?.forEach { it.delete(context) }
        }
    }

    private fun createDatabaseUpdateData(details: AccountDetails, dm: DirectMessage): GetMessagesTask.DatabaseUpdateData {
        val accountKey = details.key
        val conversationIds = setOf(ParcelableMessageUtils.outgoingConversationId(dm.senderId, dm.recipientId))
        val conversations = hashMapOf<String, ParcelableMessageConversation>()
        conversations.addLocalConversations(context, accountKey, conversationIds)
        val message = ParcelableMessageUtils.fromMessage(accountKey, dm, true)
        val sender = dm.sender.toParcelable(details, profileImageSize = profileImageSize)
        val recipient = dm.recipient.toParcelable(details, profileImageSize = profileImageSize)
        conversations.addConversation(message.conversation_id, details, message, setOf(sender, recipient), appendUsers = true)
        return GetMessagesTask.DatabaseUpdateData(conversations.values, listOf(message))
    }

    class SendMessageResult(var conversationIds: List<String>)

    companion object {
        const val TEMP_CONVERSATION_ID_PREFIX = "twidere:temp:"
    }
}
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
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.TwitterUpload
import org.mariotaku.microblog.library.twitter.model.DirectMessage
import org.mariotaku.microblog.library.twitter.model.NewDm
import org.mariotaku.microblog.library.twitter.model.fixMedia
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.ParcelableNewMessage
import org.mariotaku.twidere.model.event.SendMessageTaskEvent
import org.mariotaku.twidere.model.util.ParcelableMessageUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.task.ExceptionHandlingAbstractTask
import org.mariotaku.twidere.task.twitter.UpdateStatusTask
import org.mariotaku.twidere.task.twitter.message.GetMessagesTask
import org.mariotaku.twidere.task.twitter.message.GetMessagesTask.Companion.addConversation
import org.mariotaku.twidere.task.twitter.message.GetMessagesTask.Companion.addLocalConversations

/**
 * Created by mariotaku on 2017/2/8.
 */
class SendMessageTask(
        context: Context
) : ExceptionHandlingAbstractTask<ParcelableNewMessage, SendMessageTask.SendMessageResult, MicroBlogException, Unit>(context) {

    override fun onExecute(params: ParcelableNewMessage): SendMessageResult {
        val account = params.account
        val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        val updateData = requestSendMessage(microBlog, account, params)
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

    private fun requestSendMessage(microBlog: MicroBlog, account: AccountDetails, message: ParcelableNewMessage): GetMessagesTask.DatabaseUpdateData {
        when (account.type) {
            AccountType.TWITTER -> {
                if (account.isOfficial(context)) {
                    return sendTwitterOfficialDM(microBlog, account, message)
                }
            }
            AccountType.FANFOU -> {
                return sendFanfouDM(microBlog, account, message)
            }
        }
        return sendDefaultDM(microBlog, account, message)
    }

    private fun sendTwitterOfficialDM(microBlog: MicroBlog, account: AccountDetails,
            message: ParcelableNewMessage): GetMessagesTask.DatabaseUpdateData {
        var deleteOnSuccess: List<UpdateStatusTask.MediaDeletionItem>? = null
        var deleteAlways: List<UpdateStatusTask.MediaDeletionItem>? = null
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
                val uploadResult = UpdateStatusTask.uploadAllMediaShared(context,
                        mediaLoader, upload, account, message.media, null, true, null)
                newDm.setMediaId(uploadResult.ids[0])
                deleteAlways = uploadResult.deleteAlways
                deleteOnSuccess = uploadResult.deleteOnSuccess
            }
            microBlog.sendDm(newDm)
        } finally {
            deleteOnSuccess?.forEach { it.delete(context) }
        }
        deleteAlways?.forEach { it.delete(context) }
        val conversationId = sendResponse.entries?.firstOrNull {
            it.message != null
        }?.message?.conversationId
        val response = microBlog.getDmConversation(conversationId, null).conversationTimeline
        response.fixMedia(microBlog)
        return GetMessagesTask.createDatabaseUpdateData(context, account, response)
    }

    private fun sendFanfouDM(microBlog: MicroBlog, account: AccountDetails, message: ParcelableNewMessage): GetMessagesTask.DatabaseUpdateData {
        val recipientId = message.recipient_ids.singleOrNull() ?: throw MicroBlogException("No recipient")
        val response = microBlog.sendFanfouDirectMessage(recipientId, message.text)
        return createDatabaseUpdateData(account, response)
    }

    private fun sendDefaultDM(microBlog: MicroBlog, account: AccountDetails, message: ParcelableNewMessage): GetMessagesTask.DatabaseUpdateData {
        val recipientId = message.recipient_ids.singleOrNull() ?: throw MicroBlogException("No recipient")
        val response = microBlog.sendDirectMessage(recipientId, message.text)
        return createDatabaseUpdateData(account, response)
    }

    private fun createDatabaseUpdateData(details: AccountDetails, dm: DirectMessage): GetMessagesTask.DatabaseUpdateData {
        val accountKey = details.key
        val conversationIds = setOf(ParcelableMessageUtils.outgoingConversationId(dm.senderId, dm.recipientId))
        val conversations = hashMapOf<String, ParcelableMessageConversation>()
        conversations.addLocalConversations(context, accountKey, conversationIds)
        val message = ParcelableMessageUtils.fromMessage(accountKey, dm, true)
        conversations.addConversation(message.conversation_id, details, message, setOf(dm.sender, dm.recipient))
        return GetMessagesTask.DatabaseUpdateData(conversations.values, listOf(message))
    }

    class SendMessageResult(var conversationIds: List<String>) {

    }
}
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

package org.mariotaku.twidere.promise

import android.accounts.AccountManager
import android.content.Context
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import org.mariotaku.ktextension.weak
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.exception.AccountNotFoundException
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.queryReference
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Messages
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.mariotaku.twidere.util.updateItems

object MessagePromises {

    fun destroyConversation(context: Context, accountKey: UserKey, conversationId: String): Promise<Boolean, Exception> {
        val weakContext by weak(context)
        return task {
            val taskContext = weakContext ?: throw InterruptedException()
            val account = AccountUtils.getAccountDetails(AccountManager.get(taskContext), accountKey, true) ?:
                    throw MicroBlogException("No account")
            val conversation = DataStoreUtils.findMessageConversation(taskContext, accountKey, conversationId)

            var deleteMessages = true
            var deleteConversation = true
            // Only perform real deletion when it's not temp conversation (stored locally)
            if (conversation != null) when {
                conversation.conversation_extras_type != ParcelableMessageConversation.ExtrasType.TWITTER_OFFICIAL -> {
                    deleteMessages = false
                    deleteConversation = clearMessagesSync(taskContext, account, conversationId)
                }
                !conversation.is_temp -> if (!requestDestroyConversation(taskContext, account, conversationId)) {
                    return@task false
                }
            }

            if (deleteMessages) {
                val deleteMessageWhere = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                        Expression.equalsArgs(Messages.CONVERSATION_ID)).sql
                val deleteMessageWhereArgs = arrayOf(accountKey.toString(), conversationId)
                taskContext.contentResolver.delete(Messages.CONTENT_URI, deleteMessageWhere, deleteMessageWhereArgs)
            }
            if (deleteConversation) {
                val deleteConversationWhere = Expression.and(Expression.equalsArgs(Messages.Conversations.ACCOUNT_KEY),
                        Expression.equalsArgs(Messages.Conversations.CONVERSATION_ID)).sql
                val deleteConversationWhereArgs = arrayOf(accountKey.toString(), conversationId)
                taskContext.contentResolver.delete(Messages.Conversations.CONTENT_URI, deleteConversationWhere, deleteConversationWhereArgs)
            }
            return@task true
        }
    }

    fun destroyMessage(context: Context, accountKey: UserKey, conversationId: String?,
            messageId: String): Promise<Boolean, Exception> {
        val weakContext by weak(context)
        return task {
            val taskContext = weakContext ?: throw InterruptedException()
            val account = AccountUtils.getAccountDetails(AccountManager.get(taskContext), accountKey,
                    true) ?: throw AccountNotFoundException()
            if (!requestDestroyMessage(taskContext, account, messageId)) {
                return@task false
            }
            val deleteWhere: String
            val deleteWhereArgs: Array<String>
            if (conversationId != null) {
                deleteWhere = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                        Expression.equalsArgs(Messages.CONVERSATION_ID),
                        Expression.equalsArgs(Messages.MESSAGE_ID)).sql
                deleteWhereArgs = arrayOf(accountKey.toString(), conversationId, messageId)
            } else {
                deleteWhere = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                        Expression.equalsArgs(Messages.MESSAGE_ID)).sql
                deleteWhereArgs = arrayOf(accountKey.toString(), messageId)
            }
            taskContext.contentResolver.delete(Messages.CONTENT_URI, deleteWhere, deleteWhereArgs)
            return@task true
        }
    }


    fun clearMessages(context: Context, accountKey: UserKey, conversationId: String): Promise<Boolean, Exception> {
        val weakContext by weak(context)
        return task {
            val taskContext = weakContext ?: throw InterruptedException()
            val account = AccountUtils.getAccountDetails(AccountManager.get(taskContext), accountKey,
                    true) ?: throw AccountNotFoundException()
            clearMessagesSync(taskContext, account, conversationId)
        }
    }

    private fun clearMessagesSync(context: Context, account: AccountDetails, conversationId: String): Boolean {
        val messagesWhere = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                Expression.equalsArgs(Messages.CONVERSATION_ID)).sql
        val messagesWhereArgs = arrayOf(account.key.toString(), conversationId)
        val projection = arrayOf(Messages.MESSAGE_ID)
        val messageIds = mutableListOf<String>()
        var allSuccess = true
        context.contentResolver.queryReference(Messages.CONTENT_URI, projection, messagesWhere,
                messagesWhereArgs, null)?.use { (cur) ->
            cur.moveToFirst()
            while (!cur.isAfterLast) {
                val messageId = cur.getString(0)
                try {
                    if (requestDestroyMessage(context, account, messageId)) {
                        messageIds.add(messageId)
                    }
                } catch (e: MicroBlogException) {
                    allSuccess = false
                    // Ignore
                }
                cur.moveToNext()
            }
        }
        ContentResolverUtils.bulkDelete(context.contentResolver, Messages.CONTENT_URI,
                Messages.MESSAGE_ID, false, messageIds, messagesWhere, messagesWhereArgs)
        val conversationWhere = Expression.and(Expression.equalsArgs(Messages.Conversations.ACCOUNT_KEY),
                Expression.equalsArgs(Messages.Conversations.CONVERSATION_ID)).sql
        val conversationWhereArgs = arrayOf(account.key.toString(), conversationId)
        context.contentResolver.updateItems(Messages.Conversations.CONTENT_URI,
                Messages.Conversations.COLUMNS, conversationWhere, conversationWhereArgs,
                cls = ParcelableMessageConversation::class.java) { item ->
            item.message_extras = null
            item.message_type = null
            item.message_timestamp = -1L
            item.text_unescaped = null
            item.media = null
            return@updateItems item
        }
        return allSuccess
    }

    private fun requestDestroyConversation(context: Context, account: AccountDetails, conversationId: String): Boolean {
        when (account.type) {
            AccountType.TWITTER -> {
                if (account.isOfficial(context)) {
                    val twitter = account.newMicroBlogInstance(context, MicroBlog::class.java)
                    return twitter.deleteDmConversation(conversationId).isSuccessful
                }
            }
        }
        return false
    }

    private fun requestDestroyMessage(context: Context, account: AccountDetails,
            messageId: String): Boolean {
        when (account.type) {
            AccountType.TWITTER -> {
                val twitter = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
                if (account.isOfficial(context)) {
                    return twitter.destroyDm(messageId).isSuccessful
                }
                twitter.destroyDirectMessage(messageId)
                return true
            }
        }
        return false
    }

}
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
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Messages
import org.mariotaku.twidere.task.twitter.message.ClearMessagesTask
import org.mariotaku.twidere.util.DataStoreUtils

object MessageConversationPromises {

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
                    deleteConversation = ClearMessagesTask.clearMessages(taskContext, account, conversationId)
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
}
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

import android.accounts.AccountManager
import android.content.Context
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
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.task.ExceptionHandlingAbstractTask
import org.mariotaku.twidere.util.DataStoreUtils

/**
 * Created by mariotaku on 2017/2/14.
 */

class DestroyConversationTask(
        context: Context,
        val accountKey: UserKey,
        val conversationId: String
) : ExceptionHandlingAbstractTask<Unit?, Boolean, MicroBlogException, ((Boolean) -> Unit)?>(context) {

    override val exceptionClass = MicroBlogException::class.java

    override fun onExecute(params: Unit?): Boolean {
        val account = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true) ?:
                throw MicroBlogException("No account")
        val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        val conversation = DataStoreUtils.findMessageConversation(context, accountKey, conversationId)

        var deleteMessages = true
        var deleteConversation = true
        // Only perform real deletion when it's not temp conversation (stored locally)
        if (conversation != null) when {
            conversation.conversation_extras_type != ParcelableMessageConversation.ExtrasType.TWITTER_OFFICIAL -> {
                deleteMessages = false
                deleteConversation = ClearMessagesTask.clearMessages(context, account, conversationId)
            }
            !conversation.is_temp -> if (!requestDestroyConversation(microBlog, account)) {
                return false
            }
        }

        if (deleteMessages) {
            val deleteMessageWhere = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                    Expression.equalsArgs(Messages.CONVERSATION_ID)).sql
            val deleteMessageWhereArgs = arrayOf(accountKey.toString(), conversationId)
            context.contentResolver.delete(Messages.CONTENT_URI, deleteMessageWhere, deleteMessageWhereArgs)
        }
        if (deleteConversation) {
            val deleteConversationWhere = Expression.and(Expression.equalsArgs(Conversations.ACCOUNT_KEY),
                    Expression.equalsArgs(Conversations.CONVERSATION_ID)).sql
            val deleteConversationWhereArgs = arrayOf(accountKey.toString(), conversationId)
            context.contentResolver.delete(Conversations.CONTENT_URI, deleteConversationWhere, deleteConversationWhereArgs)
        }
        return true
    }

    override fun afterExecute(callback: ((Boolean) -> Unit)?, result: Boolean?, exception: MicroBlogException?) {
        callback?.invoke(result ?: false)
    }

    private fun requestDestroyConversation(microBlog: MicroBlog, account: AccountDetails): Boolean {
        when (account.type) {
            AccountType.TWITTER -> {
                if (account.isOfficial(context)) {
                    return microBlog.deleteDmConversation(conversationId).isSuccessful
                }
            }
        }
        return false
    }

}

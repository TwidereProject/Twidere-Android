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
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.queryReference
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Messages
import org.mariotaku.twidere.task.ExceptionHandlingAbstractTask
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.mariotaku.twidere.util.updateItems

/**
 * Created by mariotaku on 2017/2/16.
 */

class ClearMessagesTask(
        context: Context,
        val accountKey: UserKey,
        val conversationId: String
) : ExceptionHandlingAbstractTask<Unit?, Boolean, MicroBlogException, ((Boolean) -> Unit)?>(context) {

    override val exceptionClass = MicroBlogException::class.java

    override fun onExecute(params: Unit?): Boolean {
        val account = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true) ?:
                throw MicroBlogException("No account")
        val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        val messagesWhere = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                Expression.equalsArgs(Messages.CONVERSATION_ID)).sql
        val messagesWhereArgs = arrayOf(accountKey.toString(), conversationId)
        val projection = arrayOf(Messages.MESSAGE_ID)
        val messageIds = mutableListOf<String>()
        context.contentResolver.queryReference(Messages.CONTENT_URI, projection, messagesWhere,
                messagesWhereArgs, null)?.use { (cur) ->
            cur.moveToFirst()
            while (!cur.isAfterLast) {
                val messageId = cur.getString(0)
                try {
                    if (DestroyMessageTask.performDestroyMessage(context, microBlog, account,
                            messageId)) {
                        messageIds.add(messageId)
                    }
                } catch (e: MicroBlogException) {
                    // Ignore
                }
                cur.moveToNext()
            }
        }
        ContentResolverUtils.bulkDelete(context.contentResolver, Messages.CONTENT_URI,
                Messages.MESSAGE_ID, false, messageIds, messagesWhere, messagesWhereArgs)
        val conversationWhere = Expression.and(Expression.equalsArgs(Messages.Conversations.ACCOUNT_KEY),
                Expression.equalsArgs(Messages.Conversations.CONVERSATION_ID)).sql
        val conversationWhereArgs = arrayOf(accountKey.toString(), conversationId)
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
        return true
    }

    override fun afterExecute(callback: ((Boolean) -> Unit)?, result: Boolean?, exception: MicroBlogException?) {
        callback?.invoke(result ?: false)
    }
}

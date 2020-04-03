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
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.OrderBy
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.model.timestamp
import org.mariotaku.twidere.extension.queryOne
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableMessage
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.UnreadCountUpdatedEvent
import org.mariotaku.twidere.model.message.conversation.TwitterOfficialConversationExtras
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Messages
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.task.ExceptionHandlingAbstractTask
import org.mariotaku.twidere.task.twitter.message.SendMessageTask.Companion.TEMP_CONVERSATION_ID_PREFIX
import org.mariotaku.twidere.util.DataStoreUtils

/**
 * Created by mariotaku on 2017/2/16.
 */

class MarkMessageReadTask(
        context: Context,
        val accountKey: UserKey,
        val conversationId: String
) : ExceptionHandlingAbstractTask<Unit?, Boolean, MicroBlogException, Unit?>(context) {

    override val exceptionClass = MicroBlogException::class.java

    override fun onExecute(params: Unit?): Boolean {
        if (conversationId.startsWith(TEMP_CONVERSATION_ID_PREFIX)) return true
        val account = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true) ?:
                throw MicroBlogException("No account")
        val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        val conversation = DataStoreUtils.findMessageConversation(context, accountKey, conversationId)
        val lastReadEvent = conversation?.let {
            return@let performMarkRead(context, microBlog, account, conversation)
        } ?: return false
        updateLocalLastRead(context.contentResolver, accountKey, conversationId, lastReadEvent)
        return true
    }

    override fun onSucceed(callback: Unit?, result: Boolean) {
        bus.post(UnreadCountUpdatedEvent(-1))
    }


    companion object {

        @Throws(MicroBlogException::class)
        internal fun performMarkRead(context: Context, microBlog: MicroBlog, account: AccountDetails,
                conversation: ParcelableMessageConversation): Pair<String, Long>? {
            val cr = context.contentResolver
            when (account.type) {
                AccountType.TWITTER -> {
                    if (account.isOfficial(context)) {
                        val event = (conversation.conversation_extras as? TwitterOfficialConversationExtras)?.maxReadEvent ?: run {
                            val message = cr.findRecentMessage(account.key, conversation.id) ?: return null
                            return@run Pair(message.id, message.timestamp)
                        }
                        if (conversation.last_read_timestamp > event.second) {
                            // Local is newer, ignore network request
                            return event
                        }
                        if (microBlog.markDmRead(conversation.id, event.first).isSuccessful) {
                            return event
                        }
                    }
                }
            }
            val message = cr.findRecentMessage(account.key, conversation.id) ?: return null
            return Pair(message.id, message.timestamp)
        }

        internal fun updateLocalLastRead(cr: ContentResolver, accountKey: UserKey,
                conversationId: String, lastRead: Pair<String, Long>) {
            val values = ContentValues()
            values.put(Conversations.LAST_READ_ID, lastRead.first)
            values.put(Conversations.LAST_READ_TIMESTAMP, lastRead.second)
            val updateWhere = Expression.and(Expression.equalsArgs(Conversations.ACCOUNT_KEY),
                    Expression.equalsArgs(Conversations.CONVERSATION_ID),
                    Expression.lesserThan(Conversations.LAST_READ_TIMESTAMP, lastRead.second)).sql
            val updateWhereArgs = arrayOf(accountKey.toString(), conversationId)
            cr.update(Conversations.CONTENT_URI, values, updateWhere, updateWhereArgs)
        }

        private fun ContentResolver.findRecentMessage(accountKey: UserKey, conversationId: String): ParcelableMessage? {
            val where = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                    Expression.equalsArgs(Messages.CONVERSATION_ID)).sql
            val whereArgs = arrayOf(accountKey.toString(), conversationId)
            return queryOne(Messages.CONTENT_URI, Messages.COLUMNS, where, whereArgs,
                    OrderBy(Messages.LOCAL_TIMESTAMP, false).sql, ParcelableMessage::class.java)
        }

        private val TwitterOfficialConversationExtras.maxReadEvent: Pair<String, Long>?
            get() {
                val id = maxEntryId ?: return null
                if (maxEntryTimestamp < 0) return null
                return Pair(id, maxEntryTimestamp)
            }

    }
}

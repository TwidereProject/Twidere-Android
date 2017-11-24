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
import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import com.squareup.otto.Bus
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.ktextension.forEachRow
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.OrderBy
import org.mariotaku.sqliteqb.library.Table
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.dagger.component.GeneralComponent
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.model.timestamp
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableMessage
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.UnreadCountUpdatedEvent
import org.mariotaku.twidere.model.message.conversation.TwitterOfficialConversationExtras
import org.mariotaku.twidere.provider.TwidereDataStore.Messages
import org.mariotaku.twidere.task.twitter.message.SendMessageTask
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.TwidereQueryBuilder
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.mariotaku.twidere.util.getUnreadMessagesEntriesCursorReference
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder
import javax.inject.Inject

class MessagePromises private constructor(private val application: Application) {
    @Inject
    lateinit var bus: Bus

    private val accountManager = AccountManager.get(application)

    init {
        GeneralComponent.get(application).inject(this)
    }

    fun destroyConversation(accountKey: UserKey, conversationId: String): Promise<Boolean, Exception> = task {
        val account = accountManager.getDetailsOrThrow(accountKey, true)
        val conversation = DataStoreUtils.findMessageConversation(application, accountKey, conversationId)

        var deleteMessages = true
        var deleteConversation = true
        // Only perform real deletion when it's not temp conversation (stored locally)
        if (conversation != null) when {
            conversation.conversation_extras_type != ParcelableMessageConversation.ExtrasType.TWITTER_OFFICIAL -> {
                deleteMessages = false
                deleteConversation = clearMessagesSync(account, conversationId)
            }
            !conversation.is_temp -> if (!requestDestroyConversation(account, conversationId)) {
                return@task false
            }
        }

        if (deleteMessages) {
            val deleteMessageWhere = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                    Expression.equalsArgs(Messages.CONVERSATION_ID)).sql
            val deleteMessageWhereArgs = arrayOf(accountKey.toString(), conversationId)
            application.contentResolver.delete(Messages.CONTENT_URI, deleteMessageWhere, deleteMessageWhereArgs)
        }
        if (deleteConversation) {
            val deleteConversationWhere = Expression.and(Expression.equalsArgs(Messages.Conversations.ACCOUNT_KEY),
                    Expression.equalsArgs(Messages.Conversations.CONVERSATION_ID)).sql
            val deleteConversationWhereArgs = arrayOf(accountKey.toString(), conversationId)
            application.contentResolver.delete(Messages.Conversations.CONTENT_URI, deleteConversationWhere, deleteConversationWhereArgs)
        }
        return@task true
    }

    fun destroyMessage(accountKey: UserKey, conversationId: String?, messageId: String): Promise<Boolean, Exception> = task {
        val account = accountManager.getDetailsOrThrow(accountKey, true)
        if (!requestDestroyMessage(account, messageId)) {
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
        application.contentResolver.delete(Messages.CONTENT_URI, deleteWhere, deleteWhereArgs)
        return@task true
    }


    fun clearMessages(accountKey: UserKey, conversationId: String): Promise<Boolean, Exception> = task {
        val account = accountManager.getDetailsOrThrow(accountKey, true)
        clearMessagesSync(account, conversationId)
    }


    fun markRead(accountKey: UserKey, conversationId: String): Promise<Boolean, Exception> = task {
        if (conversationId.startsWith(SendMessageTask.TEMP_CONVERSATION_ID_PREFIX)) return@task true
        val account = accountManager.getDetailsOrThrow(accountKey, true)
        val microBlog = account.newMicroBlogInstance(application, cls = MicroBlog::class.java)
        val conversation = DataStoreUtils.findMessageConversation(application, accountKey, conversationId)
        val lastReadEvent = conversation?.let {
            return@let performMarkRead(account, conversation)
        } ?: return@task false
        updateLocalLastRead(application.contentResolver, accountKey, conversationId, lastReadEvent)
        return@task true
    }.successUi {
        bus.post(UnreadCountUpdatedEvent(-1))
    }


    fun batchMarkRead(accountKey: UserKey, markTimestampBefore: Long): Promise<Boolean, Exception> = task {
        val cr = application.contentResolver
        val projection = (Messages.Conversations.COLUMNS + Messages.Conversations.UNREAD_COUNT).map {
            TwidereQueryBuilder.mapConversationsProjection(it)
        }.toTypedArray()

        val unreadWhere = Expression.greaterThan(Columns.Column(Table(Messages.Conversations.TABLE_NAME),
                Messages.Conversations.LAST_READ_TIMESTAMP), markTimestampBefore)
        val unreadHaving = Expression.greaterThan(Messages.Conversations.UNREAD_COUNT, 0)

        val cRef = cr.getUnreadMessagesEntriesCursorReference(projection, arrayOf(accountKey),
                unreadWhere, null, unreadHaving, null) ?: return@task false
        val account = AccountManager.get(application).getDetailsOrThrow(accountKey, true)
        cRef.use { (cur) ->
            val indices = ObjectCursor.indicesFrom(cur, ParcelableMessageConversation::class.java)
            cur.forEachRow { c, _ ->
                val conversation = indices.newObject(c)
                try {
                    val lastReadEvent = performMarkRead(account, conversation) ?: return@forEachRow false
                    updateLocalLastRead(cr, account.key, conversation.id,
                            lastReadEvent)
                    return@forEachRow true
                } catch (e: MicroBlogException) {
                    return@forEachRow false
                }
            }
        }
        return@task true
    }.successUi {
        bus.post(UnreadCountUpdatedEvent(-1))
    }

    private fun clearMessagesSync(account: AccountDetails, conversationId: String): Boolean {
        val messagesWhere = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                Expression.equalsArgs(Messages.CONVERSATION_ID)).sql
        val messagesWhereArgs = arrayOf(account.key.toString(), conversationId)
        val projection = arrayOf(Messages.MESSAGE_ID)
        val messageIds = mutableListOf<String>()
        var allSuccess = true
        application.contentResolver.queryReference(Messages.CONTENT_URI, projection, messagesWhere,
                messagesWhereArgs, null)?.use { (cur) ->
            cur.moveToFirst()
            while (!cur.isAfterLast) {
                val messageId = cur.getString(0)
                try {
                    if (requestDestroyMessage(account, messageId)) {
                        messageIds.add(messageId)
                    }
                } catch (e: MicroBlogException) {
                    allSuccess = false
                    // Ignore
                }
                cur.moveToNext()
            }
        }
        ContentResolverUtils.bulkDelete(application.contentResolver, Messages.CONTENT_URI,
                Messages.MESSAGE_ID, false, messageIds, messagesWhere, messagesWhereArgs)
        val conversationWhere = Expression.and(Expression.equalsArgs(Messages.Conversations.ACCOUNT_KEY),
                Expression.equalsArgs(Messages.Conversations.CONVERSATION_ID)).sql
        val conversationWhereArgs = arrayOf(account.key.toString(), conversationId)
        application.contentResolver.update(Messages.Conversations.CONTENT_URI,
                Messages.Conversations.COLUMNS, conversationWhere, conversationWhereArgs,
                cls = ParcelableMessageConversation::class.java) { item ->
            item.message_extras = null
            item.message_type = null
            item.message_timestamp = -1L
            item.text_unescaped = null
            item.media = null
            return@update item
        }
        return allSuccess
    }

    private fun requestDestroyConversation(account: AccountDetails, conversationId: String): Boolean {
        when (account.type) {
            AccountType.TWITTER -> {
                if (account.isOfficial(application)) {
                    val twitter = account.newMicroBlogInstance(application, MicroBlog::class.java)
                    return twitter.deleteDmConversation(conversationId).isSuccessful
                }
            }
        }
        return false
    }

    private fun requestDestroyMessage(account: AccountDetails, messageId: String): Boolean {
        when (account.type) {
            AccountType.TWITTER -> {
                val twitter = account.newMicroBlogInstance(application, cls = MicroBlog::class.java)
                if (account.isOfficial(application)) {
                    return twitter.destroyDm(messageId).isSuccessful
                }
                twitter.destroyDirectMessage(messageId)
                return true
            }
        }
        return false
    }

    @Throws(MicroBlogException::class)
    private fun performMarkRead(account: AccountDetails, conversation: ParcelableMessageConversation): Pair<String, Long>? {
        val cr = application.contentResolver
        when (account.type) {
            AccountType.TWITTER -> {
                val microBlog = account.newMicroBlogInstance(application, cls = MicroBlog::class.java)
                if (account.isOfficial(application)) {
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

    private fun updateLocalLastRead(cr: ContentResolver, accountKey: UserKey,
            conversationId: String, lastRead: Pair<String, Long>) {
        val values = ContentValues()
        values.put(Messages.Conversations.LAST_READ_ID, lastRead.first)
        values.put(Messages.Conversations.LAST_READ_TIMESTAMP, lastRead.second)
        val updateWhere = Expression.and(Expression.equalsArgs(Messages.Conversations.ACCOUNT_KEY),
                Expression.equalsArgs(Messages.Conversations.CONVERSATION_ID),
                Expression.lesserThan(Messages.Conversations.LAST_READ_TIMESTAMP, lastRead.second)).sql
        val updateWhereArgs = arrayOf(accountKey.toString(), conversationId)
        cr.update(Messages.Conversations.CONTENT_URI, values, updateWhere, updateWhereArgs)
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

    companion object : ApplicationContextSingletonHolder<MessagePromises>(::MessagePromises)
}
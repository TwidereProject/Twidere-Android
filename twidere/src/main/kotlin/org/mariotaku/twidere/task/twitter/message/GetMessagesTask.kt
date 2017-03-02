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

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import org.mariotaku.ktextension.toInt
import org.mariotaku.ktextension.toLong
import org.mariotaku.ktextension.useCursor
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.DMResponse
import org.mariotaku.microblog.library.twitter.model.DirectMessage
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.QUERY_PARAM_SHOW_NOTIFICATION
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.ParcelableMessageConversation.ConversationType
import org.mariotaku.twidere.model.event.GetMessagesTaskEvent
import org.mariotaku.twidere.model.message.conversation.DefaultConversationExtras
import org.mariotaku.twidere.model.message.conversation.TwitterOfficialConversationExtras
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.AccountUtils.getAccountDetails
import org.mariotaku.twidere.model.util.ParcelableMessageUtils
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Messages
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.task.BaseAbstractTask
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.UriUtils
import org.mariotaku.twidere.util.content.ContentResolverUtils
import java.util.*

/**
 * Created by mariotaku on 2017/2/8.
 */

class GetMessagesTask(
        context: Context
) : BaseAbstractTask<GetMessagesTask.RefreshMessagesTaskParam, Unit, (Boolean) -> Unit>(context) {

    private val profileImageSize = context.getString(R.string.profile_image_size)

    override fun doLongOperation(param: RefreshMessagesTaskParam) {
        val accountKeys = param.accountKeys
        val am = android.accounts.AccountManager.get(context)
        accountKeys.forEachIndexed { i, accountKey ->
            val details = getAccountDetails(am, accountKey, true) ?: return@forEachIndexed
            val microBlog = details.newMicroBlogInstance(context, true, cls = MicroBlog::class.java)
            val messages = try {
                getMessages(microBlog, details, param, i)
            } catch (e: MicroBlogException) {
                return@forEachIndexed
            }
            storeMessages(context, messages, details, param.showNotification)
        }
    }

    override fun afterExecute(callback: ((Boolean) -> Unit)?, result: Unit) {
        callback?.invoke(true)
        bus.post(GetMessagesTaskEvent(Messages.CONTENT_URI, params?.taskTag, false, null))
    }

    private fun getMessages(microBlog: MicroBlog, details: AccountDetails, param: RefreshMessagesTaskParam, index: Int): DatabaseUpdateData {
        when (details.type) {
            AccountType.FANFOU -> {
                // Use fanfou DM api, disabled since it's conversation api is not suitable for paging
                // return getFanfouMessages(microBlog, details, param, index)
            }
            AccountType.TWITTER -> {
                // Use official DM api
                if (details.isOfficial(context)) {
                    return getTwitterOfficialMessages(microBlog, details, param, index)
                }
            }
        }
        // Use default method
        return getDefaultMessages(microBlog, details, param, index)
    }

    private fun getTwitterOfficialMessages(microBlog: MicroBlog, details: AccountDetails,
            param: RefreshMessagesTaskParam, index: Int): DatabaseUpdateData {
        val conversationId = param.conversationId
        if (conversationId == null) {
            return getTwitterOfficialUserInbox(microBlog, details, param, index)
        } else {
            return getTwitterOfficialConversation(microBlog, details, conversationId, param, index)
        }
    }

    private fun getFanfouMessages(microBlog: MicroBlog, details: AccountDetails, param: RefreshMessagesTaskParam, index: Int): DatabaseUpdateData {
        val conversationId = param.conversationId
        if (conversationId == null) {
            return getFanfouConversations(microBlog, details, param, index)
        } else {
            return DatabaseUpdateData(emptyList(), emptyList())
        }
    }

    private fun getDefaultMessages(microBlog: MicroBlog, details: AccountDetails, param: RefreshMessagesTaskParam, index: Int): DatabaseUpdateData {
        val accountKey = details.key

        val sinceIds = if (param.hasSinceIds) param.sinceIds else null
        val maxIds = if (param.hasMaxIds) param.maxIds else null

        val received = microBlog.getDirectMessages(Paging().apply {
            count(100)
            val maxId = maxIds?.get(index)
            val sinceId = sinceIds?.get(index)
            if (maxId != null) {
                maxId(maxId)
            }
            if (sinceId != null) {
                sinceId(sinceId)
            }
        })
        val sent = microBlog.getSentDirectMessages(Paging().apply {
            count(100)
            val accountsCount = param.accountKeys.size
            val maxId = maxIds?.get(accountsCount + index)
            val sinceId = sinceIds?.get(accountsCount + index)
            if (maxId != null) {
                maxId(maxId)
            }
            if (sinceId != null) {
                sinceId(sinceId)
            }
        })


        val insertMessages = arrayListOf<ParcelableMessage>()
        val conversations = hashMapOf<String, ParcelableMessageConversation>()

        val conversationIds = hashSetOf<String>()
        received.forEach {
            conversationIds.add(ParcelableMessageUtils.incomingConversationId(it.senderId, it.recipientId))
        }
        sent.forEach {
            conversationIds.add(ParcelableMessageUtils.outgoingConversationId(it.senderId, it.recipientId))
        }

        conversations.addLocalConversations(context, accountKey, conversationIds)

        received.forEachIndexed { i, dm ->
            addConversationMessage(insertMessages, conversations, details, dm, i, received.size, false)
        }
        sent.forEachIndexed { i, dm ->
            addConversationMessage(insertMessages, conversations, details, dm, i, sent.size, true)
        }
        return DatabaseUpdateData(conversations.values, insertMessages)
    }


    private fun getTwitterOfficialConversation(microBlog: MicroBlog, details: AccountDetails,
            conversationId: String, param: RefreshMessagesTaskParam, index: Int): DatabaseUpdateData {
        val maxId = param.maxIds?.get(index) ?: return DatabaseUpdateData(emptyList(), emptyList())
        val paging = Paging().apply {
            maxId(maxId)
        }

        val response = microBlog.getDmConversation(conversationId, paging).conversationTimeline
        return createDatabaseUpdateData(context, details, response, profileImageSize)
    }

    private fun getTwitterOfficialUserInbox(microBlog: MicroBlog, details: AccountDetails,
            param: RefreshMessagesTaskParam, index: Int): DatabaseUpdateData {
        val maxId = if (param.hasMaxIds) param.maxIds?.get(index) else null
        val cursor = if (param.hasCursors) param.cursors?.get(index) else null
        val response = if (cursor != null) {
            microBlog.getUserUpdates(cursor).userEvents
        } else {
            microBlog.getUserInbox(Paging().apply {
                if (maxId != null) {
                    maxId(maxId)
                }
            }).userInbox
        }
        return createDatabaseUpdateData(context, details, response, profileImageSize)
    }


    private fun getFanfouConversations(microBlog: MicroBlog, details: AccountDetails, param: RefreshMessagesTaskParam, index: Int): DatabaseUpdateData {
        val accountKey = details.key
        val cursor = param.cursors?.get(index)
        val page = cursor?.substringAfter("page:").toInt(-1)
        val result = microBlog.getConversationList(Paging().apply {
            count(60)
            if (page >= 0) {
                page(page)
            }
        })
        val conversations = hashMapOf<String, ParcelableMessageConversation>()

        val conversationIds = hashSetOf<String>()
        result.mapTo(conversationIds) { "${accountKey.id}-${it.otherId}" }
        conversations.addLocalConversations(context, accountKey, conversationIds)
        result.forEachIndexed { i, item ->
            val dm = item.dm
            // Sender is our self, treat as outgoing message
            val message = ParcelableMessageUtils.fromMessage(accountKey, dm, dm.senderId == accountKey.id,
                    1.0 - (i.toDouble() / result.size))
            val sender = ParcelableUserUtils.fromUser(dm.sender, accountKey)
            val recipient = ParcelableUserUtils.fromUser(dm.recipient, accountKey)
            val mc = conversations.addConversation(message.conversation_id, details, message,
                    setOf(sender, recipient))
            mc?.request_cursor = "page:$page"
        }
        return DatabaseUpdateData(conversations.values, emptyList())
    }

    data class DatabaseUpdateData(
            val conversations: Collection<ParcelableMessageConversation>,
            val messages: Collection<ParcelableMessage>,
            val deleteConversations: List<String> = emptyList(),
            val deleteMessages: Map<String, List<String>> = emptyMap(),
            val conversationRequestCursor: String? = null
    )

    abstract class RefreshNewTaskParam(
            context: Context
    ) : RefreshMessagesTaskParam(context) {

        override val showNotification: Boolean = true

        override val sinceIds: Array<String?>?
            get() {
                val incomingIds = DataStoreUtils.getNewestMessageIds(context, Messages.CONTENT_URI,
                        defaultKeys, false)
                val outgoingIds = DataStoreUtils.getNewestMessageIds(context, Messages.CONTENT_URI,
                        defaultKeys, true)
                return incomingIds + outgoingIds
            }

        override val cursors: Array<String?>?
            get() {
                val cursors = arrayOfNulls<String>(defaultKeys.size)
                val newestConversations = DataStoreUtils.getNewestConversations(context,
                        Conversations.CONTENT_URI, twitterOfficialKeys)
                newestConversations.forEachIndexed { i, conversation ->
                    cursors[i] = conversation?.request_cursor
                }
                return cursors
            }

        override val hasSinceIds: Boolean = true
        override val hasMaxIds: Boolean = false
        override val hasCursors: Boolean = true
    }

    abstract class LoadMoreEntriesTaskParam(
            context: Context
    ) : RefreshMessagesTaskParam(context) {

        override val maxIds: Array<String?>? by lazy {
            val incomingIds = DataStoreUtils.getOldestMessageIds(context, Messages.CONTENT_URI,
                    defaultKeys, false)
            val outgoingIds = DataStoreUtils.getOldestMessageIds(context, Messages.CONTENT_URI,
                    defaultKeys, true)
            val oldestConversations = DataStoreUtils.getOldestConversations(context,
                    Conversations.CONTENT_URI, twitterOfficialKeys)
            oldestConversations.forEachIndexed { i, conversation ->
                val extras = conversation?.conversation_extras as? TwitterOfficialConversationExtras ?: return@forEachIndexed
                incomingIds[i] = extras.maxEntryId
            }
            return@lazy incomingIds + outgoingIds
        }

        override val hasSinceIds: Boolean = false
        override val hasMaxIds: Boolean = true
    }

    class LoadMoreMessageTaskParam(
            context: Context,
            accountKey: UserKey,
            override val conversationId: String,
            maxId: String
    ) : RefreshMessagesTaskParam(context) {
        override val accountKeys: Array<UserKey> = arrayOf(accountKey)
        override val maxIds: Array<String?>? = arrayOf(maxId)
        override val hasMaxIds: Boolean = true
    }

    abstract class RefreshMessagesTaskParam(
            val context: Context
    ) : SimpleRefreshTaskParam() {

        /**
         * If `conversationId` has value, load messages in conversationId
         */
        open val conversationId: String? = null

        open val showNotification: Boolean = false

        var taskTag: String? = null

        protected val accounts: Array<AccountDetails?> by lazy {
            AccountUtils.getAllAccountDetails(android.accounts.AccountManager.get(context), accountKeys, false)
        }

        protected val defaultKeys: Array<UserKey?>by lazy {
            return@lazy accounts.map { account ->
                account ?: return@map null
                if (account.isOfficial(context) || account.type == AccountType.FANFOU) {
                    return@map null
                }
                return@map account.key
            }.toTypedArray()
        }

        protected val twitterOfficialKeys: Array<UserKey?> by lazy {
            return@lazy accounts.map { account ->
                account ?: return@map null
                if (!account.isOfficial(context)) {
                    return@map null
                }
                return@map account.key
            }.toTypedArray()
        }

    }

    companion object {

        fun createDatabaseUpdateData(context: Context, account: AccountDetails,
                response: DMResponse, profileImageSize: String = "normal"): DatabaseUpdateData {
            val accountKey = account.key

            val respConversations = response.conversations.orEmpty()
            val respEntries = response.entries.orEmpty()
            val respUsers = response.users.orEmpty()

            val conversations = hashMapOf<String, ParcelableMessageConversation>()

            conversations.addLocalConversations(context, accountKey, respConversations.keys)
            val messages = ArrayList<ParcelableMessage>()
            val messageDeletionsMap = HashMap<String, ArrayList<String>>()
            val conversationDeletions = ArrayList<String>()
            respEntries.mapNotNullTo(messages) { entry ->
                when {
                    entry.messageDelete != null -> {
                        val list = messageDeletionsMap.getOrPut(entry.messageDelete.conversationId) { ArrayList<String>() }
                        entry.messageDelete.messages?.forEach {
                            list.add(it.messageId)
                        }
                        return@mapNotNullTo null
                    }
                    entry.removeConversation != null -> {
                        conversationDeletions.add(entry.removeConversation.conversationId)
                        return@mapNotNullTo null
                    }
                    else -> {
                        return@mapNotNullTo ParcelableMessageUtils.fromEntry(accountKey, entry,
                                respUsers, profileImageSize)
                    }
                }
            }

            val messagesMap = messages.groupBy(ParcelableMessage::conversation_id)

            conversations.addLocalConversations(context, accountKey, messagesMap.keys)

            for ((k, v) in respConversations) {
                val recentMessage = messagesMap[k]?.maxBy(ParcelableMessage::message_timestamp)
                val participants = respUsers.filterKeys { userId ->
                    v.participants.any { it.userId == userId }
                }.values.map { ParcelableUserUtils.fromUser(it, accountKey) }
                val conversationType = when (v.type?.toUpperCase(Locale.US)) {
                    DMResponse.Conversation.Type.ONE_TO_ONE -> ConversationType.ONE_TO_ONE
                    DMResponse.Conversation.Type.GROUP_DM -> ConversationType.GROUP
                    else -> ConversationType.ONE_TO_ONE
                }
                val conversation = conversations.addConversation(k, account, recentMessage, participants,
                        false, conversationType) ?: continue
                if (conversation.id in conversationDeletions) continue
                conversation.conversation_name = v.name
                conversation.conversation_avatar = v.avatarImageHttps
                conversation.request_cursor = response.cursor
                conversation.conversation_extras_type = ParcelableMessageConversation.ExtrasType.TWITTER_OFFICIAL
                val myLastReadEventId = v.participants.first { it.userId == accountKey.id }?.lastReadEventId
                // Find recent message timestamp
                val myLastReadTimestamp = messagesMap.findLastReadTimestamp(k, myLastReadEventId)

                conversation.last_read_id = myLastReadEventId
                if (myLastReadTimestamp > 0) {
                    conversation.last_read_timestamp = myLastReadTimestamp
                }
                val conversationExtras = conversation.conversation_extras as? TwitterOfficialConversationExtras ?: run {
                    val extras = TwitterOfficialConversationExtras()
                    conversation.conversation_extras = extras
                    return@run extras
                }
                conversationExtras.apply {
                    this.minEntryId = v.minEntryId
                    this.maxEntryId = v.maxEntryId
                    this.status = v.status
                    this.readOnly = v.isReadOnly
                    this.notificationsDisabled = v.isNotificationsDisabled
                    val maxEntryTimestamp = messagesMap.findLastReadTimestamp(k, maxEntryId)
                    if (maxEntryTimestamp > 0) {
                        this.maxEntryTimestamp = maxEntryTimestamp
                    }
                }
            }
            return DatabaseUpdateData(conversations.values, messages, conversationDeletions,
                    messageDeletionsMap, response.cursor)
        }

        fun storeMessages(context: Context, data: DatabaseUpdateData, details: AccountDetails,
                showNotification: Boolean = false) {
            val resolver = context.contentResolver
            val conversationsValues = data.conversations.filterNot {
                it.id in data.deleteConversations
            }.map {
                val values = ParcelableMessageConversationValuesCreator.create(it)
                if (it._id > 0) {
                    values.put(Conversations._ID, it._id)
                }
                return@map values
            }
            val messagesValues = data.messages.filterNot {
                data.deleteMessages[it.conversation_id]?.contains(it.id) ?: false
            }.map(ParcelableMessageValuesCreator::create)

            for ((conversationId, messageIds) in data.deleteMessages) {
                val where = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                        Expression.equalsArgs(Messages.CONVERSATION_ID)).sql
                val whereArgs = arrayOf(details.key.toString(), conversationId)
                ContentResolverUtils.bulkDelete(resolver, Messages.CONTENT_URI, Messages.MESSAGE_ID,
                        false, messageIds, where, whereArgs)
            }

            val accountWhere = Expression.equalsArgs(Messages.ACCOUNT_KEY).sql
            val accountWhereArgs = arrayOf(details.key.toString())

            ContentResolverUtils.bulkDelete(resolver, Conversations.CONTENT_URI, Conversations.CONVERSATION_ID,
                    false, data.deleteConversations, accountWhere, accountWhereArgs)
            ContentResolverUtils.bulkDelete(resolver, Messages.CONTENT_URI, Messages.CONVERSATION_ID,
                    false, data.deleteConversations, accountWhere, accountWhereArgs)

            // Don't change order! insert messages first
            ContentResolverUtils.bulkInsert(resolver, Messages.CONTENT_URI, messagesValues)
            // Notifications will show on conversations inserted
            ContentResolverUtils.bulkInsert(resolver, UriUtils.appendQueryParameters(Conversations.CONTENT_URI,
                    QUERY_PARAM_SHOW_NOTIFICATION, showNotification), conversationsValues)

            if (data.conversationRequestCursor != null) {
                resolver.update(Conversations.CONTENT_URI, ContentValues().apply {
                    put(Conversations.REQUEST_CURSOR, data.conversationRequestCursor)
                }, accountWhere, accountWhereArgs)
            }
        }

        @SuppressLint("Recycle")
        internal fun MutableMap<String, ParcelableMessageConversation>.addLocalConversations(context: Context,
                accountKey: UserKey, conversationIds: Set<String>) {
            val newIds = conversationIds.filterNot { it in this.keys }
            val where = Expression.and(Expression.inArgs(Conversations.CONVERSATION_ID, newIds.size),
                    Expression.equalsArgs(Conversations.ACCOUNT_KEY)).sql
            val whereArgs = newIds.toTypedArray() + accountKey.toString()
            return context.contentResolver.query(Conversations.CONTENT_URI, Conversations.COLUMNS,
                    where, whereArgs, null).useCursor { cur ->
                val indices = ParcelableMessageConversationCursorIndices(cur)
                cur.moveToFirst()
                while (!cur.isAfterLast) {
                    val conversationId = cur.getString(indices.id)
                    val timestamp = cur.getLong(indices.local_timestamp)
                    val conversation = this[conversationId] ?: run {
                        val obj = indices.newObject(cur)
                        this[conversationId] = obj
                        return@run obj
                    }
                    if (timestamp > conversation.local_timestamp) {
                        this[conversationId] = indices.newObject(cur)
                    }
                    indices.newObject(cur)
                    cur.moveToNext()
                }
            }
        }


        private fun Map<String, List<ParcelableMessage>>.findLastReadTimestamp(conversationId: String, lastReadEventId: String?): Long {
            val longEventId = lastReadEventId.toLong(-1)
            return this[conversationId]?.filter { message ->
                if (message.id == lastReadEventId) return@filter true
                if (longEventId > 0 && longEventId >= message.id.toLong(-1)) return@filter true
                return@filter false
            }?.maxBy(ParcelableMessage::message_timestamp)?.message_timestamp ?: -1
        }

        fun MutableMap<String, ParcelableMessageConversation>.addConversation(
                conversationId: String,
                details: AccountDetails,
                message: ParcelableMessage?,
                users: Collection<ParcelableUser>,
                addUsers: Boolean = false,
                conversationType: String = ConversationType.ONE_TO_ONE
        ): ParcelableMessageConversation? {
            val conversation = this[conversationId] ?: run {
                if (message == null) return null
                val obj = ParcelableMessageConversation()
                obj.id = conversationId
                obj.conversation_type = conversationType
                obj.applyFrom(message, details)
                this[conversationId] = obj
                return@run obj
            }
            if (message != null && message.timestamp > conversation.timestamp) {
                conversation.applyFrom(message, details)
            }
            if (addUsers) {
                conversation.addParticipants(users)
            } else {
                conversation.participants = users.toTypedArray()
                conversation.participant_keys = users.map(ParcelableUser::key).toTypedArray()
            }
            return conversation
        }

        internal fun addConversationMessage(messages: MutableCollection<ParcelableMessage>,
                conversations: MutableMap<String, ParcelableMessageConversation>,
                details: AccountDetails, dm: DirectMessage, index: Int, size: Int, outgoing: Boolean) {
            val accountKey = details.key
            val message = ParcelableMessageUtils.fromMessage(accountKey, dm, outgoing,
                    1.0 - (index.toDouble() / size))
            messages.add(message)
            val sender = ParcelableUserUtils.fromUser(dm.sender, accountKey)
            val recipient = ParcelableUserUtils.fromUser(dm.recipient, accountKey)
            val conversation = conversations.addConversation(message.conversation_id, details,
                    message, setOf(sender, recipient)) ?: return
            conversation.conversation_extras_type = ParcelableMessageConversation.ExtrasType.DEFAULT
            if (conversation.conversation_extras == null) {
                conversation.conversation_extras = DefaultConversationExtras()
            }
        }
    }
}


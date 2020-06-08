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
import android.content.ContentValues
import android.content.Context
import org.mariotaku.commons.logansquare.LoganSquareMapperFinder
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.ktextension.toIntOr
import org.mariotaku.ktextension.toLongOr
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.DMResponse
import org.mariotaku.microblog.library.twitter.model.DirectMessage
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.QUERY_PARAM_SHOW_NOTIFICATION
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.exception.APINotSupportedException
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.queryCount
import org.mariotaku.twidere.extension.queryReference
import org.mariotaku.twidere.extension.set
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.ParcelableMessageConversation.ConversationType
import org.mariotaku.twidere.model.event.GetMessagesTaskEvent
import org.mariotaku.twidere.model.message.conversation.DefaultConversationExtras
import org.mariotaku.twidere.model.message.conversation.TwitterOfficialConversationExtras
import org.mariotaku.twidere.model.pagination.CursorPagination
import org.mariotaku.twidere.model.pagination.Pagination
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.AccountUtils.getAccountDetails
import org.mariotaku.twidere.model.util.ParcelableMessageUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Messages
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.task.BaseAbstractTask
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.UriUtils
import org.mariotaku.twidere.util.content.ContentResolverUtils
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.filter
import kotlin.collections.set

/**
 * Created by mariotaku on 2017/2/8.
 */

class GetMessagesTask(
        context: Context
) : BaseAbstractTask<GetMessagesTask.RefreshMessagesTaskParam, Unit, (Boolean) -> Unit>(context) {

    private val profileImageSize = context.getString(R.string.profile_image_size)

    override fun doLongOperation(param: RefreshMessagesTaskParam) {
        val accountKeys = param.accountKeys
        val am = AccountManager.get(context)
        accountKeys.forEachIndexed { i, accountKey ->
            val details = try {
                getAccountDetails(am, accountKey, true) ?: return@forEachIndexed
            } catch (e: LoganSquareMapperFinder.ClassLoaderDeadLockException) {
                return
            }
            val microBlog = details.newMicroBlogInstance(context, cls = MicroBlog::class.java)
            val messages = try {
                if (!details.hasDm) throw APINotSupportedException(details.type)
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
        return if (conversationId == null) {
            getTwitterOfficialUserInbox(microBlog, details, param, index)
        } else {
            getTwitterOfficialConversation(microBlog, details, conversationId, param, index)
        }
    }

    private fun getFanfouMessages(microBlog: MicroBlog, details: AccountDetails, param: RefreshMessagesTaskParam, index: Int): DatabaseUpdateData {
        val conversationId = param.conversationId
        return if (conversationId == null) {
            getFanfouConversations(microBlog, details, param, index)
        } else {
            DatabaseUpdateData(emptyList(), emptyList())
        }
    }

    private fun getDefaultMessages(microBlog: MicroBlog, details: AccountDetails,
            param: RefreshMessagesTaskParam, index: Int): DatabaseUpdateData {
        val accountKey = details.key
        val accountsCount = param.accountKeys.size

        val receivedPagination = param.pagination?.get(index) as? SinceMaxPagination
        val sincePagination = param.pagination?.elementAtOrNull(accountsCount + index) as? SinceMaxPagination

        val firstFetch by lazy {
            return@lazy context.contentResolver.queryCount(Conversations.CONTENT_URI,
                    Expression.equalsArgs(Conversations.ACCOUNT_KEY).sql, arrayOf(accountKey.toString())) <= 0
        }

        val updateLastRead = param.hasMaxIds || firstFetch
        // TODO: pagination support
        val list = microBlog.getDirectMessageList(50, Paging().apply {
        })

        val possibleUserId = (list.map { it.messageCreate.target.recipientId } + list.map { it.messageCreate.senderId }).distinct()
        val users = microBlog.lookupUsers(possibleUserId.toTypedArray())

        val result = list.apply {
            sortByDescending { it.createdTimestamp.toLong() }
        }.map {
            DirectMessage().also { directMessage ->
                directMessage[DirectMessage::class.java.getDeclaredField("text")] = it.messageCreate.messageData.text
                directMessage[DirectMessage::class.java.getDeclaredField("id")] = it.id
                directMessage[DirectMessage::class.java.getDeclaredField("sender")] = users.firstOrNull { user -> it.messageCreate.senderId == user.id }
                directMessage[DirectMessage::class.java.getDeclaredField("recipient")] = users.firstOrNull { user -> it.messageCreate.target.recipientId == user.id }
                directMessage[DirectMessage::class.java.getDeclaredField("createdAt")] = Date(it.createdTimestamp.toLong())
            }
        }.filter { it.sender != null && it.recipient != null }

        val insertMessages = arrayListOf<ParcelableMessage>()

        val conversationIds = result.map {
            if (it.senderId == details.key.id) {
                ParcelableMessageUtils.outgoingConversationId(it.senderId, it.recipientId)
            } else {
                ParcelableMessageUtils.incomingConversationId(it.senderId, it.recipientId)
            }
        }.distinct().toHashSet()

        val conversations = hashMapOf<String, ParcelableMessageConversation>()
        conversations.addLocalConversations(context, accountKey, conversationIds)
        // remove duplicate conversations upgrade from version 4.0.9
        val distinct = distinctLocalConversations(context, accountKey, result.map { it.id }.toSet())
                .distinct()
                .filter { !it.startsWith(details.key.id) || it == details.key.id }

        result.forEachIndexed { i, dm ->
            addConversationMessage(insertMessages, conversations, details, dm, i, list.size,
                    dm.senderId == details.key.id, profileImageSize, updateLastRead)
        }
        return DatabaseUpdateData(conversations.values, insertMessages, distinct)
    }


    private fun getTwitterOfficialConversation(microBlog: MicroBlog, details: AccountDetails,
            conversationId: String, param: RefreshMessagesTaskParam, index: Int): DatabaseUpdateData {
        val maxId = (param.pagination?.get(index) as? SinceMaxPagination)?.maxId
                ?: return DatabaseUpdateData(emptyList(), emptyList())
        val paging = Paging().apply {
            maxId(maxId)
        }

        val response = microBlog.getDmConversation(conversationId, paging).conversationTimeline
        return createDatabaseUpdateData(context, details, response, profileImageSize)
    }

    private fun getTwitterOfficialUserInbox(microBlog: MicroBlog, details: AccountDetails,
            param: RefreshMessagesTaskParam, index: Int): DatabaseUpdateData {
        val maxId = (param.pagination?.get(index) as? SinceMaxPagination)?.maxId
        val cursor = (param.pagination?.get(index) as? CursorPagination)?.cursor
        val response = if (cursor != null) {
            microBlog.getUserUpdates(cursor).userEvents
        } else {
            microBlog.getUserInbox(Paging().apply {
                if (maxId != null) {
                    maxId(maxId)
                }
            }).userInbox
        } ?: throw MicroBlogException("Null response data")
        return createDatabaseUpdateData(context, details, response, profileImageSize)
    }


    private fun getFanfouConversations(microBlog: MicroBlog, details: AccountDetails,
            param: RefreshMessagesTaskParam, index: Int): DatabaseUpdateData {
        val accountKey = details.key
        val accountType = details.type
        val cursor = (param.pagination?.get(index) as? CursorPagination)?.cursor
        val page = cursor?.substringAfter("page:").toIntOr(-1)
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
            val sender = dm.sender.toParcelable(accountKey, accountType)
            val recipient = dm.recipient.toParcelable(accountKey, accountType)
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

        override val pagination by lazy {
            val result = arrayOfNulls<Pagination>(accounts.size * 2)
            val incomingIds = DataStoreUtils.getNewestMessageIds(context, Messages.CONTENT_URI,
                    defaultKeys, false)
            val outgoingIds = DataStoreUtils.getNewestMessageIds(context, Messages.CONTENT_URI,
                    defaultKeys, true)
            val cursors = DataStoreUtils.getNewestConversations(context, Conversations.CONTENT_URI,
                    twitterOfficialKeys).mapToArray { it?.request_cursor }
            accounts.forEachIndexed { index, details ->
                if (details == null) return@forEachIndexed
                if (details.isOfficial(context)) {
                    result[index] = CursorPagination.valueOf(cursors[index])
                } else {
                    result[index] = SinceMaxPagination.sinceId(incomingIds[index], -1)
                    result[accounts.size + index] = SinceMaxPagination.sinceId(outgoingIds[index], -1)
                }
            }
            return@lazy result
        }

    }

    abstract class LoadMoreEntriesTaskParam(
            context: Context
    ) : RefreshMessagesTaskParam(context) {

        override val pagination: Array<out Pagination?>? by lazy {
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
            return@lazy (incomingIds + outgoingIds).mapToArray { maxId ->
                SinceMaxPagination.maxId(maxId, -1)
            }
        }
    }

    class LoadMoreMessageTaskParam(
            context: Context,
            accountKey: UserKey,
            override val conversationId: String,
            maxId: String
    ) : RefreshMessagesTaskParam(context) {
        override val accountKeys = arrayOf(accountKey)
        override val pagination = arrayOf(SinceMaxPagination.maxId(maxId, -1))
    }

    abstract class RefreshMessagesTaskParam(
            val context: Context
    ) : RefreshTaskParam {

        /**
         * If `conversationId` has value, load messages in conversationId
         */
        open val conversationId: String? = null

        open val showNotification: Boolean = false

        var taskTag: String? = null

        protected val accounts: Array<AccountDetails?> by lazy {
            AccountUtils.getAllAccountDetails(AccountManager.get(context), accountKeys, false)
        }

        protected val defaultKeys: Array<UserKey?> by lazy {
            return@lazy accounts.map { account ->
                account ?: return@map null
                if (account.isOfficial(context)) {
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

        private const val KEY_FIRST_FETCH = "state_first_fetch_direct_messages"

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
                        return@mapNotNullTo ParcelableMessageUtils.fromEntry(accountKey, account.type,
                                entry, respUsers, profileImageSize)
                    }
                }
            }

            val messagesMap = messages.groupBy(ParcelableMessage::conversation_id)

            conversations.addLocalConversations(context, accountKey, messagesMap.keys)

            for ((k, v) in respConversations) {
                val recentMessage = messagesMap[k]?.maxBy(ParcelableMessage::message_timestamp)
                val participants = respUsers.filterKeys { userId ->
                    v.participants.any { it.userId == userId }
                }.values.map {
                    it.toParcelable(account, profileImageSize = profileImageSize)
                }
                val conversationType = when (v.type?.toUpperCase(Locale.US)) {
                    DMResponse.Conversation.Type.ONE_TO_ONE -> ConversationType.ONE_TO_ONE
                    DMResponse.Conversation.Type.GROUP_DM -> ConversationType.GROUP
                    else -> ConversationType.ONE_TO_ONE
                }
                val conversation = conversations.addConversation(k, account, recentMessage, participants,
                        conversationType, appendUsers = false) ?: continue
                if (conversation.id in conversationDeletions) continue
                conversation.conversation_name = v.name
                conversation.conversation_avatar = v.avatarImageHttps
                conversation.request_cursor = response.cursor
                conversation.conversation_extras_type = ParcelableMessageConversation.ExtrasType.TWITTER_OFFICIAL
                val myLastReadEventId = v.participants.firstOrNull { it.userId == accountKey.id }?.lastReadEventId
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
            val conversationCreator = ObjectCursor.valuesCreatorFrom(ParcelableMessageConversation::class.java)
            val conversationsValues = data.conversations.filterNot {
                it.id in data.deleteConversations
            }.map {
                val values = conversationCreator.create(it)
                if (it._id > 0) {
                    values.put(Conversations._ID, it._id)
                }
                return@map values
            }
            val messageCreator = ObjectCursor.valuesCreatorFrom(ParcelableMessage::class.java)
            val messagesValues = data.messages.filterNot {
                data.deleteMessages[it.conversation_id]?.contains(it.id) ?: false
            }.map(messageCreator::create)

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


        internal fun distinctLocalConversations(context: Context, accountKey: UserKey, messageIds: Set<String>): ArrayList<String> {
            val where = Expression.and(Expression.inArgs(Messages.MESSAGE_ID, messageIds.size),
                    Expression.equalsArgs(Conversations.ACCOUNT_KEY)).sql
            val whereArgs = messageIds.toTypedArray() + accountKey.toString()
            val result = arrayListOf<String>()
            context.contentResolver.queryReference(Messages.CONTENT_URI, Messages.COLUMNS,
                    where, whereArgs, null)?.use { (cur) ->
                val indices = ObjectCursor.indicesFrom(cur, ParcelableMessage::class.java)
                cur.moveToFirst()
                while (!cur.isAfterLast) {
                    val conversationId = cur.getString(indices[Messages.CONVERSATION_ID])
                    result.add(conversationId)
                    indices.newObject(cur)
                    cur.moveToNext()
                }
            }
            return result
        }

        internal fun MutableMap<String, ParcelableMessageConversation>.addLocalConversations(context: Context,
                accountKey: UserKey, conversationIds: Set<String>) {
            val newIds = conversationIds.filterNot { it in this.keys }
            val where = Expression.and(Expression.inArgs(Conversations.CONVERSATION_ID, newIds.size),
                    Expression.equalsArgs(Conversations.ACCOUNT_KEY)).sql
            val whereArgs = newIds.toTypedArray() + accountKey.toString()
            context.contentResolver.queryReference(Conversations.CONTENT_URI, Conversations.COLUMNS,
                    where, whereArgs, null)?.use { (cur) ->
                val indices = ObjectCursor.indicesFrom(cur, ParcelableMessageConversation::class.java)
                cur.moveToFirst()
                while (!cur.isAfterLast) {
                    val conversationId = cur.getString(indices[Conversations.CONVERSATION_ID])
                    val timestamp = cur.getLong(indices[Conversations.LOCAL_TIMESTAMP])
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
            val longEventId = lastReadEventId.toLongOr(-1L)
            return this[conversationId]?.filter { message ->
                if (message.id == lastReadEventId) return@filter true
                if (longEventId > 0 && longEventId >= message.id.toLongOr(-1L)) return@filter true
                return@filter false
            }?.maxBy(ParcelableMessage::message_timestamp)?.message_timestamp ?: -1
        }

        /**
         * @param appendUsers True to append users, false to overwrite
         * @param updateLastRead True to set `lastRead` values to newest message
         */
        fun MutableMap<String, ParcelableMessageConversation>.addConversation(
                conversationId: String,
                details: AccountDetails,
                message: ParcelableMessage?,
                users: Collection<ParcelableUser>,
                conversationType: String = ConversationType.ONE_TO_ONE,
                appendUsers: Boolean = false,
                updateLastRead: Boolean = false
        ): ParcelableMessageConversation? {
            fun ParcelableMessageConversation.applyLastRead(message: ParcelableMessage) {
                last_read_id = message.id
                last_read_timestamp = message.timestamp
            }

            val conversation = this[conversationId] ?: run {
                if (message == null) return null
                val obj = ParcelableMessageConversation()
                obj.id = conversationId
                obj.conversation_type = conversationType
                obj.applyFrom(message, details)
                if (updateLastRead) {
                    obj.applyLastRead(message)
                }
                this[conversationId] = obj
                return@run obj
            }
            if (message != null && message.timestamp > conversation.timestamp) {
                conversation.applyFrom(message, details)
                if (updateLastRead) {
                    conversation.applyLastRead(message)
                }
            }
            if (appendUsers) {
                conversation.addParticipants(users)
            } else {
                conversation.participants = users.toTypedArray()
                conversation.participant_keys = users.mapToArray(ParcelableUser::key)
            }
            return conversation
        }

        internal fun addConversationMessage(messages: MutableCollection<ParcelableMessage>,
                conversations: MutableMap<String, ParcelableMessageConversation>,
                details: AccountDetails, dm: DirectMessage, index: Int, size: Int,
                outgoing: Boolean, profileImageSize: String = "normal", updateLastRead: Boolean) {
            val accountKey = details.key
            val accountType = details.type
            val message = ParcelableMessageUtils.fromMessage(accountKey, dm, outgoing,
                    1.0 - (index.toDouble() / size))
            messages.add(message)
            val sender = dm.sender.toParcelable(accountKey, accountType, profileImageSize = profileImageSize)
            val recipient = dm.recipient.toParcelable(accountKey, accountType, profileImageSize = profileImageSize)
            val conversation = conversations.addConversation(message.conversation_id, details,
                    message, setOf(sender, recipient), updateLastRead = updateLastRead) ?: return
            conversation.conversation_extras_type = ParcelableMessageConversation.ExtrasType.DEFAULT
            if (conversation.conversation_extras == null) {
                conversation.conversation_extras = DefaultConversationExtras()
            }
        }
    }
}


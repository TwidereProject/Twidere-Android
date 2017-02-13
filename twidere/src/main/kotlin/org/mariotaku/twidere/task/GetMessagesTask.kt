package org.mariotaku.twidere.task

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.Context
import org.mariotaku.ktextension.toInt
import org.mariotaku.ktextension.useCursor
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.DMResponse
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.applyFrom
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.model.timestamp
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.ParcelableMessageConversation.ConversationType
import org.mariotaku.twidere.model.event.GetMessagesTaskEvent
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.AccountUtils.getAccountDetails
import org.mariotaku.twidere.model.util.ParcelableMessageUtils
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.model.util.UserKeyUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Messages
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.content.ContentResolverUtils
import java.util.*

/**
 * Created by mariotaku on 2017/2/8.
 */

class GetMessagesTask(
        context: Context
) : BaseAbstractTask<GetMessagesTask.RefreshMessagesTaskParam, Unit, (Boolean) -> Unit>(context) {
    override fun doLongOperation(param: RefreshMessagesTaskParam) {
        val accountKeys = param.accountKeys
        val am = AccountManager.get(context)
        accountKeys.forEachIndexed { i, accountKey ->
            val details = getAccountDetails(am, accountKey, true) ?: return@forEachIndexed
            val microBlog = details.newMicroBlogInstance(context, true, cls = MicroBlog::class.java)
            val messages = try {
                getMessages(microBlog, details, param, i)
            } catch (e: MicroBlogException) {
                return@forEachIndexed
            }
            storeMessages(messages, details)
        }
    }

    override fun afterExecute(callback: ((Boolean) -> Unit)?, result: Unit) {
        callback?.invoke(true)
        bus.post(GetMessagesTaskEvent(Messages.CONTENT_URI, false, null))
    }

    private fun getMessages(microBlog: MicroBlog, details: AccountDetails, param: RefreshMessagesTaskParam, index: Int): GetMessagesData {
        when (details.type) {
            AccountType.FANFOU -> {
                // Use fanfou DM api
                return getFanfouMessages(microBlog, details, param, index)
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

    private fun getTwitterOfficialMessages(microBlog: MicroBlog, details: AccountDetails, param: RefreshMessagesTaskParam, index: Int): GetMessagesData {
        val accountKey = details.key
        val cursor = param.cursors?.get(index)
        val page = cursor?.substringAfter("page:").toInt(-1)
        val inbox = microBlog.getUserInbox(Paging().apply {
            count(60)
            if (page >= 0) {
                page(page)
            }
        }).userInbox
        val conversations = hashMapOf<String, ParcelableMessageConversation>()

        val conversationIds = inbox.conversations.keys
        conversations.addLocalConversations(accountKey, conversationIds)
        val messages = inbox.entries.mapNotNull {
            ParcelableMessageUtils.fromEntry(accountKey, it, inbox.users)
        }
        val messagesMap = messages.groupBy(ParcelableMessage::conversation_id)
        for ((k, v) in inbox.conversations) {
            val message = messagesMap[k]?.maxBy(ParcelableMessage::message_timestamp) ?: continue
            val participants = inbox.users.filterKeys { userId ->
                v.participants.any { it.userId == userId }
            }.values
            val conversationType = when (v.type?.toUpperCase(Locale.US)) {
                DMResponse.Conversation.Type.ONE_TO_ONE -> ConversationType.ONE_TO_ONE
                DMResponse.Conversation.Type.GROUP_DM -> ConversationType.GROUP
                else -> ConversationType.ONE_TO_ONE
            }
            val conversation = conversations.addConversation(k, details, message, participants,
                    conversationType)
            conversation.conversation_name = v.name
        }
        return GetMessagesData(conversations.values, messages)
    }

    private fun getFanfouMessages(microBlog: MicroBlog, details: AccountDetails, param: RefreshMessagesTaskParam, index: Int): GetMessagesData {
        val conversationId = param.conversationId
        if (conversationId == null) {
            return getFanfouConversations(microBlog, details, param, index)
        } else {
            return GetMessagesData(emptyList(), emptyList())
        }
    }

    private fun getDefaultMessages(microBlog: MicroBlog, details: AccountDetails, param: RefreshMessagesTaskParam, index: Int): GetMessagesData {
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

        conversations.addLocalConversations(accountKey, conversationIds)

        received.forEachIndexed { i, dm ->
            val message = ParcelableMessageUtils.fromMessage(accountKey, dm, false,
                    1.0 - (i.toDouble() / received.size))
            insertMessages.add(message)
            conversations.addConversation(message.conversation_id, details, message, setOf(dm.sender, dm.recipient))
        }
        sent.forEachIndexed { i, dm ->
            val message = ParcelableMessageUtils.fromMessage(accountKey, dm, true,
                    1.0 - (i.toDouble() / sent.size))
            insertMessages.add(message)
            conversations.addConversation(message.conversation_id, details, message, setOf(dm.sender, dm.recipient))
        }
        return GetMessagesData(conversations.values, insertMessages)
    }

    private fun getFanfouConversations(microBlog: MicroBlog, details: AccountDetails, param: RefreshMessagesTaskParam, index: Int): GetMessagesData {
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
        conversations.addLocalConversations(accountKey, conversationIds)
        result.forEachIndexed { i, item ->
            val dm = item.dm
            // Sender is our self, treat as outgoing message
            val message = ParcelableMessageUtils.fromMessage(accountKey, dm, dm.senderId == accountKey.id,
                    1.0 - (i.toDouble() / result.size))
            val mc = conversations.addConversation(message.conversation_id, details, message,
                    setOf(dm.sender, dm.recipient))
            mc.request_cursor = "page:$page"
        }
        return GetMessagesData(conversations.values, emptyList())
    }

    @SuppressLint("Recycle")
    private fun MutableMap<String, ParcelableMessageConversation>.addLocalConversations(accountKey: UserKey, conversationIds: Set<String>) {
        val where = Expression.and(Expression.inArgs(Conversations.CONVERSATION_ID, conversationIds.size),
                Expression.equalsArgs(Conversations.ACCOUNT_KEY)).sql
        val whereArgs = conversationIds.toTypedArray() + accountKey.toString()
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

    private fun storeMessages(data: GetMessagesData, details: AccountDetails) {
        val resolver = context.contentResolver
        val conversationsValues = data.conversations.map {
            val values = ParcelableMessageConversationValuesCreator.create(it)
            if (it._id > 0) {
                values.put(Conversations._ID, it._id)
            }
            return@map values
        }
        val messagesValues = data.messages.map(ParcelableMessageValuesCreator::create)

        ContentResolverUtils.bulkInsert(resolver, Conversations.CONTENT_URI, conversationsValues)
        ContentResolverUtils.bulkInsert(resolver, Messages.CONTENT_URI, messagesValues)
    }

    private fun ParcelableMessageConversation.addParticipant(
            accountKey: UserKey,
            user: User
    ) {
        val userKey = UserKeyUtils.fromUser(user)
        val participants = this.participants
        if (participants == null) {
            this.participants = arrayOf(ParcelableUserUtils.fromUser(user, accountKey))
        } else {
            val index = participants.indexOfFirst { it.key == userKey }
            if (index >= 0) {
                participants[index] = ParcelableUserUtils.fromUser(user, accountKey)
            } else {
                this.participants = participants + ParcelableUserUtils.fromUser(user, accountKey)
            }
        }
    }

    private fun MutableMap<String, ParcelableMessageConversation>.addConversation(
            conversationId: String,
            details: AccountDetails,
            message: ParcelableMessage,
            users: Collection<User>,
            conversationType: String = ConversationType.ONE_TO_ONE
    ): ParcelableMessageConversation {
        val conversation = this[conversationId] ?: run {
            val obj = ParcelableMessageConversation()
            obj.id = conversationId
            obj.conversation_type = conversationType
            obj.applyFrom(message, details)
            this[conversationId] = obj
            return@run obj
        }
        if (message.timestamp > conversation.timestamp) {
            conversation.applyFrom(message, details)
        }
        users.forEach { user ->
            conversation.addParticipant(details.key, user)
        }
        return conversation
    }


    data class GetMessagesData(
            val conversations: Collection<ParcelableMessageConversation>,
            val messages: Collection<ParcelableMessage>
    )

    class RefreshNewTaskParam(
            context: Context,
            getAccountKeys: () -> Array<UserKey>
    ) : RefreshMessagesTaskParam(context, getAccountKeys) {

        override val sinceIds: Array<String?>?
            get() {
                val keys = accounts.map { account ->
                    when (account?.type) {
                        AccountType.FANFOU -> {
                            return@map null
                        }
                    }
                    return@map account?.key
                }.toTypedArray()
                val incomingIds = DataStoreUtils.getNewestMessageIds(context, Messages.CONTENT_URI,
                        keys, false)
                val outgoingIds = DataStoreUtils.getNewestMessageIds(context, Messages.CONTENT_URI,
                        keys, true)
                return incomingIds + outgoingIds
            }

        override val hasSinceIds: Boolean = true
        override val hasMaxIds: Boolean = false
    }

    class LoadMoreTaskParam(
            context: Context,
            getAccountKeys: () -> Array<UserKey>
    ) : RefreshMessagesTaskParam(context, getAccountKeys) {

        override val maxIds: Array<String?>?
            get() {
                val keys = accounts.map { account ->
                    when (account?.type) {
                        AccountType.FANFOU -> {
                            return@map null
                        }
                    }
                    return@map account?.key
                }.toTypedArray()
                val incomingIds = DataStoreUtils.getOldestMessageIds(context, Messages.CONTENT_URI,
                        keys, false)
                val outgoingIds = DataStoreUtils.getOldestMessageIds(context, Messages.CONTENT_URI,
                        keys, true)
                return incomingIds + outgoingIds
            }

        override val hasSinceIds: Boolean = false
        override val hasMaxIds: Boolean = true
    }

    open class RefreshMessagesTaskParam(
            val context: Context,
            val getAccountKeys: () -> Array<UserKey>
    ) : SimpleRefreshTaskParam() {

        /**
         * If `conversationId` has value, load messages in conversationId
         */
        open var conversationId: String? = null

        protected val accounts: Array<AccountDetails?> by lazy {
            AccountUtils.getAllAccountDetails(AccountManager.get(context), accountKeys, false)
        }

        override final val accountKeys: Array<UserKey>
            get() = getAccountKeys()

    }
}


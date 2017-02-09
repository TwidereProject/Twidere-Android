package org.mariotaku.twidere.task

import android.accounts.AccountManager
import android.content.Context
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.model.setFrom
import org.mariotaku.twidere.extension.model.timestamp
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.util.AccountUtils.getAccountDetails
import org.mariotaku.twidere.model.util.ParcelableMessageUtils
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.model.util.UserKeyUtils
import org.mariotaku.twidere.provider.TwidereDataStore.AccountSupportColumns
import org.mariotaku.twidere.provider.TwidereDataStore.Messages
import org.mariotaku.twidere.util.content.ContentResolverUtils

/**
 * Created by mariotaku on 2017/2/8.
 */

class GetMessagesTask(context: Context) : BaseAbstractTask<RefreshTaskParam, Unit, (Boolean) -> Unit>(context) {
    override fun doLongOperation(param: RefreshTaskParam) {
        val accountKeys = param.accountKeys
        val am = AccountManager.get(context)
        accountKeys.forEachIndexed { i, accountKey ->
            val details = getAccountDetails(am, accountKey, true) ?: return@forEachIndexed
            val microBlog = details.newMicroBlogInstance(context, true, cls = MicroBlog::class.java)
            val messages = try {
                getMessages(microBlog, details)
            } catch (e: MicroBlogException) {
                return@forEachIndexed
            }
            storeMessages(messages, details)
        }
    }

    override fun afterExecute(callback: ((Boolean) -> Unit)?, result: Unit) {
        callback?.invoke(true)
    }

    private fun getMessages(microBlog: MicroBlog, details: AccountDetails): GetMessagesData {
        when (details.type) {
            AccountType.FANFOU -> {
                // Use fanfou DM api
                return getFanfouMessages(microBlog)
            }
            AccountType.TWITTER -> {
                // Use official DM api
                if (details.isOfficial(context)) {
                    return getTwitterOfficialMessages(microBlog, details)
                }
            }
        }
        // Use default method
        return getDefaultMessages(microBlog, details)
    }

    private fun getFanfouMessages(microBlog: MicroBlog): GetMessagesData {
        return GetMessagesData(emptyList(), emptyList(), emptyList())
    }

    private fun getTwitterOfficialMessages(microBlog: MicroBlog, details: AccountDetails): GetMessagesData {
        return getDefaultMessages(microBlog, details)
    }

    private fun getDefaultMessages(microBlog: MicroBlog, details: AccountDetails): GetMessagesData {
        val accountKey = details.key
        val paging = Paging()
        val insertMessages = arrayListOf<ParcelableMessage>()
        val conversations = hashMapOf<String, ParcelableMessageConversation>()
        microBlog.getDirectMessages(paging).forEach { dm ->
            val message = ParcelableMessageUtils.incomingMessage(accountKey, dm)
            insertMessages.add(message)
            conversations.addConversation(accountKey, message, dm.sender, dm.recipient)
        }
        microBlog.getSentDirectMessages(paging).forEach { dm ->
            val message = ParcelableMessageUtils.outgoingMessage(accountKey, dm)
            insertMessages.add(message)
            conversations.addConversation(accountKey, message, dm.sender, dm.recipient)
        }
        return GetMessagesData(conversations.values, emptyList(), insertMessages)
    }

    private fun storeMessages(data: GetMessagesData, details: AccountDetails) {
        val resolver = context.contentResolver
        val where = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY).sql
        val whereArgs = arrayOf(details.key.toString())
        resolver.delete(Messages.CONTENT_URI, where, whereArgs)
        resolver.delete(Messages.Conversations.CONTENT_URI, where, whereArgs)
        val conversationsValues = data.insertConversations.map(ParcelableMessageConversationValuesCreator::create)
        val messagesValues = data.insertMessages.map(ParcelableMessageValuesCreator::create)

        ContentResolverUtils.bulkInsert(resolver, Messages.Conversations.CONTENT_URI, conversationsValues)
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
            accountKey: UserKey,
            message: ParcelableMessage,
            vararg users: User
    ) {
        val conversation = this[message.conversation_id] ?: run {
            val obj = ParcelableMessageConversation()
            this[message.conversation_id] = obj
            obj.setFrom(message)
            return@run obj
        }
        if (message.timestamp > conversation.timestamp) {
            conversation.setFrom(message)
        }
        users.forEach { user ->
            conversation.addParticipant(accountKey, user)
        }
    }


    data class GetMessagesData(
            val insertConversations: Collection<ParcelableMessageConversation>,
            val updateConversations: Collection<ParcelableMessageConversation>,
            val insertMessages: Collection<ParcelableMessage>
    )
}


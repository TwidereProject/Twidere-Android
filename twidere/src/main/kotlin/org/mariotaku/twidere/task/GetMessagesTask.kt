package org.mariotaku.twidere.task

import android.accounts.AccountManager
import android.content.Context
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableMessage
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.RefreshTaskParam
import org.mariotaku.twidere.model.util.AccountUtils.getAccountDetails
import org.mariotaku.twidere.model.util.ParcelableMessageUtils
import org.mariotaku.twidere.util.DebugLog

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
            storeMessages(messages)
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
//                if (details.isOfficial(context)) {
//                    return getTwitterOfficialMessages(microBlog)
//                }
            }
        }
        // Use default method
        return getDefaultMessages(microBlog, details)
    }

    private fun getFanfouMessages(microBlog: MicroBlog): GetMessagesData {
        return GetMessagesData(emptyList(), emptyList(), emptyList())
    }

    private fun getTwitterOfficialMessages(microBlog: MicroBlog): GetMessagesData {
        return GetMessagesData(emptyList(), emptyList(), emptyList())
    }

    private fun getDefaultMessages(microBlog: MicroBlog, details: AccountDetails): GetMessagesData {
        val accountKey = details.key
        val paging = Paging()
        val insertMessages = arrayListOf<ParcelableMessage>()
        microBlog.getDirectMessages(paging).forEach { dm ->
            val message = ParcelableMessageUtils.incomingMessage(accountKey, dm)
            insertMessages.add(message)
        }
        microBlog.getSentDirectMessages(paging).forEach { dm ->
            val message = ParcelableMessageUtils.outgoingMessage(accountKey, dm)
            insertMessages.add(message)
        }
        return GetMessagesData(emptyList(), emptyList(), insertMessages)
    }

    private fun storeMessages(data: GetMessagesData) {
        DebugLog.d(LOGTAG, data.toString())
    }

    data class GetMessagesData(
            val insertConversations: List<ParcelableMessageConversation>,
            val updateConversations: List<ParcelableMessageConversation>,
            val insertMessages: List<ParcelableMessage>
    )
}

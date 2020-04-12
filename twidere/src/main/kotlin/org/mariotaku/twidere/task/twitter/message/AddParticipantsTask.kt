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
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.addParticipants
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.task.ExceptionHandlingAbstractTask
import org.mariotaku.twidere.util.DataStoreUtils

/**
 * Created by mariotaku on 2017/2/25.
 */

class AddParticipantsTask(
        context: Context,
        val accountKey: UserKey,
        val conversationId: String,
        val participants: Collection<ParcelableUser>
) : ExceptionHandlingAbstractTask<Unit?, Boolean, MicroBlogException, ((Boolean) -> Unit)?>(context) {

    private val profileImageSize: String = context.getString(R.string.profile_image_size)

    override val exceptionClass = MicroBlogException::class.java

    override fun onExecute(params: Unit?): Boolean {
        val account = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true) ?:
                throw MicroBlogException("No account")
        val conversation = DataStoreUtils.findMessageConversation(context, accountKey, conversationId)
        if (conversation != null && conversation.is_temp) {
            val addData = GetMessagesTask.DatabaseUpdateData(listOf(conversation), emptyList())
            conversation.addParticipants(participants)
            GetMessagesTask.storeMessages(context, addData, account, showNotification = false)
            // Don't finish too fast
            Thread.sleep(300L)
            return true
        }
        val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        val addData = requestAddParticipants(microBlog, account, conversation)
        GetMessagesTask.storeMessages(context, addData, account, showNotification = false)
        return true
    }

    override fun afterExecute(callback: ((Boolean) -> Unit)?, result: Boolean?, exception: MicroBlogException?) {
        callback?.invoke(result ?: false)
    }

    private fun requestAddParticipants(microBlog: MicroBlog, account: AccountDetails, conversation: ParcelableMessageConversation?):
            GetMessagesTask.DatabaseUpdateData {
        when (account.type) {
            AccountType.TWITTER -> {
                if (account.isOfficial(context)) {
                    val ids = participants.mapToArray { it.key.id }
                    val response = microBlog.addParticipants(conversationId, ids)
                    if (conversation != null) {
                        conversation.addParticipants(participants)
                        return GetMessagesTask.DatabaseUpdateData(listOf(conversation), emptyList())
                    }
                    return GetMessagesTask.createDatabaseUpdateData(context, account, response,
                            profileImageSize)
                }
            }

        }
        throw MicroBlogException("Adding participants is not supported")
    }

}

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
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.model.notificationDisabled
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.task.ExceptionHandlingAbstractTask

/**
 * Created by mariotaku on 2017/2/25.
 */

class SetConversationNotificationDisabledTask(
        context: Context,
        val accountKey: UserKey,
        val conversationId: String,
        val notificationDisabled: Boolean
) : ExceptionHandlingAbstractTask<Unit?, Boolean, MicroBlogException, ((Boolean) -> Unit)?>(context) {
    override fun onExecute(params: Unit?): Boolean {
        val account = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true) ?:
                throw MicroBlogException("No account")
        val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        val addData = requestSetNotificationDisabled(microBlog, account)
        GetMessagesTask.storeMessages(context, addData, account)
        return true
    }

    override fun afterExecute(callback: ((Boolean) -> Unit)?, result: Boolean?, exception: MicroBlogException?) {
        callback?.invoke(result ?: false)
    }

    private fun requestSetNotificationDisabled(microBlog: MicroBlog, account: AccountDetails):
            GetMessagesTask.DatabaseUpdateData {
        when (account.type) {
            AccountType.TWITTER -> {
                if (account.isOfficial(context)) {
                    val response = if (notificationDisabled) {
                        microBlog.disableDmConversations(conversationId)
                    } else {
                        microBlog.enableDmConversations(conversationId)
                    }
                    val conversation = MarkMessageReadTask.findConversation(context, accountKey,
                            conversationId) ?: return GetMessagesTask.DatabaseUpdateData(emptyList(), emptyList())
                    if (response.isSuccessful) {
                        conversation.notificationDisabled = notificationDisabled
                    }
                    return GetMessagesTask.DatabaseUpdateData(listOf(conversation), emptyList())
                }
            }
        }

        val conversation = MarkMessageReadTask.findConversation(context, accountKey,
                conversationId) ?: return GetMessagesTask.DatabaseUpdateData(emptyList(), emptyList())
        conversation.notificationDisabled = notificationDisabled
        // Don't finish too fast
        Thread.sleep(300L)
        return GetMessagesTask.DatabaseUpdateData(listOf(conversation), emptyList())
    }

}

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
import android.content.Context
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.exception.AccountNotFoundException
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.model.notificationDisabled
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.task.twitter.message.GetMessagesTask
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.SingletonHolder

class ConversationPromises private constructor(private val application: Application) {

    fun setNotificationDisabled(accountKey: UserKey, conversationId: String,
            notificationDisabled: Boolean): Promise<Boolean, Exception> = task {
        val account = AccountUtils.getAccountDetails(AccountManager.get(application),
                accountKey, true) ?: throw AccountNotFoundException()
        val addData = requestSetNotificationDisabled(account, conversationId, notificationDisabled)
        GetMessagesTask.storeMessages(application, addData, account)
        return@task true
    }

    private fun requestSetNotificationDisabled(account: AccountDetails,
            conversationId: String, notificationDisabled: Boolean):
            GetMessagesTask.DatabaseUpdateData {
        when (account.type) {
            AccountType.TWITTER -> {
                val microBlog = account.newMicroBlogInstance(application, cls = MicroBlog::class.java)
                if (account.isOfficial(application)) {
                    val response = if (notificationDisabled) {
                        microBlog.disableDmConversations(conversationId)
                    } else {
                        microBlog.enableDmConversations(conversationId)
                    }
                    val conversation = DataStoreUtils.findMessageConversation(application, account.key,
                            conversationId) ?: return GetMessagesTask.DatabaseUpdateData(emptyList(), emptyList())
                    if (response.isSuccessful) {
                        conversation.notificationDisabled = notificationDisabled
                    }
                    return GetMessagesTask.DatabaseUpdateData(listOf(conversation), emptyList())
                }
            }
        }

        val conversation = DataStoreUtils.findMessageConversation(application, account.key,
                conversationId) ?: return GetMessagesTask.DatabaseUpdateData(emptyList(), emptyList())
        conversation.notificationDisabled = notificationDisabled
        return GetMessagesTask.DatabaseUpdateData(listOf(conversation), emptyList())
    }

    companion object : SingletonHolder<ConversationPromises, Context>({
        ConversationPromises(it.applicationContext as Application)
    })

}
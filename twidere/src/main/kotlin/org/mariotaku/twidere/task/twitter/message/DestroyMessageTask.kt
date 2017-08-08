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
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Messages
import org.mariotaku.twidere.task.ExceptionHandlingAbstractTask

/**
 * Created by mariotaku on 2017/2/16.
 */

class DestroyMessageTask(
        context: Context,
        val accountKey: UserKey,
        val conversationId: String?,
        val messageId: String
) : ExceptionHandlingAbstractTask<Unit?, Boolean, MicroBlogException, Unit?>(context) {

    override val exceptionClass = MicroBlogException::class.java

    override fun onExecute(params: Unit?): Boolean {
        val account = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true) ?:
                throw MicroBlogException("No account")
        val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        if (!performDestroyMessage(context, microBlog, account, messageId)) {
            return false
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
        context.contentResolver.delete(Messages.CONTENT_URI, deleteWhere, deleteWhereArgs)
        return true
    }

    companion object {

        internal fun performDestroyMessage(context: Context, microBlog: MicroBlog,
                account: AccountDetails, messageId: String): Boolean {
            when (account.type) {
                AccountType.TWITTER -> {
                    if (account.isOfficial(context)) {
                        return microBlog.destroyDm(messageId).isSuccessful
                    }
                }
            }
            microBlog.destroyDirectMessage(messageId)
            return true
        }

    }
}

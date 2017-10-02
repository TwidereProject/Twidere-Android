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
import org.mariotaku.ktextension.forEachRow
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.Table
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.UnreadCountUpdatedEvent
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.task.ExceptionHandlingAbstractTask
import org.mariotaku.twidere.util.TwidereQueryBuilder
import org.mariotaku.twidere.util.getUnreadMessagesEntriesCursorReference

/**
 * Created by mariotaku on 2017/2/16.
 */

class BatchMarkMessageReadTask(
        context: Context,
        val accountKey: UserKey,
        val markTimestampBefore: Long
) : ExceptionHandlingAbstractTask<Unit?, Boolean, MicroBlogException, Unit?>(context) {

    override val exceptionClass = MicroBlogException::class.java

    override fun onExecute(params: Unit?): Boolean {
        val cr = context.contentResolver
        val projection = (Conversations.COLUMNS + Conversations.UNREAD_COUNT).map {
            TwidereQueryBuilder.mapConversationsProjection(it)
        }.toTypedArray()

        val unreadWhere = Expression.greaterThan(Columns.Column(Table(Conversations.TABLE_NAME),
                Conversations.LAST_READ_TIMESTAMP), markTimestampBefore)
        val unreadHaving = Expression.greaterThan(Conversations.UNREAD_COUNT, 0)

        val cRef = cr.getUnreadMessagesEntriesCursorReference(projection, arrayOf(accountKey),
                unreadWhere, null, unreadHaving, null) ?: return false
        val account = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true) ?:
                throw MicroBlogException("No account")
        val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        cRef.use { (cur) ->
            val indices = ObjectCursor.indicesFrom(cur, ParcelableMessageConversation::class.java)
            cur.forEachRow { c, _ ->
                val conversation = indices.newObject(c)
                try {
                    val lastReadEvent = MarkMessageReadTask.performMarkRead(context, microBlog,
                            account, conversation) ?: return@forEachRow false
                    MarkMessageReadTask.updateLocalLastRead(cr, account.key, conversation.id,
                            lastReadEvent)
                    return@forEachRow true
                } catch (e: MicroBlogException) {
                    return@forEachRow false
                }
            }
        }
        return true
    }

    override fun onSucceed(callback: Unit?, result: Boolean) {
        bus.post(UnreadCountUpdatedEvent(-1))
    }
}

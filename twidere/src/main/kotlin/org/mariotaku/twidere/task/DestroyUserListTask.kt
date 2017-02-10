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

package org.mariotaku.twidere.task

import android.content.Context
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.twidere.R
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.UserListDestroyedEvent
import org.mariotaku.twidere.model.util.ParcelableUserListUtils
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 2017/2/10.
 */
class DestroyUserListTask(
        context: Context,
        private val accountKey: UserKey,
        private val listId: String
) : BaseAbstractTask<Any?, SingleResponse<ParcelableUserList>, Any?>(context) {

    override fun doLongOperation(params: Any?): SingleResponse<ParcelableUserList> {
        val microBlog = MicroBlogAPIFactory.getInstance(context, accountKey) ?:
                return SingleResponse(MicroBlogException("No account"))
        try {
            val userList = microBlog.destroyUserList(listId)
            val list = ParcelableUserListUtils.from(userList, accountKey)
            return SingleResponse(list)
        } catch (e: MicroBlogException) {
            return SingleResponse(e)
        }

    }

    override fun afterExecute(callback: Any?, result: SingleResponse<ParcelableUserList>) {
        val context = context
        if (result.data != null) {
            val message = context.getString(R.string.deleted_list, result.data.name)
            Utils.showInfoMessage(context, message, false)
            bus.post(UserListDestroyedEvent(result.data))
        } else {
            Utils.showErrorMessage(context, R.string.action_deleting, result.exception, true)
        }
    }

}

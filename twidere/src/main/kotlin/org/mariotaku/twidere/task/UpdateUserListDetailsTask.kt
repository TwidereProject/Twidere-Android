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
import org.mariotaku.microblog.library.twitter.model.UserListUpdate
import org.mariotaku.twidere.R
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.UserListUpdatedEvent
import org.mariotaku.twidere.model.util.ParcelableUserListUtils
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 2017/2/10.
 */
class UpdateUserListDetailsTask(
        context: Context,
        private val accountKey: UserKey,
        private val listId: String,
        private val update: UserListUpdate
) : BaseAbstractTask<Any?, SingleResponse<ParcelableUserList>, Any>(context) {

    override fun doLongOperation(params: Any?): SingleResponse<ParcelableUserList> {
        val microBlog = MicroBlogAPIFactory.getInstance(context, accountKey)
        if (microBlog != null) {
            try {
                val list = microBlog.updateUserList(listId, update)
                return SingleResponse(ParcelableUserListUtils.from(list, accountKey))
            } catch (e: MicroBlogException) {
                return SingleResponse(e)
            }

        }
        return SingleResponse.getInstance<ParcelableUserList>()
    }

    override fun afterExecute(callback: Any?, result: SingleResponse<ParcelableUserList>) {
        if (result.data != null) {
            val message = context.getString(R.string.updated_list_details, result.data.name)
            Utils.showOkMessage(context, message, false)
            bus.post(UserListUpdatedEvent(result.data))
        } else {
            Utils.showErrorMessage(context, R.string.action_updating_details, result.exception, true)
        }
    }

}

/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.loader

import android.accounts.AccountManager
import android.content.Context
import android.util.Log
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ListResponse
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import java.util.*

abstract class MicroBlogAPIUsersLoader(
        context: Context,
        val accountKey: UserKey?,
        data: List<ParcelableUser>?,
        fromUser: Boolean
) : ParcelableUsersLoader(context, data, fromUser) {

    override fun loadInBackground(): List<ParcelableUser> {
        if (accountKey == null) {
            return ListResponse.getListInstance(MicroBlogException("No Account"))
        }
        val am = AccountManager.get(context)
        val details = AccountUtils.getAccountDetails(am, accountKey, true) ?:
                return ListResponse.getListInstance(MicroBlogException("No Account"))
        val twitter: MicroBlog = details.newMicroBlogInstance(context = context, cls = MicroBlog::class.java)
        val data = data
        val users: List<User>
        try {
            users = getUsers(twitter, details)
        } catch (e: MicroBlogException) {
            Log.w(TwidereConstants.LOGTAG, e)
            return ListResponse.getListInstance(data)
        }

        var pos = data.size
        for (user in users) {
            if (hasId(user.id)) {
                continue
            }
            val item = ParcelableUserUtils.fromUser(user, accountKey, pos.toLong())
            processUser(item)
            data.add(item)
            pos++
        }
        Collections.sort(data)
        return ListResponse.getListInstance(data)
    }

    protected open fun processUser(user: ParcelableUser) {

    }

    @Throws(MicroBlogException::class)
    protected abstract fun getUsers(twitter: MicroBlog, details: AccountDetails): List<User>
}

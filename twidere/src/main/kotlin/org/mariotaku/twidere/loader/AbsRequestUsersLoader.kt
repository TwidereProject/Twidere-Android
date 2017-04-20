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
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.twidere.R
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ListResponse
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.util.DebugLog
import java.util.*

abstract class AbsRequestUsersLoader(
        context: Context,
        val accountKey: UserKey?,
        data: List<ParcelableUser>?,
        fromUser: Boolean
) : ParcelableUsersLoader(context, data, fromUser) {

    protected val profileImageSize: String = context.getString(R.string.profile_image_size)

    override fun loadInBackground(): List<ParcelableUser> {
        if (accountKey == null) {
            return ListResponse.getListInstance(MicroBlogException("No Account"))
        }
        val am = AccountManager.get(context)
        val details = AccountUtils.getAccountDetails(am, accountKey, true) ?:
                return ListResponse.getListInstance(MicroBlogException("No Account"))
        val data = data
        val users: List<ParcelableUser>
        try {
            users = getUsers(details)
        } catch (e: MicroBlogException) {
            DebugLog.w(tr = e)
            return ListResponse.getListInstance(data)
        }

        var pos = data.size
        for (user in users) {
            if (hasId(user.key)) {
                continue
            }
            user.position = pos.toLong()
            processUser(details, user)
            pos++
        }
        data.addAll(users)
        processUsersData(details, data)
        return ListResponse.getListInstance(data)
    }

    protected open fun processUser(details: AccountDetails, user: ParcelableUser) {

    }

    @Throws(MicroBlogException::class)
    protected abstract fun getUsers(details: AccountDetails): List<ParcelableUser>

    protected open fun processUsersData(details: AccountDetails, list: MutableList<ParcelableUser>) {
        Collections.sort(data)
    }
}

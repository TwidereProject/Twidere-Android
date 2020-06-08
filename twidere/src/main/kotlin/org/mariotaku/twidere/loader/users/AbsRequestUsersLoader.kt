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

package org.mariotaku.twidere.loader.users

import android.accounts.AccountManager
import android.content.Context
import org.mariotaku.kpreferences.get
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.loadItemLimitKey
import org.mariotaku.twidere.exception.AccountNotFoundException
import org.mariotaku.twidere.extension.model.api.applyLoadLimit
import org.mariotaku.twidere.loader.iface.IPaginationLoader
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ListResponse
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.PaginatedList
import org.mariotaku.twidere.model.pagination.Pagination
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.util.DebugLog
import org.mariotaku.twidere.util.dagger.DependencyHolder
import java.util.*

abstract class AbsRequestUsersLoader(
        context: Context,
        val accountKey: UserKey?,
        data: List<ParcelableUser>?,
        fromUser: Boolean
) : ParcelableUsersLoader(context, data, fromUser), IPaginationLoader {

    protected val profileImageSize: String = context.getString(R.string.profile_image_size)
    override var pagination: Pagination? = null
    override var prevPagination: Pagination? = null
        protected set
    override var nextPagination: Pagination? = null
        protected set
    protected val loadItemLimit: Int

    init {
        val preferences = DependencyHolder.get(context).preferences
        loadItemLimit = preferences[loadItemLimitKey]
    }

    override fun loadInBackground(): List<ParcelableUser> {
        val data = data
        val details: AccountDetails
        val users: List<ParcelableUser>
        try {
            val am = AccountManager.get(context)
            details = accountKey?.let { AccountUtils.getAccountDetails(am, it, true) } ?:
                    throw AccountNotFoundException()
            users = getUsersInternal(details)
        } catch (e: MicroBlogException) {
            DebugLog.w(tr = e)
            return ListResponse.getListInstance(data, e)
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

    protected open fun processUsersData(details: AccountDetails, list: MutableList<ParcelableUser>) {
        data.sort()
    }

    protected open fun processPaging(paging: Paging, details: AccountDetails, loadItemLimit: Int) {
        paging.applyLoadLimit(details, loadItemLimit)
    }

    @Throws(MicroBlogException::class)
    protected abstract fun getUsers(details: AccountDetails, paging: Paging):
            PaginatedList<ParcelableUser>

    @Throws(MicroBlogException::class)
    private fun getUsersInternal(details: AccountDetails): List<ParcelableUser> {
        val paging = Paging()
        processPaging(paging, details, loadItemLimit)
        pagination?.applyTo(paging)
        val users = getUsers(details, paging)
        prevPagination = users.previousPage
        nextPagination = users.nextPage
        return users
    }
}

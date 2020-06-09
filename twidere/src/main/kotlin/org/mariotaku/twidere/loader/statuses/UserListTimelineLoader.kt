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

package org.mariotaku.twidere.loader.statuses

import android.content.Context
import androidx.annotation.WorkerThread
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.ResponseList
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.PaginatedList
import org.mariotaku.twidere.model.tab.extra.HomeTabExtras
import org.mariotaku.twidere.util.database.ContentFiltersUtils

class UserListTimelineLoader(
        context: Context,
        accountKey: UserKey?,
        private val listId: String?,
        private val userKey: UserKey?,
        private val screenName: String?,
        private val listName: String?,
        adapterData: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        loadingMore: Boolean,
        val extras: HomeTabExtras?
) : AbsRequestStatusesLoader(context, accountKey, adapterData, savedStatusesArgs, tabPosition, fromUser, loadingMore) {

    @Throws(MicroBlogException::class)
    override fun getStatuses(account: AccountDetails, paging: Paging): PaginatedList<ParcelableStatus> {
        return getMicroBlogStatuses(account, paging).mapMicroBlogToPaginated {
            it.toParcelable(account, profileImageSize)
        }
    }

    @WorkerThread
    override fun shouldFilterStatus(status: ParcelableStatus): Boolean {
        return ContentFiltersUtils.isFiltered(context.contentResolver, status, true,
                FilterScope.LIST_GROUP_TIMELINE) || extras?.let {
            it.isHideQuotes && status.is_quote || it.isHideRetweets && status.is_retweet
        } ?: false
    }

    private fun getMicroBlogStatuses(account: AccountDetails, paging: Paging): ResponseList<Status> {
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        return when {
            listId != null -> {
                microBlog.getUserListStatuses(listId, paging)
            }
            listName != null && userKey != null -> {
                microBlog.getUserListStatuses(listName.replace(' ', '-'), userKey.id, paging)
            }
            listName != null && screenName != null -> {
                microBlog.getUserListStatuses(listName.replace(' ', '-'), screenName, paging)
            }
            else -> {
                throw MicroBlogException("User id or screen name is required for list name")
            }
        }
    }

}

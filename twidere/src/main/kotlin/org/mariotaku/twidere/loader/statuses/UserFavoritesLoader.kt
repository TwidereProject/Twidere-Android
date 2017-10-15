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
import android.support.annotation.WorkerThread
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.ResponseList
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.extension.model.api.mastodon.mapToPaginated
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.pagination.PaginatedList
import org.mariotaku.twidere.model.refresh.UserRelatedContentRefreshParam
import org.mariotaku.twidere.util.database.ContentFiltersUtils

class UserFavoritesLoader(
        context: Context,
        private val refreshParam: UserRelatedContentRefreshParam?,
        data: List<ParcelableStatus>?,
        fromUser: Boolean,
        loadingMore: Boolean
) : AbsRequestStatusesLoader(context, refreshParam?.accountKeys?.singleOrNull(), data, fromUser, loadingMore) {

    @Throws(MicroBlogException::class)
    override fun getStatuses(account: AccountDetails, paging: Paging): PaginatedList<ParcelableStatus> {
        when (account.type) {
            AccountType.MASTODON -> {
                return getMastodonStatuses(account, paging)
            }
        }
        return getMicroBlogStatuses(account, paging).mapMicroBlogToPaginated {
            it.toParcelable(account, profileImageSize)
        }
    }

    @WorkerThread
    override fun shouldFilterStatus(status: ParcelableStatus): Boolean {
        return ContentFiltersUtils.isFiltered(context.contentResolver, status, false,
                FilterScope.FAVORITES)
    }

    private fun getMicroBlogStatuses(account: AccountDetails, paging: Paging): ResponseList<Status> {
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        return when {
            refreshParam?.userKey != null -> microBlog.getFavorites(refreshParam.userKey.id, paging)
            refreshParam?.userScreenName != null -> microBlog.getFavoritesByScreenName(refreshParam.userScreenName, paging)
            else -> throw MicroBlogException("Null user")
        }
    }

    private fun getMastodonStatuses(account: AccountDetails, paging: Paging): PaginatedList<ParcelableStatus> {
        if (refreshParam?.userKey != account.key) {
            throw MicroBlogException("Only current account favorites is supported")
        }
        val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
        return mastodon.getFavourites(paging).mapToPaginated { it.toParcelable(account) }
    }
}

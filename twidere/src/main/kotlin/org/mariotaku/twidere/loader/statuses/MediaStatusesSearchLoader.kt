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
import org.mariotaku.ktextension.isNullOrEmpty
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.SearchQuery
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.microblog.library.twitter.model.UniversalSearchQuery
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.model.official
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.PaginatedList
import org.mariotaku.twidere.util.database.ContentFiltersUtils

open class MediaStatusesSearchLoader(
        context: Context,
        accountKey: UserKey?,
        private val query: String?,
        adapterData: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        override val isGapEnabled: Boolean,
        loadingMore: Boolean
) : AbsRequestStatusesLoader(context, accountKey, adapterData, savedStatusesArgs, tabPosition,
        fromUser, loadingMore) {

    @Throws(MicroBlogException::class)
    override fun getStatuses(account: AccountDetails, paging: Paging): PaginatedList<ParcelableStatus> {
        return getMicroBlogStatuses(account, paging).mapMicroBlogToPaginated {
            it.toParcelable(account, profileImageSize)
        }
    }

    @WorkerThread
    override fun shouldFilterStatus(status: ParcelableStatus): Boolean {
        if (status.media.isNullOrEmpty()) return true
        val allowed = query?.split(' ')?.toTypedArray()
        return ContentFiltersUtils.isFiltered(context.contentResolver, status, true,
                FilterScope.SEARCH_RESULTS, allowed)
    }

    override fun processPaging(paging: Paging, details: AccountDetails, loadItemLimit: Int) {
        if (details.type == AccountType.STATUSNET) {
            paging.rpp(loadItemLimit)
            pagination?.applyTo(paging)
        } else {
            super.processPaging(paging, details, loadItemLimit)
        }
    }

    protected open fun processQuery(details: AccountDetails, query: String): String {
        if (details.type == AccountType.TWITTER) {
            if (details.extras?.official == true) {
                return TweetSearchLoader.smQuery("$query filter:media", pagination)
            }
            return "$query filter:media exclude:retweets"
        }
        return query
    }

    private fun getMicroBlogStatuses(account: AccountDetails, paging: Paging): List<Status> {
        if (query == null) throw MicroBlogException("Empty query")
        val queryText = processQuery(account, query)
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        when (account.type) {
            AccountType.TWITTER -> {
                if (account.extras?.official == true) {
                    val universalQuery = UniversalSearchQuery(queryText)
                    universalQuery.setModules(UniversalSearchQuery.Module.TWEET)
                    universalQuery.setResultType(UniversalSearchQuery.ResultType.RECENT)
                    universalQuery.setPaging(paging)
                    val searchResult = microBlog.universalSearch(universalQuery)
                    return searchResult.modules.mapNotNull { it.status?.data }
                }

                val searchQuery = SearchQuery(queryText)
                searchQuery.paging(paging)
                return microBlog.search(searchQuery)
            }
        }
        throw MicroBlogException("Not implemented")
    }

}

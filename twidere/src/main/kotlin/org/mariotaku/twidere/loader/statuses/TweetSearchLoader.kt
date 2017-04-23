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
import android.database.sqlite.SQLiteDatabase
import android.support.annotation.WorkerThread
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.mastodon.model.Results
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.SearchQuery
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.microblog.library.twitter.model.UniversalSearchQuery
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.api.mastodon.mapToPaginated
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.model.official
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.PaginatedList
import org.mariotaku.twidere.model.pagination.Pagination
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.util.InternalTwitterContentUtils

open class TweetSearchLoader(
        context: Context,
        accountKey: UserKey?,
        private val query: String?,
        adapterData: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        override val isGapEnabled: Boolean,
        loadingMore: Boolean
) : AbsRequestStatusesLoader(context, accountKey, adapterData, savedStatusesArgs, tabPosition, fromUser, loadingMore) {

    @Throws(MicroBlogException::class)
    override fun getStatuses(account: AccountDetails, paging: Paging): PaginatedList<ParcelableStatus> {
        when (account.type) {
            AccountType.MASTODON -> return getMastodonStatuses(account, paging).mapToPaginated(Results::getStatuses) {
                it.toParcelable(account)
            }
            else -> return getMicroBlogStatuses(account, paging).mapMicroBlogToPaginated {
                it.toParcelable(account, profileImageSize)
            }
        }
    }

    @WorkerThread
    override fun shouldFilterStatus(database: SQLiteDatabase, status: ParcelableStatus): Boolean {
        return InternalTwitterContentUtils.isFiltered(database, status, true)
    }

    protected open fun processQuery(details: AccountDetails, query: String): String {
        if (details.type == AccountType.TWITTER) {
            if (details.extras?.official ?: false) {
                return smQuery(query, pagination)
            }
            return "$query exclude:retweets"
        }
        return query
    }

    override fun processPaging(paging: Paging, details: AccountDetails, loadItemLimit: Int) {
        if (details.type == AccountType.STATUSNET) {
            paging.rpp(loadItemLimit)
            pagination?.applyTo(paging)
        } else {
            super.processPaging(paging, details, loadItemLimit)
        }
    }

    private fun getMastodonStatuses(account: AccountDetails, paging: Paging): Results {
        val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
        if (query == null) throw MicroBlogException("Empty query")
        return mastodon.search(query, true, paging)
    }

    private fun getMicroBlogStatuses(account: AccountDetails, paging: Paging): List<Status> {
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        if (query == null) throw MicroBlogException("Empty query")
        val queryText = processQuery(account, query)
        when (account.type) {
            AccountType.TWITTER -> {
                if (account.extras?.official ?: false) {
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
            AccountType.STATUSNET -> {
                return microBlog.searchStatuses(queryText, paging)
            }
            AccountType.FANFOU -> {
                return microBlog.searchPublicTimeline(queryText, paging)
            }
        }
        throw MicroBlogException("Not implemented")
    }

    companion object {

        fun smQuery(query: String, pagination: Pagination?): String {
            var universalQueryText = query

            if (pagination !is SinceMaxPagination) return universalQueryText
            pagination.maxId?.let { maxId ->
                universalQueryText += " max_id:$maxId"
            }
            pagination.sinceId?.let { sinceId ->
                universalQueryText += " since_id:$sinceId"
            }

            return universalQueryText
        }
    }

}

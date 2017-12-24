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

package org.mariotaku.twidere.data.fetcher

import org.mariotaku.microblog.library.*
import org.mariotaku.microblog.library.model.Paging
import org.mariotaku.microblog.library.model.mastodon.LinkHeaderList
import org.mariotaku.microblog.library.model.microblog.SearchQuery
import org.mariotaku.microblog.library.model.microblog.Status
import org.mariotaku.microblog.library.model.microblog.UniversalSearchQuery
import org.mariotaku.twidere.alias.MastodonStatus
import org.mariotaku.twidere.extension.model.official
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.timeline.TimelineFilter


class SearchTimelineFetcher(val query: String?, val local: Boolean) : StatusesFetcher {
    override fun forTwitter(account: AccountDetails, twitter: Twitter, paging: Paging, filter: TimelineFilter?): List<Status> {
        if (query == null) throw MicroBlogException("Query required")
        if (account.extras?.official != true) {
            val searchQuery = SearchQuery("$query exclude:retweets")
            searchQuery.paging(paging)
            return twitter.search(searchQuery)
        }
        val universalQuery = UniversalSearchQuery(smQuery(query, paging)).apply {
            setModules(UniversalSearchQuery.Module.TWEET)
            setResultType(UniversalSearchQuery.ResultType.RECENT)
            setPaging(paging)
        }
        val searchResult = twitter.universalSearch(universalQuery)
        return searchResult.modules.mapNotNull { it.status?.data }
    }

    override fun forStatusNet(account: AccountDetails, statusNet: StatusNet, paging: Paging, filter: TimelineFilter?): List<Status> {
        if (query == null) throw MicroBlogException("Query required")
        return statusNet.searchStatuses(query, paging)
    }

    override fun forFanfou(account: AccountDetails, fanfou: Fanfou, paging: Paging, filter: TimelineFilter?): List<Status> {
        if (query == null) throw MicroBlogException("Query required")
        return fanfou.searchPublicTimeline(query, paging)
    }

    override fun forMastodon(account: AccountDetails, mastodon: Mastodon, paging: Paging, filter: TimelineFilter?): LinkHeaderList<MastodonStatus> {
        if (query == null) throw MicroBlogException("Empty query")
        val tagQuery = if (query.startsWith("#")) query.substringAfter("#") else query
        return mastodon.getHashtagTimeline(tagQuery, paging, local)
    }

    companion object {

        fun smQuery(query: String, paging: Paging): String {
            var universalQueryText = query

            paging.get("max_id")?.let { maxId ->
                universalQueryText += " max_id:$maxId"
            }
            paging.get("since_id")?.let { sinceId ->
                universalQueryText += " since_id:$sinceId"
            }

            return universalQueryText
        }
    }
}

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

import android.support.v4.util.ArrayMap
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.model.microblog.Paging
import org.mariotaku.microblog.library.model.microblog.SearchQuery
import org.mariotaku.microblog.library.model.microblog.Status
import org.mariotaku.microblog.library.model.microblog.UniversalSearchQuery
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.exception.RequiredFieldNotFoundException
import org.mariotaku.twidere.extension.api.tryShowUser
import org.mariotaku.twidere.extension.model.official
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.timeline.TimelineFilter

class UserMentionsTimelineFetcher(val userKey: UserKey?, val userScreenName: String?) : StatusesFetcher {

    override fun forTwitter(account: AccountDetails, twitter: MicroBlog, paging: Paging, filter: TimelineFilter?): List<Status> {
        val screenName = getSearchScreenName(twitter, account.type)
        if (!account.official) {
            val searchQuery = SearchQuery("@$screenName exclude:retweets").paging(paging)
            return twitter.search(searchQuery)
        }
        val universalQuery = UniversalSearchQuery(SearchTimelineFetcher.smQuery("@$screenName", paging)).apply {
            setModules(UniversalSearchQuery.Module.TWEET)
            setResultType(UniversalSearchQuery.ResultType.RECENT)
            setPaging(paging)
        }
        val searchResult = twitter.universalSearch(universalQuery)
        return searchResult.modules.mapNotNull { it.status?.data }
    }

    override fun forStatusNet(account: AccountDetails, statusNet: MicroBlog, paging: Paging, filter: TimelineFilter?): List<Status> {
        val screenName = getSearchScreenName(statusNet, account.type)
        return statusNet.searchStatuses("@$screenName", paging)
    }

    override fun forFanfou(account: AccountDetails, fanfou: MicroBlog, paging: Paging, filter: TimelineFilter?): List<Status> {
        val screenName = getSearchScreenName(fanfou, account.type)
        return fanfou.searchPublicTimeline("@$screenName", paging)
    }

    private fun getSearchScreenName(microBlog: MicroBlog, @AccountType type: String?): String {
        return userScreenName ?: findScreenName(microBlog, userKey ?:
                throw RequiredFieldNotFoundException("user_id", "screen_name"), type)
    }

    companion object {

        private val userKeyCache = ArrayMap<UserKey, String>()

        fun findScreenName(microBlog: MicroBlog, key: UserKey, @AccountType type: String?): String {
            val cached = userKeyCache[key]
            if (cached != null) return cached
            val user = microBlog.tryShowUser(key.id, null, type)
            userKeyCache[key] = user.screenName
            return user.screenName
        }
    }
}
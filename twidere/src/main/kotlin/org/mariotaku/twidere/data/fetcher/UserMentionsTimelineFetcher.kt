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
import org.mariotaku.microblog.library.Fanfou
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.StatusNet
import org.mariotaku.microblog.library.Twitter
import org.mariotaku.microblog.library.model.Paging
import org.mariotaku.microblog.library.model.microblog.SearchQuery
import org.mariotaku.microblog.library.model.microblog.Status
import org.mariotaku.microblog.library.model.microblog.UniversalSearchQuery
import org.mariotaku.twidere.exception.RequiredFieldNotFoundException
import org.mariotaku.twidere.extension.api.tryShowUser
import org.mariotaku.twidere.extension.model.official
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.timeline.TimelineFilter

class UserMentionsTimelineFetcher(val userKey: UserKey?, val userScreenName: String?) : StatusesFetcher {

    override fun forTwitter(account: AccountDetails, twitter: Twitter, paging: Paging, filter: TimelineFilter?): List<Status> {
        val screenName = twitter.getSearchScreenName { tryShowUser(it.id, null).screenName }
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

    override fun forStatusNet(account: AccountDetails, statusNet: StatusNet, paging: Paging, filter: TimelineFilter?): List<Status> {
        val screenName = statusNet.getSearchScreenName { showUser(it.id).screenName }
        return statusNet.searchStatuses("@$screenName", paging)
    }

    override fun forFanfou(account: AccountDetails, fanfou: Fanfou, paging: Paging, filter: TimelineFilter?): List<Status> {
        val screenName = fanfou.getSearchScreenName { showFanfouUser(it.id).screenName }
        return fanfou.searchPublicTimeline("@$screenName", paging)
    }

    private fun <MB : MicroBlog> MB.getSearchScreenName(findAction: MB.(UserKey) -> String): String {
        if (userScreenName != null) return userScreenName
        val key = userKey ?: throw RequiredFieldNotFoundException("user_id", "screen_name")
        return findScreenName(this, key) { mb ->
            findAction(mb, key)
        }
    }

    companion object {

        private val userKeyCache = ArrayMap<UserKey, String>()

        fun <MB : MicroBlog> findScreenName(mb: MB, key: UserKey, findAction: (MB) -> String): String {
            val cached = userKeyCache[key]
            if (cached != null) return cached
            val screenName = findAction(mb)
            userKeyCache[key] = screenName
            return screenName
        }
    }
}
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

import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.model.microblog.Paging
import org.mariotaku.microblog.library.model.microblog.SearchQuery
import org.mariotaku.microblog.library.model.microblog.Status
import org.mariotaku.microblog.library.model.microblog.UniversalSearchQuery
import org.mariotaku.microblog.library.model.microblog.UniversalSearchQuery.Module
import org.mariotaku.microblog.library.model.microblog.UniversalSearchQuery.ResultType
import org.mariotaku.twidere.exception.RequiredFieldNotFoundException
import org.mariotaku.twidere.extension.model.official
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.timeline.TimelineFilter

class MediaSearchTimelineFetcher(val query: String?) : StatusesFetcher {
    override fun forTwitter(account: AccountDetails, twitter: MicroBlog, paging: Paging, filter: TimelineFilter?): List<Status> {
        if (query == null) throw RequiredFieldNotFoundException("query")
        if (account.official) {
            val searchQuery = SearchTimelineFetcher.smQuery("$query filter:media", paging)
            val universalQuery = UniversalSearchQuery(searchQuery)
            universalQuery.setModules(Module.TWEET)
            universalQuery.setResultType(ResultType.RECENT)
            universalQuery.setPaging(paging)
            val searchResult = twitter.universalSearch(universalQuery)
            return searchResult.modules.mapNotNull { it.status?.data }
        }

        val searchQuery = SearchQuery("$query filter:media exclude:retweets").paging(paging)
        return twitter.search(searchQuery)
    }
}
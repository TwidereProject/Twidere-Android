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

import org.mariotaku.microblog.library.Fanfou
import org.mariotaku.microblog.library.Mastodon
import org.mariotaku.microblog.library.StatusNet
import org.mariotaku.microblog.library.Twitter
import org.mariotaku.microblog.library.model.Paging
import org.mariotaku.microblog.library.model.mastodon.LinkHeaderList
import org.mariotaku.microblog.library.model.microblog.ResponseList
import org.mariotaku.microblog.library.model.microblog.SearchQuery
import org.mariotaku.microblog.library.model.microblog.Status
import org.mariotaku.twidere.alias.MastodonStatus
import org.mariotaku.twidere.alias.MastodonTimelineOption
import org.mariotaku.twidere.exception.APINotSupportedException
import org.mariotaku.twidere.exception.RequiredFieldNotFoundException
import org.mariotaku.twidere.extension.api.tryShowUser
import org.mariotaku.twidere.extension.model.official
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.timeline.TimelineFilter

class UserMediaTimelineFetcher(
        private val userKey: UserKey?,
        private val userScreenName: String?
) : StatusesFetcher {
    override fun forTwitter(account: AccountDetails, twitter: Twitter, paging: Paging, filter: TimelineFilter?): List<Status> {
        if (account.official) {
            return when {
                userKey != null -> twitter.getMediaTimeline(userKey.id, paging)
                userScreenName != null -> twitter.getMediaTimelineByScreenName(userScreenName, paging)
                else -> throw RequiredFieldNotFoundException("user_id", "screen_name")
            }
        }
        val screenName = when {
            userScreenName != null -> userScreenName
            userKey != null -> UserMentionsTimelineFetcher.findScreenName(twitter, userKey) {
                it.tryShowUser(userKey.id, null).screenName
            }
            else -> throw RequiredFieldNotFoundException("user_id", "screen_name")
        }
        val query = SearchQuery("from:$screenName filter:media exclude:retweets").paging(paging)
        val result = ResponseList<Status>()
        twitter.search(query).filterTo(result) { status ->
            val user = status.user
            return@filterTo user.id == userKey?.id || user.screenName.equals(screenName, ignoreCase = true)
        }
        return result
    }

    override fun forFanfou(account: AccountDetails, fanfou: Fanfou, paging: Paging, filter: TimelineFilter?): List<Status> {
        return when {
            userKey != null -> fanfou.getPhotosUserTimeline(userKey.id, paging)
            userScreenName != null -> fanfou.getPhotosUserTimeline(userScreenName, paging)
            else -> throw RequiredFieldNotFoundException("user_id", "screen_name")
        }
    }

    override fun forStatusNet(account: AccountDetails, statusNet: StatusNet, paging: Paging, filter: TimelineFilter?): List<Status> {
        throw APINotSupportedException("Media timeline", account.type)
    }

    override fun forMastodon(account: AccountDetails, mastodon: Mastodon, paging: Paging, filter: TimelineFilter?): LinkHeaderList<MastodonStatus> {
        val option = MastodonTimelineOption()
        option.onlyMedia(true)
        return when {
            userKey != null -> mastodon.getStatuses(userKey.id, paging, option)
            userScreenName != null -> throw APINotSupportedException("Get statuses with screenName", account.type)
            else -> throw RequiredFieldNotFoundException("user_id", "screen_name")
        }
    }

}

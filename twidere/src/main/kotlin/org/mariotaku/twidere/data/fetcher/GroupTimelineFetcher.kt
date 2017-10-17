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
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.alias.MastodonStatus
import org.mariotaku.twidere.exception.APINotSupportedException
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.timeline.TimelineFilter


class GroupTimelineFetcher(val groupId: String?, val groupName: String?) : StatusesFetcher {
    override fun forTwitter(account: AccountDetails, twitter: MicroBlog, paging: Paging, filter: TimelineFilter?): List<Status> {
        throw APINotSupportedException("Group timeline", account.type)
    }

    override fun forStatusNet(account: AccountDetails, statusNet: MicroBlog, paging: Paging, filter: TimelineFilter?): List<Status> {
        return when {
            groupId != null -> {
                statusNet.getGroupStatuses(groupId, paging)
            }
            groupName != null -> {
                statusNet.getGroupStatusesByName(groupName, paging)
            }
            else -> {
                throw MicroBlogException("No group name or id given")
            }
        }
    }

    override fun forFanfou(account: AccountDetails, fanfou: MicroBlog, paging: Paging, filter: TimelineFilter?): List<Status> {
        throw APINotSupportedException("Group timeline", account.type)
    }

    override fun forMastodon(account: AccountDetails, mastodon: Mastodon, paging: Paging, filter: TimelineFilter?): List<MastodonStatus> {
        throw APINotSupportedException("Group timeline", account.type)
    }

}

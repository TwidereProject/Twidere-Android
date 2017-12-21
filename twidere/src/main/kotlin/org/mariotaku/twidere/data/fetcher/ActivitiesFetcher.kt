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

import org.mariotaku.microblog.library.Mastodon
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.model.mastodon.LinkHeaderList
import org.mariotaku.microblog.library.model.microblog.Activity
import org.mariotaku.microblog.library.model.microblog.Paging
import org.mariotaku.microblog.library.model.microblog.Status
import org.mariotaku.twidere.alias.MastodonNotification
import org.mariotaku.twidere.exception.APINotSupportedException
import org.mariotaku.twidere.model.AccountDetails

interface ActivitiesFetcher {

    fun forTwitterOfficial(account: AccountDetails, twitter: MicroBlog, paging: Paging): List<Activity>
            = throw APINotSupportedException(account.type)

    fun forTwitter(account: AccountDetails, twitter: MicroBlog, paging: Paging): List<Status>
            = throw APINotSupportedException(account.type)

    fun forStatusNet(account: AccountDetails, statusNet: MicroBlog, paging: Paging): List<Status>
            = throw APINotSupportedException(account.type)

    fun forFanfou(account: AccountDetails, fanfou: MicroBlog, paging: Paging): List<Status>
            = throw APINotSupportedException(account.type)

    fun forMastodon(account: AccountDetails, mastodon: Mastodon, paging: Paging): LinkHeaderList<MastodonNotification>
            = throw APINotSupportedException(account.type)
}
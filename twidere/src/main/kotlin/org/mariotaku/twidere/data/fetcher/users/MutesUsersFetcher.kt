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

package org.mariotaku.twidere.data.fetcher.users

import org.mariotaku.microblog.library.Mastodon
import org.mariotaku.microblog.library.Twitter
import org.mariotaku.microblog.library.model.Paging
import org.mariotaku.microblog.library.model.mastodon.Account
import org.mariotaku.microblog.library.model.mastodon.LinkHeaderList
import org.mariotaku.microblog.library.model.microblog.User
import org.mariotaku.twidere.data.fetcher.UsersFetcher
import org.mariotaku.twidere.model.AccountDetails

class MutesUsersFetcher : UsersFetcher {

    override fun forMastodon(account: AccountDetails, mastodon: Mastodon, paging: Paging): LinkHeaderList<Account> {
        return mastodon.getMutes(paging)
    }

    override fun forTwitter(account: AccountDetails, twitter: Twitter, paging: Paging): List<User> {
        return twitter.getMutesUsersList(paging)
    }
}

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

package org.mariotaku.twidere.fragment.users

import android.os.Bundle
import org.mariotaku.twidere.R
import org.mariotaku.twidere.data.fetcher.UsersFetcher
import org.mariotaku.twidere.extension.linkHandlerTitle
import org.mariotaku.twidere.fragment.AbsUsersFragment
import org.mariotaku.twidere.data.fetcher.users.MutesUsersFetcher
import org.mariotaku.twidere.model.event.FriendshipTaskEvent

class MutesUsersListFragment : AbsUsersFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = getString(R.string.action_twitter_muted_users)
    }

    override fun onCreateUsersFetcher(): UsersFetcher {
        return MutesUsersFetcher()
    }

    override fun shouldRemoveUser(position: Int, event: FriendshipTaskEvent): Boolean {
        if (!event.isSucceeded) return false
        when (event.action) {
            FriendshipTaskEvent.Action.UNMUTE -> {
                return true
            }
        }
        return false
    }
}

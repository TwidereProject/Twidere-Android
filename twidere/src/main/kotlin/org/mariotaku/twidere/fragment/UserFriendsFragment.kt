/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.fragment

import android.content.Context
import android.os.Bundle
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.loader.CursorSupportUsersLoader
import org.mariotaku.twidere.loader.UserFriendsLoader
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.FriendshipTaskEvent

class UserFriendsFragment : CursorSupportUsersListFragment() {

    public override fun onCreateUsersLoader(context: Context,
                                            args: Bundle, fromUser: Boolean): CursorSupportUsersLoader {
        val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        val userKey = args.getParcelable<UserKey>(EXTRA_USER_KEY)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        val loader = UserFriendsLoader(context, accountKey, userKey,
                screenName, adapter!!.getData(), fromUser)
        loader.cursor = nextCursor
        loader.page = nextPage
        return loader
    }

    override fun shouldRemoveUser(position: Int, event: FriendshipTaskEvent): Boolean {
        if (!event.isSucceeded) return false
        when (event.action) {
            FriendshipTaskEvent.Action.UNFOLLOW, FriendshipTaskEvent.Action.BLOCK -> {
                return true
            }
        }
        return false
    }
}

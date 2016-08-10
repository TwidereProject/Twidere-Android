/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.adapter.iface

import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.view.holder.UserViewHolder

/**
 * Created by mariotaku on 15/4/16.
 */
interface IUsersAdapter<Data> : IContentCardAdapter {

    val userCount: Int

    val userClickListener: UserClickListener?

    val requestClickListener: RequestClickListener?

    val followClickListener: FriendshipClickListener?

    val showAccountsColor: Boolean

    fun setData(data: Data?): Boolean

    fun getUser(position: Int): ParcelableUser?

    fun getUserId(position: Int): String?

    interface UserClickListener {

        fun onUserClick(holder: UserViewHolder, position: Int)

        fun onUserLongClick(holder: UserViewHolder, position: Int): Boolean

    }

    interface RequestClickListener {

        fun onAcceptClicked(holder: UserViewHolder, position: Int)

        fun onDenyClicked(holder: UserViewHolder, position: Int)
    }

    interface FriendshipClickListener {
        fun onFollowClicked(holder: UserViewHolder, position: Int)
        fun onUnblockClicked(holder: UserViewHolder, position: Int)
        fun onUnmuteClicked(holder: UserViewHolder, position: Int)
    }

    abstract class SimpleUserClickListener : UserClickListener, RequestClickListener, FriendshipClickListener {

        override fun onFollowClicked(holder: UserViewHolder, position: Int) {

        }

        override fun onUnblockClicked(holder: UserViewHolder, position: Int) {

        }

        override fun onUnmuteClicked(holder: UserViewHolder, position: Int) {

        }

        override fun onAcceptClicked(holder: UserViewHolder, position: Int) {

        }

        override fun onDenyClicked(holder: UserViewHolder, position: Int) {

        }

        override fun onUserClick(holder: UserViewHolder, position: Int) {

        }

        override fun onUserLongClick(holder: UserViewHolder, position: Int): Boolean {
            return false
        }
    }
}

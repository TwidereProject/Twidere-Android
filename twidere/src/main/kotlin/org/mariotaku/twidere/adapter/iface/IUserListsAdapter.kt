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

import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.view.holder.UserListViewHolder

/**
 * Created by mariotaku on 15/4/16.
 */
interface IUserListsAdapter<Data> : IContentCardAdapter {

    val userListsCount: Int

    val nameFirst: Boolean

    val showAccountsColor: Boolean

    val userListClickListener: UserListClickListener?

    fun getUserList(position: Int): ParcelableUserList?

    fun getUserListId(position: Int): String?

    fun setData(data: Data?): Boolean

    interface UserListClickListener {

        fun onUserListClick(holder: UserListViewHolder, position: Int)

        fun onUserListLongClick(holder: UserListViewHolder, position: Int): Boolean

    }
}

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

import org.mariotaku.twidere.data.fetcher.UsersFetcher
import org.mariotaku.twidere.extension.groupId
import org.mariotaku.twidere.extension.groupName
import org.mariotaku.twidere.fragment.AbsUsersFragment
import org.mariotaku.twidere.data.fetcher.users.GroupMembersFetcher

class GroupMembersFragment : AbsUsersFragment() {
    override fun onCreateUsersFetcher(): UsersFetcher {
        val args = arguments!!
        val groupId = args.groupId
        val groupName = args.groupName
        return GroupMembersFetcher(groupId, groupName)
    }
}

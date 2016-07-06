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
import org.mariotaku.twidere.loader.GroupMembersLoader
import org.mariotaku.twidere.model.UserKey

class GroupMembersFragment : CursorSupportUsersListFragment() {

    public override fun onCreateUsersLoader(context: Context,
                                            args: Bundle, fromUser: Boolean): CursorSupportUsersLoader {
        val accountId = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        val groupId = args.getString(EXTRA_GROUP_ID)
        val groupName = args.getString(EXTRA_GROUP_NAME)
        val loader = GroupMembersLoader(context, accountId, groupId,
                groupName, adapter!!.getData(), fromUser)
        loader.cursor = nextCursor
        loader.page = nextPage
        return loader
    }
}

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

package org.mariotaku.twidere.activity.shortcut

import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.shortcut.ShortcutCreator

/**
 * Created by mariotaku on 2017/9/2.
 */

class UserListTimelineShortcutCreatorActivity : AbsUserListRelatedShortcutCreatorActivity() {
    override fun onUserListSelected(accountKey: UserKey?, userList: ParcelableUserList) {
        addShortcut { ShortcutCreator.userListTimeline(this, accountKey, userList) }
    }

}

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

package org.mariotaku.twidere.model.util

import android.text.TextUtils
import org.mariotaku.microblog.library.twitter.model.UserList
import org.mariotaku.twidere.extension.model.api.getProfileImageOfSize
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.UserKey

/**
 * Created by mariotaku on 16/3/5.
 */
object ParcelableUserListUtils {

    fun from(list: UserList, accountKey: UserKey, position: Long = 0,
            isFollowing: Boolean = false, profileImageSize: String = "normal"): ParcelableUserList {
        val obj = ParcelableUserList()
        val user = list.user
        obj.position = position
        obj.account_key = accountKey
        obj.id = list.id
        obj.is_public = UserList.Mode.PUBLIC == list.mode
        obj.is_following = isFollowing
        obj.name = list.name
        obj.description = list.description
        obj.user_key = UserKeyUtils.fromUser(user)
        obj.user_name = user.name
        obj.user_screen_name = user.screenName
        obj.user_profile_image_url = user.getProfileImageOfSize(profileImageSize)
        obj.members_count = list.memberCount
        obj.subscribers_count = list.subscriberCount
        return obj
    }

    fun fromUserLists(userLists: Array<UserList>?, accountKey: UserKey,
            profileImageSize: String = "normal"): Array<ParcelableUserList>? {
        if (userLists == null) return emptyArray()
        val size = userLists.size
        return Array(size) { from(userLists[it], accountKey, profileImageSize = profileImageSize) }
    }

    fun check(userList: ParcelableUserList, accountKey: UserKey, listId: String?,
            userKey: UserKey?, screenName: String?, listName: String?): Boolean {
        if (userList.account_key != accountKey) return false
        if (listId != null) {
            return TextUtils.equals(listId, userList.id)
        } else if (listName != null) {
            if (!TextUtils.equals(listName, userList.name)) return false
            if (userKey != null) {
                return userKey == userList.user_key
            } else if (screenName != null) {
                return TextUtils.equals(screenName, userList.user_screen_name)
            }
        }
        return false
    }
}

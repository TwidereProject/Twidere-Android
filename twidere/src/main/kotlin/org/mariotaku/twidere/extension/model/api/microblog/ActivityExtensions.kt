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

package org.mariotaku.twidere.extension.model.api.microblog

import org.mariotaku.ktextension.mapToArray
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.twidere.extension.model.toParcelables
import org.mariotaku.twidere.model.ParcelableActivity
import org.mariotaku.twidere.model.UserKey

/**
 * Created by mariotaku on 2017/4/22.
 */

fun Activity.toParcelable(accountKey: UserKey, accountType: String, isGap: Boolean = false,
        profileImageSize: String = "normal"): ParcelableActivity {
    val result = ParcelableActivity()
    result.account_key = accountKey
    result.timestamp = createdAt.time
    result.action = action
    result.max_sort_position = maxSortPosition
    result.min_sort_position = minSortPosition
    result.max_position = maxPosition
    result.min_position = minPosition
    result.sources = sources?.toParcelables(accountKey, accountType,
            profileImageSize)
    result.target_users = targetUsers?.toParcelables(accountKey,
            accountType, profileImageSize)
    result.target_user_lists = targetUserLists?.toParcelables(accountKey,
            profileImageSize)
    result.target_statuses = targetStatuses?.toParcelables(accountKey,
            accountType, profileImageSize)
    result.target_object_statuses = targetObjectStatuses?.toParcelables(accountKey,
            accountType, profileImageSize)
    result.target_object_user_lists = targetObjectUserLists?.toParcelables(accountKey,
            profileImageSize)
    result.target_object_users = targetObjectUsers?.toParcelables(accountKey, accountType,
            profileImageSize)
    result.has_following_source = sources?.fold(false) { folded, item ->
        if (item.isFollowing == true) {
            return@fold true
        }
        return@fold folded
    } ?: false
    result.source_keys = result.sources?.mapToArray { it.key }
    result.is_gap = isGap
    return result
}
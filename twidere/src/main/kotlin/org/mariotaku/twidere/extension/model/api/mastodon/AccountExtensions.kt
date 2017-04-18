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

package org.mariotaku.twidere.extension.model.api.mastodon

import org.mariotaku.microblog.library.mastodon.model.Account
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey

/**
 * Created by mariotaku on 2017/4/18.
 */

fun Account.toParcelable(accountKey: UserKey, position: Long = 0): ParcelableUser {
    val obj = ParcelableUser()
    obj.position = position
    obj.account_key = accountKey
    obj.key = UserKey(id, host ?: accountKey.host)
    obj.created_at = createdAt?.time ?: -1
    obj.is_protected = isLocked
    obj.name = displayName
    obj.screen_name = username
    obj.description_plain = note
    obj.description_unescaped = note
    obj.url = url
    obj.profile_image_url = avatar
    obj.profile_banner_url = header
    obj.followers_count = followersCount
    obj.friends_count = followingCount
    obj.statuses_count = statusesCount
    obj.favorites_count = -1
    obj.listed_count = -1
    obj.media_count = -1
    obj.user_type = AccountType.MASTODON
    return obj
}

val Account.host: String? get() = acct?.let(UserKey::valueOf)?.host
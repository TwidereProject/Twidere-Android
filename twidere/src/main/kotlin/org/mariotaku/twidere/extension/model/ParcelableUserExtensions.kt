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

package org.mariotaku.twidere.extension.model

import org.mariotaku.twidere.TwidereConstants.USER_TYPE_FANFOU_COM
import org.mariotaku.twidere.TwidereConstants.USER_TYPE_TWITTER_COM
import org.mariotaku.twidere.extension.model.api.getUserHost
import org.mariotaku.twidere.model.ParcelableLiteUser
import org.mariotaku.twidere.model.ParcelableRelationship
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.util.InternalTwitterContentUtils
import org.mariotaku.twidere.util.Utils

fun ParcelableUser.getBestProfileBanner(width: Int, height: Int = 0): String? {
    return profile_banner_url?.let {
        InternalTwitterContentUtils.getBestBannerUrl(it, width, height)
    } ?: if (USER_TYPE_FANFOU_COM == key.host) {
        profile_background_url
    } else {
        null
    }
}

fun ParcelableUser.toLite(): ParcelableLiteUser {
    val result = ParcelableLiteUser()
    result.account_key = account_key
    result.key = key
    result.screen_name = screen_name
    result.name = name
    result.profile_image_url = profile_image_url
    result.is_following = is_following
    return result
}

fun ParcelableUser.applyTo(relationship: ParcelableRelationship) {
    relationship.following = is_following
    extras?.let { extras ->
        relationship.followed_by = extras.followed_by
        relationship.blocking = extras.blocking
        relationship.blocked_by = extras.blocked_by
        relationship.muting = extras.muting
        relationship.notifications_enabled = extras.notifications_enabled
    }
}

val ParcelableUser.relationship: ParcelableRelationship get() = ParcelableRelationship().also {
    it.account_key = this.account_key
    it.user_key = this.key
    this.applyTo(it)
}

val ParcelableUser.host: String
    get() {
        if (this.isFanfouUser) return USER_TYPE_FANFOU_COM
        if (extras == null) return USER_TYPE_TWITTER_COM

        return getUserHost(extras?.statusnet_profile_url, USER_TYPE_TWITTER_COM)
    }

val ParcelableUser.isFanfouUser: Boolean
    get() = USER_TYPE_FANFOU_COM == key.host

inline val ParcelableUser.originalProfileImage: String? get() {
    return extras?.profile_image_url_original?.takeIf(String::isNotEmpty)
            ?: Utils.getOriginalTwitterProfileImage(profile_image_url)
}

inline val ParcelableUser.urlPreferred: String? get() = url_expanded?.takeIf(String::isNotEmpty) ?: url


inline val ParcelableUser.acct: String get() = if (account_key.host == key.host) {
    screen_name
} else {
    "$screen_name@${key.host}"
}

inline val ParcelableUser.groups_count: Long get() = extras?.groups_count ?: -1
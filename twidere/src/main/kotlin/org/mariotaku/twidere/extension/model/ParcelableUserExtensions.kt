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

import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.TwidereConstants.USER_TYPE_FANFOU_COM
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.util.InternalTwitterContentUtils
import org.mariotaku.twidere.util.Utils

fun ParcelableUser.getBestProfileBanner(width: Int): String? {
    return profile_banner_url?.let {
        InternalTwitterContentUtils.getBestBannerUrl(it, width)
    } ?: if (USER_TYPE_FANFOU_COM == key.host) {
        profile_background_url
    } else {
        null
    }
}

inline val ParcelableUser.originalProfileImage: String? get() {
    return extras?.profile_image_url_original?.takeIf(String::isNotEmpty)
            ?: Utils.getOriginalTwitterProfileImage(profile_image_url)
}

inline val ParcelableUser.urlPreferred: String? get() = url_expanded?.takeIf(String::isNotEmpty) ?: url


fun Array<User>.toParcelables(accountKey: UserKey, accountType: String, profileImageSize: String = "normal"): Array<ParcelableUser>? {
    return map {
        ParcelableUserUtils.fromUser(it, accountKey, accountType, profileImageSize = profileImageSize)
    }.toTypedArray()
}
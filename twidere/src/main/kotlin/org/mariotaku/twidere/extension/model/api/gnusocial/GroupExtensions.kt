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

package org.mariotaku.twidere.extension.model.api.gnusocial

import org.mariotaku.microblog.library.statusnet.model.Group
import org.mariotaku.twidere.model.ParcelableGroup
import org.mariotaku.twidere.model.UserKey

fun Group.toParcelable(accountKey: UserKey, position: Int = -1, member: Boolean = false): ParcelableGroup {
    val obj = ParcelableGroup()
    obj.account_key = accountKey
    obj.member = member
    obj.position = position.toLong()
    obj.id = id
    obj.nickname = nickname
    obj.homepage = homepage
    obj.fullname = fullname
    obj.url = url
    obj.description = description
    obj.location = location
    obj.created = created?.time ?: -1
    obj.modified = modified?.time ?: -1
    obj.admin_count = adminCount
    obj.member_count = memberCount
    obj.original_logo = originalLogo
    obj.homepage_logo = homepageLogo
    obj.stream_logo = streamLogo
    obj.mini_logo = miniLogo
    obj.blocked = isBlocked
    obj.id = id
    return obj
}

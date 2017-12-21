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

import org.mariotaku.microblog.library.model.mastodon.Account
import org.mariotaku.microblog.library.model.mastodon.Relationship
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.api.isHtml
import org.mariotaku.twidere.extension.model.api.spanItems
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.HtmlEscapeHelper
import org.mariotaku.twidere.util.HtmlSpanBuilder
import org.mariotaku.twidere.util.emoji.EmojioneTranslator

/**
 * Created by mariotaku on 2017/4/18.
 */

fun Account.toParcelable(details: AccountDetails, position: Long = 0,
        relationship: Relationship? = null): ParcelableUser {
    return toParcelable(details.key, position, relationship).apply {
        account_color = details.color
    }
}

fun Account.toParcelable(accountKey: UserKey, position: Long = 0,
        relationship: Relationship? = null): ParcelableUser {
    val obj = ParcelableUser()
    obj.position = position
    obj.account_key = accountKey
    obj.key = getKey(accountKey.host)
    obj.created_at = createdAt?.time ?: -1
    obj.is_protected = isLocked
    obj.name = name
    obj.screen_name = username
    if (note?.isHtml == true) {
        val descriptionHtml = HtmlSpanBuilder.fromHtml(note, note, MastodonSpanProcessor())
        obj.description_unescaped = descriptionHtml?.toString()
        obj.description_plain = obj.description_unescaped
        obj.description_spans = descriptionHtml?.spanItems
    } else {
        obj.description_unescaped = note?.let(HtmlEscapeHelper::unescape)
        obj.description_plain = obj.description_unescaped
    }
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

    val extras = ParcelableUser.Extras()

    if (relationship != null && relationship.id == id) {
        obj.is_following = relationship.isFollowing
        obj.is_follow_request_sent = relationship.isRequested
        extras.followed_by = relationship.isFollowedBy
        extras.muting = relationship.isMuting
        extras.blocking = relationship.isBlocking
    }

    return obj
}

inline val Account.host: String? get() = acct?.let(UserKey::valueOf)?.host

inline val Account.name: String? get() = displayName?.takeIf(String::isNotEmpty)
        ?.let(EmojioneTranslator::translate) ?: username

fun Account.getKey(host: String?) = UserKey(id, acct?.let(UserKey::valueOf)?.host ?: host)

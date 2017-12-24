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

import org.mariotaku.ktextension.mapToArray
import org.mariotaku.microblog.library.model.mastodon.Notification
import org.mariotaku.microblog.library.model.mastodon.Relationship
import org.mariotaku.microblog.library.model.twitter.Activity.Action
import org.mariotaku.twidere.extension.model.toLite
import org.mariotaku.twidere.extension.model.toSummaryLine
import org.mariotaku.twidere.extension.model.updateActivityFilterInfo
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableActivity
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey

fun Notification.toParcelable(details: AccountDetails, relationships: Map<String, Relationship>?):
        ParcelableActivity {
    return toParcelable(details.key, relationships).apply {
        account_color = details.color
    }
}

fun Notification.toParcelable(accountKey: UserKey, relationships: Map<String, Relationship>?):
        ParcelableActivity {
    val result = ParcelableActivity()
    result.account_key = accountKey
    result.id = "$id-$id"
    result.timestamp = createdAt.time
    result.min_position = id
    result.max_position = id
    result.min_sort_position = result.timestamp
    result.max_sort_position = result.timestamp

    result.sources = toSources(accountKey, relationships)
    result.user_key = result.sources?.singleOrNull()?.key ?: UserKey("multiple", null)

    val status = this.status
    when (type) {
        Notification.Type.MENTION -> {
            if (status == null) {
                result.action = Action.INVALID
                return result
            }
            result.action = Action.MENTION
            status.applyTo(accountKey, result)
        }
        Notification.Type.REBLOG -> {
            if (status == null) {
                result.action = Action.INVALID
                return result
            }
            result.action = Action.RETWEET
            val parcelableStatus = status.toParcelable(accountKey)
            result.target_objects = ParcelableActivity.RelatedObject.statuses(parcelableStatus)
            result.summary_line = arrayOf(parcelableStatus.toSummaryLine())
        }
        Notification.Type.FAVOURITE -> {
            if (status == null) {
                result.action = Action.INVALID
                return result
            }
            result.action = Action.FAVORITE
            val parcelableStatus = status.toParcelable(accountKey)
            result.targets = ParcelableActivity.RelatedObject.statuses(parcelableStatus)
            result.summary_line = arrayOf(parcelableStatus.toSummaryLine())
        }
        Notification.Type.FOLLOW -> {
            result.action = Action.FOLLOW
        }
        else -> {
            result.action = type
        }
    }

    result.sources_lite = result.sources?.mapToArray { it.toLite() }
    result.source_keys = result.sources_lite?.mapToArray { it.key }

    result.updateActivityFilterInfo()

    return result
}

private fun Notification.toSources(accountKey: UserKey, relationships: Map<String, Relationship>?):
        Array<ParcelableUser>? {
    val account = this.account ?: return null
    val relationship = relationships?.get(account.id)
    return arrayOf(account.toParcelable(accountKey, relationship = relationship))
}


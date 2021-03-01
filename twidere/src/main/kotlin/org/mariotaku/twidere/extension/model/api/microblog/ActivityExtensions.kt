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
import org.mariotaku.microblog.library.twitter.model.Activity.Action
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.extension.model.api.applyTo
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.toLite
import org.mariotaku.twidere.extension.model.toSummaryLine
import org.mariotaku.twidere.extension.model.updateActivityFilterInfo
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableActivity
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.UserKey

inline val Activity.activityStatus: Status?
    get() = when (action) {
        Action.MENTION -> {
            targetObjectStatuses?.firstOrNull()
        }
        Action.REPLY -> {
            targetStatuses?.firstOrNull()
        }
        Action.QUOTE -> {
            targetStatuses?.firstOrNull()
        }
        else -> null
    }

fun Activity.toParcelable(details: AccountDetails, isGap: Boolean = false,
        profileImageSize: String = "normal"): ParcelableActivity {
    return toParcelable(details.key, details.type, isGap, profileImageSize).apply {
        account_color = details.color
    }
}

fun Activity.toParcelable(accountKey: UserKey, accountType: String, isGap: Boolean = false,
        profileImageSize: String = "normal"): ParcelableActivity {
    val result = ParcelableActivity()
    result.account_key = accountKey
    result.id = "$minPosition-$maxPosition"
    result.timestamp = createdAt.time
    result.max_sort_position = maxSortPosition
    result.min_sort_position = minSortPosition
    result.max_position = maxPosition
    result.min_position = minPosition

    result.action = action

    result.sources = sources?.mapToArray {
        it.toParcelable(accountKey, accountType, profileImageSize = profileImageSize)
    }

    result.targets = ParcelableActivity.RelatedObject().also { relatedObject ->
        relatedObject.statuses = targetStatuses?.mapToArray {
            it.toParcelable(accountKey, accountType, profileImageSize)
        }
        relatedObject.users = targetUsers?.mapToArray {
            it.toParcelable(accountKey, accountType, profileImageSize = profileImageSize)
        }
        relatedObject.user_lists = targetUserLists?.mapToArray {
            it.toParcelable(accountKey, profileImageSize = profileImageSize)
        }
    }

    result.target_objects = ParcelableActivity.RelatedObject().also { relatedObject ->
        relatedObject.statuses = targetObjectStatuses?.mapToArray {
            it.toParcelable(accountKey, accountType, profileImageSize)
        }
        relatedObject.users = targetObjectUsers?.mapToArray {
            it.toParcelable(accountKey, accountType, profileImageSize = profileImageSize)
        }
        relatedObject.user_lists = targetObjectUserLists?.mapToArray {
            it.toParcelable(accountKey, profileImageSize = profileImageSize)
        }
    }

    val status = activityStatus
    if (status == null) {
        when (action) {
            Action.FOLLOW -> {
                // No summary line
            }
            Action.FAVORITE -> {
                // Targets (Statuses) as summary line
                result.summary_line = result.targets?.statuses?.mapToArray {
                    it.toSummaryLine()
                }
            }
            Action.RETWEET -> {
                // Target objects (Statuses) as summary line
                result.summary_line = result.target_objects?.statuses?.mapToArray {
                    it.toSummaryLine()
                }
            }
            Action.FAVORITED_RETWEET, Action.RETWEETED_RETWEET -> {
                // Targets (Statuses) as summary line
                result.summary_line = result.targets?.statuses?.mapToArray {
                    it.toSummaryLine()
                }
            }
            Action.RETWEETED_MENTION, Action.FAVORITED_MENTION -> {
                // Targets (Statuses) as summary line
                result.summary_line = result.targets?.statuses?.mapToArray {
                    it.toSummaryLine()
                }
            }
            Action.LIST_MEMBER_ADDED -> {
                // Target objects (lists) as summary line
            }
            Action.JOINED_TWITTER -> {
                // No summary line
            }
            Action.MEDIA_TAGGED, Action.FAVORITED_MEDIA_TAGGED, Action.RETWEETED_MEDIA_TAGGED -> {
                // Targets (Statuses) as summary line
                result.summary_line = result.targets?.statuses?.mapToArray {
                    it.toSummaryLine()
                }
            }
        }
        val singleSource = result.sources?.singleOrNull()
        result.user_key = singleSource?.key ?: UserKey("multiple", null)
        result.user_name = singleSource?.name
        result.user_screen_name = singleSource?.screen_name
    } else {
        status.applyTo(accountKey, accountType, profileImageSize, result)
        result.summary_line = arrayOf(result.toSummaryLine())
    }

    result.sources_lite = result.sources?.mapToArray { it.toLite() }
    result.source_keys = result.sources_lite?.mapToArray { it.key }

    result.has_following_source = sources?.fold(false) { folded, item ->
        return@fold folded || (item.isFollowing == true)
    } ?: false
    result.is_gap = isGap

    result.updateActivityFilterInfo()

    return result
}

private fun ParcelableUserList.toSummaryLine(): ParcelableActivity.SummaryLine {
    val result = ParcelableActivity.SummaryLine()
    result.name = name
    result.content = description
    return result
}
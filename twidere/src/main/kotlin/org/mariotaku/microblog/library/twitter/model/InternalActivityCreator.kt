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

package org.mariotaku.microblog.library.twitter.model

import java.util.*

/**
 * Created by mariotaku on 2017/3/10.
 */

object InternalActivityCreator {
    fun status(status: Status, accountId: String): Activity {
        val activity = Activity()

        activity.minPosition = status.getId()
        activity.maxPosition = activity.minPosition
        activity.minSortPosition = status.sortId
        activity.maxSortPosition = activity.minSortPosition
        activity.createdAt = status.getCreatedAt()

        when {
            status.getInReplyToUserId() == accountId -> {
                activity.action = Activity.Action.REPLY
                activity.targetStatuses = arrayOf(status)

                //TODO set target statuses (in reply to status)
                activity.targetObjectStatuses = arrayOfNulls<Status>(0)
            }
            status.quotedStatus?.user?.id == accountId -> {
                activity.action = Activity.Action.QUOTE
                activity.targetStatuses = arrayOf(status)
                activity.targetObjectStatuses = arrayOfNulls<Status>(0)
            }
            else -> {
                activity.action = Activity.Action.MENTION
                activity.targetUsers = arrayOfNulls<User>(0)
                activity.targetObjectStatuses = arrayOf(status)
            }
        }
        activity.sourcesSize = 1
        activity.sources = arrayOf(status.getUser())
        return activity
    }

    fun retweet(status: Status): Activity {
        val activity = Activity()

        activity.initBasic(status.createdAt)

        activity.action = Activity.Action.RETWEET

        activity.sources = arrayOf(status.user)
        activity.targetStatuses = arrayOf(status)
        activity.targetObjectStatuses = arrayOf(status.retweetedStatus)
        return activity
    }

    fun follow(createdAt: Date, source: User, target: User): Activity {
        val activity = Activity()

        activity.initBasic(createdAt)

        activity.action = Activity.Action.FOLLOW

        activity.sources = arrayOf(source)
        activity.targetUsers = arrayOf(target)
        return activity
    }

    fun targetStatus(action: String, createdAt: Date, source: User, target: Status): Activity {
        val activity = Activity()

        activity.initBasic(createdAt)

        activity.action = action

        activity.sources = arrayOf(source)
        activity.targetStatuses = arrayOf(target)
        return activity
    }

    fun targetObject(action: String, createdAt: Date, source: User, target: User, targetObject: UserList): Activity {
        val activity = Activity()

        activity.initBasic(createdAt)

        activity.action = action

        activity.sources = arrayOf(source)
        activity.targetUsers = arrayOf(target)
        activity.targetObjectUserLists = arrayOf(targetObject)
        return activity
    }

    private fun Activity.initBasic(createdAt: Date) {
        val timestamp = createdAt.time
        minPosition = timestamp.toString()
        maxPosition = timestamp.toString()
        minSortPosition = timestamp
        maxSortPosition = timestamp
        this.createdAt = createdAt
    }
}

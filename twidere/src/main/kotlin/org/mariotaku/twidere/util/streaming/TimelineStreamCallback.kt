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

package org.mariotaku.twidere.util.streaming

import org.mariotaku.microblog.library.twitter.UserStreamCallback
import org.mariotaku.microblog.library.twitter.model.*
import java.util.*

/**
 * Created by mariotaku on 2017/3/10.
 */

abstract class TimelineStreamCallback(val accountId: String) : UserStreamCallback() {

    private val friends = mutableSetOf<String>()

    override final fun onFriendList(friendIds: Array<String>): Boolean {
        friends.addAll(friendIds)
        return true
    }

    override final fun onStatus(status: Status): Boolean {
        val userId = status.user.id
        if (accountId == userId || userId in friends) {
            onHomeTimeline(status)
        }
        if (status.inReplyToUserId == accountId) {
            // Reply
            onActivityAboutMe(InternalActivityCreator.status(accountId, status))
        } else if (userId != accountId && status.retweetedStatus?.user?.id == accountId) {
            // Retweet
            onActivityAboutMe(InternalActivityCreator.retweet(status))
        } else if (status.userMentionEntities?.find { it.id == accountId } != null) {
            // Mention
            onActivityAboutMe(InternalActivityCreator.status(accountId, status))
        }
        return true
    }

    override fun onFollow(createdAt: Date, source: User, target: User): Boolean {
        if (source.id == accountId) {
            friends.add(target.id)
        } else if (target.id == accountId) {
            // Dispatch follow activity
            onActivityAboutMe(InternalActivityCreator.follow(createdAt, source, target))
        }
        return true
    }

    override fun onFavorite(createdAt: Date, source: User, target: User,
            targetObject: Status): Boolean {
        if (source.id == accountId) {
            // Update my favorite status
        } else if (target.id == accountId) {
            // Dispatch favorite activity
            onActivityAboutMe(InternalActivityCreator.targetStatus(Activity.Action.FAVORITE,
                    createdAt, source, targetObject))
        }
        return true
    }

    override fun onUnfollow(createdAt: Date, source: User, followedUser: User): Boolean {
        if (source.id == accountId) {
            friends.remove(followedUser.id)
        }
        return true
    }

    override fun onQuotedTweet(createdAt: Date, source: User, target: User, targetObject: Status): Boolean {
        if (source.id == accountId) {
        } else if (target.id == accountId) {
            // Dispatch activity
            onActivityAboutMe(InternalActivityCreator.targetStatus(Activity.Action.QUOTE,
                    createdAt, source, targetObject))
        }
        return true
    }

    override fun onFavoritedRetweet(createdAt: Date, source: User, target: User, targetObject: Status): Boolean {
        if (source.id == accountId) {
        } else if (target.id == accountId) {
            // Dispatch activity
            onActivityAboutMe(InternalActivityCreator.targetStatus(Activity.Action.FAVORITED_RETWEET,
                    createdAt, source, targetObject))
        }
        return true
    }

    override fun onRetweetedRetweet(createdAt: Date, source: User, target: User, targetObject: Status): Boolean {
        if (source.id == accountId) {
        } else if (target.id == accountId) {
            // Dispatch activity
            onActivityAboutMe(InternalActivityCreator.targetStatus(Activity.Action.RETWEETED_RETWEET,
                    createdAt, source, targetObject))
        }
        return true
    }

    override fun onUserListMemberAddition(createdAt: Date, source: User, target: User, targetObject: UserList): Boolean {
        if (source.id == accountId) {
        } else if (target.id == accountId) {
            // Dispatch activity
            onActivityAboutMe(InternalActivityCreator.targetObject(Activity.Action.LIST_MEMBER_ADDED,
                    createdAt, source, target, targetObject))
        }
        return true
    }

    protected abstract fun onHomeTimeline(status: Status)

    protected abstract fun onActivityAboutMe(activity: Activity)
}

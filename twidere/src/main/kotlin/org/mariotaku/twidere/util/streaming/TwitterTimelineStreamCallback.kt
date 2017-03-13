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

import android.support.annotation.WorkerThread
import org.mariotaku.microblog.library.twitter.callback.SimpleUserStreamCallback
import org.mariotaku.microblog.library.twitter.model.*
import java.util.*

/**
 * Created by mariotaku on 2017/3/10.
 */
@WorkerThread
abstract class TwitterTimelineStreamCallback(val accountId: String) : SimpleUserStreamCallback() {

    private val friends = mutableSetOf<String>()

    override final fun onFriendList(friendIds: Array<String>): Boolean {
        friends.addAll(friendIds)
        return true
    }

    override final fun onStatus(status: Status): Boolean {
        val userId = status.user.id
        var handled = false
        if (accountId == userId || userId in friends) {
            handled = handled or onHomeTimeline(status)
        }
        if (status.inReplyToUserId == accountId) {
            // Reply
            handled = handled or onActivityAboutMe(InternalActivityCreator.status(accountId, status))
        } else if (userId != accountId && status.retweetedStatus?.user?.id == accountId) {
            // Retweet
            handled = handled or onActivityAboutMe(InternalActivityCreator.retweet(status))
        } else if (status.userMentionEntities?.find { it.id == accountId } != null) {
            // Mention
            handled = handled or onActivityAboutMe(InternalActivityCreator.status(accountId, status))
        }
        return handled
    }

    override final fun onFollow(createdAt: Date, source: User, target: User): Boolean {
        if (source.id == accountId) {
            friends.add(target.id)
            return true
        } else if (target.id == accountId) {
            // Dispatch follow activity
            return onActivityAboutMe(InternalActivityCreator.follow(createdAt, source, target))
        }
        return false
    }

    override final fun onFavorite(createdAt: Date, source: User, target: User,
            targetObject: Status): Boolean {
        if (source.id == accountId) {
            // TODO Update my favorite status
        } else if (target.id == accountId) {
            // Dispatch favorite activity
            return onActivityAboutMe(InternalActivityCreator.targetStatus(Activity.Action.FAVORITE,
                    createdAt, source, targetObject))
        }
        return true
    }

    override final fun onUnfollow(createdAt: Date, source: User, followedUser: User): Boolean {
        if (source.id == accountId) {
            friends.remove(followedUser.id)
            return true
        }
        return false
    }

    override final fun onQuotedTweet(createdAt: Date, source: User, target: User, targetObject: Status): Boolean {
        if (source.id == accountId) {
            return false
        } else if (target.id == accountId) {
            // Dispatch activity
            return onActivityAboutMe(InternalActivityCreator.targetStatus(Activity.Action.QUOTE,
                    createdAt, source, targetObject))
        }
        return true
    }

    override final fun onFavoritedRetweet(createdAt: Date, source: User, target: User, targetObject: Status): Boolean {
        if (source.id == accountId) {
            return false
        } else if (target.id == accountId) {
            // Dispatch activity
            return onActivityAboutMe(InternalActivityCreator.targetStatus(Activity.Action.FAVORITED_RETWEET,
                    createdAt, source, targetObject))
        }
        return true
    }

    override final fun onRetweetedRetweet(createdAt: Date, source: User, target: User, targetObject: Status): Boolean {
        if (source.id == accountId) {
            return false
        } else if (target.id == accountId) {
            // Dispatch activity
            return onActivityAboutMe(InternalActivityCreator.targetStatus(Activity.Action.RETWEETED_RETWEET,
                    createdAt, source, targetObject))
        }
        return false
    }

    override final fun onUserListMemberAddition(createdAt: Date, source: User, target: User, targetObject: UserList): Boolean {
        if (source.id == accountId) {
            return false
        } else if (target.id == accountId) {
            // Dispatch activity
            return onActivityAboutMe(InternalActivityCreator.targetObject(Activity.Action.LIST_MEMBER_ADDED,
                    createdAt, source, target, targetObject))
        }
        return false
    }

    @WorkerThread
    protected abstract fun onHomeTimeline(status: Status): Boolean

    @WorkerThread
    protected abstract fun onActivityAboutMe(activity: Activity): Boolean

    @WorkerThread
    override abstract fun onDirectMessage(directMessage: DirectMessage): Boolean
}

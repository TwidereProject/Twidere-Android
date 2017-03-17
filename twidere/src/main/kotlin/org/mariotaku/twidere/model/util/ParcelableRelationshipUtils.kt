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

package org.mariotaku.twidere.model.util

import org.mariotaku.microblog.library.twitter.model.Relationship
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.model.ParcelableRelationship
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey

object ParcelableRelationshipUtils {

    fun create(accountKey: UserKey, userKey: UserKey, relationship: Relationship?,
            filtering: Boolean): ParcelableRelationship {
        val obj = ParcelableRelationship()
        obj.account_key = accountKey
        obj.user_key = userKey
        if (relationship != null) {
            obj.following = relationship.isSourceFollowingTarget
            obj.followed_by = relationship.isSourceFollowedByTarget
            obj.blocking = relationship.isSourceBlockingTarget
            obj.blocked_by = relationship.isSourceBlockedByTarget
            obj.muting = relationship.isSourceMutingTarget
            obj.retweet_enabled = relationship.isSourceWantRetweetsFromTarget
            obj.notifications_enabled = relationship.isSourceNotificationsEnabledForTarget
            obj.can_dm = relationship.canSourceDMTarget()
        }
        obj.filtering = filtering
        return obj
    }

    fun create(user: ParcelableUser, filtering: Boolean): ParcelableRelationship {
        val obj = ParcelableRelationship()
        obj.account_key = user.account_key
        obj.user_key = user.key
        obj.filtering = filtering
        if (user.extras != null) {
            obj.following = user.is_following
            obj.followed_by = user.extras.followed_by
            obj.blocking = user.extras.blocking
            obj.blocked_by = user.extras.blocked_by
            obj.can_dm = user.extras.followed_by
            obj.notifications_enabled = user.extras.notifications_enabled
        }
        return obj
    }

    fun create(accountKey: UserKey, userKey: UserKey, user: User,
            filtering: Boolean): ParcelableRelationship {
        val obj = ParcelableRelationship()
        obj.account_key = accountKey
        obj.user_key = userKey
        obj.filtering = filtering
        obj.following = user.isFollowing == true
        obj.followed_by = user.isFollowedBy == true
        obj.blocking = user.isBlocking == true
        obj.blocked_by = user.isBlockedBy == true
        obj.can_dm = user.isFollowedBy == true
        obj.notifications_enabled = user.isNotificationsEnabled == true
        return obj
    }
}

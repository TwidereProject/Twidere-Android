/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.microblog.library.twitter.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 15/5/7.
 */
@JsonObject
public class Relationship extends TwitterResponseObject implements TwitterResponse {

    @JsonField(name = "relationship")
    RelationshipObject object;

    public boolean isSourceBlockingTarget() {
        return object.source.blocking;
    }

    public boolean isTargetFollowingSource() {
        return object.target.following;
    }

    public boolean isTargetFollowedBySource() {
        return object.target.followedBy;
    }

    public boolean isSourceNotificationsEnabled() {
        return object.source.notificationsEnabled;
    }

    public boolean isSourceMutingTarget() {
        return object.source.muting;
    }

    public boolean isSourceMarkedTargetAsSpam() {
        return false;
    }

    public boolean isSourceFollowingTarget() {
        return object.source.following;
    }

    public boolean isSourceFollowedByTarget() {
        return object.source.followedBy;
    }

    public boolean isSourceBlockedByTarget() {
        return object.source.blockedBy;
    }

    public String getTargetUserScreenName() {
        return object.target.screenName;
    }

    public String getTargetUserId() {
        return object.target.id;
    }

    public String getSourceUserScreenName() {
        return object.source.screenName;
    }

    public String getSourceUserId() {
        return object.source.id;
    }

    public boolean canSourceMediaTagTarget() {
        return object.source.canMediaTag;
    }

    public boolean canSourceDMTarget() {
        return object.source.canDm;
    }

    public boolean isSourceRequestedFollowingTarget() {
        return object.source.followingRequested;
    }

    public boolean isTargetRequestedFollowingSource() {
        return object.target.followingRequested;
    }

    public boolean isSourceWantRetweetsFromTarget() {
        return object.source.wantRetweets;
    }

    public boolean isSourceNotificationsEnabledForTarget() {
        return object.source.notificationsEnabled;
    }

    @JsonObject
    public static class RelationshipObject {

        @JsonField(name = "source")
        Source source;
        @JsonField(name = "target")
        Target target;


        @JsonObject
        public static class Target {
            @JsonField(name = "id")
            String id;
            @JsonField(name = "screen_name")
            String screenName;
            @JsonField(name = "following")
            boolean following;
            @JsonField(name = "followed_by")
            boolean followedBy;
            @JsonField(name = "following_requested")
            boolean followingRequested;
        }

        @JsonObject
        public static class Source {
            @JsonField(name = "id")
            String id;
            @JsonField(name = "screen_name")
            String screenName;
            @JsonField(name = "blocked_by")
            boolean blockedBy;
            @JsonField(name = "blocking")
            boolean blocking;
            @JsonField(name = "muting")
            boolean muting;
            @JsonField(name = "following")
            boolean following;
            @JsonField(name = "followed_by")
            boolean followedBy;
            @JsonField(name = "following_requested")
            boolean followingRequested;
            @JsonField(name = "want_retweets")
            boolean wantRetweets;
            @JsonField(name = "notifications_enabled")
            boolean notificationsEnabled;
            @JsonField(name = "can_dm")
            boolean canDm;
            @JsonField(name = "can_media_tag")
            boolean canMediaTag;
        }
    }
}

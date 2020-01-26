/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.microblog.library.twitter.model;

import androidx.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 16/2/26.
 */
@JsonObject
public class TwitterStreamObject {

    @JsonField(name = "sender")
    EmptyObject sender;
    @JsonField(name = "event")
    String event;
    @JsonField(name = "text")
    String text;
    @JsonField(name = "direct_message")
    DirectMessage directMessage;
    @JsonField(name = "delete")
    Delete delete;
    @JsonField(name = "disconnect")
    Disconnect disconnect;
    @JsonField(name = "limit")
    Limit limit;
    @JsonField(name = "warning")
    Warning warning;
    @JsonField(name = "scrub_geo")
    ScrubGeo scrubGeo;
    @JsonField(name = {"friends", "friends_str"})
    String[] friends;

    @Type
    public String determine() {
        // This code originally lived in AbstractStreamImplementation.
        // I've moved it in here to expose it as a public encapsulation of
        // the object type determination logic.
        if (sender == null && text != null) {
            return Type.STATUS;
        } else if (directMessage != null) {
            return Type.DIRECT_MESSAGE;
        } else if (delete != null) {
            return Type.DELETE;
        } else if (limit != null) {
            return Type.LIMIT;
        } else if (warning != null) {
            return Type.STALL_WARNING;
        } else if (scrubGeo != null) {
            return Type.SCRUB_GEO;
        } else if (friends != null) {
            return Type.FRIENDS;
        } else if (disconnect != null) {
            return Type.DISCONNECTION;
        } else if (event != null) {
            switch (event) {
                case "favorite":
                    return Type.FAVORITE;
                case "unfavorite":
                    return Type.UNFAVORITE;
                case "follow":
                    return Type.FOLLOW;
                case "unfollow":
                    return Type.UNFOLLOW;
                case "list_member_added":
                    return Type.USER_LIST_MEMBER_ADDED;
                case "list_member_removed":
                    return Type.USER_LIST_MEMBER_DELETED;
                case "list_user_subscribed":
                    return Type.USER_LIST_SUBSCRIBED;
                case "list_user_unsubscribed":
                    return Type.USER_LIST_UNSUBSCRIBED;
                case "list_created":
                    return Type.USER_LIST_CREATED;
                case "list_updated":
                    return Type.USER_LIST_UPDATED;
                case "list_destroyed":
                    return Type.USER_LIST_DESTROYED;
                case "user_update":
                    return Type.USER_UPDATE;
                case "block":
                    return Type.BLOCK;
                case "unblock":
                    return Type.UNBLOCK;
                case "quoted_tweet":
                    return Type.QUOTED_TWEET;
                case "favorited_retweet":
                    return Type.FAVORITED_RETWEET;
                case "retweeted_retweet":
                    return Type.RETWEETED_RETWEET;
            }
        }
        return Type.UNKNOWN;
    }

    public DirectMessage getDirectMessage() {
        return directMessage;
    }

    public Delete getDelete() {
        return delete;
    }

    public ScrubGeo getScrubGeo() {
        return scrubGeo;
    }

    public Limit getLimit() {
        return limit;
    }

    public Disconnect getDisconnect() {
        return disconnect;
    }

    public String[] getFriends() {
        return friends;
    }

    public Warning getWarning() {
        return warning;
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({Type.STATUS, Type.DIRECT_MESSAGE, Type.DELETE, Type.LIMIT,
            Type.STALL_WARNING, Type.SCRUB_GEO, Type.FRIENDS, Type.FAVORITE, Type.UNFAVORITE,
            Type.FOLLOW, Type.UNFOLLOW, Type.USER_LIST_MEMBER_ADDED, Type.USER_LIST_MEMBER_DELETED,
            Type.USER_LIST_SUBSCRIBED, Type.USER_LIST_UNSUBSCRIBED, Type.USER_LIST_CREATED,
            Type.USER_LIST_UPDATED, Type.USER_LIST_DESTROYED, Type.USER_UPDATE, Type.BLOCK,
            Type.UNBLOCK, Type.DISCONNECTION, Type.QUOTED_TWEET, Type.FAVORITED_RETWEET,
            Type.RETWEETED_RETWEET, Type.UNKNOWN})
    public @interface Type {
        String STATUS = "status";
        String DIRECT_MESSAGE = "direct_message";
        String DELETE = "delete";
        String LIMIT = "limit";
        String STALL_WARNING = "stall_warning";
        String SCRUB_GEO = "scrub_geo";
        String FRIENDS = "friends";
        String FAVORITE = "favorite";
        String UNFAVORITE = "unfavorite";
        String FOLLOW = "follow";
        String UNFOLLOW = "unfollow";
        String USER_LIST_MEMBER_ADDED = "user_list_member_added";
        String USER_LIST_MEMBER_DELETED = "user_list_member_deleted";
        String USER_LIST_SUBSCRIBED = "user_list_subscribed";
        String USER_LIST_UNSUBSCRIBED = "user_list_unsubscribed";
        String USER_LIST_CREATED = "user_list_created";
        String USER_LIST_UPDATED = "user_list_updated";
        String USER_LIST_DESTROYED = "user_list_destroyed";
        String USER_UPDATE = "user_update";
        String BLOCK = "block";
        String UNBLOCK = "unblock";
        String DISCONNECTION = "disconnection";
        String QUOTED_TWEET = "quoted_tweet";
        String FAVORITED_RETWEET = "favorited_retweet";
        String RETWEETED_RETWEET = "retweeted_retweet";
        String UNKNOWN = "unknown";
    }

    @JsonObject
    public static class EmptyObject {

    }

    @JsonObject
    public static class Delete {
        @JsonField(name = "status")
        DeletionEvent status;
        @JsonField(name = "direct_message")
        DeletionEvent directMessage;

        public DeletionEvent getStatus() {
            return status;
        }

        public DeletionEvent getDirectMessage() {
            return directMessage;
        }
    }

    @JsonObject
    public static class ScrubGeo {
        @JsonField(name = "user_id")
        String userId;

        @JsonField(name = "up_to_status_id")
        String upToStatusId;

        public String getUserId() {
            return userId;
        }

        public String getUpToStatusId() {
            return upToStatusId;
        }
    }

    @JsonObject
    public static class Limit {
        @JsonField(name = "track")
        int track;

        public int getTrack() {
            return track;
        }
    }

    @JsonObject
    public static class Disconnect {
        @JsonField(name = "code")
        int code;
        @JsonField(name = "stream_name")
        String streamName;
        @JsonField(name = "reason")
        String reason;

        public int getCode() {
            return code;
        }

        public String getStreamName() {
            return streamName;
        }

        public String getReason() {
            return reason;
        }
    }
}

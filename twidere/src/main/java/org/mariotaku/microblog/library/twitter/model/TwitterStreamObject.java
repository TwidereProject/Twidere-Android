package org.mariotaku.microblog.library.twitter.model;

import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

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
    EmptyObject disconnect;
    @JsonField(name = "limit")
    EmptyObject limit;
    @JsonField(name = "warning")
    EmptyObject warning;
    @JsonField(name = "scrub_geo")
    EmptyObject scrubGeo;
    @JsonField(name = "friends")
    EmptyObject friends;

    @Type
    public String determine() {
        // This code originally lived in AbstractStreamImplementation.
        // I've moved it in here to expose it as a public encapsulation of
        // the object type determination logic.
        if (sender != null) {
            return Type.SENDER;
        } else if (text != null) {
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
                case "user_delete":
                    return Type.USER_DELETE;
                case "user_suspend":
                    return Type.USER_SUSPEND;
                case "block":
                    return Type.BLOCK;
                case "unblock":
                    return Type.UNBLOCK;
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

    @StringDef({Type.SENDER, Type.STATUS, Type.DIRECT_MESSAGE, Type.DELETE, Type.LIMIT,
            Type.STALL_WARNING, Type.SCRUB_GEO, Type.FRIENDS, Type.FAVORITE, Type.UNFAVORITE,
            Type.FOLLOW, Type.UNFOLLOW, Type.USER_LIST_MEMBER_ADDED, Type.USER_LIST_MEMBER_DELETED,
            Type.USER_LIST_SUBSCRIBED, Type.USER_LIST_UNSUBSCRIBED, Type.USER_LIST_CREATED,
            Type.USER_LIST_UPDATED, Type.USER_LIST_DESTROYED, Type.USER_UPDATE, Type.USER_DELETE,
            Type.USER_SUSPEND, Type.BLOCK, Type.UNBLOCK, Type.DISCONNECTION, Type.UNKNOWN})
    public @interface Type {
        String SENDER = "sender";
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
        String USER_DELETE = "user_delete";
        String USER_SUSPEND = "user_suspend";
        String BLOCK = "block";
        String UNBLOCK = "unblock";
        String DISCONNECTION = "disconnection";
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
}

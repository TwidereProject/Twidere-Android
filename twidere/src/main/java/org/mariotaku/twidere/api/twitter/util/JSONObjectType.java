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

package org.mariotaku.twidere.api.twitter.util;

import com.fasterxml.jackson.core.TreeNode;

/**
 * @author Dan Checkoway - dcheckoway at gmail.com
 * @since Twitter4J 2.1.9
 */
public final class JSONObjectType {
    public enum Type {
        SENDER,
        STATUS,
        DIRECT_MESSAGE,
        DELETE,
        LIMIT,
        STALL_WARNING,
        SCRUB_GEO,
        FRIENDS,
        FAVORITE,
        UNFAVORITE,
        FOLLOW,
        UNFOLLOW,
        USER_LIST_MEMBER_ADDED,
        USER_LIST_MEMBER_DELETED,
        USER_LIST_SUBSCRIBED,
        USER_LIST_UNSUBSCRIBED,
        USER_LIST_CREATED,
        USER_LIST_UPDATED,
        USER_LIST_DESTROYED,
        USER_UPDATE,
        USER_DELETE,
        USER_SUSPEND,
        BLOCK,
        UNBLOCK,
        DISCONNECTION,
        UNKNOWN
    }


    /**
     * Determine the respective object type for a given JSONObject.  This
     * method inspects the object to figure out what type of object it
     * represents.  This is useful when processing JSON events of mixed type
     * from a stream, in which case you may need to know what type of object
     * to construct, or how to handle the event properly.
     *
     * @param json the JSONObject whose type should be determined
     * @return the determined JSONObjectType, or null if not recognized
     */
    public static Type determine(TreeNode json) {
        // This code originally lived in AbstractStreamImplementation.
        // I've moved it in here to expose it as a public encapsulation of
        // the object type determination logic.
        if (json.get("sender") != null) {
            return Type.SENDER;
        } else if (json.get("text") != null) {
            return Type.STATUS;
        } else if (json.get("direct_message") != null) {
            return Type.DIRECT_MESSAGE;
        } else if (json.get("delete") != null) {
            return Type.DELETE;
        } else if (json.get("limit") != null) {
            return Type.LIMIT;
        } else if (json.get("warning") != null) {
            return Type.STALL_WARNING;
        } else if (json.get("scrub_geo") != null) {
            return Type.SCRUB_GEO;
        } else if (json.get("friends") != null) {
            return Type.FRIENDS;
        } else if (json.get("event") != null) {
            String event;
            event = json.get("event").asToken().asString();
            if ("favorite".equals(event)) {
                return Type.FAVORITE;
            } else if ("unfavorite".equals(event)) {
                return Type.UNFAVORITE;
            } else if ("follow".equals(event)) {
                return Type.FOLLOW;
            } else if ("unfollow".equals(event)) {
                return Type.UNFOLLOW;
            } else if (event.startsWith("list")) {
                if ("list_member_added".equals(event)) {
                    return Type.USER_LIST_MEMBER_ADDED;
                } else if ("list_member_removed".equals(event)) {
                    return Type.USER_LIST_MEMBER_DELETED;
                } else if ("list_user_subscribed".equals(event)) {
                    return Type.USER_LIST_SUBSCRIBED;
                } else if ("list_user_unsubscribed".equals(event)) {
                    return Type.USER_LIST_UNSUBSCRIBED;
                } else if ("list_created".equals(event)) {
                    return Type.USER_LIST_CREATED;
                } else if ("list_updated".equals(event)) {
                    return Type.USER_LIST_UPDATED;
                } else if ("list_destroyed".equals(event)) {
                    return Type.USER_LIST_DESTROYED;
                }
            } else if ("user_update".equals(event)) {
                return Type.USER_UPDATE;
            } else if ("user_delete".equals(event)) {
                return Type.USER_DELETE;
            } else if ("user_suspend".equals(event)) {
                return Type.USER_SUSPEND;
            } else if ("block".equals(event)) {
                return Type.BLOCK;
            } else if ("unblock".equals(event)) {
                return Type.UNBLOCK;
            }
        } else if (json.get("disconnect") != null) {
            return Type.DISCONNECTION;
        }
        return Type.UNKNOWN;
    }
}
/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.model;

import android.support.annotation.NonNull;

import org.mariotaku.jsonserializer.JSONParcel;
import org.mariotaku.jsonserializer.JSONParcelable;

import java.util.Arrays;

import twitter4j.Activity;
import twitter4j.Status;
import twitter4j.User;
import twitter4j.UserList;

public class ParcelableActivity implements Comparable<ParcelableActivity>, JSONParcelable {

    public static final JSONParcelable.Creator<ParcelableActivity> JSON_CREATOR = new JSONParcelable.Creator<ParcelableActivity>() {
        @Override
        public ParcelableActivity createFromParcel(final JSONParcel in) {
            return new ParcelableActivity(in);
        }

        @Override
        public ParcelableActivity[] newArray(final int size) {
            return new ParcelableActivity[size];
        }
    };

    public final static int ACTION_FAVORITE = Activity.Action.ACTION_FAVORITE;
    public final static int ACTION_FOLLOW = Activity.Action.ACTION_FOLLOW;
    public final static int ACTION_MENTION = Activity.Action.ACTION_MENTION;
    public final static int ACTION_REPLY = Activity.Action.ACTION_REPLY;
    public final static int ACTION_RETWEET = Activity.Action.ACTION_RETWEET;
    public final static int ACTION_LIST_MEMBER_ADDED = Activity.Action.ACTION_LIST_MEMBER_ADDED;
    public final static int ACTION_LIST_CREATED = Activity.Action.ACTION_LIST_CREATED;
    public final static int ACTION_FAVORITED_RETWEET = Activity.Action.ACTION_FAVORITED_RETWEET;
    public final static int ACTION_RETWEETED_RETWEET = Activity.Action.ACTION_RETWEETED_RETWEET;

    public final long account_id, timestamp, max_position, min_position;
    public final int action;

    public final ParcelableUser[] sources;
    public final ParcelableUser[] target_users;
    public final ParcelableStatus[] target_statuses;
    public final ParcelableUserList[] target_user_lists;

    public final ParcelableUserList[] target_object_user_lists;
    public final ParcelableStatus[] target_object_statuses;
    public final boolean is_gap;

    public ParcelableActivity(final Activity activity, final long account_id, boolean is_gap) {
        this.account_id = account_id;
        timestamp = activity.getCreatedAt().getTime();
        action = activity.getAction().getActionId();
        max_position = activity.getMaxPosition();
        min_position = activity.getMinPosition();
        final int sources_size = activity.getSourcesSize();
        sources = new ParcelableUser[sources_size];
        for (int i = 0; i < sources_size; i++) {
            sources[i] = new ParcelableUser(activity.getSources()[i], account_id);
        }
        final int targets_size = activity.getTargetsSize();
        final User[] targetUsers = activity.getTargetUsers();
        if (targetUsers != null) {
            target_users = new ParcelableUser[targets_size];
            for (int i = 0; i < targets_size; i++) {
                target_users[i] = new ParcelableUser(targetUsers[i], account_id);
            }
        } else {
            target_users = null;
        }
        final UserList[] targetUserLists = activity.getTargetUserLists();
        if (targetUserLists != null) {
            target_user_lists = new ParcelableUserList[targets_size];
            for (int i = 0; i < targets_size; i++) {
                target_user_lists[i] = new ParcelableUserList(targetUserLists[i], account_id);
            }
        } else {
            target_user_lists = null;
        }
        final Status[] targetStatuses = activity.getTargetStatuses();
        if (targetStatuses != null) {
            target_statuses = new ParcelableStatus[targets_size];
            for (int i = 0; i < targets_size; i++) {
                target_statuses[i] = new ParcelableStatus(targetStatuses[i], account_id, false);
            }
        } else {
            target_statuses = null;
        }
        final int target_objects_size = activity.getTargetObjectsSize();
        final Status[] targetObjectStatuses = activity.getTargetObjectStatuses();
        if (targetObjectStatuses != null) {
            target_object_statuses = new ParcelableStatus[target_objects_size];
            for (int i = 0; i < target_objects_size; i++) {
                target_object_statuses[i] = new ParcelableStatus(targetObjectStatuses[i], account_id, false);
            }
        } else {
            target_object_statuses = null;
        }
        final UserList[] targetObjectUserLists = activity.getTargetObjectUserLists();
        if (targetObjectUserLists != null) {
            target_object_user_lists = new ParcelableUserList[target_objects_size];
            for (int i = 0; i < target_objects_size; i++) {
                target_object_user_lists[i] = new ParcelableUserList(targetObjectUserLists[i], account_id);
            }
        } else {
            target_object_user_lists = null;
        }
        this.is_gap = is_gap;
    }

    public ParcelableActivity(final JSONParcel in) {
        account_id = in.readLong("account_id");
        timestamp = in.readLong("timestamp");
        max_position = in.readLong("max_position");
        min_position = in.readLong("min_position");
        action = in.readInt("action");
        sources = in.readParcelableArray("sources", ParcelableUser.JSON_CREATOR);
        target_users = in.readParcelableArray("target_users", ParcelableUser.JSON_CREATOR);
        target_statuses = in.readParcelableArray("target_statuses", ParcelableStatus.JSON_CREATOR);
        target_user_lists = in.readParcelableArray("target_user_lists", ParcelableUserList.JSON_CREATOR);
        target_object_user_lists = in.readParcelableArray("target_object_user_lists", ParcelableUserList.JSON_CREATOR);
        target_object_statuses = in.readParcelableArray("target_object_statuses", ParcelableStatus.JSON_CREATOR);
        is_gap = in.readBoolean("is_gap", false);
    }

    @Override
    public int compareTo(@NonNull final ParcelableActivity another) {
        final long delta = another.timestamp - timestamp;
        if (delta < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        if (delta > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) delta;
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof ParcelableActivity)) return false;
        final ParcelableActivity activity = (ParcelableActivity) that;
        return max_position == activity.max_position && min_position == activity.min_position;
    }

    @Override
    public String toString() {
        return "ParcelableActivity{account_id=" + account_id + ", timestamp=" + timestamp
                + ", max_position=" + max_position + ", min_position=" + min_position + ", action=" + action
                + ", sources=" + Arrays.toString(sources) + ", target_users=" + Arrays.toString(target_users)
                + ", target_statuses=" + Arrays.toString(target_statuses) + ", target_user_lists="
                + Arrays.toString(target_user_lists) + ", target_object_user_lists="
                + Arrays.toString(target_object_user_lists) + ", target_object_statuses="
                + Arrays.toString(target_object_statuses) + "}";
    }

    @Override
    public void writeToParcel(final JSONParcel out) {
        out.writeLong("account_id", account_id);
        out.writeLong("timestamp", timestamp);
        out.writeLong("max_position", max_position);
        out.writeLong("min_position", min_position);
        out.writeInt("action", action);
        out.writeParcelableArray("sources", sources);
        out.writeParcelableArray("target_users", target_users);
        out.writeParcelableArray("target_statuses", target_statuses);
        out.writeParcelableArray("target_user_lists", target_user_lists);
        out.writeParcelableArray("target_object_user_lists", target_object_user_lists);
        out.writeParcelableArray("target_object_statuses", target_object_statuses);
        out.writeBoolean("is_gap", is_gap);
    }


}

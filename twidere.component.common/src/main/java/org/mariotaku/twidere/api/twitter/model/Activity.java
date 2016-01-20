/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.api.twitter.model;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.util.Arrays;
import java.util.Date;

public class Activity extends TwitterResponseObject implements TwitterResponse, Comparable<Activity> {

    @Action
    String action;
    String rawAction;

    Date createdAt;

    User[] sources;
    User[] targetUsers;
    User[] targetObjectUsers;
    Status[] targetObjectStatuses, targetStatuses;
    UserList[] targetUserLists, targetObjectUserLists;
    long maxPosition, minPosition;
    int targetObjectsSize, targetsSize, sourcesSize;

    Activity() {
    }

    public User[] getTargetObjectUsers() {
        return targetObjectUsers;
    }

    @Override
    public int compareTo(@NonNull final Activity another) {
        final Date thisDate = getCreatedAt(), thatDate = another.getCreatedAt();
        if (thisDate == null || thatDate == null) return 0;
        return thisDate.compareTo(thatDate);
    }

    public
    @Action
    String getAction() {
        return action;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public long getMaxPosition() {
        return maxPosition;
    }

    public long getMinPosition() {
        return minPosition;
    }

    public User[] getSources() {
        return sources;
    }

    public int getSourcesSize() {
        return sourcesSize;
    }

    public int getTargetObjectsSize() {
        return targetObjectsSize;
    }

    public Status[] getTargetObjectStatuses() {
        return targetObjectStatuses;
    }

    public UserList[] getTargetObjectUserLists() {
        return targetObjectUserLists;
    }

    public int getTargetsSize() {
        return targetsSize;
    }

    public Status[] getTargetStatuses() {
        return targetStatuses;
    }

    public UserList[] getTargetUserLists() {
        return targetUserLists;
    }

    public User[] getTargetUsers() {
        return targetUsers;
    }

    @Override
    public String toString() {
        return "ActivityJSONImpl{" +
                "action=" + action +
                ", createdAt=" + createdAt +
                ", sources=" + Arrays.toString(sources) +
                ", targetUsers=" + Arrays.toString(targetUsers) +
                ", targetObjectStatuses=" + Arrays.toString(targetObjectStatuses) +
                ", targetStatuses=" + Arrays.toString(targetStatuses) +
                ", targetUserLists=" + Arrays.toString(targetUserLists) +
                ", targetObjectUserLists=" + Arrays.toString(targetObjectUserLists) +
                ", maxPosition=" + maxPosition +
                ", minPosition=" + minPosition +
                ", targetObjectsSize=" + targetObjectsSize +
                ", targetsSize=" + targetsSize +
                ", sourcesSize=" + sourcesSize +
                '}';
    }

    public static Activity fromMention(long accountId, Status status) {
        final Activity activity = new Activity();

        activity.maxPosition = activity.minPosition = status.getId();
        activity.createdAt = status.getCreatedAt();

        if (status.getInReplyToUserId() == accountId) {
            activity.action = Action.REPLY;
            activity.rawAction = "reply";
            activity.targetStatuses = new Status[]{status};

            //TODO set target statuses (in reply to status)
            activity.targetObjectStatuses = new Status[0];
        } else {
            activity.action = Action.MENTION;
            activity.rawAction = "mention";
            activity.targetObjectStatuses = new Status[]{status};

            // TODO set target users (mentioned users)
            activity.targetUsers = null;
        }
        activity.sourcesSize = 1;
        activity.sources = new User[]{status.getUser()};
        return activity;
    }

    @StringDef({Action.FAVORITE, Action.FOLLOW, Action.MENTION, Action.REPLY, Action.RETWEET,
            Action.LIST_MEMBER_ADDED, Action.LIST_CREATED, Action.FAVORITED_RETWEET,
            Action.RETWEETED_RETWEET, Action.QUOTE, Action.RETWEETED_MENTION,
            Action.FAVORITED_MENTION, Action.JOINED_TWITTER, Action.MEDIA_TAGGED,
            Action.FAVORITED_MEDIA_TAGGED, Action.RETWEETED_MEDIA_TAGGED})
    public @interface Action {
        String FAVORITE = ("favorite");
        /**
         * Sources: followers to targets (User)
         * Targets: following user (User)
         */
        String FOLLOW = ("follow");
        /**
         * Targets: mentioned users (User)
         * Target objects: mention status (Status)
         */
        String MENTION = ("mention");
        /**
         * Targets: reply status (Status)
         * Target objects: in reply to status (Status)
         */
        String REPLY = ("reply");
        String RETWEET = ("retweet");
        String LIST_MEMBER_ADDED = ("list_member_added");
        String LIST_CREATED = ("list_created");
        String FAVORITED_RETWEET = ("favorited_retweet");
        String RETWEETED_RETWEET = ("retweeted_retweet");
        /**
         * Targets: Quote result (Status)
         * Target objects: Original status (Status)
         */
        String QUOTE = ("quote");
        String RETWEETED_MENTION = ("retweeted_mention");
        String FAVORITED_MENTION = ("favorited_mention");
        String JOINED_TWITTER = ("joined_twitter");
        String MEDIA_TAGGED = ("media_tagged");
        String FAVORITED_MEDIA_TAGGED = ("favorited_media_tagged");
        String RETWEETED_MEDIA_TAGGED = ("retweeted_media_tagged");

    }
}
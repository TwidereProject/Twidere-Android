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

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

import org.mariotaku.twidere.util.AbsLogger;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class Activity extends TwitterResponseObject implements TwitterResponse, Comparable<Activity> {

    public static final int ACTION_UNKNOWN = 0x00;
    public static final int ACTION_FAVORITE = 0x01;
    public static final int ACTION_FOLLOW = 0x02;
    public static final int ACTION_MENTION = 0x03;
    public static final int ACTION_REPLY = 0x04;
    public static final int ACTION_RETWEET = 0x05;
    public static final int ACTION_LIST_MEMBER_ADDED = 0x06;
    public static final int ACTION_LIST_CREATED = 0x07;
    public static final int ACTION_FAVORITED_RETWEET = 0x08;
    public static final int ACTION_RETWEETED_RETWEET = 0x09;
    public static final int ACTION_QUOTE = 0x0A;
    public static final int ACTION_RETWEETED_MENTION = 0x0B;
    public static final int ACTION_FAVORITED_MENTION = 0x0C;
    public static final int ACTION_JOINED_TWITTER = 0x0D;
    public static final int ACTION_MEDIA_TAGGED = 0x0E;
    public static final int ACTION_FAVORITED_MEDIA_TAGGED = 0x0F;
    public static final int ACTION_RETWEETED_MEDIA_TAGGED = 0x10;
    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
    Action action;
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

    public String getRawAction() {
        return rawAction;
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

    public Action getAction() {
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

    public enum Action {
        FAVORITE(ACTION_FAVORITE),
        /**
         * Sources: followers to targets (User)
         * Targets: following user (User)
         */
        FOLLOW(ACTION_FOLLOW),
        /**
         * Targets: mentioned users (User)
         * Target objects: mention status (Status)
         */
        MENTION(ACTION_MENTION),
        /**
         * Targets: reply status (Status)
         * Target objects: in reply to status (Status)
         */
        REPLY(ACTION_REPLY),
        RETWEET(ACTION_RETWEET), LIST_MEMBER_ADDED(ACTION_LIST_MEMBER_ADDED), LIST_CREATED(ACTION_LIST_CREATED),
        FAVORITED_RETWEET(ACTION_FAVORITED_RETWEET), RETWEETED_RETWEET(ACTION_RETWEETED_RETWEET),
        /**
         * Targets: Quote result (Status)
         * Target objects: Original status (Status)
         */
        QUOTE(ACTION_QUOTE),
        RETWEETED_MENTION(ACTION_RETWEETED_MENTION),
        FAVORITED_MENTION(ACTION_FAVORITED_MENTION), JOINED_TWITTER(ACTION_JOINED_TWITTER),
        MEDIA_TAGGED(ACTION_MEDIA_TAGGED), FAVORITED_MEDIA_TAGGED(ACTION_FAVORITED_MEDIA_TAGGED),
        RETWEETED_MEDIA_TAGGED(ACTION_RETWEETED_MEDIA_TAGGED), UNKNOWN(ACTION_UNKNOWN);

        private final int actionId;

        Action(final int action) {
            actionId = action;
        }

        public static Action parse(final String string) {
            if ("favorite".equalsIgnoreCase(string)) return FAVORITE;
            if ("follow".equalsIgnoreCase(string)) return FOLLOW;
            if ("mention".equalsIgnoreCase(string)) return MENTION;
            if ("reply".equalsIgnoreCase(string)) return REPLY;
            if ("retweet".equalsIgnoreCase(string)) return RETWEET;
            if ("list_member_added".equalsIgnoreCase(string)) return LIST_MEMBER_ADDED;
            if ("list_created".equalsIgnoreCase(string)) return LIST_CREATED;
            if ("favorited_retweet".equalsIgnoreCase(string)) return FAVORITED_RETWEET;
            if ("retweeted_retweet".equalsIgnoreCase(string)) return RETWEETED_RETWEET;
            if ("quote".equalsIgnoreCase(string)) return QUOTE;
            if ("retweeted_mention".equalsIgnoreCase(string)) return RETWEETED_MENTION;
            if ("favorited_mention".equalsIgnoreCase(string)) return FAVORITED_MENTION;
            if ("joined_twitter".equalsIgnoreCase(string)) return JOINED_TWITTER;
            if ("media_tagged".equalsIgnoreCase(string)) return MEDIA_TAGGED;
            if ("favorited_media_tagged".equalsIgnoreCase(string)) return FAVORITED_MEDIA_TAGGED;
            if ("retweeted_media_tagged".equalsIgnoreCase(string)) return RETWEETED_MEDIA_TAGGED;
            AbsLogger.error("Unknown Twitter activity action " + string);
            return UNKNOWN;
        }

        public int getActionId() {
            return actionId;
        }

        public static class Converter extends StringBasedTypeConverter<Action> {

            @Override
            public Action getFromString(String string) {
                return Action.parse(string);
            }

            @Override
            public String convertToString(Action object) {
                //TODO use better literal
                return object.name().toLowerCase(Locale.US);
            }
        }
    }
}
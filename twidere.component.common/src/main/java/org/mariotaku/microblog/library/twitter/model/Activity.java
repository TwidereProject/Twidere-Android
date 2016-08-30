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

package org.mariotaku.microblog.library.twitter.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.commons.logansquare.JsonStringConverter;
import org.mariotaku.microblog.library.twitter.util.TwitterDateConverter;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Date;

/**
 * Twitter Activity object
 */
@ParcelablePlease
@JsonObject
public class Activity extends TwitterResponseObject implements TwitterResponse, Comparable<Activity>,
        Parcelable {

    private static final JsonMapper<User> USER_JSON_MAPPER = LoganSquare.mapperFor(User.class);
    private static final JsonMapper<Status> STATUS_JSON_MAPPER = LoganSquare.mapperFor(Status.class);
    private static final JsonMapper<UserList> USER_LIST_JSON_MAPPER = LoganSquare.mapperFor(UserList.class);

    @Action
    @JsonField(name = "action")
    String action;

    @JsonField(name = "created_at", typeConverter = TwitterDateConverter.class)
    Date createdAt;

    @JsonField(name = "sources")
    User[] sources;
    @JsonField(name = "targets", typeConverter = JsonStringConverter.class)
    String rawTargets;
    @JsonField(name = "target_objects", typeConverter = JsonStringConverter.class)
    String rawTargetObjects;

    User[] targetUsers;
    User[] targetObjectUsers;
    Status[] targetObjectStatuses, targetStatuses;
    UserList[] targetUserLists, targetObjectUserLists;

    @JsonField(name = "max_position")
    String maxPosition = null;
    @JsonField(name = "min_position")
    String minPosition = null;
    long maxSortPosition = -1, minSortPosition = -1;
    @JsonField(name = "target_objects_size")
    int targetObjectsSize;
    @JsonField(name = "targets_size")
    int targetsSize;
    @JsonField(name = "sources_size")
    int sourcesSize;

    Activity() {
    }

    public User[] getTargetObjectUsers() {
        return targetObjectUsers;
    }

    @Action
    public String getAction() {
        return action;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getMaxPosition() {
        return maxPosition;
    }

    public String getMinPosition() {
        return minPosition;
    }

    public long getMaxSortPosition() {
        return maxSortPosition;
    }

    public long getMinSortPosition() {
        return minSortPosition;
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
    public int compareTo(@NonNull final Activity another) {
        final Date thisDate = getCreatedAt(), thatDate = another.getCreatedAt();
        if (thisDate == null || thatDate == null) return 0;
        return thisDate.compareTo(thatDate);
    }

    @Override
    public String toString() {
        return "Activity{" +
                "action='" + action + '\'' +
                ", createdAt=" + createdAt +
                ", sources=" + Arrays.toString(sources) +
                ", targetUsers=" + Arrays.toString(targetUsers) +
                ", targetObjectUsers=" + Arrays.toString(targetObjectUsers) +
                ", targetObjectStatuses=" + Arrays.toString(targetObjectStatuses) +
                ", targetStatuses=" + Arrays.toString(targetStatuses) +
                ", targetUserLists=" + Arrays.toString(targetUserLists) +
                ", targetObjectUserLists=" + Arrays.toString(targetObjectUserLists) +
                ", maxPosition='" + maxPosition + '\'' +
                ", minPosition='" + minPosition + '\'' +
                ", maxSortPosition=" + maxSortPosition +
                ", minSortPosition=" + minSortPosition +
                ", targetObjectsSize=" + targetObjectsSize +
                ", targetsSize=" + targetsSize +
                ", sourcesSize=" + sourcesSize +
                "} " + super.toString();
    }

    @OnJsonParseComplete
    void onParseComplete() throws IOException {
        if (action == null) throw new IOException("Malformed Activity object");
        switch (action) {
            case Activity.Action.FAVORITE:
            case Activity.Action.REPLY:
            case Activity.Action.RETWEET:
            case Activity.Action.QUOTE:
            case Activity.Action.FAVORITED_RETWEET:
            case Activity.Action.RETWEETED_RETWEET:
            case Activity.Action.RETWEETED_MENTION:
            case Activity.Action.FAVORITED_MENTION:
            case Activity.Action.MEDIA_TAGGED:
            case Activity.Action.FAVORITED_MEDIA_TAGGED:
            case Activity.Action.RETWEETED_MEDIA_TAGGED: {
                targetStatuses = STATUS_JSON_MAPPER.parseList(rawTargets).toArray(new Status[targetsSize]);
                break;
            }
            case Activity.Action.FOLLOW:
            case Activity.Action.MENTION:
            case Activity.Action.LIST_MEMBER_ADDED: {
                targetUsers = USER_JSON_MAPPER.parseList(rawTargets).toArray(new User[targetsSize]);
                break;
            }
            case Activity.Action.LIST_CREATED: {
                targetUserLists = USER_LIST_JSON_MAPPER.parseList(rawTargets).toArray(new UserList[targetsSize]);
                break;
            }
        }
        switch (action) {
            case Activity.Action.FAVORITE:
            case Activity.Action.FOLLOW:
            case Activity.Action.MENTION:
            case Activity.Action.REPLY:
            case Activity.Action.RETWEET:
            case Activity.Action.LIST_CREATED:
            case Activity.Action.QUOTE: {
                targetObjectStatuses = STATUS_JSON_MAPPER.parseList(rawTargetObjects).toArray(new Status[targetObjectsSize]);
                break;
            }
            case Activity.Action.LIST_MEMBER_ADDED: {
                targetObjectUserLists = USER_LIST_JSON_MAPPER.parseList(rawTargetObjects).toArray(new UserList[targetObjectsSize]);
                break;
            }
            case Activity.Action.FAVORITED_RETWEET:
            case Activity.Action.RETWEETED_RETWEET:
            case Activity.Action.RETWEETED_MENTION:
            case Activity.Action.FAVORITED_MENTION:
            case Activity.Action.MEDIA_TAGGED:
            case Activity.Action.FAVORITED_MEDIA_TAGGED:
            case Activity.Action.RETWEETED_MEDIA_TAGGED: {
                targetObjectUsers = USER_JSON_MAPPER.parseList(rawTargetObjects).toArray(new User[targetObjectsSize]);
                break;
            }
        }
        try {
            maxSortPosition = Long.parseLong(maxPosition);
            minSortPosition = Long.parseLong(minPosition);
        } catch (NumberFormatException e) {
            final long time = createdAt != null ? createdAt.getTime() : -1;
            maxSortPosition = time;
            minSortPosition = time;
        }
    }

    public static Activity fromMention(@NonNull String accountId, @NonNull Status status) {
        final Activity activity = new Activity();

        activity.maxPosition = activity.minPosition = status.getId();
        activity.maxSortPosition = activity.minSortPosition = status.getSortId();
        activity.createdAt = status.getCreatedAt();

        if (TextUtils.equals(status.getInReplyToUserId(), accountId)) {
            activity.action = Action.REPLY;
            activity.targetStatuses = new Status[]{status};

            //TODO set target statuses (in reply to status)
            activity.targetObjectStatuses = new Status[0];
        } else if (status.quotedStatus != null && TextUtils.equals(status.quotedStatus.user.getId(),
                accountId)) {
            activity.action = Action.QUOTE;
            activity.targetStatuses = new Status[]{status};
            activity.targetObjectStatuses = new Status[0];
        } else {
            activity.action = Action.MENTION;
            activity.targetUsers = new User[0];
            activity.targetObjectStatuses = new Status[]{status};
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
    @Retention(RetentionPolicy.SOURCE)
    public @interface Action {
        String FAVORITE = "favorite";
        /**
         * Sources: followers to targets (User)
         * Targets: following user (User)
         */
        String FOLLOW = "follow";
        /**
         * Targets: mentioned users (User)
         * Target objects: mention status (Status)
         */
        String MENTION = "mention";
        /**
         * Targets: reply status (Status)
         * Target objects: in reply to status (Status)
         */
        String REPLY = "reply";
        String RETWEET = "retweet";
        String LIST_MEMBER_ADDED = "list_member_added";
        String LIST_CREATED = "list_created";
        String FAVORITED_RETWEET = "favorited_retweet";
        String RETWEETED_RETWEET = "retweeted_retweet";
        /**
         * Targets: Quote result (Status)
         * Target objects: Original status (Status)
         */
        String QUOTE = "quote";
        String RETWEETED_MENTION = "retweeted_mention";
        String FAVORITED_MENTION = "favorited_mention";
        String JOINED_TWITTER = "joined_twitter";
        String MEDIA_TAGGED = "media_tagged";
        String FAVORITED_MEDIA_TAGGED = "favorited_media_tagged";
        String RETWEETED_MEDIA_TAGGED = "retweeted_media_tagged";

        String[] MENTION_ACTIONS = {MENTION, REPLY, QUOTE};
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ActivityParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Activity> CREATOR = new Creator<Activity>() {
        @Override
        public Activity createFromParcel(Parcel source) {
            Activity target = new Activity();
            ActivityParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public Activity[] newArray(int size) {
            return new Activity[size];
        }
    };
}
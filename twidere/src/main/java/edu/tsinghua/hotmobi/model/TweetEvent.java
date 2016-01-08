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

package edu.tsinghua.hotmobi.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.twidere.model.ParcelableStatus;

import edu.tsinghua.hotmobi.HotMobiLogger;

/**
 * Created by mariotaku on 15/8/7.
 */
@ParcelablePlease
@JsonObject
public class TweetEvent extends BaseEvent implements Parcelable {

    @ParcelableThisPlease
    @JsonField(name = "id")
    long id;
    @ParcelableThisPlease
    @JsonField(name = "account_id")
    long accountId;
    @ParcelableThisPlease
    @JsonField(name = "user_id")
    long userId;
    @ParcelableThisPlease
    @JsonField(name = "tweet_type")
    @TweetType
    String tweetType;
    @ParcelableThisPlease
    @JsonField(name = "timeline_type")
    @TimelineType
    String timelineType;
    @ParcelableThisPlease
    @JsonField(name = "action")
    @Action
    String action;
    @ParcelableThisPlease
    @JsonField(name = "following")
    boolean following;

    public static TweetEvent create(Context context, ParcelableStatus status, @TimelineType String timelineType) {
        final TweetEvent event = new TweetEvent();
        event.markStart(context);
        event.setId(status.id);
        event.setAccountId(status.account_id);
        event.setUserId(status.user_id);
        event.setTimelineType(timelineType);
        event.setTweetType(HotMobiLogger.getTweetType(status));
        event.setFollowing(status.user_is_following);
        return event;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public void setAction(@Action String action) {
        this.action = action;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setTweetType(@TweetType String tweetType) {
        this.tweetType = tweetType;
    }

    public void setTimelineType(@TimelineType String timelineType) {
        this.timelineType = timelineType;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @Override
    public String toString() {
        return "TweetEvent{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", userId=" + userId +
                ", tweetType=" + tweetType +
                ", timelineType=" + timelineType +
                ", action=" + action +
                "} " + super.toString();
    }

    public @interface Action {
        String OPEN = "open", RETWEET = "retweet", FAVORITE = "favorite", UNFAVORITE = "unfavorite",
                TWEET = "tweet", UNKNOWN = "unknown";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TweetEventParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<TweetEvent> CREATOR = new Creator<TweetEvent>() {
        public TweetEvent createFromParcel(Parcel source) {
            TweetEvent target = new TweetEvent();
            TweetEventParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public TweetEvent[] newArray(int size) {
            return new TweetEvent[size];
        }
    };
}

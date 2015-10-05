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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.twidere.model.ParcelableStatus;

/**
 * Created by mariotaku on 15/8/7.
 */
@ParcelablePlease
@JsonObject
public class TweetEvent extends BaseEvent {

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
    @JsonField(name = "tweet_type", typeConverter = TweetType.TweetTypeConverter.class)
    TweetType tweetType;
    @ParcelableThisPlease
    @JsonField(name = "timeline_type", typeConverter = TimelineType.TimelineTypeConverter.class)
    TimelineType timelineType;
    @ParcelableThisPlease
    @JsonField(name = "action", typeConverter = Action.TweetActionConverter.class)
    Action action;

    public static TweetEvent create(Context context, ParcelableStatus status, TimelineType timelineType) {
        final TweetEvent event = new TweetEvent();
        event.markStart(context);
        event.setId(status.id);
        event.setAccountId(status.account_id);
        event.setUserId(status.user_id);
        event.setTimelineType(timelineType);
        event.setTweetType(TweetType.getTweetType(status));
        return event;
    }


    public void setAction(Action action) {
        this.action = action;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setTweetType(TweetType tweetType) {
        this.tweetType = tweetType;
    }

    public void setTimelineType(TimelineType timelineType) {
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

    public enum Action {
        OPEN("open"), RETWEET("retweet"), FAVORITE("favorite"), UNFAVORITE("unfavorite"),
        TWEET("tweet"), UNKNOWN("unknown");

        private final String value;

        Action(String value) {
            this.value = value;
        }

        public static Action parse(String action) {
            if (OPEN.value.equalsIgnoreCase(action)) {
                return OPEN;
            } else if (RETWEET.value.equalsIgnoreCase(action)) {
                return RETWEET;
            } else if (FAVORITE.value.equalsIgnoreCase(action)) {
                return FAVORITE;
            } else if (UNFAVORITE.value.equalsIgnoreCase(action)) {
                return UNFAVORITE;
            }
            return UNKNOWN;
        }


        public static class TweetActionConverter extends StringBasedTypeConverter<Action> {

            @Override
            public Action getFromString(String string) {
                return Action.parse(string);
            }

            @Override
            public String convertToString(Action action) {
                if (action == null) return null;
                return action.value;
            }
        }
    }
}

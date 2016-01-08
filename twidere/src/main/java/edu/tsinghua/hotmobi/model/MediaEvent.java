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

import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;

import edu.tsinghua.hotmobi.HotMobiLogger;

/**
 * Created by mariotaku on 15/8/7.
 */
@ParcelablePlease
@JsonObject
public class MediaEvent extends BaseEvent implements Parcelable {

    @JsonField(name = "id")
    long id;

    @JsonField(name = "user_id")
    long userId;

    @JsonField(name = "tweet_type")
    @TweetType
    String tweetType;

    @JsonField(name = "timeline_type")
    @TimelineType
    String timelineType;

    @JsonField(name = "preview_url")
    String previewUrl;

    @JsonField(name = "media_url")
    String mediaUrl;

    @JsonField(name = "preview_enabled")
    boolean previewEnabled;

    public static MediaEvent create(Context context, ParcelableStatus status, ParcelableMedia media,
                                    @TimelineType String timelineType, boolean previewEnabled) {
        final MediaEvent event = new MediaEvent();
        event.markStart(context);
        event.setId(status.id);
        event.setUserId(status.user_id);
        event.setMediaUrl(media.media_url);
        event.setPreviewUrl(media.preview_url);
        event.setPreviewEnabled(previewEnabled);
        event.setTimelineType(timelineType);
        event.setTweetType(HotMobiLogger.getTweetType(status));
        return event;
    }

    public void setPreviewEnabled(boolean previewEnabled) {
        this.previewEnabled = previewEnabled;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
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

    @Override
    public String toString() {
        return "MediaEvent{" +
                "id=" + id +
                ", userId=" + userId +
                ", tweetType=" + tweetType +
                ", timelineType=" + timelineType +
                ", previewUrl='" + previewUrl + '\'' +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", previewEnabled=" + previewEnabled +
                "} " + super.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        MediaEventParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<MediaEvent> CREATOR = new Creator<MediaEvent>() {
        public MediaEvent createFromParcel(Parcel source) {
            MediaEvent target = new MediaEvent();
            MediaEventParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public MediaEvent[] newArray(int size) {
            return new MediaEvent[size];
        }
    };
}

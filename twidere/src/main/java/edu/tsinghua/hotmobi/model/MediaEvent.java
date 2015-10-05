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
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;

/**
 * Created by mariotaku on 15/8/7.
 */
@ParcelablePlease
@JsonObject
public class MediaEvent extends BaseEvent {

    @ParcelableThisPlease
    @JsonField(name = "id")
    long id;
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
    @JsonField(name = "preview_url")
    String previewUrl;
    @ParcelableThisPlease
    @JsonField(name = "media_url")
    String mediaUrl;
    @ParcelableThisPlease
    @JsonField(name = "preview_enabled")
    boolean previewEnabled;

    public static MediaEvent create(Context context, ParcelableStatus status, ParcelableMedia media, TimelineType timelineType, boolean previewEnabled) {
        final MediaEvent event = new MediaEvent();
        event.markStart(context);
        event.setId(status.id);
        event.setUserId(status.user_id);
        event.setMediaUrl(media.media_url);
        event.setPreviewUrl(media.preview_url);
        event.setPreviewEnabled(previewEnabled);
        event.setTimelineType(timelineType);
        event.setTweetType(TweetType.getTweetType(status));
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

    public void setTweetType(TweetType tweetType) {
        this.tweetType = tweetType;
    }

    public void setTimelineType(TimelineType timelineType) {
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
}

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

package org.mariotaku.twidere.api.twitter.model.impl;

import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;

import org.mariotaku.twidere.api.twitter.model.CardEntity;
import org.mariotaku.twidere.api.twitter.model.GeoLocation;
import org.mariotaku.twidere.api.twitter.model.HashtagEntity;
import org.mariotaku.twidere.api.twitter.model.MediaEntity;
import org.mariotaku.twidere.api.twitter.model.Place;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.UrlEntity;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserMentionEntity;
import org.mariotaku.twidere.api.twitter.util.TwitterDateConverter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by mariotaku on 15/5/5.
 */
@JsonObject
public class StatusImpl extends TwitterResponseImpl implements Status {

    @JsonField(name = "created_at", typeConverter = TwitterDateConverter.class)
    Date createdAt;

    @JsonField(name = "id")
    long id;

    @JsonField(name = "text")
    String text;

    @JsonField(name = "source")
    String source;

    @JsonField(name = "truncated")
    boolean truncated;

    @JsonField(name = "entities")
    EntitiesImpl entities;

    @JsonField(name = "extended_entities")
    EntitiesImpl extendedEntities;

    @JsonField(name = "in_reply_to_status_id")
    long inReplyToStatusId;

    @JsonField(name = "in_reply_to_user_id")
    long inReplyToUserId;

    @JsonField(name = "in_reply_to_screen_name")
    String inReplyToScreenName;

    @JsonField(name = "user")
    UserImpl user;

    @JsonField(name = "geo")
    GeoLocation geo;

    @JsonField(name = "place")
    Place place;

    @JsonField(name = "current_user_retweet")
    CurrentUserRetweet currentUserRetweet;

    @JsonField(name = "contributors")
    long[] contributors;

    @JsonField(name = "retweet_count")
    long retweetCount;

    @JsonField(name = "favorite_count")
    long favoriteCount;

    @JsonField(name = "reply_count")
    long replyCount;

    @JsonField(name = "favorited")
    boolean favorited;
    @JsonField(name = "retweeted")
    boolean retweeted;
    @JsonField(name = "lang")
    String lang;

    @JsonField(name = "descendent_reply_count")
    long descendentReplyCount;

    @JsonField(name = "retweeted_status")
    Status retweetedStatus;

    @JsonField(name = "quoted_status")
    Status quotedStatus;

    @JsonField(name = "card")
    CardEntity card;

    @JsonField(name = "possibly_sensitive")
    boolean possiblySensitive;

    public static void setQuotedStatus(Status status, Status quoted) {
        if (!(status instanceof StatusImpl)) return;
        ((StatusImpl) status).quotedStatus = quoted;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getInReplyToScreenName() {

        return inReplyToScreenName;
    }

    @Override
    public long getInReplyToUserId() {

        return inReplyToUserId;
    }

    @Override
    public long getInReplyToStatusId() {

        return inReplyToStatusId;
    }

    @Override
    public boolean isTruncated() {

        return truncated;
    }

    @Override
    public String getText() {

        return text;
    }

    @Override
    public String getSource() {

        return source;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getRetweetCount() {
        return retweetCount;
    }

    @Override
    public long getReplyCount() {
        return replyCount;
    }

    @Override
    public boolean isFavorited() {
        return favorited;
    }

    @Override
    public boolean isRetweet() {
        return retweetedStatus != null;
    }

    @Override
    public boolean isQuote() {
        return quotedStatus != null;
    }

    @Override
    public boolean isRetweetedByMe() {
        return currentUserRetweet != null;
    }

    @Override
    public long getFavoriteCount() {
        return favoriteCount;
    }

    @Override
    public GeoLocation getGeoLocation() {
        return geo;
    }

    @Override
    public long getCurrentUserRetweet() {
        if (currentUserRetweet == null) return -1;
        return currentUserRetweet.id;
    }

    @Override
    public Status getQuotedStatus() {
        return quotedStatus;
    }

    @Override
    public Status getRetweetedStatus() {
        return retweetedStatus;
    }

    @Override
    public long getDescendentReplyCount() {
        return descendentReplyCount;
    }

    @Override
    public Place getPlace() {
        return place;
    }

    @Override
    public CardEntity getCard() {
        return card;
    }

    @Override
    public boolean isPossiblySensitive() {
        return possiblySensitive;
    }

    @Override
    public MediaEntity[] getExtendedMediaEntities() {
        if (extendedEntities == null) return null;
        return extendedEntities.getMedia();
    }

    @Override
    public HashtagEntity[] getHashtagEntities() {
        if (entities == null) return null;
        return entities.getHashtags();
    }

    @Override
    public MediaEntity[] getMediaEntities() {
        if (entities == null) return null;
        return entities.getMedia();
    }

    @Override
    public UrlEntity[] getUrlEntities() {
        if (entities == null) return null;
        return entities.getUrls();
    }

    @Override
    public UserMentionEntity[] getUserMentionEntities() {
        if (entities == null) return null;
        return entities.getUserMentions();
    }

    @Override
    public long[] getContributors() {
        return contributors;
    }

    @Override
    public int compareTo(@NonNull final Status that) {
        final long delta = id - that.getId();
        if (delta < Integer.MIN_VALUE)
            return Integer.MIN_VALUE;
        else if (delta > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) delta;
    }

    @Override
    public String toString() {
        return "StatusImpl{" +
                "createdAt=" + createdAt +
                ", id=" + id +
                ", text='" + text + '\'' +
                ", source='" + source + '\'' +
                ", truncated=" + truncated +
                ", entities=" + entities +
                ", extendedEntities=" + extendedEntities +
                ", inReplyToStatusId=" + inReplyToStatusId +
                ", inReplyToUserId=" + inReplyToUserId +
                ", inReplyToScreenName='" + inReplyToScreenName + '\'' +
                ", user=" + user +
                ", geo=" + geo +
                ", place=" + place +
                ", currentUserRetweet=" + currentUserRetweet +
                ", contributors=" + Arrays.toString(contributors) +
                ", retweetCount=" + retweetCount +
                ", favoriteCount=" + favoriteCount +
                ", replyCount=" + replyCount +
                ", favorited=" + favorited +
                ", retweeted=" + retweeted +
                ", lang='" + lang + '\'' +
                ", descendentReplyCount=" + descendentReplyCount +
                ", retweetedStatus=" + retweetedStatus +
                ", quotedStatus=" + quotedStatus +
                ", card=" + card +
                ", possiblySensitive=" + possiblySensitive +
                '}';
    }

    @OnJsonParseComplete
    void onJsonParseComplete() throws IOException {
        if (id <= 0 || text == null) throw new IOException("Malformed Status object");
    }

    @JsonObject
    static class CurrentUserRetweet {
        @JsonField(name = "id")
        long id;

    }
}

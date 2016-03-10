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

package org.mariotaku.twidere.api.twitter.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;

import org.mariotaku.twidere.api.gnusocial.model.Attachment;
import org.mariotaku.twidere.api.statusnet.model.Attention;
import org.mariotaku.twidere.api.twitter.util.TwitterDateConverter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by mariotaku on 15/5/5.
 */
@JsonObject
public class Status extends TwitterResponseObject implements Comparable<Status>, TwitterResponse,
        ExtendedEntitySupport {

    @JsonField(name = "created_at", typeConverter = TwitterDateConverter.class)
    Date createdAt;

    @JsonField(name = "id")
    String id;

    @JsonField(name = "raw_id")
    long rawId;

    @JsonField(name = "text")
    String text;

    @JsonField(name = "source")
    String source;

    @JsonField(name = "truncated")
    boolean truncated;

    @JsonField(name = "entities")
    Entities entities;

    @JsonField(name = "extended_entities")
    Entities extendedEntities;

    @JsonField(name = "in_reply_to_status_id")
    String inReplyToStatusId;

    @JsonField(name = "in_reply_to_user_id")
    String inReplyToUserId;

    @JsonField(name = "in_reply_to_screen_name")
    String inReplyToScreenName;

    @JsonField(name = "user")
    User user;

    @JsonField(name = "geo")
    GeoPoint geo;

    @JsonField(name = "place")
    Place place;

    @JsonField(name = "current_user_retweet")
    CurrentUserRetweet currentUserRetweet;

    @Nullable
    @JsonField(name = "contributors")
    Contributor[] contributors;

    @JsonField(name = "retweet_count")
    long retweetCount = -1;

    @JsonField(name = "favorite_count")
    long favoriteCount = -1;

    @JsonField(name = "reply_count")
    long replyCount = -1;

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


    @JsonField(name = "attachments")
    Attachment[] attachments;

    @JsonField(name = "external_url")
    String externalUrl;

    @JsonField(name = "attentions")
    Attention[] attentions;


    public User getUser() {
        return user;
    }


    public String getInReplyToScreenName() {
        return inReplyToScreenName;
    }


    public String getInReplyToUserId() {
        return inReplyToUserId;
    }


    public String getInReplyToStatusId() {
        return inReplyToStatusId;
    }


    public boolean isTruncated() {
        return truncated;
    }


    public String getText() {

        return text;
    }


    public String getSource() {
        return source;
    }

    /**
     * UTC time when this Tweet was created.
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    public String getId() {
        return id;
    }

    public long getRawId() {
        return rawId;
    }

    public long getRetweetCount() {
        return retweetCount;
    }


    public long getReplyCount() {
        return replyCount;
    }


    public boolean isFavorited() {
        return favorited;
    }


    public boolean isRetweet() {
        return retweetedStatus != null;
    }


    public boolean isQuote() {
        return quotedStatus != null;
    }


    public boolean isRetweetedByMe() {
        return currentUserRetweet != null;
    }

    public boolean wasRetweeted() {
        return retweeted;
    }


    public long getFavoriteCount() {
        return favoriteCount;
    }


    public GeoLocation getGeoLocation() {
        if (geo == null) return null;
        return geo.getGeoLocation();
    }


    /**
     * <i>Perspectival</i>. Only surfaces on methods supporting the <code>include_my_retweet</code> parameter,
     * when set to true. Details the Tweet ID of the user’s own retweet (if existent) of this Tweet.
     */
    public String getCurrentUserRetweet() {
        if (currentUserRetweet == null) return null;
        return currentUserRetweet.id;
    }


    public Status getQuotedStatus() {
        return quotedStatus;
    }


    public Status getRetweetedStatus() {
        return retweetedStatus;
    }


    public long getDescendentReplyCount() {
        return descendentReplyCount;
    }


    public Place getPlace() {
        return place;
    }


    public CardEntity getCard() {
        return card;
    }


    public boolean isPossiblySensitive() {
        return possiblySensitive;
    }


    public MediaEntity[] getExtendedMediaEntities() {
        if (extendedEntities == null) return null;
        return extendedEntities.getMedia();
    }


    public HashtagEntity[] getHashtagEntities() {
        if (entities == null) return null;
        return entities.getHashtags();
    }


    public MediaEntity[] getMediaEntities() {
        if (entities == null) return null;
        return entities.getMedia();
    }


    public UrlEntity[] getUrlEntities() {
        if (entities == null) return null;
        return entities.getUrls();
    }

    public Entities getEntities() {
        return entities;
    }

    public UserMentionEntity[] getUserMentionEntities() {
        if (entities == null) return null;
        return entities.getUserMentions();
    }


    /**
     * An collection of brief user objects (usually only one) indicating users who contributed to
     * the authorship of the tweet, on behalf of the official tweet author.
     */
    @Nullable
    public Contributor[] getContributors() {
        return contributors;
    }

    public Attachment[] getAttachments() {
        return attachments;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public Attention[] getAttentions() {
        return attentions;
    }

    @Override
    public int compareTo(@NonNull final Status that) {
        final int delta = createdAt.compareTo(that.createdAt);
        if (delta == 0) {
            // TODO compare with raw id
        }
        return delta;
    }

    @Override
    public String toString() {
        return "Status{" +
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
                ", attachments=" + Arrays.toString(attachments) +
                ", externalUrl='" + externalUrl + '\'' +
                ", attentions=" + Arrays.toString(attentions) +
                "} " + super.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Status status = (Status) o;

        return id.equals(status.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @OnJsonParseComplete
    void onJsonParseComplete() throws IOException {
        if (id == null || text == null) throw new IOException("Malformed Status object");
    }

    public String getLang() {
        return lang;
    }

    public static void setQuotedStatus(Status status, Status quoted) {
        if (status == null) return;
        status.quotedStatus = quoted;
    }


    @JsonObject
    public static class CurrentUserRetweet {
        @JsonField(name = "id")
        String id;

    }
}

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

package org.mariotaku.microblog.library.twitter.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.microblog.library.fanfou.model.Photo;
import org.mariotaku.microblog.library.gnusocial.model.Attachment;
import org.mariotaku.microblog.library.statusnet.model.Attention;
import org.mariotaku.microblog.library.twitter.util.TwitterDateConverter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by mariotaku on 15/5/5.
 */
@ParcelablePlease
@JsonObject
public class Status extends TwitterResponseObject implements Comparable<Status>, TwitterResponse,
        ExtendedEntitySupport, Parcelable {

    @JsonField(name = "created_at", typeConverter = TwitterDateConverter.class)
    Date createdAt;

    @JsonField(name = "id")
    String id;

    // Fanfou uses this key
    @JsonField(name = "rawid")
    long rawId = -1;

    @JsonField(name = "text")
    String text;

    /**
     * https://dev.twitter.com/overview/api/upcoming-changes-to-tweets
     */
    @JsonField(name = "full_text")
    String fullText;

    @JsonField(name = "statusnet_html")
    String statusnetHtml;

    @JsonField(name = "source")
    String source;

    @JsonField(name = "truncated")
    boolean truncated;

    @JsonField(name = "entities")
    Entities entities;

    @JsonField(name = "extended_entities")
    Entities extendedEntities;

    @Nullable
    @JsonField(name = "in_reply_to_status_id")
    String inReplyToStatusId;

    @Nullable
    @JsonField(name = "in_reply_to_user_id")
    String inReplyToUserId;

    @Nullable
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

    @JsonField(name = "is_quote_status")
    boolean isQuoteStatus;

    @JsonField(name = "quoted_status_id_str")
    String quotedStatusId;

    @JsonField(name = "repost_status")
    Status repostStatus;

    @JsonField(name = "repost_status_id")
    String repostStatusId;

    @JsonField(name = "card")
    CardEntity card;

    @JsonField(name = "possibly_sensitive")
    boolean possiblySensitive;

    /**
     * For GNU social
     */
    @JsonField(name = "attachments")
    Attachment[] attachments;


    /**
     * For GNU social
     */
    @JsonField(name = "external_url")
    String externalUrl;

    /**
     * For GNU social
     */
    @JsonField(name = "statusnet_conversation_id")
    String statusnetConversationId;


    /**
     * For GNU social
     */
    @JsonField(name = "attentions")
    Attention[] attentions;

    /**
     * For Fanfou
     */
    @JsonField(name = "photo")
    Photo photo;
    /**
     * For Fanfou
     */
    @JsonField(name = "location")
    String location;

    @JsonField(name = "display_text_range")
    int[] displayTextRange;

    @ParcelableNoThanks
    private transient long sortId = -1;


    public User getUser() {
        return user;
    }

    @Nullable
    public String getInReplyToScreenName() {
        return inReplyToScreenName;
    }


    @Nullable
    public String getInReplyToUserId() {
        return inReplyToUserId;
    }

    @Nullable
    public String getInReplyToStatusId() {
        return inReplyToStatusId;
    }


    public boolean isTruncated() {
        return truncated;
    }

    public String getText() {
        return text;
    }

    public String getFullText() {
        return fullText;
    }

    public String getExtendedText() {
        if (fullText != null) return fullText;
        return text;
    }

    public String getHtmlText() {
        if (statusnetHtml != null) return statusnetHtml;
        if (fullText != null) return fullText;
        return text;
    }

    public String getStatusnetHtml() {
        return statusnetHtml;
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

    public Entities getEntities() {
        return entities;
    }

    @Override
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

    public String getStatusnetConversationId() {
        return statusnetConversationId;
    }

    public Attention[] getAttentions() {
        return attentions;
    }

    public String getLang() {
        return lang;
    }

    public long getSortId() {
        if (sortId != -1) return sortId;
        sortId = rawId;
        if (sortId == -1) {
            // Try use long id
            try {
                sortId = Long.parseLong(id);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        if (sortId == -1 && createdAt != null) {
            // Try use timestamp
            sortId = createdAt.getTime();
        }
        return sortId;
    }

    public Photo getPhoto() {
        return photo;
    }

    public String getLocation() {
        return location;
    }

    public int[] getDisplayTextRange() {
        return displayTextRange;
    }

    public boolean isQuoteStatus() {
        return isQuoteStatus;
    }

    public String getQuotedStatusId() {
        return quotedStatusId;
    }

    @Override
    public int compareTo(@NonNull final Status that) {
        final long diff = getSortId() - that.getSortId();
        if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) diff;
    }

    @Override
    public String toString() {
        return "Status{" +
                "createdAt=" + createdAt +
                ", id='" + id + '\'' +
                ", rawId=" + rawId +
                ", text='" + text + '\'' +
                ", source='" + source + '\'' +
                ", truncated=" + truncated +
                ", entities=" + entities +
                ", extendedEntities=" + extendedEntities +
                ", inReplyToStatusId='" + inReplyToStatusId + '\'' +
                ", inReplyToUserId='" + inReplyToUserId + '\'' +
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
                ", repostStatus=" + repostStatus +
                ", card=" + card +
                ", possiblySensitive=" + possiblySensitive +
                ", attachments=" + Arrays.toString(attachments) +
                ", externalUrl='" + externalUrl + '\'' +
                ", attentions=" + Arrays.toString(attentions) +
                ", photo=" + photo +
                ", sortId=" + sortId +
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
    void afterStatusParsed() throws IOException {
        if (id == null) {
            throw new IOException("Malformed Status object (no id)");
        }
        if (text == null && fullText == null) {
            throw new IOException("Malformed Status object (no text)");
        }
        fixStatus();
    }

    protected void fixStatus() {
        // Fix for fanfou
        if (TextUtils.isEmpty(inReplyToStatusId)) {
            inReplyToStatusId = null;
            inReplyToUserId = null;
            inReplyToScreenName = null;
        }
        if (quotedStatus == null && repostStatus != null) {
            quotedStatus = repostStatus;
            quotedStatusId = repostStatusId;
            isQuoteStatus = true;

            // Set repost media to null if identical to original
            if (photo != null && photo.equals(repostStatus.photo)) {
                photo = null;
            }
        }
    }


    @ParcelablePlease
    @JsonObject
    public static class CurrentUserRetweet implements Parcelable {
        @JsonField(name = "id")
        String id;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Status$CurrentUserRetweetParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<CurrentUserRetweet> CREATOR = new Creator<CurrentUserRetweet>() {
            @Override
            public CurrentUserRetweet createFromParcel(Parcel source) {
                CurrentUserRetweet target = new CurrentUserRetweet();
                Status$CurrentUserRetweetParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public CurrentUserRetweet[] newArray(int size) {
                return new CurrentUserRetweet[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        StatusParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Status> CREATOR = new Creator<Status>() {
        @Override
        public Status createFromParcel(Parcel source) {
            Status target = new Status();
            StatusParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public Status[] newArray(int size) {
            return new Status[size];
        }
    };
}

/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.microblog.library.twitter.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    @JsonField(name = {"retweet_count", "repeat_num"})
    long retweetCount = -1;

    @JsonField(name = {"favorite_count", "fave_num"})
    long favoriteCount = -1;

    @JsonField(name = "reply_count")
    long replyCount = -1;

    @JsonField(name = "favorited")
    boolean favorited;
    @JsonField(name = {"retweeted", "repeated"})
    boolean retweeted;
    @JsonField(name = "lang")
    String lang;

    @JsonField(name = "descendent_reply_count")
    long descendentReplyCount;

    @JsonField(name = "retweeted_status")
    Status retweetedStatus;

    /**
     * <code>repost_status</code> is for Fanfou, <code>quoted_status</code> is for twitter
     */
    @JsonField(name = {"quoted_status", "repost_status"})
    Status quotedStatus;

    /**
     * <code>repost_status_id</code> is for Fanfou, <code>quoted_status_id_str</code> is for twitter
     */
    @JsonField(name = {"quoted_status_id_str", "repost_status_id"})
    String quotedStatusId;

    @JsonField(name = "is_quote_status")
    boolean isQuoteStatus;

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

    @JsonField(name = "conversation_id")
    String conversationId;


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

    /**
     * GNU social value
     * Format: {@code "tag:[gnusocial.host],YYYY-MM-DD:noticeId=[noticeId]:objectType=[objectType]"}
     */
    @JsonField(name = "uri")
    String uri;

    @JsonField(name = "timestamp_ms")
    long timestampMs = -1;

    @JsonField(name = "extended_tweet")
    ExtendedTweet extendedTweet;

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
        if (extendedTweet != null) {
            return extendedTweet.fullText;
        }
        return fullText;
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
        if (timestampMs != -1) {
            return new Date(timestampMs);
        }
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
        Entities extendedEntities = getExtendedEntities();
        if (extendedEntities == null) return null;
        return extendedEntities.getMedia();
    }

    @Override
    public HashtagEntity[] getHashtagEntities() {
        Entities entities = getEntities();
        if (entities == null) return null;
        return entities.getHashtags();
    }


    @Override
    public MediaEntity[] getMediaEntities() {
        Entities entities = getEntities();
        if (entities == null) return null;
        return entities.getMedia();
    }


    @Override
    public UrlEntity[] getUrlEntities() {
        Entities entities = getEntities();
        if (entities == null) return null;
        return entities.getUrls();
    }


    @Override
    public UserMentionEntity[] getUserMentionEntities() {
        Entities entities = getEntities();
        if (entities == null) return null;
        return entities.getUserMentions();
    }

    public Entities getEntities() {
        if (extendedTweet != null) {
            return extendedTweet.entities;
        }
        return entities;
    }

    public Entities getExtendedEntities() {
        if (extendedTweet != null) {
            return extendedTweet.extendedEntities;
        }
        return extendedEntities;
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

    public String getConversationId() {
        return conversationId;
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
        if (extendedTweet != null) {
            return extendedTweet.displayTextRange;
        }
        return displayTextRange;
    }

    public boolean isQuoteStatus() {
        return isQuoteStatus;
    }

    public String getQuotedStatusId() {
        return quotedStatusId;
    }

    public String getUri() {
        return uri;
    }


    // Convenience getters

    public String getExtendedText() {
        String fullText = getFullText();
        if (fullText != null) return fullText;
        return getText();
    }

    public String getHtmlText() {
        String statusnetHtml = getStatusnetHtml();
        if (statusnetHtml != null) return statusnetHtml;
        String fullText = getFullText();
        if (fullText != null) return fullText;
        return getText();
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

    void fixStatus() {
        // Fix for fanfou
        if (TextUtils.isEmpty(inReplyToStatusId)) {
            inReplyToStatusId = null;
            inReplyToUserId = null;
            inReplyToScreenName = null;
        }
        if (quotedStatus != null) {
            isQuoteStatus = true;
            // Set repost media to null if identical to original
            if (photo != null && photo.equals(quotedStatus.photo)) {
                photo = null;
            }
        }
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

    @ParcelablePlease
    @JsonObject
    public static class ExtendedTweet implements Parcelable {

        @JsonField(name = "full_text")
        String fullText;

        @JsonField(name = "entities")
        Entities entities;

        @JsonField(name = "extended_entities")
        Entities extendedEntities;

        @JsonField(name = "display_text_range")
        int[] displayTextRange;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Status$ExtendedTweetParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<ExtendedTweet> CREATOR = new Creator<ExtendedTweet>() {
            public ExtendedTweet createFromParcel(Parcel source) {
                ExtendedTweet target = new ExtendedTweet();
                Status$ExtendedTweetParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public ExtendedTweet[] newArray(int size) {
                return new ExtendedTweet[size];
            }
        };
    }
}

/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.mariotaku.library.objectcursor.annotation.AfterCursorObjectCreated;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.api.twitter.model.Place;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.model.util.LoganSquareCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.util.HtmlEscapeHelper;
import org.mariotaku.twidere.util.TwitterContentUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.Date;

@CursorObject(valuesCreator = true)
@JsonObject
@ParcelablePlease
public class ParcelableStatus implements Parcelable, Comparable<ParcelableStatus>, Cloneable {
    @ParcelableThisPlease
    @JsonField(name = "id")
    @CursorField(Statuses.STATUS_ID)
    public long id;
    public static final Comparator<ParcelableStatus> REVERSE_ID_COMPARATOR = new Comparator<ParcelableStatus>() {

        @Override
        public int compare(final ParcelableStatus object1, final ParcelableStatus object2) {
            final long diff = object1.id - object2.id;
            if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
            if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
            return (int) diff;
        }
    };
    @ParcelableThisPlease
    @JsonField(name = "account_id")
    @CursorField(Statuses.ACCOUNT_ID)
    public long account_id;
    @ParcelableThisPlease
    @JsonField(name = "timestamp")
    @CursorField(Statuses.STATUS_TIMESTAMP)
    public long timestamp;
    public static final Comparator<ParcelableStatus> TIMESTAMP_COMPARATOR = new Comparator<ParcelableStatus>() {

        @Override
        public int compare(final ParcelableStatus object1, final ParcelableStatus object2) {
            final long diff = object2.timestamp - object1.timestamp;
            if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
            if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
            return (int) diff;
        }
    };
    @ParcelableThisPlease
    @JsonField(name = "user_id")
    @CursorField(Statuses.USER_ID)
    public long user_id = -1;
    @ParcelableThisPlease
    @JsonField(name = "retweet_id")
    @CursorField(Statuses.RETWEET_ID)
    public long retweet_id = -1;
    @ParcelableThisPlease
    @JsonField(name = "retweeted_by_user_id")
    @CursorField(Statuses.RETWEETED_BY_USER_ID)
    public long retweeted_by_user_id = -1;
    @ParcelableThisPlease
    @JsonField(name = "retweet_timestamp")
    @CursorField(Statuses.RETWEET_TIMESTAMP)
    public long retweet_timestamp = -1;
    @ParcelableThisPlease
    @JsonField(name = "retweet_count")
    @CursorField(Statuses.RETWEET_COUNT)
    public long retweet_count;
    @ParcelableThisPlease
    @JsonField(name = "favorite_count")
    @CursorField(Statuses.FAVORITE_COUNT)
    public long favorite_count;
    @ParcelableThisPlease
    @JsonField(name = "reply_count")
    @CursorField(Statuses.REPLY_COUNT)
    public long reply_count;
    @ParcelableThisPlease
    @JsonField(name = "in_reply_to_status_id")
    @CursorField(Statuses.IN_REPLY_TO_STATUS_ID)
    public long in_reply_to_status_id;
    @ParcelableThisPlease
    @JsonField(name = "in_reply_to_user_id")
    @CursorField(Statuses.IN_REPLY_TO_USER_ID)
    public long in_reply_to_user_id;
    @ParcelableThisPlease
    @JsonField(name = "my_retweet_id")
    @CursorField(Statuses.MY_RETWEET_ID)
    public long my_retweet_id;
    @ParcelableThisPlease
    @JsonField(name = "quoted_id")
    @CursorField(Statuses.QUOTED_ID)
    public long quoted_id;
    @ParcelableThisPlease
    @JsonField(name = "quoted_timestamp")
    @CursorField(Statuses.QUOTED_TIMESTAMP)
    public long quoted_timestamp;
    @ParcelableThisPlease
    @JsonField(name = "quoted_user_id")
    @CursorField(Statuses.QUOTED_USER_ID)
    public long quoted_user_id;
    @ParcelableThisPlease
    @JsonField(name = "is_gap")
    @CursorField(Statuses.IS_GAP)
    public boolean is_gap;
    @ParcelableThisPlease
    @JsonField(name = "is_retweet")
    @CursorField(Statuses.IS_RETWEET)
    public boolean is_retweet;
    @ParcelableThisPlease
    @JsonField(name = "retweeted")
    @CursorField(Statuses.RETWEETED)
    public boolean retweeted;
    @ParcelableThisPlease
    @JsonField(name = "is_favorite")
    @CursorField(Statuses.IS_FAVORITE)
    public boolean is_favorite;
    @ParcelableThisPlease
    @JsonField(name = "is_possibly_sensitive")
    @CursorField(Statuses.IS_POSSIBLY_SENSITIVE)
    public boolean is_possibly_sensitive;
    @ParcelableThisPlease
    @JsonField(name = "user_is_following")
    @CursorField(Statuses.IS_FOLLOWING)
    public boolean user_is_following;
    @ParcelableThisPlease
    @JsonField(name = "user_is_protected")
    @CursorField(Statuses.IS_PROTECTED)
    public boolean user_is_protected;
    @ParcelableThisPlease
    @JsonField(name = "user_is_verified")
    @CursorField(Statuses.IS_VERIFIED)
    public boolean user_is_verified;
    @ParcelableThisPlease
    @JsonField(name = "is_quote")
    @CursorField(Statuses.IS_QUOTE)
    public boolean is_quote;
    @ParcelableThisPlease
    @JsonField(name = "quoted_user_is_protected")
    @CursorField(Statuses.QUOTED_USER_IS_PROTECTED)
    public boolean quoted_user_is_protected;
    @ParcelableThisPlease
    @JsonField(name = "quoted_user_is_verified")
    @CursorField(Statuses.QUOTED_USER_IS_VERIFIED)
    public boolean quoted_user_is_verified;
    @ParcelableThisPlease
    @JsonField(name = "retweeted_by_user_name")
    @CursorField(Statuses.RETWEETED_BY_USER_NAME)
    public String retweeted_by_user_name;
    @ParcelableThisPlease
    @JsonField(name = "retweeted_by_user_screen_name")
    @CursorField(Statuses.RETWEETED_BY_USER_SCREEN_NAME)
    public String retweeted_by_user_screen_name;
    @ParcelableThisPlease
    @JsonField(name = "retweeted_by_user_profile_image")
    @CursorField(Statuses.RETWEETED_BY_USER_PROFILE_IMAGE)
    public String retweeted_by_user_profile_image;
    @ParcelableThisPlease
    @JsonField(name = "text_html")
    @CursorField(Statuses.TEXT_HTML)
    public String text_html;
    @ParcelableThisPlease
    @JsonField(name = "text_plain")
    @CursorField(Statuses.TEXT_PLAIN)
    public String text_plain;
    @ParcelableThisPlease
    @JsonField(name = "lang")
    @CursorField(Statuses.LANG)
    public String lang;
    @ParcelableThisPlease
    @JsonField(name = "user_name")
    @CursorField(Statuses.USER_NAME)
    public String user_name;
    @ParcelableThisPlease
    @JsonField(name = "user_screen_name")
    @CursorField(Statuses.USER_SCREEN_NAME)
    public String user_screen_name;
    @ParcelableThisPlease
    @JsonField(name = "in_reply_to_name")
    @CursorField(Statuses.IN_REPLY_TO_USER_NAME)
    public String in_reply_to_name;
    @ParcelableThisPlease
    @JsonField(name = "in_reply_to_screen_name")
    @CursorField(Statuses.IN_REPLY_TO_USER_SCREEN_NAME)
    public String in_reply_to_screen_name;
    @ParcelableThisPlease
    @JsonField(name = "source")
    @CursorField(Statuses.SOURCE)
    public String source;
    @ParcelableThisPlease
    @JsonField(name = "user_profile_image_url")
    @CursorField(Statuses.USER_PROFILE_IMAGE_URL)
    public String user_profile_image_url;
    @ParcelableThisPlease
    @JsonField(name = "text_unescaped")
    @CursorField(Statuses.TEXT_UNESCAPED)
    public String text_unescaped;
    @Nullable
    @ParcelableThisPlease
    @JsonField(name = "card_name")
    @CursorField(Statuses.CARD_NAME)
    public String card_name;
    @ParcelableThisPlease
    @JsonField(name = "quoted_text_html")
    @CursorField(Statuses.QUOTED_TEXT_HTML)
    public String quoted_text_html;
    @ParcelableThisPlease
    @JsonField(name = "quoted_text_plain")
    @CursorField(Statuses.QUOTED_TEXT_PLAIN)
    public String quoted_text_plain;
    @ParcelableThisPlease
    @JsonField(name = "quoted_text_unescaped")
    @CursorField(Statuses.QUOTED_TEXT_UNESCAPED)
    public String quoted_text_unescaped;
    @ParcelableThisPlease
    @JsonField(name = "quoted_source")
    @CursorField(Statuses.QUOTED_SOURCE)
    public String quoted_source;
    @ParcelableThisPlease
    @JsonField(name = "quoted_user_name")
    @CursorField(Statuses.QUOTED_USER_NAME)
    public String quoted_user_name;
    @ParcelableThisPlease
    @JsonField(name = "quoted_user_screen_name")
    @CursorField(Statuses.QUOTED_USER_SCREEN_NAME)
    public String quoted_user_screen_name;
    @ParcelableThisPlease
    @JsonField(name = "quoted_user_profile_image")
    @CursorField(Statuses.QUOTED_USER_PROFILE_IMAGE)
    public String quoted_user_profile_image;
    @ParcelableThisPlease
    @JsonField(name = "quoted_location")
    @CursorField(value = Statuses.LOCATION, converter = ParcelableLocation.Converter.class)
    public ParcelableLocation quoted_location;
    @ParcelableThisPlease
    @JsonField(name = "quoted_place_full_name")
    @CursorField(value = Statuses.PLACE_FULL_NAME, converter = LoganSquareCursorFieldConverter.class)
    public String quoted_place_full_name;
    @ParcelableThisPlease
    @JsonField(name = "location")
    @CursorField(value = Statuses.LOCATION, converter = ParcelableLocation.Converter.class)
    public ParcelableLocation location;
    @ParcelableThisPlease
    @JsonField(name = "place_full_name")
    @CursorField(value = Statuses.PLACE_FULL_NAME, converter = LoganSquareCursorFieldConverter.class)
    public String place_full_name;
    @ParcelableThisPlease
    @JsonField(name = "mentions")
    @CursorField(value = Statuses.MENTIONS_JSON, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableUserMention[] mentions;
    @ParcelableThisPlease
    @JsonField(name = "media")
    @CursorField(value = Statuses.MEDIA_JSON, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableMedia[] media;
    @ParcelableThisPlease
    @JsonField(name = "quoted_media")
    @CursorField(value = Statuses.QUOTED_MEDIA_JSON, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableMedia[] quoted_media;
    @Nullable
    @ParcelableThisPlease
    @JsonField(name = "card")
    @CursorField(value = Statuses.CARD, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableCardEntity card;

    @CursorField(value = Statuses._ID, excludeWrite = true)
    long _id;
    public static final Creator<ParcelableStatus> CREATOR = new Creator<ParcelableStatus>() {
        public ParcelableStatus createFromParcel(Parcel source) {
            ParcelableStatus target = new ParcelableStatus();
            ParcelableStatusParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ParcelableStatus[] newArray(int size) {
            return new ParcelableStatus[size];
        }
    };


    ParcelableStatus() {
    }

    public ParcelableStatus(final Status orig, final long account_id, final boolean is_gap) {
        this.is_gap = is_gap;
        this.account_id = account_id;
        id = orig.getId();
        timestamp = getTime(orig.getCreatedAt());

        final Status retweetedStatus = orig.getRetweetedStatus();
        final User retweetUser = retweetedStatus != null ? orig.getUser() : null;
        is_retweet = orig.isRetweet();
        retweeted = orig.wasRetweeted();
        if (retweetedStatus != null) {
            retweet_id = retweetedStatus.getId();
            retweet_timestamp = getTime(retweetedStatus.getCreatedAt());
            retweeted_by_user_id = retweetUser.getId();
            retweeted_by_user_name = retweetUser.getName();
            retweeted_by_user_screen_name = retweetUser.getScreenName();
            retweeted_by_user_profile_image = TwitterContentUtils.getProfileImageUrl(retweetUser);
        }

        final Status quoted = orig.getQuotedStatus();
        is_quote = orig.isQuote();
        if (quoted != null) {
            final User quoted_user = quoted.getUser();
            quoted_id = quoted.getId();
            quoted_text_html = TwitterContentUtils.formatStatusText(quoted);
            quoted_text_plain = TwitterContentUtils.unescapeTwitterStatusText(quoted.getText());
            quoted_text_unescaped = HtmlEscapeHelper.toPlainText(quoted_text_html);
            quoted_timestamp = quoted.getCreatedAt().getTime();
            quoted_source = quoted.getSource();
            quoted_media = ParcelableMedia.fromStatus(quoted);
            quoted_location = ParcelableLocation.fromGeoLocation(quoted.getGeoLocation());
            quoted_place_full_name = getPlaceFullName(quoted.getPlace());

            quoted_user_id = quoted_user.getId();
            quoted_user_name = quoted_user.getName();
            quoted_user_screen_name = quoted_user.getScreenName();
            quoted_user_profile_image = TwitterContentUtils.getProfileImageUrl(quoted_user);
            quoted_user_is_protected = quoted_user.isProtected();
            quoted_user_is_verified = quoted_user.isVerified();
        }

        final Status status;
        if (retweetedStatus != null) {
            status = retweetedStatus;
            reply_count = retweetedStatus.getReplyCount();
            retweet_count = retweetedStatus.getRetweetCount();
            favorite_count = retweetedStatus.getFavoriteCount();


            in_reply_to_name = TwitterContentUtils.getInReplyToName(retweetedStatus);
            in_reply_to_screen_name = retweetedStatus.getInReplyToScreenName();
            in_reply_to_status_id = retweetedStatus.getInReplyToStatusId();
            in_reply_to_user_id = retweetedStatus.getInReplyToUserId();
        } else {
            status = orig;
            reply_count = orig.getReplyCount();
            retweet_count = orig.getRetweetCount();
            favorite_count = orig.getFavoriteCount();

            in_reply_to_name = TwitterContentUtils.getInReplyToName(orig);
            in_reply_to_screen_name = orig.getInReplyToScreenName();
            in_reply_to_status_id = orig.getInReplyToStatusId();
            in_reply_to_user_id = orig.getInReplyToUserId();
        }

        final User user = status.getUser();
        user_id = user.getId();
        user_name = user.getName();
        user_screen_name = user.getScreenName();
        user_profile_image_url = TwitterContentUtils.getProfileImageUrl(user);
        user_is_protected = user.isProtected();
        user_is_verified = user.isVerified();
        user_is_following = user.isFollowing();
        text_html = TwitterContentUtils.formatStatusText(status);
        media = ParcelableMedia.fromStatus(status);
        text_plain = TwitterContentUtils.unescapeTwitterStatusText(status.getText());
        source = status.getSource();
        location = ParcelableLocation.fromGeoLocation(status.getGeoLocation());
        is_favorite = status.isFavorited();
        text_unescaped = HtmlEscapeHelper.toPlainText(text_html);
        my_retweet_id = retweeted_by_user_id == account_id ? id : status.getCurrentUserRetweet();
        is_possibly_sensitive = status.isPossiblySensitive();
        mentions = ParcelableUserMention.fromUserMentionEntities(status.getUserMentionEntities());
        card = ParcelableCardEntity.fromCardEntity(status.getCard(), account_id);
        place_full_name = getPlaceFullName(status.getPlace());
        card_name = card != null ? card.name : null;
        lang = status.getLang();
    }

    @Nullable
    private static String getPlaceFullName(@Nullable Place place) {
        if (place == null) return null;
        return place.getFullName();
    }

    private static long getTime(final Date date) {
        return date != null ? date.getTime() : 0;
    }

    public static ParcelableStatus[] fromStatuses(Status[] statuses, long accountId) {
        if (statuses == null) return null;
        int size = statuses.length;
        final ParcelableStatus[] result = new ParcelableStatus[size];
        for (int i = 0; i < size; i++) {
            result[i] = new ParcelableStatus(statuses[i], accountId, false);
        }
        return result;
    }

    @AfterCursorObjectCreated
    void finishCursorObjectCreation() {
        card_name = card != null ? card.name : null;
    }

    @Override
    public int compareTo(@NonNull final ParcelableStatus another) {
        final long diff = another.id - id;
        if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) diff;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof ParcelableStatus)) return false;
        final ParcelableStatus other = (ParcelableStatus) obj;
        return account_id == other.account_id && id == other.id;
    }

    @Override
    public int hashCode() {
        return calculateHashCode(account_id, id);
    }

    public static int calculateHashCode(long account_id, long id) {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (account_id ^ account_id >>> 32);
        result = prime * result + (int) (id ^ id >>> 32);
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("account_id", account_id)
                .append("timestamp", timestamp)
                .append("user_id", user_id)
                .append("retweet_id", retweet_id)
                .append("retweeted_by_user_id", retweeted_by_user_id)
                .append("retweet_timestamp", retweet_timestamp)
                .append("retweet_count", retweet_count)
                .append("favorite_count", favorite_count)
                .append("reply_count", reply_count)
                .append("in_reply_to_status_id", in_reply_to_status_id)
                .append("in_reply_to_user_id", in_reply_to_user_id)
                .append("my_retweet_id", my_retweet_id)
                .append("quoted_id", quoted_id)
                .append("quoted_timestamp", quoted_timestamp)
                .append("quoted_user_id", quoted_user_id)
                .append("is_gap", is_gap)
                .append("is_retweet", is_retweet)
                .append("retweeted", retweeted)
                .append("is_favorite", is_favorite)
                .append("is_possibly_sensitive", is_possibly_sensitive)
                .append("user_is_following", user_is_following)
                .append("user_is_protected", user_is_protected)
                .append("user_is_verified", user_is_verified)
                .append("is_quote", is_quote)
                .append("quoted_user_is_protected", quoted_user_is_protected)
                .append("quoted_user_is_verified", quoted_user_is_verified)
                .append("retweeted_by_user_name", retweeted_by_user_name)
                .append("retweeted_by_user_screen_name", retweeted_by_user_screen_name)
                .append("retweeted_by_user_profile_image", retweeted_by_user_profile_image)
                .append("text_html", text_html)
                .append("text_plain", text_plain)
                .append("lang", lang)
                .append("user_name", user_name)
                .append("user_screen_name", user_screen_name)
                .append("in_reply_to_name", in_reply_to_name)
                .append("in_reply_to_screen_name", in_reply_to_screen_name)
                .append("source", source)
                .append("user_profile_image_url", user_profile_image_url)
                .append("text_unescaped", text_unescaped)
                .append("card_name", card_name)
                .append("quoted_text_html", quoted_text_html)
                .append("quoted_text_plain", quoted_text_plain)
                .append("quoted_text_unescaped", quoted_text_unescaped)
                .append("quoted_source", quoted_source)
                .append("quoted_user_name", quoted_user_name)
                .append("quoted_user_screen_name", quoted_user_screen_name)
                .append("quoted_user_profile_image", quoted_user_profile_image)
                .append("quoted_location", quoted_location)
                .append("quoted_place_full_name", quoted_place_full_name)
                .append("location", location)
                .append("place_full_name", place_full_name)
                .append("mentions", mentions)
                .append("media", media)
                .append("quoted_media", quoted_media)
                .append("card", card)
                .append("_id", _id)
                .toString();
    }

    @OnJsonParseComplete
    void onParseComplete() throws IOException {
        if (is_quote && TextUtils.isEmpty(quoted_text_html))
            throw new IOException("Incompatible model");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableStatusParcelablePlease.writeToParcel(this, dest, flags);
    }

}

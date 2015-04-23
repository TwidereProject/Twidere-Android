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

import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.util.HtmlEscapeHelper;
import org.mariotaku.twidere.util.TwitterContentUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import twitter4j.CardEntity;
import twitter4j.CardEntity.BindingValue;
import twitter4j.CardEntity.BooleanValue;
import twitter4j.CardEntity.ImageValue;
import twitter4j.CardEntity.StringValue;
import twitter4j.CardEntity.UserValue;
import twitter4j.Place;
import twitter4j.Status;
import twitter4j.User;

@SuppressWarnings("unused")
@JsonObject
public class ParcelableStatus implements Parcelable, Comparable<ParcelableStatus> {

    public static final Parcelable.Creator<ParcelableStatus> CREATOR = new Parcelable.Creator<ParcelableStatus>() {
        @Override
        public ParcelableStatus createFromParcel(final Parcel in) {
            return new ParcelableStatus(in);
        }

        @Override
        public ParcelableStatus[] newArray(final int size) {
            return new ParcelableStatus[size];
        }
    };
    @JsonField(name = "id")
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
    @JsonField(name = "account_id")
    public long account_id;
    @JsonField(name = "timestamp")
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
    @JsonField(name = "user_id")
    public long user_id;
    @JsonField(name = "retweet_id")
    public long retweet_id;
    @JsonField(name = "retweeted_by_id")
    public long retweeted_by_id;
    @JsonField(name = "retweet_timestamp")
    public long retweet_timestamp;
    @JsonField(name = "retweet_count")
    public long retweet_count;
    @JsonField(name = "favorite_count")
    public long favorite_count;
    @JsonField(name = "reply_count")
    public long reply_count;
    @JsonField(name = "descendent_reply_count")
    public long descendent_reply_count;
    @JsonField(name = "in_reply_to_status_id")
    public long in_reply_to_status_id;
    @JsonField(name = "in_reply_to_user_id")
    public long in_reply_to_user_id;
    @JsonField(name = "my_retweet_id")
    public long my_retweet_id;
    @JsonField(name = "quote_id")
    public long quote_id;
    @JsonField(name = "quote_timestamp")
    public long quote_timestamp;
    @JsonField(name = "quoted_by_user_id")
    public long quoted_by_user_id;

    @JsonField(name = "is_gap")
    public boolean is_gap;
    @JsonField(name = "is_retweet")
    public boolean is_retweet;
    @JsonField(name = "is_favorite")
    public boolean is_favorite;
    @JsonField(name = "is_possibly_sensitive")
    public boolean is_possibly_sensitive;
    @JsonField(name = "user_is_following")
    public boolean user_is_following;
    @JsonField(name = "user_is_protected")
    public boolean user_is_protected;
    @JsonField(name = "user_is_verified")
    public boolean user_is_verified;
    @JsonField(name = "is_quote")
    public boolean is_quote;
    @JsonField(name = "quoted_by_user_is_protected")
    public boolean quoted_by_user_is_protected;
    @JsonField(name = "quoted_by_user_is_verified")
    public boolean quoted_by_user_is_verified;

    @JsonField(name = "retweeted_by_name")
    public String retweeted_by_name;
    @JsonField(name = "retweeted_by_screen_name")
    public String retweeted_by_screen_name;
    @JsonField(name = "retweeted_by_profile_image")
    public String retweeted_by_profile_image;
    @JsonField(name = "text_html")
    public String text_html;
    @JsonField(name = "text_plain")
    public String text_plain;
    @JsonField(name = "user_name")
    public String user_name;
    @JsonField(name = "user_screen_name")
    public String user_screen_name;
    @JsonField(name = "in_reply_to_name")
    public String in_reply_to_name;
    @JsonField(name = "in_reply_to_screen_name")
    public String in_reply_to_screen_name;
    @JsonField(name = "source")
    public String source;
    @JsonField(name = "user_profile_image_url")
    public String user_profile_image_url;
    @JsonField(name = "text_unescaped")
    public String text_unescaped;
    @JsonField(name = "card_name")
    public String card_name;
    @JsonField(name = "quote_text_html")
    public String quote_text_html;
    @JsonField(name = "quote_text_plain")
    public String quote_text_plain;
    @JsonField(name = "quote_text_unescaped")
    public String quote_text_unescaped;
    @JsonField(name = "quote_source")
    public String quote_source;
    @JsonField(name = "quoted_by_user_name")
    public String quoted_by_user_name;
    @JsonField(name = "quoted_by_user_screen_name")
    public String quoted_by_user_screen_name;
    @JsonField(name = "quoted_by_user_profile_image")
    public String quoted_by_user_profile_image;

    @JsonField(name = "location")
    public ParcelableLocation location;

    @JsonField(name = "place_full_name")
    public String place_full_name;

    @JsonField(name = "mentions")
    public ParcelableUserMention[] mentions;

    @JsonField(name = "media")
    public ParcelableMedia[] media;
    @JsonField(name = "quote_media")
    public ParcelableMedia[] quote_media;

    @JsonField(name = "card")
    public ParcelableCardEntity card;

    public ParcelableStatus(final Cursor c, final CursorIndices idx) {
        id = idx.status_id != -1 ? c.getLong(idx.status_id) : -1;
        account_id = idx.account_id != -1 ? c.getLong(idx.account_id) : -1;
        timestamp = idx.status_timestamp != -1 ? c.getLong(idx.status_timestamp) : 0;
        user_id = idx.user_id != -1 ? c.getLong(idx.user_id) : -1;
        retweet_id = idx.retweet_id != -1 ? c.getLong(idx.retweet_id) : -1;
        retweet_timestamp = idx.retweet_timestamp != -1 ? c.getLong(idx.retweet_timestamp) : -1;
        retweeted_by_id = idx.retweeted_by_user_id != -1 ? c.getLong(idx.retweeted_by_user_id) : -1;
        retweet_count = idx.retweet_count != -1 ? c.getLong(idx.retweet_count) : -1;
        favorite_count = idx.favorite_count != -1 ? c.getLong(idx.favorite_count) : -1;
        reply_count = idx.reply_count != -1 ? c.getLong(idx.reply_count) : -1;
        descendent_reply_count = idx.descendent_reply_count != -1 ? c.getLong(idx.descendent_reply_count) : -1;
        in_reply_to_status_id = idx.in_reply_to_status_id != -1 ? c.getLong(idx.in_reply_to_status_id) : -1;
        in_reply_to_user_id = idx.in_reply_to_user_id != -1 ? c.getLong(idx.in_reply_to_user_id) : -1;
        is_gap = idx.is_gap != -1 && c.getInt(idx.is_gap) == 1;
        is_retweet = idx.is_retweet != -1 && c.getInt(idx.is_retweet) == 1;
        is_favorite = idx.is_favorite != -1 && c.getInt(idx.is_favorite) == 1;
        user_is_protected = idx.is_protected != -1 && c.getInt(idx.is_protected) == 1;
        user_is_verified = idx.is_verified != -1 && c.getInt(idx.is_verified) == 1;
        retweeted_by_name = idx.retweeted_by_user_name != -1 ? c.getString(idx.retweeted_by_user_name) : null;
        retweeted_by_screen_name = idx.retweeted_by_user_screen_name != -1 ? c
                .getString(idx.retweeted_by_user_screen_name) : null;
        retweeted_by_profile_image = idx.retweeted_by_user_profile_image != -1 ? c
                .getString(idx.retweeted_by_user_profile_image) : null;
        text_html = idx.text_html != -1 ? c.getString(idx.text_html) : null;
        media = idx.media != -1 ? ParcelableMedia.fromSerializedJson(c.getString(idx.media)) : null;
        quote_media = idx.quote_media != -1 ? ParcelableMedia.fromSerializedJson(c.getString(idx.quote_media)) : null;
        text_plain = idx.text_plain != -1 ? c.getString(idx.text_plain) : null;
        user_name = idx.user_name != -1 ? c.getString(idx.user_name) : null;
        user_screen_name = idx.user_screen_name != -1 ? c.getString(idx.user_screen_name) : null;
        in_reply_to_name = idx.in_reply_to_user_name != -1 ? c.getString(idx.in_reply_to_user_name) : null;
        in_reply_to_screen_name = idx.in_reply_to_user_screen_name != -1 ? c
                .getString(idx.in_reply_to_user_screen_name) : null;
        source = idx.source != -1 ? c.getString(idx.source) : null;
        location = idx.location != -1 ? new ParcelableLocation(c.getString(idx.location)) : null;
        user_profile_image_url = idx.user_profile_image_url != -1 ? c.getString(idx.user_profile_image_url) : null;
        text_unescaped = idx.text_unescaped != -1 ? c.getString(idx.text_unescaped) : null;
        my_retweet_id = idx.my_retweet_id != -1 ? c.getLong(idx.my_retweet_id) : -1;
        is_possibly_sensitive = idx.is_possibly_sensitive != -1 && c.getInt(idx.is_possibly_sensitive) == 1;
        user_is_following = idx.is_following != -1 && c.getInt(idx.is_following) == 1;
        mentions = idx.mentions != -1 ? ParcelableUserMention.fromSerializedJson(c.getString(idx.mentions)) : null;
        card = idx.card != -1 ? ParcelableCardEntity.fromJSONString(c.getString(idx.card)) : null;
        place_full_name = idx.place_full_name != -1 ? c.getString(idx.place_full_name) : null;
        is_quote = idx.is_quote != -1 && c.getShort(idx.is_quote) == 1;
        quote_id = idx.quote_id != -1 ? c.getLong(idx.quote_id) : -1;
        quote_timestamp = idx.quote_timestamp != -1 ? c.getLong(idx.quote_timestamp) : -1;
        quoted_by_user_id = idx.quoted_by_user_id != -1 ? c.getLong(idx.quoted_by_user_id) : -1;
        quote_text_html = idx.quote_text_html != -1 ? c.getString(idx.quote_text_html) : null;
        quote_text_plain = idx.quote_text_plain != -1 ? c.getString(idx.quote_text_plain) : null;
        quote_text_unescaped = idx.quote_text_unescaped != -1 ? c.getString(idx.quote_text_unescaped) : null;
        quoted_by_user_name = idx.quoted_by_user_name != -1 ? c.getString(idx.quoted_by_user_name) : null;
        quoted_by_user_screen_name = idx.quoted_by_user_screen_name != -1 ? c.getString(idx.quoted_by_user_screen_name) : null;
        quoted_by_user_profile_image = idx.quoted_by_user_profile_image != -1 ? c.getString(idx.quoted_by_user_profile_image) : null;
        quoted_by_user_is_protected = idx.quoted_by_user_is_protected != -1 && c.getShort(idx.quoted_by_user_is_protected) == 1;
        quoted_by_user_is_verified = idx.quoted_by_user_is_verified != -1 && c.getShort(idx.quoted_by_user_is_verified) == 1;
        quote_source = idx.quote_source != -1 ? c.getString(idx.quote_source) : null;
        card_name = card != null ? card.name : null;
    }

    public ParcelableStatus() {
    }

    public ParcelableStatus(final Parcel in) {
        id = in.readLong();
        account_id = in.readLong();
        timestamp = in.readLong();
        user_id = in.readLong();
        retweet_id = in.readLong();
        retweet_timestamp = in.readLong();
        retweeted_by_id = in.readLong();
        retweet_count = in.readLong();
        favorite_count = in.readLong();
        reply_count = in.readLong();
        descendent_reply_count = in.readLong();
        in_reply_to_status_id = in.readLong();
        is_gap = in.readByte() == 1;
        is_retweet = in.readByte() == 1;
        is_favorite = in.readByte() == 1;
        user_is_protected = in.readByte() == 1;
        user_is_verified = in.readByte() == 1;
        retweeted_by_name = in.readString();
        retweeted_by_screen_name = in.readString();
        retweeted_by_profile_image = in.readString();
        text_html = in.readString();
        text_plain = in.readString();
        user_name = in.readString();
        user_screen_name = in.readString();
        in_reply_to_screen_name = in.readString();
        source = in.readString();
        user_profile_image_url = in.readString();
        media = in.createTypedArray(ParcelableMedia.CREATOR);
        quote_media = in.createTypedArray(ParcelableMedia.CREATOR);
        location = in.readParcelable(ParcelableLocation.class.getClassLoader());
        my_retweet_id = in.readLong();
        is_possibly_sensitive = in.readByte() == 1;
        user_is_following = in.readByte() == 1;
        text_unescaped = in.readString();
        in_reply_to_user_id = in.readLong();
        in_reply_to_name = in.readString();
        mentions = in.createTypedArray(ParcelableUserMention.CREATOR);
        card = in.readParcelable(ParcelableCardEntity.class.getClassLoader());
        place_full_name = in.readString();
        is_quote = in.readByte() == 1;
        quote_id = in.readLong();
        quote_text_html = in.readString();
        quote_text_plain = in.readString();
        quote_text_unescaped = in.readString();
        quote_timestamp = in.readLong();
        quoted_by_user_id = in.readLong();
        quoted_by_user_name = in.readString();
        quoted_by_user_screen_name = in.readString();
        quoted_by_user_profile_image = in.readString();
        quoted_by_user_is_protected = in.readByte() == 1;
        quoted_by_user_is_verified = in.readByte() == 1;
        quote_source = in.readString();
        card_name = card != null ? card.name : null;
    }

    public ParcelableStatus(final ParcelableStatus orig, final long override_my_retweet_id,
                            final long override_retweet_count) {
        id = orig.id;
        account_id = orig.account_id;
        timestamp = orig.timestamp;
        user_id = orig.user_id;
        retweet_id = orig.retweet_id;
        retweet_timestamp = orig.retweet_timestamp;
        retweeted_by_id = orig.retweeted_by_id;
        retweet_count = override_retweet_count;
        favorite_count = orig.favorite_count;
        reply_count = orig.reply_count;
        descendent_reply_count = orig.descendent_reply_count;
        in_reply_to_status_id = orig.in_reply_to_status_id;
        is_gap = orig.is_gap;
        is_retweet = orig.is_retweet;
        is_favorite = orig.is_favorite;
        user_is_protected = orig.user_is_protected;
        user_is_verified = orig.user_is_verified;
        retweeted_by_name = orig.retweeted_by_name;
        retweeted_by_screen_name = orig.retweeted_by_screen_name;
        retweeted_by_profile_image = orig.retweeted_by_profile_image;
        text_html = orig.text_html;
        text_plain = orig.text_plain;
        user_name = orig.user_name;
        user_screen_name = orig.user_screen_name;
        in_reply_to_screen_name = orig.in_reply_to_screen_name;
        source = orig.source;
        user_profile_image_url = orig.user_profile_image_url;
        media = orig.media;
        quote_media = orig.quote_media;
        location = orig.location;
        my_retweet_id = override_my_retweet_id;
        is_possibly_sensitive = orig.is_possibly_sensitive;
        user_is_following = orig.user_is_following;
        text_unescaped = orig.text_unescaped;
        in_reply_to_user_id = orig.in_reply_to_user_id;
        in_reply_to_name = orig.in_reply_to_name;
        mentions = orig.mentions;
        card = orig.card;
        place_full_name = orig.place_full_name;
        is_quote = orig.is_quote;
        quote_id = orig.quote_id;
        quote_timestamp = orig.quote_timestamp;
        quoted_by_user_id = orig.quoted_by_user_id;
        quoted_by_user_name = orig.quoted_by_user_name;
        quoted_by_user_screen_name = orig.quoted_by_user_screen_name;
        quoted_by_user_profile_image = orig.quoted_by_user_profile_image;
        quote_text_html = orig.quote_text_html;
        quote_text_plain = orig.quote_text_plain;
        quote_text_unescaped = orig.quote_text_unescaped;
        quote_source = orig.quote_source;
        quoted_by_user_is_protected = orig.quoted_by_user_is_protected;
        quoted_by_user_is_verified = orig.quoted_by_user_is_verified;
        card_name = card != null ? card.name : null;
    }

    public ParcelableStatus(final Status orig, final long account_id, final boolean is_gap) {
        this.is_gap = is_gap;
        this.account_id = account_id;
        id = orig.getId();
        timestamp = getTime(orig.getCreatedAt());

        final Status retweeted = orig.getRetweetedStatus();
        final User retweet_user = retweeted != null ? orig.getUser() : null;
        is_retweet = orig.isRetweet();
        retweet_id = retweeted != null ? retweeted.getId() : -1;
        retweet_timestamp = retweeted != null ? getTime(retweeted.getCreatedAt()) : -1;
        retweeted_by_id = retweet_user != null ? retweet_user.getId() : -1;
        retweeted_by_name = retweet_user != null ? retweet_user.getName() : null;
        retweeted_by_screen_name = retweet_user != null ? retweet_user.getScreenName() : null;
        retweeted_by_profile_image = retweet_user != null ? retweet_user.getProfileImageUrlHttps() : null;

        final Status quoted = orig.getQuotedStatus();
        final User quote_user = quoted != null ? orig.getUser() : null;
        is_quote = orig.isQuote();
        quote_id = quoted != null ? quoted.getId() : -1;
        quote_text_html = TwitterContentUtils.formatStatusText(orig);
        quote_text_plain = orig.getText();
        quote_text_unescaped = HtmlEscapeHelper.toPlainText(quote_text_html);
        quote_timestamp = orig.getCreatedAt().getTime();
        quote_source = orig.getSource();

        quoted_by_user_id = quote_user != null ? quote_user.getId() : -1;
        quoted_by_user_name = quote_user != null ? quote_user.getName() : null;
        quoted_by_user_screen_name = quote_user != null ? quote_user.getScreenName() : null;
        quoted_by_user_profile_image = quote_user != null ? quote_user.getProfileImageUrlHttps() : null;
        quoted_by_user_is_protected = quote_user != null && quote_user.isProtected();
        quoted_by_user_is_verified = quote_user != null && quote_user.isVerified();

        final Status status;
        if (quoted != null) {
            status = quoted;
        } else if (retweeted != null) {
            status = retweeted;
        } else {
            status = orig;
        }
        final User user = status.getUser();
        user_id = user.getId();
        user_name = user.getName();
        user_screen_name = user.getScreenName();
        user_profile_image_url = user.getProfileImageUrlHttps();
        user_is_protected = user.isProtected();
        user_is_verified = user.isVerified();
        user_is_following = user.isFollowing();
        text_html = TwitterContentUtils.formatStatusText(status);
        media = ParcelableMedia.fromStatus(status);
        quote_media = quoted != null ? ParcelableMedia.fromStatus(orig) : null;
        text_plain = status.getText();
        retweet_count = status.getRetweetCount();
        favorite_count = status.getFavoriteCount();
        reply_count = status.getReplyCount();
        descendent_reply_count = status.getDescendentReplyCount();
        in_reply_to_name = TwitterContentUtils.getInReplyToName(retweeted != null ? retweeted : orig);
        in_reply_to_screen_name = (retweeted != null ? retweeted : orig).getInReplyToScreenName();
        in_reply_to_status_id = (retweeted != null ? retweeted : orig).getInReplyToStatusId();
        in_reply_to_user_id = (retweeted != null ? retweeted : orig).getInReplyToUserId();
        source = status.getSource();
        location = ParcelableLocation.fromGeoLocation(status.getGeoLocation());
        is_favorite = status.isFavorited();
        text_unescaped = HtmlEscapeHelper.toPlainText(text_html);
        my_retweet_id = retweeted_by_id == account_id ? id : status.getCurrentUserRetweet();
        is_possibly_sensitive = status.isPossiblySensitive();
        mentions = ParcelableUserMention.fromUserMentionEntities(status.getUserMentionEntities());
        card = ParcelableCardEntity.fromCardEntity(status.getCard(), account_id);
        place_full_name = getPlaceFullName(status.getPlace());
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
    public int describeContents() {
        return 0;
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
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (account_id ^ account_id >>> 32);
        result = prime * result + (int) (id ^ id >>> 32);
        return result;
    }

    @Override
    public String toString() {
        return "ParcelableStatus{" +
                "id=" + id +
                ", account_id=" + account_id +
                ", timestamp=" + timestamp +
                ", user_id=" + user_id +
                ", retweet_id=" + retweet_id +
                ", retweeted_by_id=" + retweeted_by_id +
                ", retweet_timestamp=" + retweet_timestamp +
                ", retweet_count=" + retweet_count +
                ", favorite_count=" + favorite_count +
                ", reply_count=" + reply_count +
                ", descendent_reply_count=" + descendent_reply_count +
                ", in_reply_to_status_id=" + in_reply_to_status_id +
                ", in_reply_to_user_id=" + in_reply_to_user_id +
                ", my_retweet_id=" + my_retweet_id +
                ", quote_id=" + quote_id +
                ", quote_timestamp=" + quote_timestamp +
                ", quoted_by_user_id=" + quoted_by_user_id +
                ", is_gap=" + is_gap +
                ", is_retweet=" + is_retweet +
                ", is_favorite=" + is_favorite +
                ", is_possibly_sensitive=" + is_possibly_sensitive +
                ", user_is_following=" + user_is_following +
                ", user_is_protected=" + user_is_protected +
                ", user_is_verified=" + user_is_verified +
                ", is_quote=" + is_quote +
                ", quoted_by_user_is_protected=" + quoted_by_user_is_protected +
                ", quoted_by_user_is_verified=" + quoted_by_user_is_verified +
                ", retweeted_by_name='" + retweeted_by_name + '\'' +
                ", retweeted_by_screen_name='" + retweeted_by_screen_name + '\'' +
                ", retweeted_by_profile_image='" + retweeted_by_profile_image + '\'' +
                ", text_html='" + text_html + '\'' +
                ", text_plain='" + text_plain + '\'' +
                ", user_name='" + user_name + '\'' +
                ", user_screen_name='" + user_screen_name + '\'' +
                ", in_reply_to_name='" + in_reply_to_name + '\'' +
                ", in_reply_to_screen_name='" + in_reply_to_screen_name + '\'' +
                ", source='" + source + '\'' +
                ", user_profile_image_url='" + user_profile_image_url + '\'' +
                ", text_unescaped='" + text_unescaped + '\'' +
                ", card_name='" + card_name + '\'' +
                ", quote_text_html='" + quote_text_html + '\'' +
                ", quote_text_plain='" + quote_text_plain + '\'' +
                ", quote_text_unescaped='" + quote_text_unescaped + '\'' +
                ", quote_source='" + quote_source + '\'' +
                ", quoted_by_user_name='" + quoted_by_user_name + '\'' +
                ", quoted_by_user_screen_name='" + quoted_by_user_screen_name + '\'' +
                ", quoted_by_user_profile_image='" + quoted_by_user_profile_image + '\'' +
                ", location=" + location +
                ", place_full_name='" + place_full_name + '\'' +
                ", mentions=" + Arrays.toString(mentions) +
                ", media=" + Arrays.toString(media) +
                ", card=" + card +
                '}';
    }

    @Nullable
    private static String getPlaceFullName(@Nullable Place place) {
        if (place == null) return null;
        return place.getFullName();
    }

    private static long getTime(final Date date) {
        return date != null ? date.getTime() : 0;
    }

    public static final class CursorIndices {

        public final int _id, account_id, status_id, status_timestamp, user_name, user_screen_name,
                text_html, text_plain, text_unescaped, user_profile_image_url, is_favorite, is_retweet,
                is_gap, location, is_protected, is_verified, in_reply_to_status_id, in_reply_to_user_id,
                in_reply_to_user_name, in_reply_to_user_screen_name, my_retweet_id, retweeted_by_user_name,
                retweeted_by_user_screen_name, retweeted_by_user_profile_image, retweet_id, retweet_timestamp,
                retweeted_by_user_id, user_id, source, retweet_count, favorite_count, reply_count,
                descendent_reply_count, is_possibly_sensitive, is_following, media, mentions, quote_media,
                card_name, card, place_full_name, is_quote, quote_id, quote_text_html, quote_text_plain,
                quote_text_unescaped, quote_timestamp, quote_source, quoted_by_user_id, quoted_by_user_name,
                quoted_by_user_screen_name, quoted_by_user_profile_image, quoted_by_user_is_protected,
                quoted_by_user_is_verified;

        public CursorIndices(final Cursor cursor) {
            _id = cursor.getColumnIndex(Statuses._ID);
            account_id = cursor.getColumnIndex(Statuses.ACCOUNT_ID);
            status_id = cursor.getColumnIndex(Statuses.STATUS_ID);
            status_timestamp = cursor.getColumnIndex(Statuses.STATUS_TIMESTAMP);
            user_name = cursor.getColumnIndex(Statuses.USER_NAME);
            user_screen_name = cursor.getColumnIndex(Statuses.USER_SCREEN_NAME);
            text_html = cursor.getColumnIndex(Statuses.TEXT_HTML);
            text_plain = cursor.getColumnIndex(Statuses.TEXT_PLAIN);
            text_unescaped = cursor.getColumnIndex(Statuses.TEXT_UNESCAPED);
            user_profile_image_url = cursor.getColumnIndex(Statuses.USER_PROFILE_IMAGE_URL);
            is_favorite = cursor.getColumnIndex(Statuses.IS_FAVORITE);
            is_retweet = cursor.getColumnIndex(Statuses.IS_RETWEET);
            is_quote = cursor.getColumnIndex(Statuses.IS_QUOTE);
            is_gap = cursor.getColumnIndex(Statuses.IS_GAP);
            location = cursor.getColumnIndex(Statuses.LOCATION);
            is_protected = cursor.getColumnIndex(Statuses.IS_PROTECTED);
            is_verified = cursor.getColumnIndex(Statuses.IS_VERIFIED);
            in_reply_to_status_id = cursor.getColumnIndex(Statuses.IN_REPLY_TO_STATUS_ID);
            in_reply_to_user_id = cursor.getColumnIndex(Statuses.IN_REPLY_TO_USER_ID);
            in_reply_to_user_name = cursor.getColumnIndex(Statuses.IN_REPLY_TO_USER_NAME);
            in_reply_to_user_screen_name = cursor.getColumnIndex(Statuses.IN_REPLY_TO_USER_SCREEN_NAME);
            my_retweet_id = cursor.getColumnIndex(Statuses.MY_RETWEET_ID);
            retweet_id = cursor.getColumnIndex(Statuses.RETWEET_ID);
            retweet_timestamp = cursor.getColumnIndex(Statuses.RETWEET_TIMESTAMP);
            retweeted_by_user_id = cursor.getColumnIndex(Statuses.RETWEETED_BY_USER_ID);
            retweeted_by_user_name = cursor.getColumnIndex(Statuses.RETWEETED_BY_USER_NAME);
            retweeted_by_user_screen_name = cursor.getColumnIndex(Statuses.RETWEETED_BY_USER_SCREEN_NAME);
            retweeted_by_user_profile_image = cursor.getColumnIndex(Statuses.RETWEETED_BY_USER_PROFILE_IMAGE);
            quote_id = cursor.getColumnIndex(Statuses.QUOTE_ID);
            quote_text_html = cursor.getColumnIndex(Statuses.QUOTE_TEXT_HTML);
            quote_text_plain = cursor.getColumnIndex(Statuses.QUOTE_TEXT_PLAIN);
            quote_text_unescaped = cursor.getColumnIndex(Statuses.QUOTE_TEXT_UNESCAPED);
            quote_timestamp = cursor.getColumnIndex(Statuses.QUOTE_TIMESTAMP);
            quote_source = cursor.getColumnIndex(Statuses.QUOTE_SOURCE);
            quoted_by_user_id = cursor.getColumnIndex(Statuses.QUOTED_BY_USER_ID);
            quoted_by_user_name = cursor.getColumnIndex(Statuses.QUOTED_BY_USER_NAME);
            quoted_by_user_screen_name = cursor.getColumnIndex(Statuses.QUOTED_BY_USER_SCREEN_NAME);
            quoted_by_user_profile_image = cursor.getColumnIndex(Statuses.QUOTED_BY_USER_PROFILE_IMAGE);
            quoted_by_user_is_protected = cursor.getColumnIndex(Statuses.QUOTED_BY_USER_IS_PROTECTED);
            quoted_by_user_is_verified = cursor.getColumnIndex(Statuses.QUOTED_BY_USER_IS_VERIFIED);
            user_id = cursor.getColumnIndex(Statuses.USER_ID);
            source = cursor.getColumnIndex(Statuses.SOURCE);
            retweet_count = cursor.getColumnIndex(Statuses.RETWEET_COUNT);
            favorite_count = cursor.getColumnIndex(Statuses.FAVORITE_COUNT);
            reply_count = cursor.getColumnIndex(Statuses.REPLY_COUNT);
            descendent_reply_count = cursor.getColumnIndex(Statuses.DESCENDENT_REPLY_COUNT);
            is_possibly_sensitive = cursor.getColumnIndex(Statuses.IS_POSSIBLY_SENSITIVE);
            is_following = cursor.getColumnIndex(Statuses.IS_FOLLOWING);
            media = cursor.getColumnIndex(Statuses.MEDIA_JSON);
            mentions = cursor.getColumnIndex(Statuses.MENTIONS_JSON);
            quote_media = cursor.getColumnIndex(Statuses.QUOTE_MEDIA_JSON);
            card = cursor.getColumnIndex(Statuses.CARD);
            card_name = cursor.getColumnIndex(Statuses.CARD_NAME);
            place_full_name = cursor.getColumnIndex(Statuses.PLACE_FULL_NAME);
        }

        @Override
        public String toString() {
            return "CursorIndices{" +
                    "_id=" + _id +
                    ", account_id=" + account_id +
                    ", status_id=" + status_id +
                    ", status_timestamp=" + status_timestamp +
                    ", user_name=" + user_name +
                    ", user_screen_name=" + user_screen_name +
                    ", text_html=" + text_html +
                    ", text_plain=" + text_plain +
                    ", text_unescaped=" + text_unescaped +
                    ", user_profile_image_url=" + user_profile_image_url +
                    ", is_favorite=" + is_favorite +
                    ", is_retweet=" + is_retweet +
                    ", is_gap=" + is_gap +
                    ", location=" + location +
                    ", is_protected=" + is_protected +
                    ", is_verified=" + is_verified +
                    ", in_reply_to_status_id=" + in_reply_to_status_id +
                    ", in_reply_to_user_id=" + in_reply_to_user_id +
                    ", in_reply_to_user_name=" + in_reply_to_user_name +
                    ", in_reply_to_user_screen_name=" + in_reply_to_user_screen_name +
                    ", my_retweet_id=" + my_retweet_id +
                    ", retweeted_by_user_name=" + retweeted_by_user_name +
                    ", retweeted_by_user_screen_name=" + retweeted_by_user_screen_name +
                    ", retweeted_by_user_profile_image=" + retweeted_by_user_profile_image +
                    ", retweet_id=" + retweet_id +
                    ", retweet_timestamp=" + retweet_timestamp +
                    ", retweeted_by_user_id=" + retweeted_by_user_id +
                    ", user_id=" + user_id +
                    ", source=" + source +
                    ", retweet_count=" + retweet_count +
                    ", favorite_count=" + favorite_count +
                    ", reply_count=" + reply_count +
                    ", descendent_reply_count=" + descendent_reply_count +
                    ", is_possibly_sensitive=" + is_possibly_sensitive +
                    ", is_following=" + is_following +
                    ", media=" + media +
                    ", mentions=" + mentions +
                    ", quote_media=" + quote_media +
                    ", card_name=" + card_name +
                    ", card=" + card +
                    ", place_full_name=" + place_full_name +
                    ", is_quote=" + is_quote +
                    ", quote_id=" + quote_id +
                    ", quote_text_html=" + quote_text_html +
                    ", quote_text_plain=" + quote_text_plain +
                    ", quote_text_unescaped=" + quote_text_unescaped +
                    ", quote_timestamp=" + quote_timestamp +
                    ", quote_source=" + quote_source +
                    ", quoted_by_user_id=" + quoted_by_user_id +
                    ", quoted_by_user_name=" + quoted_by_user_name +
                    ", quoted_by_user_screen_name=" + quoted_by_user_screen_name +
                    ", quoted_by_user_profile_image=" + quoted_by_user_profile_image +
                    ", quoted_by_user_is_protected=" + quoted_by_user_is_protected +
                    ", quoted_by_user_is_verified=" + quoted_by_user_is_verified +
                    '}';
        }


    }

    @JsonObject
    public static final class ParcelableCardEntity implements Parcelable {

        public static final Parcelable.Creator<ParcelableCardEntity> CREATOR = new Parcelable.Creator<ParcelableCardEntity>() {
            @Override
            public ParcelableCardEntity createFromParcel(final Parcel in) {
                return new ParcelableCardEntity(in);
            }

            @Override
            public ParcelableCardEntity[] newArray(final int size) {
                return new ParcelableCardEntity[size];
            }
        };

        @JsonField(name = "name")
        public String name;

        @JsonField(name = "users")
        public ParcelableUser[] users;

        @JsonField(name = "values")
        public Map<String, ParcelableBindingValue> values;

        public ParcelableCardEntity(Parcel src) {
            name = src.readString();
            users = src.createTypedArray(ParcelableUser.CREATOR);
            final Bundle bundle = src.readBundle(ParcelableBindingValue.class.getClassLoader());
            for (String key : bundle.keySet()) {
                if (values == null) {
                    values = new HashMap<>();
                }
                final ParcelableBindingValue value = bundle.getParcelable(key);
                values.put(key, value);
            }
        }

        public ParcelableCardEntity() {

        }

        public ParcelableCardEntity(CardEntity card, long account_id) {
            name = card.getName();
            users = ParcelableUser.fromUsersArray(card.getUsers(), account_id);
            values = ParcelableBindingValue.from(card.getBindingValues());
        }

        @Override
        public String toString() {
            return "ParcelableCardEntity{" +
                    "name='" + name + '\'' +
                    ", users=" + Arrays.toString(users) +
                    ", values=" + values +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeTypedArray(users, flags);
            final Bundle bundle = new Bundle();
            if (values != null) {
                for (Entry<String, ParcelableBindingValue> entry : values.entrySet()) {
                    bundle.putParcelable(entry.getKey(), entry.getValue());
                }
            }
            dest.writeBundle(bundle);
        }

        public static ParcelableCardEntity fromCardEntity(CardEntity card, long account_id) {
            if (card == null) return null;
            return new ParcelableCardEntity(card, account_id);
        }

        public static ParcelableCardEntity fromJSONString(final String json) {
            if (TextUtils.isEmpty(json)) return null;
            try {
                return LoganSquare.parse(json, ParcelableCardEntity.class);
            } catch (final IOException e) {
                return null;
            }
        }

        public static ParcelableBindingValue getValue(@Nullable ParcelableCardEntity entity, @Nullable String key) {
            if (entity == null || entity.values == null || key == null) return null;
            return entity.values.get(key);
        }

        @JsonObject
        public static final class ParcelableBindingValue implements Parcelable {

            public static final Parcelable.Creator<ParcelableBindingValue> CREATOR = new Parcelable.Creator<ParcelableBindingValue>() {
                @Override
                public ParcelableBindingValue createFromParcel(final Parcel in) {
                    return new ParcelableBindingValue(in);
                }

                @Override
                public ParcelableBindingValue[] newArray(final int size) {
                    return new ParcelableBindingValue[size];
                }
            };

            @JsonField(name = "type")
            public String type;
            @JsonField(name = "value")
            public String value;

            public ParcelableBindingValue() {
            }

            public ParcelableBindingValue(Parcel in) {
                this.type = in.readString();
                this.value = in.readString();
            }

            public ParcelableBindingValue(BindingValue value) {
                this.type = value.getType();
                if (BindingValue.TYPE_IMAGE.equals(type)) {
                    this.value = ((ImageValue) value).getUrl();
                } else if (BindingValue.TYPE_STRING.equals(type)) {
                    this.value = ((StringValue) value).getValue();
                } else if (BindingValue.TYPE_BOOLEAN.equals(type)) {
                    this.value = String.valueOf(((BooleanValue) value).getValue());
                } else if (BindingValue.TYPE_USER.equals(type)) {
                    this.value = String.valueOf(((UserValue) value).getUserId());
                }
            }

            @Override
            public int describeContents() {
                return 0;
            }

            public static Map<String, ParcelableBindingValue> from(Map<String, BindingValue> bindingValues) {
                if (bindingValues == null) return null;
                final Map<String, ParcelableBindingValue> map = new HashMap<>();
                for (Entry<String, BindingValue> entry : bindingValues.entrySet()) {
                    map.put(entry.getKey(), new ParcelableBindingValue(entry.getValue()));
                }
                return map;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(type);
                dest.writeString(value);
            }


        }

    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        out.writeLong(id);
        out.writeLong(account_id);
        out.writeLong(timestamp);
        out.writeLong(user_id);
        out.writeLong(retweet_id);
        out.writeLong(retweet_timestamp);
        out.writeLong(retweeted_by_id);
        out.writeLong(retweet_count);
        out.writeLong(favorite_count);
        out.writeLong(reply_count);
        out.writeLong(descendent_reply_count);
        out.writeLong(in_reply_to_status_id);
        out.writeByte((byte) (is_gap ? 1 : 0));
        out.writeByte((byte) (is_retweet ? 1 : 0));
        out.writeByte((byte) (is_favorite ? 1 : 0));
        out.writeByte((byte) (user_is_protected ? 1 : 0));
        out.writeByte((byte) (user_is_verified ? 1 : 0));
        out.writeString(retweeted_by_name);
        out.writeString(retweeted_by_screen_name);
        out.writeString(retweeted_by_profile_image);
        out.writeString(text_html);
        out.writeString(text_plain);
        out.writeString(user_name);
        out.writeString(user_screen_name);
        out.writeString(in_reply_to_screen_name);
        out.writeString(source);
        out.writeString(user_profile_image_url);
        out.writeTypedArray(media, flags);
        out.writeTypedArray(quote_media, flags);
        out.writeParcelable(location, flags);
        out.writeLong(my_retweet_id);
        out.writeByte((byte) (is_possibly_sensitive ? 1 : 0));
        out.writeByte((byte) (user_is_following ? 1 : 0));
        out.writeString(text_unescaped);
        out.writeLong(in_reply_to_user_id);
        out.writeString(in_reply_to_name);
        out.writeTypedArray(mentions, flags);
        out.writeParcelable(card, flags);
        out.writeString(place_full_name);
        out.writeByte((byte) (is_quote ? 1 : 0));
        out.writeLong(quote_id);
        out.writeString(quote_text_html);
        out.writeString(quote_text_plain);
        out.writeString(quote_text_unescaped);
        out.writeLong(quote_timestamp);
        out.writeLong(quoted_by_user_id);
        out.writeString(quoted_by_user_name);
        out.writeString(quoted_by_user_screen_name);
        out.writeString(quoted_by_user_profile_image);
        out.writeByte((byte) (quoted_by_user_is_protected ? 1 : 0));
        out.writeByte((byte) (quoted_by_user_is_verified ? 1 : 0));
        out.writeString(quote_source);
    }

}

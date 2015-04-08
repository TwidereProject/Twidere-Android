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
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.jsonserializer.JSONParcel;
import org.mariotaku.jsonserializer.JSONParcelable;
import org.mariotaku.jsonserializer.JSONSerializer;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.util.HtmlEscapeHelper;
import org.mariotaku.twidere.util.SimpleValueSerializer;
import org.mariotaku.twidere.util.TwitterContentUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

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
public class ParcelableStatus implements TwidereParcelable, Comparable<ParcelableStatus> {

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

    public static final JSONParcelable.Creator<ParcelableStatus> JSON_CREATOR = new JSONParcelable.Creator<ParcelableStatus>() {
        @Override
        public ParcelableStatus createFromParcel(final JSONParcel in) {
            return new ParcelableStatus(in);
        }

        @Override
        public ParcelableStatus[] newArray(final int size) {
            return new ParcelableStatus[size];
        }
    };

    public static final Comparator<ParcelableStatus> TIMESTAMP_COMPARATOR = new Comparator<ParcelableStatus>() {

        @Override
        public int compare(final ParcelableStatus object1, final ParcelableStatus object2) {
            final long diff = object2.timestamp - object1.timestamp;
            if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
            if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
            return (int) diff;
        }
    };
    public static final Comparator<ParcelableStatus> REVERSE_ID_COMPARATOR = new Comparator<ParcelableStatus>() {

        @Override
        public int compare(final ParcelableStatus object1, final ParcelableStatus object2) {
            final long diff = object1.id - object2.id;
            if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
            if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
            return (int) diff;
        }
    };

    public final long id, account_id, timestamp, user_id, retweet_id, retweeted_by_id, retweet_timestamp,
            retweet_count, favorite_count, reply_count, descendent_reply_count, in_reply_to_status_id,
            in_reply_to_user_id, my_retweet_id, quote_id, quote_timestamp, quoted_by_user_id;

    public final boolean is_gap, is_retweet, is_favorite, is_possibly_sensitive, user_is_following, user_is_protected,
            user_is_verified, is_quote, quoted_by_user_is_protected, quoted_by_user_is_verified;

    public final String retweeted_by_name, retweeted_by_screen_name, retweeted_by_profile_image,
            text_html, text_plain, user_name, user_screen_name, in_reply_to_name, in_reply_to_screen_name,
            source, user_profile_image_url, text_unescaped, card_name, quote_text_html, quote_text_plain,
            quote_text_unescaped, quote_source, quoted_by_user_name, quoted_by_user_screen_name,
            quoted_by_user_profile_image;

    public final ParcelableLocation location;

    public final String place_full_name;

    public final ParcelableUserMention[] mentions;

    public final ParcelableMedia[] media;

    public final ParcelableCardEntity card;

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
        media = SimpleValueSerializer.fromSerializedString(idx.media != -1 ? c.getString(idx.media) : null, ParcelableMedia.SIMPLE_CREATOR);
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
        mentions = idx.mentions != -1 ? ParcelableUserMention.fromJSONString(c.getString(idx.mentions)) : null;
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

    public ParcelableStatus(final JSONParcel in) {
        id = in.readLong("status_id");
        account_id = in.readLong("account_id");
        timestamp = in.readLong("status_timestamp");
        user_id = in.readLong("user_id");
        retweet_id = in.readLong("retweet_id");
        retweet_timestamp = in.readLong("retweet_timestamp");
        retweeted_by_id = in.readLong("retweeted_by_id");
        retweet_count = in.readLong("retweet_count");
        favorite_count = in.readLong("favorite_count");
        reply_count = in.readLong("reply_count");
        descendent_reply_count = in.readLong("descendent_reply_count");
        in_reply_to_status_id = in.readLong("in_reply_to_status_id");
        in_reply_to_user_id = in.readLong("in_reply_to_user_id");
        is_gap = in.readBoolean("is_gap");
        is_retweet = in.readBoolean("is_retweet");
        is_favorite = in.readBoolean("is_favorite");
        user_is_protected = in.readBoolean("is_protected");
        user_is_verified = in.readBoolean("is_verified");
        retweeted_by_name = in.readString("retweeted_by_name");
        retweeted_by_screen_name = in.readString("retweeted_by_screen_name");
        retweeted_by_profile_image = in.readString("retweeted_by_profile_image");
        text_html = in.readString("text_html");
        text_plain = in.readString("text_plain");
        user_name = in.readString("name");
        user_screen_name = in.readString("screen_name");
        in_reply_to_name = in.readString("in_reply_to_name");
        in_reply_to_screen_name = in.readString("in_reply_to_screen_name");
        source = in.readString("source");
        user_profile_image_url = in.readString("profile_image_url");
        media = in.readParcelableArray("media", ParcelableMedia.JSON_CREATOR);
        location = in.readParcelable("location", ParcelableLocation.JSON_CREATOR);
        my_retweet_id = in.readLong("my_retweet_id");
        is_possibly_sensitive = in.readBoolean("is_possibly_sensitive");
        text_unescaped = in.readString("text_unescaped");
        user_is_following = in.readBoolean("is_following");
        mentions = in.readParcelableArray("mentions", ParcelableUserMention.JSON_CREATOR);
        card = in.readParcelable("card", ParcelableCardEntity.JSON_CREATOR);
        place_full_name = in.readString("place_full_name");
        is_quote = in.readBoolean("is_quote");
        quote_id = in.readLong("quote_id");
        quote_text_html = in.readString("quote_text_html");
        quote_text_plain = in.readString("quote_text_plain");
        quote_text_unescaped = in.readString("quote_text_unescaped");
        quote_timestamp = in.readLong("quote_timestamp");
        quoted_by_user_id = in.readLong("quoted_by_user_id");
        quoted_by_user_name = in.readString("quoted_by_user_name");
        quoted_by_user_screen_name = in.readString("quoted_by_user_screen_name");
        quoted_by_user_profile_image = in.readString("quoted_by_user_profile_image");
        quoted_by_user_is_protected = in.readBoolean("quoted_by_user_is_protected");
        quoted_by_user_is_verified = in.readBoolean("quoted_by_user_is_verified");
        quote_source = in.readString("quote_source");
        card_name = card != null ? card.name : null;
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
        media = ParcelableMedia.fromEntities(status);
        text_plain = status.getText();
        retweet_count = status.getRetweetCount();
        favorite_count = status.getFavoriteCount();
        reply_count = status.getReplyCount();
        descendent_reply_count = status.getDescendentReplyCount();
        in_reply_to_name = TwitterContentUtils.getInReplyToName(status);
        in_reply_to_screen_name = status.getInReplyToScreenName();
        in_reply_to_status_id = status.getInReplyToStatusId();
        in_reply_to_user_id = status.getInReplyToUserId();
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

    @Override
    public void writeToParcel(final JSONParcel out) {
        out.writeLong("status_id", id);
        out.writeLong("account_id", account_id);
        out.writeLong("status_timestamp", timestamp);
        out.writeLong("user_id", user_id);
        out.writeLong("retweet_id", retweet_id);
        out.writeLong("retweet_timestamp", retweet_timestamp);
        out.writeLong("retweeted_by_id", retweeted_by_id);
        out.writeLong("retweet_count", retweet_count);
        out.writeLong("favorite_count", favorite_count);
        out.writeLong("reply_count", reply_count);
        out.writeLong("descendent_reply_count", descendent_reply_count);
        out.writeLong("in_reply_to_status_id", in_reply_to_status_id);
        out.writeLong("in_reply_to_user_id", in_reply_to_user_id);
        out.writeBoolean("is_gap", is_gap);
        out.writeBoolean("is_retweet", is_retweet);
        out.writeBoolean("is_favorite", is_favorite);
        out.writeBoolean("is_protected", user_is_protected);
        out.writeBoolean("is_verified", user_is_verified);
        out.writeString("retweeted_by_name", retweeted_by_name);
        out.writeString("retweeted_by_screen_name", retweeted_by_screen_name);
        out.writeString("retweeted_by_profile_image", retweeted_by_profile_image);
        out.writeString("text_html", text_html);
        out.writeString("text_plain", text_plain);
        out.writeString("text_unescaped", text_unescaped);
        out.writeString("name", user_name);
        out.writeString("screen_name", user_screen_name);
        out.writeString("in_reply_to_name", in_reply_to_name);
        out.writeString("in_reply_to_screen_name", in_reply_to_screen_name);
        out.writeString("source", source);
        out.writeString("profile_image_url", user_profile_image_url);
        out.writeParcelableArray("media", media);
        out.writeParcelable("location", location);
        out.writeLong("my_retweet_id", my_retweet_id);
        out.writeBoolean("is_possibly_sensitive", is_possibly_sensitive);
        out.writeBoolean("is_following", user_is_following);
        out.writeParcelableArray("mentions", mentions);
        out.writeParcelable("card", card);
        out.writeString("place_full_name", place_full_name);
        out.writeBoolean("is_quote", is_quote);
        out.writeLong("quote_id", quote_id);
        out.writeString("quote_text_html", quote_text_html);
        out.writeString("quote_text_plain", quote_text_plain);
        out.writeString("quote_text_unescaped", quote_text_unescaped);
        out.writeLong("quote_timestamp", quote_timestamp);
        out.writeLong("quoted_by_user_id", quoted_by_user_id);
        out.writeString("quoted_by_user_name", quoted_by_user_name);
        out.writeString("quoted_by_user_screen_name", quoted_by_user_screen_name);
        out.writeString("quoted_by_user_profile_image", quoted_by_user_profile_image);
        out.writeBoolean("quoted_by_user_is_protected", quoted_by_user_is_protected);
        out.writeBoolean("quoted_by_user_is_verified", quoted_by_user_is_verified);
        out.writeString("quote_source", quote_source);
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
                descendent_reply_count, is_possibly_sensitive, is_following, media, mentions, card_name,
                card, place_full_name, is_quote, quote_id, quote_text_html, quote_text_plain, quote_text_unescaped,
                quote_timestamp, quote_source, quoted_by_user_id, quoted_by_user_name, quoted_by_user_screen_name,
                quoted_by_user_profile_image, quoted_by_user_is_protected, quoted_by_user_is_verified;

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
            media = cursor.getColumnIndex(Statuses.MEDIA_LIST);
            mentions = cursor.getColumnIndex(Statuses.MENTIONS_LIST);
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
                    ", card_name=" + card_name +
                    ", card=" + card +
                    ", place_full_name=" + place_full_name +
                    '}';
        }


    }

    public static final class ParcelableCardEntity implements TwidereParcelable {

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
        public static final JSONParcelable.Creator<ParcelableCardEntity> JSON_CREATOR = new JSONParcelable.Creator<ParcelableCardEntity>() {
            @Override
            public ParcelableCardEntity createFromParcel(final JSONParcel in) {
                return new ParcelableCardEntity(in);
            }

            @Override
            public ParcelableCardEntity[] newArray(final int size) {
                return new ParcelableCardEntity[size];
            }
        };

        public final String name;
        public final ParcelableUser[] users;
        public final ParcelableValueItem[] values;

        public ParcelableCardEntity(Parcel src) {
            name = src.readString();
            values = src.createTypedArray(ParcelableValueItem.CREATOR);
            users = src.createTypedArray(ParcelableUser.CREATOR);
        }

        public ParcelableCardEntity(JSONParcel src) {
            name = src.readString("name");
            values = src.readParcelableArray("values", ParcelableValueItem.JSON_CREATOR);
            users = src.readParcelableArray("users", ParcelableUser.JSON_CREATOR);
        }

        public ParcelableCardEntity(CardEntity card, long account_id) {
            name = card.getName();
            users = ParcelableUser.fromUsersArray(card.gerUsers(), account_id);
            final BindingValue[] bindingValues = card.getBindingValues();
            if (bindingValues != null) {
                values = new ParcelableValueItem[bindingValues.length];
                for (int i = 0, j = bindingValues.length; i < j; i++) {
                    values[i] = new ParcelableValueItem(bindingValues[i]);
                }
            } else {
                values = null;
            }
        }

        @Override
        public String toString() {
            return "ParcelableCardEntity{" +
                    "name='" + name + '\'' +
                    ", users=" + Arrays.toString(users) +
                    ", values=" + Arrays.toString(values) +
                    '}';
        }

        public static ParcelableCardEntity fromCardEntity(CardEntity card, long account_id) {
            if (card == null) return null;
            return new ParcelableCardEntity(card, account_id);
        }

        public static ParcelableCardEntity fromJSONString(final String json) {
            if (TextUtils.isEmpty(json)) return null;
            try {
                return JSONSerializer.createObject(JSON_CREATOR, new JSONObject(json));
            } catch (final JSONException e) {
                return null;
            }
        }

        public static ParcelableValueItem getValue(ParcelableCardEntity entity, String key) {
            for (ParcelableValueItem item : entity.values) {
                if (item.name.equals(key)) return item;
            }
            return null;
        }

        public static final class ParcelableImageValue implements TwidereParcelable {

            public static final Parcelable.Creator<ParcelableImageValue> CREATOR = new Parcelable.Creator<ParcelableImageValue>() {
                @Override
                public ParcelableImageValue createFromParcel(final Parcel in) {
                    return new ParcelableImageValue(in);
                }

                @Override
                public ParcelableImageValue[] newArray(final int size) {
                    return new ParcelableImageValue[size];
                }
            };

            public static final JSONParcelable.Creator<ParcelableImageValue> JSON_CREATOR = new JSONParcelable.Creator<ParcelableImageValue>() {
                @Override
                public ParcelableImageValue createFromParcel(final JSONParcel in) {
                    return new ParcelableImageValue(in);
                }

                @Override
                public ParcelableImageValue[] newArray(final int size) {
                    return new ParcelableImageValue[size];
                }
            };
            public final int width, height;
            public final String url;

            public ParcelableImageValue(JSONParcel in) {
                this.width = in.readInt("width");
                this.height = in.readInt("height");
                this.url = in.readString("url");
            }

            public ParcelableImageValue(Parcel in) {
                this.width = in.readInt();
                this.height = in.readInt();
                this.url = in.readString();
            }

            public ParcelableImageValue(ImageValue value) {
                this.width = value.getWidth();
                this.height = value.getHeight();
                this.url = value.getUrl();
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeInt(width);
                dest.writeInt(height);
                dest.writeString(url);
            }

            @Override
            public void writeToParcel(JSONParcel dest) {
                dest.writeInt("width", width);
                dest.writeInt("height", height);
                dest.writeString("url", url);
            }
        }

        public static final class ParcelableUserValue implements TwidereParcelable {

            public static final Parcelable.Creator<ParcelableUserValue> CREATOR = new Parcelable.Creator<ParcelableUserValue>() {
                @Override
                public ParcelableUserValue createFromParcel(final Parcel in) {
                    return new ParcelableUserValue(in);
                }

                @Override
                public ParcelableUserValue[] newArray(final int size) {
                    return new ParcelableUserValue[size];
                }
            };

            public static final JSONParcelable.Creator<ParcelableUserValue> JSON_CREATOR = new JSONParcelable.Creator<ParcelableUserValue>() {
                @Override
                public ParcelableUserValue createFromParcel(final JSONParcel in) {
                    return new ParcelableUserValue(in);
                }

                @Override
                public ParcelableUserValue[] newArray(final int size) {
                    return new ParcelableUserValue[size];
                }
            };
            public final long id;

            public ParcelableUserValue(JSONParcel in) {
                this.id = in.readLong("id");
            }

            public ParcelableUserValue(Parcel in) {
                this.id = in.readLong();
            }

            public ParcelableUserValue(UserValue value) {
                this.id = value.getUserId();
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeLong(id);
            }

            @Override
            public void writeToParcel(JSONParcel dest) {
                dest.writeLong("id", id);
            }
        }

        public static final class ParcelableValueItem implements TwidereParcelable {

            public static final Parcelable.Creator<ParcelableValueItem> CREATOR = new Parcelable.Creator<ParcelableValueItem>() {
                @Override
                public ParcelableValueItem createFromParcel(final Parcel in) {
                    return new ParcelableValueItem(in);
                }

                @Override
                public ParcelableValueItem[] newArray(final int size) {
                    return new ParcelableValueItem[size];
                }
            };
            public static final JSONParcelable.Creator<ParcelableValueItem> JSON_CREATOR = new JSONParcelable.Creator<ParcelableValueItem>() {
                @Override
                public ParcelableValueItem createFromParcel(final JSONParcel in) {
                    return new ParcelableValueItem(in);
                }

                @Override
                public ParcelableValueItem[] newArray(final int size) {
                    return new ParcelableValueItem[size];
                }
            };

            public final String name, type;
            public final Object value;

            public ParcelableValueItem(JSONParcel in) {
                this.name = in.readString("name");
                this.type = in.readString("type");
                switch (type) {
                    case BindingValue.TYPE_STRING:
                        value = in.readString("value");
                        break;
                    case BindingValue.TYPE_BOOLEAN:
                        value = in.readBoolean("value");
                        break;
                    case BindingValue.TYPE_IMAGE:
                        value = in.readParcelable("value", ParcelableImageValue.JSON_CREATOR);
                        break;
                    case BindingValue.TYPE_USER:
                        value = in.readParcelable("value", ParcelableUserValue.JSON_CREATOR);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }

            public ParcelableValueItem(Parcel in) {
                this.name = in.readString();
                this.type = in.readString();
                this.value = in.readValue(ParcelableValueItem.class.getClassLoader());
            }

            public ParcelableValueItem(BindingValue bindingValue) {
                name = bindingValue.getName();
                type = bindingValue.getType();
                switch (type) {
                    case BindingValue.TYPE_STRING:
                        value = ((StringValue) bindingValue).getValue();
                        break;
                    case BindingValue.TYPE_BOOLEAN:
                        value = ((BooleanValue) bindingValue).getValue();
                        break;
                    case BindingValue.TYPE_IMAGE:
                        value = new ParcelableImageValue((ImageValue) bindingValue);
                        break;
                    case BindingValue.TYPE_USER:
                        value = new ParcelableUserValue((UserValue) bindingValue);
                        break;
                    default:
                        value = null;
                        break;
                }
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public String toString() {
                return "ParcelableValueItem{" +
                        "name='" + name + '\'' +
                        ", type='" + type + '\'' +
                        ", value=" + value +
                        '}';
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(name);
                dest.writeString(type);
                dest.writeValue(value);
            }

            @Override
            public void writeToParcel(JSONParcel dest) {
                dest.writeString("name", name);
                dest.writeString("type", type);
                dest.writeObject("value", value);
            }
        }


        @Override
        public int describeContents() {
            return 0;
        }


        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeTypedArray(values, flags);
            dest.writeTypedArray(users, flags);
        }

        @Override
        public void writeToParcel(JSONParcel dest) {
            dest.writeString("name", name);
            dest.writeParcelableArray("values", values);
            dest.writeParcelableArray("users", users);
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

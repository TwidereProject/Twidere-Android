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

import org.mariotaku.library.objectcursor.annotation.AfterCursorObjectCreated;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.LoganSquareCursorFieldConverter;
import org.mariotaku.twidere.model.util.UserKeyConverter;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

@CursorObject(valuesCreator = true)
@JsonObject
@ParcelablePlease
public class ParcelableStatus implements Parcelable, Comparable<ParcelableStatus>, Cloneable {

    public static final Comparator<ParcelableStatus> REVERSE_COMPARATOR = new Comparator<ParcelableStatus>() {

        @Override
        public int compare(final ParcelableStatus object1, final ParcelableStatus object2) {
            return object2.compareTo(object1);
        }
    };

    @ParcelableThisPlease
    @JsonField(name = "id")
    @CursorField(Statuses.STATUS_ID)
    public String id;
    @ParcelableThisPlease
    @JsonField(name = "sort_id")
    @CursorField(Statuses.SORT_ID)
    public long sort_id = -1;
    @ParcelableThisPlease
    @JsonField(name = "account_id", typeConverter = UserKeyConverter.class)
    @CursorField(value = Statuses.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;
    @ParcelableThisPlease
    @JsonField(name = "timestamp")
    @CursorField(Statuses.STATUS_TIMESTAMP)
    public long timestamp;
    @ParcelableThisPlease
    @JsonField(name = "user_id", typeConverter = UserKeyConverter.class)
    @CursorField(value = Statuses.USER_ID, converter = UserKeyCursorFieldConverter.class)
    public UserKey user_key;
    @ParcelableThisPlease
    @JsonField(name = "retweet_id")
    @CursorField(Statuses.RETWEET_ID)
    public String retweet_id;
    @ParcelableThisPlease
    @JsonField(name = "retweeted_by_user_id", typeConverter = UserKeyConverter.class)
    @CursorField(value = Statuses.RETWEETED_BY_USER_ID, converter = UserKeyCursorFieldConverter.class)
    public UserKey retweeted_by_user_id;
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
    public String in_reply_to_status_id;
    @ParcelableThisPlease
    @JsonField(name = "in_reply_to_user_id", typeConverter = UserKeyConverter.class)
    @CursorField(value = Statuses.IN_REPLY_TO_USER_ID, converter = UserKeyCursorFieldConverter.class)
    public UserKey in_reply_to_user_id;
    @ParcelableThisPlease
    @JsonField(name = "my_retweet_id")
    @CursorField(Statuses.MY_RETWEET_ID)
    public String my_retweet_id;
    @ParcelableThisPlease
    @JsonField(name = "quoted_id")
    @CursorField(Statuses.QUOTED_ID)
    public String quoted_id;
    @ParcelableThisPlease
    @JsonField(name = "quoted_timestamp")
    @CursorField(Statuses.QUOTED_TIMESTAMP)
    public long quoted_timestamp;
    @ParcelableThisPlease
    @JsonField(name = "quoted_user_id", typeConverter = UserKeyConverter.class)
    @CursorField(value = Statuses.QUOTED_USER_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey quoted_user_id;
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
    @ParcelableThisPlease
    @JsonField(name = "extras")
    @CursorField(value = Statuses.EXTRAS, converter = LoganSquareCursorFieldConverter.class)
    public Extras extras;

    @CursorField(value = Statuses._ID, excludeWrite = true)
    long _id;

    @ParcelableThisPlease
    public int account_color;

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


    public ParcelableStatus() {
    }


    @Override
    public int compareTo(@NonNull final ParcelableStatus another) {
        long timeDelta = timestamp - another.timestamp;
        if (timeDelta == 0) {
            return (int) (sort_id - another.sort_id);
        }
        return (int) timeDelta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParcelableStatus status = (ParcelableStatus) o;

        if (!TextUtils.equals(id, status.id)) return false;
        return account_key.equals(status.account_key);

    }

    @Override
    public int hashCode() {
        return calculateHashCode(account_key, id);
    }

    public static int calculateHashCode(UserKey accountKey, String id) {
        int result = id.hashCode();
        result = 31 * result + accountKey.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ParcelableStatus{" +
                "id=" + id +
                ", account_key=" + account_key +
                ", timestamp=" + timestamp +
                ", user_id=" + user_key +
                ", retweet_id=" + retweet_id +
                ", retweeted_by_user_id=" + retweeted_by_user_id +
                ", retweet_timestamp=" + retweet_timestamp +
                ", retweet_count=" + retweet_count +
                ", favorite_count=" + favorite_count +
                ", reply_count=" + reply_count +
                ", in_reply_to_status_id=" + in_reply_to_status_id +
                ", in_reply_to_user_id=" + in_reply_to_user_id +
                ", my_retweet_id=" + my_retweet_id +
                ", quoted_id=" + quoted_id +
                ", quoted_timestamp=" + quoted_timestamp +
                ", quoted_user_id=" + quoted_user_id +
                ", is_gap=" + is_gap +
                ", is_retweet=" + is_retweet +
                ", retweeted=" + retweeted +
                ", is_favorite=" + is_favorite +
                ", is_possibly_sensitive=" + is_possibly_sensitive +
                ", user_is_following=" + user_is_following +
                ", user_is_protected=" + user_is_protected +
                ", user_is_verified=" + user_is_verified +
                ", is_quote=" + is_quote +
                ", quoted_user_is_protected=" + quoted_user_is_protected +
                ", quoted_user_is_verified=" + quoted_user_is_verified +
                ", retweeted_by_user_name='" + retweeted_by_user_name + '\'' +
                ", retweeted_by_user_screen_name='" + retweeted_by_user_screen_name + '\'' +
                ", retweeted_by_user_profile_image='" + retweeted_by_user_profile_image + '\'' +
                ", text_html='" + text_html + '\'' +
                ", text_plain='" + text_plain + '\'' +
                ", lang='" + lang + '\'' +
                ", user_name='" + user_name + '\'' +
                ", user_screen_name='" + user_screen_name + '\'' +
                ", in_reply_to_name='" + in_reply_to_name + '\'' +
                ", in_reply_to_screen_name='" + in_reply_to_screen_name + '\'' +
                ", source='" + source + '\'' +
                ", user_profile_image_url='" + user_profile_image_url + '\'' +
                ", text_unescaped='" + text_unescaped + '\'' +
                ", card_name='" + card_name + '\'' +
                ", quoted_text_html='" + quoted_text_html + '\'' +
                ", quoted_text_plain='" + quoted_text_plain + '\'' +
                ", quoted_text_unescaped='" + quoted_text_unescaped + '\'' +
                ", quoted_source='" + quoted_source + '\'' +
                ", quoted_user_name='" + quoted_user_name + '\'' +
                ", quoted_user_screen_name='" + quoted_user_screen_name + '\'' +
                ", quoted_user_profile_image='" + quoted_user_profile_image + '\'' +
                ", quoted_location=" + quoted_location +
                ", quoted_place_full_name='" + quoted_place_full_name + '\'' +
                ", location=" + location +
                ", place_full_name='" + place_full_name + '\'' +
                ", mentions=" + Arrays.toString(mentions) +
                ", media=" + Arrays.toString(media) +
                ", quoted_media=" + Arrays.toString(quoted_media) +
                ", card=" + card +
                ", extras=" + extras +
                ", _id=" + _id +
                '}';
    }

    @AfterCursorObjectCreated
    void finishCursorObjectCreation() {
        card_name = card != null ? card.name : null;
        fixSortId();
    }

    @OnJsonParseComplete
    void onParseComplete() throws IOException {
        if (is_quote && TextUtils.isEmpty(quoted_text_html))
            throw new IOException("Incompatible model");
        fixSortId();
    }

    void fixSortId() {
        if (sort_id <= 0) {
            try {
                sort_id = Long.parseLong(id);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        if (sort_id <= 0) {
            sort_id = timestamp;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableStatusParcelablePlease.writeToParcel(this, dest, flags);
    }


    @ParcelablePlease
    @JsonObject
    public static class Extras implements Parcelable {

        @JsonField(name = "external_url")
        @ParcelableThisPlease
        public String external_url;
        @JsonField(name = "support_entities")
        @ParcelableThisPlease
        public boolean support_entities;
        @JsonField(name = "user_profile_image_url_profile_size")
        @ParcelableThisPlease
        public String user_profile_image_url_profile_size;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            ParcelableStatus$ExtrasParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<Extras> CREATOR = new Creator<Extras>() {
            public Extras createFromParcel(Parcel source) {
                Extras target = new Extras();
                ParcelableStatus$ExtrasParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public Extras[] newArray(int size) {
                return new Extras[size];
            }
        };
    }
}

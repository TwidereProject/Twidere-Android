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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.commons.objectcursor.LoganSquareCursorFieldConverter;
import org.mariotaku.library.objectcursor.annotation.AfterCursorObjectCreated;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.UserKeyConverter;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;

import java.util.Arrays;


@ParcelablePlease(allFields = false)
@JsonObject
@CursorObject(valuesCreator = true, tableInfo = true)
public class ParcelableUser implements Parcelable, Comparable<ParcelableUser> {

    @ParcelableThisPlease
    @JsonField(name = "account_id", typeConverter = UserKeyConverter.class)
    public UserKey account_key;

    @ParcelableThisPlease
    public int account_color;

    @ParcelableThisPlease
    @CursorField(value = CachedUsers._ID, type = TwidereDataStore.TYPE_PRIMARY_KEY, excludeWrite = true)
    public long _id;

    @ParcelableThisPlease
    @JsonField(name = "id", typeConverter = UserKeyConverter.class)
    @CursorField(value = CachedUsers.USER_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey key;
    @ParcelableThisPlease
    @JsonField(name = "created_at")
    @CursorField(CachedUsers.CREATED_AT)
    public long created_at;
    @ParcelableThisPlease
    @JsonField(name = "position")
    public long position;

    @ParcelableThisPlease
    @JsonField(name = "is_protected")
    @CursorField(CachedUsers.IS_PROTECTED)
    public boolean is_protected;
    @ParcelableThisPlease
    @JsonField(name = "is_verified")
    @CursorField(CachedUsers.IS_VERIFIED)
    public boolean is_verified;
    @ParcelableThisPlease
    @JsonField(name = "is_follow_request_sent")
    public boolean is_follow_request_sent;
    @ParcelableThisPlease
    @JsonField(name = "is_following")
    @CursorField(CachedUsers.IS_FOLLOWING)
    public boolean is_following;

    @ParcelableThisPlease
    @JsonField(name = "description_plain")
    @CursorField(CachedUsers.DESCRIPTION_PLAIN)
    public String description_plain;
    @ParcelableThisPlease
    @JsonField(name = "name")
    @CursorField(CachedUsers.NAME)
    public String name;
    @ParcelableThisPlease
    @JsonField(name = "screen_name")
    @CursorField(CachedUsers.SCREEN_NAME)
    public String screen_name;
    @ParcelableThisPlease
    @JsonField(name = "location")
    @CursorField(CachedUsers.LOCATION)
    public String location;
    @ParcelableThisPlease
    @JsonField(name = "profile_image_url")
    @CursorField(CachedUsers.PROFILE_IMAGE_URL)
    public String profile_image_url;
    @ParcelableThisPlease
    @JsonField(name = "profile_banner_url")
    @CursorField(CachedUsers.PROFILE_BANNER_URL)
    public String profile_banner_url;
    @ParcelableThisPlease
    @JsonField(name = "profile_background_url")
    @CursorField(CachedUsers.PROFILE_BACKGROUND_URL)
    public String profile_background_url;
    @ParcelableThisPlease
    @JsonField(name = "url")
    @CursorField(CachedUsers.URL)
    public String url;
    @ParcelableThisPlease
    @JsonField(name = "url_expanded")
    @CursorField(CachedUsers.URL_EXPANDED)
    public String url_expanded;
    @ParcelableThisPlease
    @JsonField(name = "description_unescaped")
    @CursorField(CachedUsers.DESCRIPTION_UNESCAPED)
    public String description_unescaped;

    @ParcelableThisPlease
    @JsonField(name = "description_spans")
    @CursorField(value = CachedUsers.DESCRIPTION_SPANS, converter = LoganSquareCursorFieldConverter.class)
    public SpanItem[] description_spans;

    @ParcelableThisPlease
    @JsonField(name = "followers_count")
    @CursorField(CachedUsers.FOLLOWERS_COUNT)
    public long followers_count = -1;
    @ParcelableThisPlease
    @JsonField(name = "friends_count")
    @CursorField(CachedUsers.FRIENDS_COUNT)
    public long friends_count = -1;
    @ParcelableThisPlease
    @JsonField(name = "statuses_count")
    @CursorField(CachedUsers.STATUSES_COUNT)
    public long statuses_count = -1;
    @ParcelableThisPlease
    @JsonField(name = "favorites_count")
    @CursorField(CachedUsers.FAVORITES_COUNT)
    public long favorites_count = -1;
    @ParcelableThisPlease
    @JsonField(name = "listed_count")
    @CursorField(CachedUsers.LISTED_COUNT)
    public long listed_count = -1;
    @ParcelableThisPlease
    @JsonField(name = "media_count")
    @CursorField(CachedUsers.MEDIA_COUNT)
    public long media_count = -1;

    @ParcelableThisPlease
    @JsonField(name = "background_color")
    @CursorField(CachedUsers.BACKGROUND_COLOR)
    public int background_color;
    @ParcelableThisPlease
    @JsonField(name = "link_color")
    @CursorField(CachedUsers.LINK_COLOR)
    public int link_color;
    @ParcelableThisPlease
    @JsonField(name = "text_color")
    @CursorField(CachedUsers.TEXT_COLOR)
    public int text_color;

    @ParcelableThisPlease
    @JsonField(name = "is_cache")
    public boolean is_cache;
    @ParcelableThisPlease
    @JsonField(name = "is_basic")
    public boolean is_basic;

    @ParcelableThisPlease
    @JsonField(name = "extras")
    @CursorField(value = CachedUsers.EXTRAS, converter = LoganSquareCursorFieldConverter.class)
    public Extras extras;

    @ParcelableNoThanks
    @CursorField(CachedUsers.LAST_SEEN)
    public long last_seen;

    @ParcelableNoThanks
    @CursorField(value = CachedUsers.SCORE, excludeWrite = true)
    public int score;

    @ParcelableThisPlease
    public int color;
    @ParcelableThisPlease
    public String nickname;

    public static final Creator<ParcelableUser> CREATOR = new Creator<ParcelableUser>() {
        @Override
        public ParcelableUser createFromParcel(Parcel source) {
            ParcelableUser target = new ParcelableUser();
            ParcelableUserParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public ParcelableUser[] newArray(int size) {
            return new ParcelableUser[size];
        }
    };

    public ParcelableUser() {
    }

    public ParcelableUser(final UserKey account_key, final UserKey key, final String name,
                          final String screenName, final String profileImageUrl) {
        this.account_key = account_key;
        this.key = key;
        this.name = name;
        this.screen_name = screenName;
        this.profile_image_url = profileImageUrl;
        is_cache = true;
        is_basic = true;
    }


    @AfterCursorObjectCreated
    void afterCursorObjectCreated() {
        is_cache = true;
        if (description_unescaped == null) {
            description_unescaped = description_plain;
        }
        is_basic = description_plain == null || url == null || location == null;
    }

    @Override
    public int compareTo(@NonNull final ParcelableUser that) {
        final long diff = position - that.position;
        if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) diff;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParcelableUser user = (ParcelableUser) o;

        if (account_key != null ? !account_key.equals(user.account_key) : user.account_key != null)
            return false;
        return key.equals(user.key);

    }

    @Override
    public int hashCode() {
        return calculateHashCode(account_key, key);
    }

    public static int calculateHashCode(UserKey accountKey, UserKey key) {
        int result = accountKey != null ? key.hashCode() : 0;
        result = 31 * result + key.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ParcelableUser{" +
                "account_key=" + account_key +
                ", account_color=" + account_color +
                ", key=" + key +
                ", created_at=" + created_at +
                ", position=" + position +
                ", is_protected=" + is_protected +
                ", is_verified=" + is_verified +
                ", is_follow_request_sent=" + is_follow_request_sent +
                ", is_following=" + is_following +
                ", description_plain='" + description_plain + '\'' +
                ", name='" + name + '\'' +
                ", screen_name='" + screen_name + '\'' +
                ", location='" + location + '\'' +
                ", profile_image_url='" + profile_image_url + '\'' +
                ", profile_banner_url='" + profile_banner_url + '\'' +
                ", profile_background_url='" + profile_background_url + '\'' +
                ", url='" + url + '\'' +
                ", url_expanded='" + url_expanded + '\'' +
                ", description_unescaped='" + description_unescaped + '\'' +
                ", description_spans=" + Arrays.toString(description_spans) +
                ", followers_count=" + followers_count +
                ", friends_count=" + friends_count +
                ", statuses_count=" + statuses_count +
                ", favorites_count=" + favorites_count +
                ", listed_count=" + listed_count +
                ", media_count=" + media_count +
                ", background_color=" + background_color +
                ", link_color=" + link_color +
                ", text_color=" + text_color +
                ", is_cache=" + is_cache +
                ", is_basic=" + is_basic +
                ", extras=" + extras +
                ", color=" + color +
                ", nickname='" + nickname + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableUserParcelablePlease.writeToParcel(this, dest, flags);
    }

    @ParcelablePlease
    @JsonObject
    public static class Extras implements Parcelable {

        @JsonField(name = "statusnet_profile_url")
        @ParcelableThisPlease
        public String statusnet_profile_url;
        @JsonField(name = "ostatus_uri")
        @ParcelableThisPlease
        public String ostatus_uri;
        @JsonField(name = "profile_image_url_original")
        @ParcelableThisPlease
        public String profile_image_url_original;
        @JsonField(name = "profile_image_url_profile_size")
        @ParcelableThisPlease
        public String profile_image_url_profile_size;
        @JsonField(name = "groups_count")
        @ParcelableThisPlease
        public long groups_count = -1;
        @JsonField(name = "unique_id")
        @ParcelableThisPlease
        public String unique_id;
        @JsonField(name = "blocking")
        @ParcelableThisPlease
        public boolean blocking;
        @JsonField(name = "blocked_by")
        @ParcelableThisPlease
        public boolean blocked_by;
        @JsonField(name = "followed_by")
        @ParcelableThisPlease
        public boolean followed_by;
        @JsonField(name = "muting")
        @ParcelableThisPlease
        public boolean muting;
        @JsonField(name = "pinned_status_ids")
        @ParcelableThisPlease
        public String[] pinned_status_ids;


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            ParcelableUser$ExtrasParcelablePlease.writeToParcel(this, dest, flags);
        }

        @Override
        public String toString() {
            return "Extras{" +
                    "statusnet_profile_url='" + statusnet_profile_url + '\'' +
                    ", ostatus_uri='" + ostatus_uri + '\'' +
                    ", profile_image_url_original='" + profile_image_url_original + '\'' +
                    ", profile_image_url_profile_size='" + profile_image_url_profile_size + '\'' +
                    ", groups_count=" + groups_count +
                    ", unique_id='" + unique_id + '\'' +
                    ", blocking=" + blocking +
                    ", blocked_by=" + blocked_by +
                    ", followed_by=" + followed_by +
                    ", muting=" + muting +
                    ", pinned_status_ids=" + Arrays.toString(pinned_status_ids) +
                    '}';
        }

        public static final Creator<Extras> CREATOR = new Creator<Extras>() {
            @Override
            public Extras createFromParcel(Parcel source) {
                Extras target = new Extras();
                ParcelableUser$ExtrasParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public Extras[] newArray(int size) {
                return new Extras[size];
            }
        };
    }
}

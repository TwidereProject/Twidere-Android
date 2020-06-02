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

package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.commons.objectcursor.LoganSquareCursorFieldConverter;
import org.mariotaku.library.objectcursor.annotation.AfterCursorObjectCreated;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.FilterStringsFieldConverter;
import org.mariotaku.twidere.model.util.FilterUserKeysFieldConverter;
import org.mariotaku.twidere.model.util.UserKeyConverter;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Comparator;

@CursorObject(valuesCreator = true, tableInfo = true)
@JsonObject
@ParcelablePlease
public class ParcelableStatus implements Parcelable, Comparable<ParcelableStatus>, Cloneable {

    public static final Comparator<ParcelableStatus> REVERSE_COMPARATOR = (object1, object2) -> object2.compareTo(object1);
    public static final Creator<ParcelableStatus> CREATOR = new Creator<ParcelableStatus>() {
        @Override
        public ParcelableStatus createFromParcel(Parcel source) {
            ParcelableStatus target = new ParcelableStatus();
            ParcelableStatusParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public ParcelableStatus[] newArray(int size) {
            return new ParcelableStatus[size];
        }
    };
    @CursorField(value = Statuses._ID, excludeWrite = true, type = TwidereDataStore.TYPE_PRIMARY_KEY)
    public long _id;

    @JsonField(name = "id")
    @CursorField(Statuses.ID)
    @NonNull
    public String id;

    @JsonField(name = "account_id", typeConverter = UserKeyConverter.class)
    @CursorField(value = Statuses.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    @NonNull
    public UserKey account_key;

    @JsonField(name = "sort_id")
    @CursorField(Statuses.SORT_ID)
    public long sort_id = -1;

    @JsonField(name = "position_key")
    @CursorField(Statuses.POSITION_KEY)
    public long position_key;

    @JsonField(name = "timestamp")
    @CursorField(Statuses.TIMESTAMP)
    public long timestamp;

    @JsonField(name = "user_id", typeConverter = UserKeyConverter.class)
    @CursorField(value = Statuses.USER_KEY, converter = UserKeyCursorFieldConverter.class)
    @NonNull
    public UserKey user_key;

    @JsonField(name = "retweet_id")
    @CursorField(Statuses.RETWEET_ID)
    public String retweet_id;

    @JsonField(name = "retweeted_by_user_id", typeConverter = UserKeyConverter.class)
    @CursorField(value = Statuses.RETWEETED_BY_USER_KEY, converter = UserKeyCursorFieldConverter.class)
    @Nullable
    public UserKey retweeted_by_user_key;

    @JsonField(name = "retweet_timestamp")
    @CursorField(Statuses.RETWEET_TIMESTAMP)
    public long retweet_timestamp = -1;

    @JsonField(name = "retweet_count")
    @CursorField(Statuses.RETWEET_COUNT)
    public long retweet_count;

    @JsonField(name = "favorite_count")
    @CursorField(Statuses.FAVORITE_COUNT)
    public long favorite_count;

    @JsonField(name = "reply_count")
    @CursorField(Statuses.REPLY_COUNT)
    public long reply_count;

    @JsonField(name = "in_reply_to_status_id")
    @CursorField(Statuses.IN_REPLY_TO_STATUS_ID)
    public String in_reply_to_status_id;

    @JsonField(name = "in_reply_to_user_id", typeConverter = UserKeyConverter.class)
    @CursorField(value = Statuses.IN_REPLY_TO_USER_KEY, converter = UserKeyCursorFieldConverter.class)
    @Nullable
    public UserKey in_reply_to_user_key;

    @JsonField(name = "my_retweet_id")
    @CursorField(Statuses.MY_RETWEET_ID)
    public String my_retweet_id;

    @JsonField(name = "quoted_id")
    @CursorField(Statuses.QUOTED_ID)
    public String quoted_id;

    @JsonField(name = "quoted_timestamp")
    @CursorField(Statuses.QUOTED_TIMESTAMP)
    public long quoted_timestamp;

    @JsonField(name = "quoted_user_id", typeConverter = UserKeyConverter.class)
    @CursorField(value = Statuses.QUOTED_USER_KEY, converter = UserKeyCursorFieldConverter.class)
    @Nullable
    public UserKey quoted_user_key;

    @JsonField(name = "is_gap")
    @CursorField(Statuses.IS_GAP)
    public boolean is_gap;

    @JsonField(name = "is_retweet")
    @CursorField(Statuses.IS_RETWEET)
    public boolean is_retweet;

    @JsonField(name = "retweeted")
    @CursorField(Statuses.RETWEETED)
    public boolean retweeted;

    @JsonField(name = "is_favorite")
    @CursorField(Statuses.IS_FAVORITE)
    public boolean is_favorite;

    @JsonField(name = "is_possibly_sensitive")
    @CursorField(Statuses.IS_POSSIBLY_SENSITIVE)
    public boolean is_possibly_sensitive;

    @JsonField(name = "user_is_following")
    @CursorField(Statuses.IS_FOLLOWING)
    public boolean user_is_following;

    @JsonField(name = "user_is_protected")
    @CursorField(Statuses.IS_PROTECTED)
    public boolean user_is_protected;

    @JsonField(name = "user_is_verified")
    @CursorField(Statuses.IS_VERIFIED)
    public boolean user_is_verified;

    @JsonField(name = "is_quote")
    @CursorField(Statuses.IS_QUOTE)
    public boolean is_quote;

    @JsonField(name = "quoted_user_is_protected")
    @CursorField(Statuses.QUOTED_USER_IS_PROTECTED)
    public boolean quoted_user_is_protected;

    @JsonField(name = "quoted_user_is_verified")
    @CursorField(Statuses.QUOTED_USER_IS_VERIFIED)
    public boolean quoted_user_is_verified;

    @JsonField(name = "retweeted_by_user_name")
    @CursorField(Statuses.RETWEETED_BY_USER_NAME)
    public String retweeted_by_user_name;

    @JsonField(name = "retweeted_by_user_screen_name")
    @CursorField(Statuses.RETWEETED_BY_USER_SCREEN_NAME)
    public String retweeted_by_user_screen_name;

    @JsonField(name = "retweeted_by_user_profile_image")
    @CursorField(Statuses.RETWEETED_BY_USER_PROFILE_IMAGE)
    public String retweeted_by_user_profile_image;

    @JsonField(name = "text_plain")
    @CursorField(Statuses.TEXT_PLAIN)
    public String text_plain;

    @JsonField(name = "lang")
    @CursorField(Statuses.LANG)
    public String lang;

    @JsonField(name = "user_name")
    @CursorField(Statuses.USER_NAME)
    public String user_name;

    @JsonField(name = "user_screen_name")
    @CursorField(Statuses.USER_SCREEN_NAME)
    public String user_screen_name;

    @JsonField(name = "in_reply_to_name")
    @CursorField(Statuses.IN_REPLY_TO_USER_NAME)
    public String in_reply_to_name;

    @JsonField(name = "in_reply_to_screen_name")
    @CursorField(Statuses.IN_REPLY_TO_USER_SCREEN_NAME)
    public String in_reply_to_screen_name;

    @JsonField(name = "source")
    @CursorField(Statuses.SOURCE)
    @Nullable
    public String source;

    @JsonField(name = "user_profile_image_url")
    @CursorField(Statuses.USER_PROFILE_IMAGE)
    public String user_profile_image_url;

    @JsonField(name = "text_unescaped")
    @CursorField(Statuses.TEXT_UNESCAPED)
    public String text_unescaped;
    @Nullable

    @JsonField(name = "card_name")
    @CursorField(Statuses.CARD_NAME)
    public String card_name;

    @JsonField(name = "quoted_text_plain")
    @CursorField(Statuses.QUOTED_TEXT_PLAIN)
    public String quoted_text_plain;

    @JsonField(name = "quoted_text_unescaped")
    @CursorField(Statuses.QUOTED_TEXT_UNESCAPED)
    public String quoted_text_unescaped;

    @JsonField(name = "quoted_source")
    @CursorField(Statuses.QUOTED_SOURCE)
    public String quoted_source;

    @JsonField(name = "quoted_user_name")
    @CursorField(Statuses.QUOTED_USER_NAME)
    public String quoted_user_name;

    @JsonField(name = "quoted_user_screen_name")
    @CursorField(Statuses.QUOTED_USER_SCREEN_NAME)
    public String quoted_user_screen_name;

    @JsonField(name = "quoted_user_profile_image")
    @CursorField(Statuses.QUOTED_USER_PROFILE_IMAGE)
    public String quoted_user_profile_image;

    @JsonField(name = "location")
    @CursorField(value = Statuses.LOCATION, converter = ParcelableLocation.Converter.class)
    public ParcelableLocation location;

    @JsonField(name = "place_full_name")
    @CursorField(value = Statuses.PLACE_FULL_NAME)
    public String place_full_name;

    @JsonField(name = "mentions")
    @CursorField(value = Statuses.MENTIONS_JSON, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableUserMention[] mentions;

    // TODO: Simplify for list loader
    @JsonField(name = "media")
    @CursorField(value = Statuses.MEDIA_JSON, converter = LoganSquareCursorFieldConverter.class)
    @Nullable
    public ParcelableMedia[] media;

    // TODO: Simplify for list loader
    @JsonField(name = "quoted_media")
    @CursorField(value = Statuses.QUOTED_MEDIA_JSON, converter = LoganSquareCursorFieldConverter.class)
    @Nullable
    public ParcelableMedia[] quoted_media;

    @JsonField(name = "card")
    @CursorField(value = Statuses.CARD, converter = LoganSquareCursorFieldConverter.class)
    @Nullable
    public ParcelableCardEntity card;

    @JsonField(name = "extras")
    @CursorField(value = Statuses.EXTRAS, converter = LoganSquareCursorFieldConverter.class)
    @Nullable
    public Extras extras;

    @JsonField(name = "spans")
    @CursorField(value = Statuses.SPANS, converter = LoganSquareCursorFieldConverter.class)
    public SpanItem[] spans;

    @JsonField(name = "quoted_spans")
    @CursorField(value = Statuses.QUOTED_SPANS, converter = LoganSquareCursorFieldConverter.class)
    public SpanItem[] quoted_spans;

    @JsonField(name = "account_color")
    @CursorField(Statuses.ACCOUNT_COLOR)
    public int account_color;

    @CursorField(Statuses.INSERTED_DATE)
    public long inserted_date;

    @FilterFlags
    @JsonField(name = "filter_flags")
    @CursorField(Statuses.FILTER_FLAGS)
    public long filter_flags;

    @JsonField(name = "filter_users")
    @CursorField(value = Statuses.FILTER_USERS, converter = FilterUserKeysFieldConverter.class)
    public UserKey[] filter_users;

    @JsonField(name = "filter_sources")
    @CursorField(value = Statuses.FILTER_SOURCES, converter = FilterStringsFieldConverter.class)
    public String[] filter_sources;

    @JsonField(name = "filter_links")
    @CursorField(value = Statuses.FILTER_LINKS, converter = FilterStringsFieldConverter.class)
    public String[] filter_links;

    @JsonField(name = "filter_names")
    @CursorField(value = Statuses.FILTER_NAMES, converter = FilterStringsFieldConverter.class)
    public String[] filter_names;

    @JsonField(name = "filter_texts")
    @CursorField(value = Statuses.FILTER_TEXTS)
    public String filter_texts;

    @JsonField(name = "filter_descriptions")
    @CursorField(value = Statuses.FILTER_DESCRIPTIONS)
    public String filter_descriptions;

    public transient boolean is_pinned_status;
    public transient boolean is_filtered;

    public ParcelableStatus() {
    }

    public static int calculateHashCode(UserKey accountKey, String id) {
        int result = id.hashCode();
        result = 31 * result + accountKey.hashCode();
        return result;
    }

    @Override
    public int compareTo(@NonNull final ParcelableStatus another) {
        long diff = timestamp - another.timestamp;
        if (diff == 0) {
            diff = sort_id - another.sort_id;
        }
        if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) diff;
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

    @Override
    public String toString() {
        return "ParcelableStatus{" +
                "_id=" + _id +
                ", id='" + id + '\'' +
                ", account_key=" + account_key +
                ", sort_id=" + sort_id +
                ", position_key=" + position_key +
                ", timestamp=" + timestamp +
                ", user_key=" + user_key +
                ", retweet_id='" + retweet_id + '\'' +
                ", retweeted_by_user_key=" + retweeted_by_user_key +
                ", retweet_timestamp=" + retweet_timestamp +
                ", retweet_count=" + retweet_count +
                ", favorite_count=" + favorite_count +
                ", reply_count=" + reply_count +
                ", in_reply_to_status_id='" + in_reply_to_status_id + '\'' +
                ", in_reply_to_user_key=" + in_reply_to_user_key +
                ", my_retweet_id='" + my_retweet_id + '\'' +
                ", quoted_id='" + quoted_id + '\'' +
                ", quoted_timestamp=" + quoted_timestamp +
                ", quoted_user_key=" + quoted_user_key +
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
                ", quoted_text_plain='" + quoted_text_plain + '\'' +
                ", quoted_text_unescaped='" + quoted_text_unescaped + '\'' +
                ", quoted_source='" + quoted_source + '\'' +
                ", quoted_user_name='" + quoted_user_name + '\'' +
                ", quoted_user_screen_name='" + quoted_user_screen_name + '\'' +
                ", quoted_user_profile_image='" + quoted_user_profile_image + '\'' +
                ", location=" + location +
                ", place_full_name='" + place_full_name + '\'' +
                ", mentions=" + Arrays.toString(mentions) +
                ", media=" + Arrays.toString(media) +
                ", quoted_media=" + Arrays.toString(quoted_media) +
                ", card=" + card +
                ", extras=" + extras +
                ", spans=" + Arrays.toString(spans) +
                ", quoted_spans=" + Arrays.toString(quoted_spans) +
                ", account_color=" + account_color +
                ", inserted_date=" + inserted_date +
                ", filter_flags=" + filter_flags +
                ", filter_users=" + Arrays.toString(filter_users) +
                ", filter_sources=" + Arrays.toString(filter_sources) +
                ", filter_links=" + Arrays.toString(filter_links) +
                ", filter_names=" + Arrays.toString(filter_names) +
                ", filter_texts='" + filter_texts + '\'' +
                ", filter_descriptions='" + filter_descriptions + '\'' +
                ", is_pinned_status=" + is_pinned_status +
                ", is_filtered=" + is_filtered +
                '}';
    }

    @AfterCursorObjectCreated
    void finishCursorObjectCreation() {
        card_name = card != null ? card.name : null;
        fixSortId();
    }

    @OnJsonParseComplete
    void onParseComplete() {
        fixSortId();
    }

    private void fixSortId() {
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

        public static final Creator<Extras> CREATOR = new Creator<Extras>() {
            @Override
            public Extras createFromParcel(Parcel source) {
                Extras target = new Extras();
                ParcelableStatus$ExtrasParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public Extras[] newArray(int size) {
                return new Extras[size];
            }
        };

        @JsonField(name = "external_url")
        public String external_url;

        @JsonField(name = "entities_url")
        public String[] entities_url;

        @JsonField(name = "quoted_external_url")
        public String quoted_external_url;

        @JsonField(name = "retweeted_external_url")
        public String retweeted_external_url;

        @JsonField(name = "statusnet_conversation_id")
        public String statusnet_conversation_id;

        @JsonField(name = "support_entities")
        public boolean support_entities;

        @JsonField(name = "user_profile_image_url_fallback")
        @Nullable
        public String user_profile_image_url_fallback;

        @JsonField(name = "user_statusnet_profile_url")
        public String user_statusnet_profile_url;

        @JsonField(name = "display_text_range")
        @Nullable
        public int[] display_text_range;

        @JsonField(name = "quoted_display_text_range")
        @Nullable
        public int[] quoted_display_text_range;

        @JsonField(name = "conversation_id")
        @Nullable
        public String conversation_id;

        @JsonField(name = "summary_text")
        @Nullable
        public String summary_text;

        @JsonField(name = "visibility")
        @Nullable
        public String visibility;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            ParcelableStatus$ExtrasParcelablePlease.writeToParcel(this, dest, flags);
        }
    }

    /**
     * Flags for filtering some kind of tweet.
     * We use bitwise instead of string comparisons because it's much faster.
     * <p>
     * DO NOT CHANGE ONCE DEFINED!
     */
    @LongDef(value = {
            FilterFlags.QUOTE_NOT_AVAILABLE,
            FilterFlags.BLOCKING_USER,
            FilterFlags.BLOCKED_BY_USER,
            FilterFlags.POSSIBLY_SENSITIVE
    }, flag = true)
    @Retention(RetentionPolicy.SOURCE)
    public @interface FilterFlags {
        /**
         * Original tweet of a quote tweet is unavailable.
         * Happens when:
         * <p>
         * <li/>You were blocked by this user
         * <li/>You blocked/muted this user
         * <li/>Original tweet was marked sensitive and your account settings blocked them
         * <li/>Original tweet was deleted
         * <li/>Original tweet author blocked or blocked by quoted tweet author
         */
        long QUOTE_NOT_AVAILABLE = 0x1;
        /**
         * Original author of a quote/retweet was blocked by you
         */
        long BLOCKING_USER = 0x2;
        /**
         * You were blocked by original author of a quote/retweet
         */
        long BLOCKED_BY_USER = 0x4;
        /**
         * Status possibly sensitive (NSFW etc)
         */
        long POSSIBLY_SENSITIVE = 0x8;
        /**
         * Status (or quote) has media
         */
        long HAS_MEDIA = 0x10;
    }
}

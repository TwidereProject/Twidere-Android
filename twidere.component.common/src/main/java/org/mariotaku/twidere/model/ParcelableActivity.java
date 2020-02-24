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
import androidx.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.commons.objectcursor.LoganSquareCursorFieldConverter;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.UserKeyConverter;
import org.mariotaku.twidere.model.util.UserKeysConverter;
import org.mariotaku.twidere.model.util.UserKeysCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;

import java.util.Arrays;

@ParcelablePlease
@JsonObject
@CursorObject(valuesCreator = true, tableInfo = true)
public class ParcelableActivity extends ParcelableStatus implements Parcelable {

    public static final Creator<ParcelableActivity> CREATOR = new Creator<ParcelableActivity>() {
        @Override
        public ParcelableActivity createFromParcel(Parcel source) {
            ParcelableActivity target = new ParcelableActivity();
            ParcelableActivityParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public ParcelableActivity[] newArray(int size) {
            return new ParcelableActivity[size];
        }
    };

    @JsonField(name = "action")
    @CursorField(value = Activities.ACTION)
    public String action;

    @JsonField(name = "max_position")
    @CursorField(value = Activities.MAX_SORT_POSITION)
    public long max_sort_position;
    @JsonField(name = "min_position")
    @CursorField(value = Activities.MIN_SORT_POSITION)
    public long min_sort_position;

    @JsonField(name = "max_request_position")
    @CursorField(value = Activities.MAX_REQUEST_POSITION)
    public String max_position;

    @JsonField(name = "min_request_position")
    @CursorField(value = Activities.MIN_REQUEST_POSITION)
    public String min_position;


    @JsonField(name = "source_ids", typeConverter = UserKeysConverter.class)
    @CursorField(value = Activities.SOURCE_KEYS, converter = UserKeysCursorFieldConverter.class)
    public UserKey[] source_keys;

    @JsonField(name = "sources")
    @CursorField(value = Activities.SOURCES, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableUser[] sources;

    @JsonField(name = "targets")
    @CursorField(value = Activities.TARGETS, converter = LoganSquareCursorFieldConverter.class)
    public RelatedObject targets;

    @JsonField(name = "target_objects")
    @CursorField(value = Activities.TARGET_OBJECTS, converter = LoganSquareCursorFieldConverter.class)
    public RelatedObject target_objects;

    @JsonField(name = "sources_lite")
    @CursorField(value = Activities.SOURCES_LITE, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableLiteUser[] sources_lite;

    @JsonField(name = "summary_line")
    @CursorField(value = Activities.SUMMARY_LINE, converter = LoganSquareCursorFieldConverter.class)
    public SummaryLine[] summary_line;

    @CursorField(Activities.HAS_FOLLOWING_SOURCE)
    @JsonField(name = "has_following_source")
    public boolean has_following_source = true;

    public transient ParcelableLiteUser[] after_filtered_sources;

    public ParcelableActivity() {
    }

    public static int calculateHashCode(UserKey accountKey, long timestamp, long maxPosition, long minPosition) {
        int result = accountKey.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (maxPosition ^ (maxPosition >>> 32));
        result = 31 * result + (int) (minPosition ^ (minPosition >>> 32));
        return result;
    }

    @Override
    public int hashCode() {
        return calculateHashCode(account_key, timestamp, max_sort_position, min_sort_position);
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof ParcelableActivity)) return false;
        final ParcelableActivity activity = (ParcelableActivity) that;
        return max_sort_position == activity.max_sort_position && min_sort_position == activity.min_sort_position;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableActivityParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public String toString() {
        return "ParcelableActivity{" +
                "action='" + action + '\'' +
                ", max_sort_position=" + max_sort_position +
                ", min_sort_position=" + min_sort_position +
                ", max_position='" + max_position + '\'' +
                ", min_position='" + min_position + '\'' +
                ", source_keys=" + Arrays.toString(source_keys) +
                ", _id=" + _id +
                ", sources=" + Arrays.toString(sources) +
                ", id='" + id + '\'' +
                ", targets=" + targets +
                ", account_key=" + account_key +
                ", target_objects=" + target_objects +
                ", sort_id=" + sort_id +
                ", position_key=" + position_key +
                ", sources_lite=" + Arrays.toString(sources_lite) +
                ", timestamp=" + timestamp +
                ", summary_line=" + Arrays.toString(summary_line) +
                ", has_following_source=" + has_following_source +
                ", user_key=" + user_key +
                ", after_filtered_sources=" + Arrays.toString(after_filtered_sources) +
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

    @ParcelablePlease
    @JsonObject
    public static class SummaryLine implements Parcelable {
        @JsonField(name = "key", typeConverter = UserKeyConverter.class)
        public UserKey key;
        @JsonField(name = "name")
        public String name;
        @JsonField(name = "screen_name")
        public String screen_name;
        @JsonField(name = "content")
        public String content;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            ParcelableActivity$SummaryLineParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<SummaryLine> CREATOR = new Creator<SummaryLine>() {
            public SummaryLine createFromParcel(Parcel source) {
                SummaryLine target = new SummaryLine();
                ParcelableActivity$SummaryLineParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public SummaryLine[] newArray(int size) {
                return new SummaryLine[size];
            }
        };
    }

    @ParcelablePlease
    @JsonObject
    public static class RelatedObject implements Parcelable {

        @JsonField(name = "users")
        public ParcelableUser[] users;

        @JsonField(name = "statuses")
        public ParcelableStatus[] statuses;

        @JsonField(name = "user_lists")
        public ParcelableUserList[] user_lists;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            ParcelableActivity$RelatedObjectParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<RelatedObject> CREATOR = new Creator<RelatedObject>() {
            public RelatedObject createFromParcel(Parcel source) {
                RelatedObject target = new RelatedObject();
                ParcelableActivity$RelatedObjectParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public RelatedObject[] newArray(int size) {
                return new RelatedObject[size];
            }
        };

        @Nullable
        public static RelatedObject statuses(@Nullable ParcelableStatus... statuses) {
            RelatedObject object = new RelatedObject();
            object.statuses = statuses;
            return object;
        }
    }
}

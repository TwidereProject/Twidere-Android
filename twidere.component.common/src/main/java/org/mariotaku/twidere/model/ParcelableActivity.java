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
import android.support.annotation.Nullable;

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

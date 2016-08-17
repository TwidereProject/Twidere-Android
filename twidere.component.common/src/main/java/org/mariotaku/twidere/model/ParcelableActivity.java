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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.commons.objectcursor.LoganSquareCursorFieldConverter;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.UserKeyConverter;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.model.util.UserKeysConverter;
import org.mariotaku.twidere.model.util.UserKeysCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;

import java.util.Arrays;

@ParcelablePlease(allFields = false)
@JsonObject
@CursorObject(valuesCreator = true, tableInfo = true)
public class ParcelableActivity implements Comparable<ParcelableActivity>, Parcelable {

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

    @ParcelableThisPlease
    @CursorField(value = Activities._ID, excludeWrite = true, type = TwidereDataStore.TYPE_PRIMARY_KEY)
    public long _id;
    @ParcelableThisPlease
    @JsonField(name = "position_key")
    @CursorField(Activities.POSITION_KEY)
    public long position_key;
    @ParcelableThisPlease
    @JsonField(name = "account_id", typeConverter = UserKeyConverter.class)
    @CursorField(value = Activities.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;
    @ParcelableThisPlease
    @JsonField(name = "timestamp")
    @CursorField(value = Activities.TIMESTAMP)
    public long timestamp;
    @ParcelableThisPlease
    @JsonField(name = "max_position")
    @CursorField(value = Activities.MAX_SORT_POSITION)
    public long max_sort_position;
    @ParcelableThisPlease
    @JsonField(name = "min_position")
    @CursorField(value = Activities.MIN_SORT_POSITION)
    public long min_sort_position;
    @JsonField(name = "max_request_position")
    @CursorField(value = Activities.MAX_REQUEST_POSITION)
    public String max_position;
    @ParcelableThisPlease
    @JsonField(name = "min_request_position")
    @CursorField(value = Activities.MIN_REQUEST_POSITION)
    public String min_position;
    @ParcelableThisPlease
    @JsonField(name = "action")
    @CursorField(value = Activities.ACTION)
    public String action;

    @ParcelableThisPlease
    @JsonField(name = "source_ids", typeConverter = UserKeysConverter.class)
    @CursorField(value = Activities.SOURCE_IDS, converter = UserKeysCursorFieldConverter.class)
    public UserKey[] source_ids;

    @ParcelableThisPlease
    @JsonField(name = "sources")
    @CursorField(value = Activities.SOURCES, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableUser[] sources;
    @ParcelableThisPlease
    @JsonField(name = "target_users")
    @CursorField(value = Activities.TARGET_USERS, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableUser[] target_users;
    @ParcelableThisPlease
    @JsonField(name = "target_statuses")
    @CursorField(value = Activities.TARGET_STATUSES, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableStatus[] target_statuses;
    @ParcelableThisPlease
    @JsonField(name = "target_user_lists")
    @CursorField(value = Activities.TARGET_USER_LISTS, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableUserList[] target_user_lists;

    @ParcelableThisPlease
    @JsonField(name = "target_object_user_lists")
    @CursorField(value = Activities.TARGET_OBJECT_USER_LISTS, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableUserList[] target_object_user_lists;
    @ParcelableThisPlease
    @JsonField(name = "target_object_statuses")
    @CursorField(value = Activities.TARGET_OBJECT_STATUSES, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableStatus[] target_object_statuses;
    @ParcelableThisPlease
    @JsonField(name = "target_object_users")
    @CursorField(value = Activities.TARGET_OBJECT_USERS, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableUser[] target_object_users;
    @ParcelableThisPlease
    @JsonField(name = "is_gap")
    @CursorField(value = Activities.IS_GAP)
    public boolean is_gap;
    @ParcelableThisPlease
    @JsonField(name = "status_user_following")
    @CursorField(value = Activities.STATUS_USER_FOLLOWING, excludeWrite = true)
    public boolean status_user_following;

    @ParcelableThisPlease
    @JsonField(name = "account_color")
    @CursorField(Activities.ACCOUNT_COLOR)
    public int account_color;
    @ParcelableThisPlease
    @JsonField(name = "status_user_color")
    @CursorField(Activities.STATUS_USER_COLOR)
    public int status_user_color;
    @ParcelableThisPlease
    @JsonField(name = "status_quoted_user_color")
    @CursorField(Activities.STATUS_QUOTED_USER_COLOR)
    public int status_quoted_user_color;
    @ParcelableThisPlease
    @JsonField(name = "status_retweet_user_color")
    @CursorField(Activities.STATUS_RETWEET_USER_COLOR)
    public int status_retweet_user_color;

    @ParcelableThisPlease
    @JsonField(name = "status_user_nickname")
    @CursorField(Activities.STATUS_USER_NICKNAME)
    public String status_user_nickname;
    @ParcelableThisPlease
    @JsonField(name = "status_quoted_user_nickname")
    @CursorField(Activities.STATUS_QUOTED_USER_NICKNAME)
    public String status_quoted_user_nickname;
    @ParcelableThisPlease
    @JsonField(name = "status_retweet_user_nickname")
    @CursorField(Activities.STATUS_RETWEET_USER_NICKNAME)
    public String status_retweet_user_nickname;
    @ParcelableThisPlease
    @JsonField(name = "status_in_reply_to_user_nickname")
    @CursorField(Activities.STATUS_IN_REPLY_TO_USER_NICKNAME)
    public String status_in_reply_to_user_nickname;


    @CursorField(value = Activities.STATUS_QUOTE_SPANS, converter = LoganSquareCursorFieldConverter.class)
    public SpanItem[] status_quote_spans;

    @CursorField(Activities.STATUS_QUOTE_TEXT_PLAIN)
    public String status_quote_text_plain;

    @CursorField(Activities.STATUS_QUOTE_SOURCE)
    public String status_quote_source;

    @CursorField(value = Activities.STATUS_QUOTED_USER_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey status_quoted_user_key;

    @CursorField(value = Activities.STATUS_USER_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey status_user_key;

    @CursorField(value = Activities.STATUS_SPANS, converter = LoganSquareCursorFieldConverter.class)
    public SpanItem[] status_spans;

    @CursorField(Activities.STATUS_TEXT_PLAIN)
    public String status_text_plain;

    @CursorField(Activities.STATUS_SOURCE)
    public String status_source;

    @CursorField(value = Activities.STATUS_RETWEETED_BY_USER_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey status_retweeted_by_user_key;

    @CursorField(Activities.INSERTED_DATE)
    public long inserted_date;

    @CursorField(Activities.STATUS_ID)
    @Nullable
    public String status_id;

    @CursorField(Activities.STATUS_RETWEET_ID)
    @Nullable
    public String status_retweet_id;

    @CursorField(Activities.STATUS_MY_RETWEET_ID)
    @Nullable
    public String status_my_retweet_id;

    @CursorField(Activities.HAS_FOLLOWING_SOURCE)
    @ParcelableThisPlease
    @JsonField(name = "has_following_source")
    public boolean has_following_source = true;

    public transient UserKey[] after_filtered_source_ids;
    public transient ParcelableUser[] after_filtered_sources;


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
    public String toString() {
        return "ParcelableActivity{" +
                "_id=" + _id +
                ", account_key=" + account_key +
                ", timestamp=" + timestamp +
                ", max_sort_position=" + max_sort_position +
                ", min_sort_position=" + min_sort_position +
                ", max_position='" + max_position + '\'' +
                ", min_position='" + min_position + '\'' +
                ", action='" + action + '\'' +
                ", source_ids=" + Arrays.toString(source_ids) +
                ", sources=" + Arrays.toString(sources) +
                ", target_users=" + Arrays.toString(target_users) +
                ", target_statuses=" + Arrays.toString(target_statuses) +
                ", target_user_lists=" + Arrays.toString(target_user_lists) +
                ", target_object_user_lists=" + Arrays.toString(target_object_user_lists) +
                ", target_object_statuses=" + Arrays.toString(target_object_statuses) +
                ", target_object_users=" + Arrays.toString(target_object_users) +
                ", is_gap=" + is_gap +
                ", status_user_following=" + status_user_following +
                ", account_color=" + account_color +
                ", after_filtered_source_ids=" + Arrays.toString(after_filtered_source_ids) +
                ", after_filtered_sources=" + Arrays.toString(after_filtered_sources) +
                '}';
    }

    @Override
    public int hashCode() {
        return calculateHashCode(account_key, timestamp, max_sort_position, min_sort_position);
    }

    @Override
    public int compareTo(@NonNull final ParcelableActivity another) {
        final long delta = another.timestamp - timestamp;
        if (delta < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        if (delta > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) delta;
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

}

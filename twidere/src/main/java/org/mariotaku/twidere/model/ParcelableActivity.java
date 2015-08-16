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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.loader.support.ObjectCursorLoader;

import java.util.Arrays;

@ParcelablePlease(allFields = false)
@JsonObject
public class ParcelableActivity implements Comparable<ParcelableActivity>, Parcelable {

    public static final Creator<ParcelableActivity> CREATOR = new Creator<ParcelableActivity>() {
        @Override
        public ParcelableActivity createFromParcel(Parcel source) {
            return new ParcelableActivity(source);
        }

        @Override
        public ParcelableActivity[] newArray(int size) {
            return new ParcelableActivity[size];
        }
    };

    @ParcelableThisPlease
    @JsonField(name = "account_id")
    public long account_id;
    @ParcelableThisPlease
    @JsonField(name = "timestamp")
    public long timestamp;
    @ParcelableThisPlease
    @JsonField(name = "max_position")
    public long max_position;
    @ParcelableThisPlease
    @JsonField(name = "min_position")
    public long min_position;
    @ParcelableThisPlease
    @JsonField(name = "action")
    public int action;

    @ParcelableThisPlease
    @JsonField(name = "sources")
    public ParcelableUser[] sources;
    @ParcelableThisPlease
    @JsonField(name = "target_users")
    public ParcelableUser[] target_users;
    @ParcelableThisPlease
    @JsonField(name = "target_statuses")
    public ParcelableStatus[] target_statuses;
    @ParcelableThisPlease
    @JsonField(name = "target_user_lists")
    public ParcelableUserList[] target_user_lists;

    @ParcelableThisPlease
    @JsonField(name = "target_object_user_lists")
    public ParcelableUserList[] target_object_user_lists;
    @ParcelableThisPlease
    @JsonField(name = "target_object_statuses")
    public ParcelableStatus[] target_object_statuses;
    @ParcelableThisPlease
    @JsonField(name = "target_object_users")
    public ParcelableUser[] target_object_users;
    @ParcelableThisPlease
    @JsonField(name = "is_gap")
    public boolean is_gap;

    public ParcelableActivity() {
    }

    public ParcelableActivity(final Activity activity, final long account_id, boolean is_gap) {
        this.account_id = account_id;
        timestamp = activity.getCreatedAt().getTime();
        action = activity.getAction().getActionId();
        max_position = activity.getMaxPosition();
        min_position = activity.getMinPosition();
        sources = ParcelableUser.fromUsers(activity.getSources(), account_id);
        target_users = ParcelableUser.fromUsers(activity.getTargetUsers(), account_id);
        target_user_lists = ParcelableUserList.fromUserLists(activity.getTargetUserLists(), account_id);
        target_statuses = ParcelableStatus.fromStatuses(activity.getTargetStatuses(), account_id);
        target_object_statuses = ParcelableStatus.fromStatuses(activity.getTargetObjectStatuses(), account_id);
        target_object_user_lists = ParcelableUserList.fromUserLists(activity.getTargetObjectUserLists(), account_id);
        target_object_users = ParcelableUser.fromUsers(activity.getTargetObjectUsers(), account_id);
        this.is_gap = is_gap;
    }

    public ParcelableActivity(Parcel src) {
        ParcelableActivityParcelablePlease.readFromParcel(this, src);
    }

    @Override
    public String toString() {
        return "ParcelableActivity{" +
                "account_id=" + account_id +
                ", timestamp=" + timestamp +
                ", max_position=" + max_position +
                ", min_position=" + min_position +
                ", action=" + action +
                ", sources=" + Arrays.toString(sources) +
                ", target_users=" + Arrays.toString(target_users) +
                ", target_statuses=" + Arrays.toString(target_statuses) +
                ", target_user_lists=" + Arrays.toString(target_user_lists) +
                ", target_object_user_lists=" + Arrays.toString(target_object_user_lists) +
                ", target_object_statuses=" + Arrays.toString(target_object_statuses) +
                ", target_object_users=" + Arrays.toString(target_object_users) +
                ", is_gap=" + is_gap +
                '}';
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
        return max_position == activity.max_position && min_position == activity.min_position;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableActivityParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static class CursorIndices extends ObjectCursor.CursorIndices<ParcelableActivity> {

        public CursorIndices(@NonNull Cursor cursor) {
            super(cursor);
        }

        @Override
        public ParcelableActivity newObject(Cursor cursor) {
            throw new UnsupportedOperationException();
        }
    }
}

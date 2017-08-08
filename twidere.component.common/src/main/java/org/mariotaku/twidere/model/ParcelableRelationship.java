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

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships;

/**
 * Created by mariotaku on 16/2/1.
 */
@ParcelablePlease
@CursorObject(valuesCreator = true, tableInfo = true)
public class ParcelableRelationship implements Parcelable {

    @CursorField(value = CachedRelationships.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;

    @CursorField(value = CachedRelationships.USER_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey user_key;

    @CursorField(CachedRelationships.FOLLOWING)
    public boolean following;

    @CursorField(CachedRelationships.FOLLOWED_BY)
    public boolean followed_by;

    @CursorField(CachedRelationships.BLOCKING)
    public boolean blocking;

    @CursorField(CachedRelationships.BLOCKED_BY)
    public boolean blocked_by;

    @CursorField(CachedRelationships.MUTING)
    public boolean muting;

    @CursorField(CachedRelationships.RETWEET_ENABLED)
    public boolean retweet_enabled;

    @CursorField(CachedRelationships.NOTIFICATIONS_ENABLED)
    public boolean notifications_enabled;

    @CursorField(value = CachedRelationships._ID, excludeWrite = true, type = TwidereDataStore.TYPE_PRIMARY_KEY)
    public long _id;

    public boolean can_dm;

    public boolean filtering;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParcelableRelationship that = (ParcelableRelationship) o;

        if (!account_key.equals(that.account_key)) return false;
        return user_key.equals(that.user_key);

    }

    @Override
    public int hashCode() {
        int result = account_key.hashCode();
        result = 31 * result + user_key.hashCode();
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableRelationshipParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ParcelableRelationship> CREATOR = new Creator<ParcelableRelationship>() {
        public ParcelableRelationship createFromParcel(Parcel source) {
            ParcelableRelationship target = new ParcelableRelationship();
            ParcelableRelationshipParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ParcelableRelationship[] newArray(int size) {
            return new ParcelableRelationship[size];
        }
    };
}

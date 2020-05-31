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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.UserKeyConverter;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends;

/**
 * Created by mariotaku on 2017/2/3.
 */


@CursorObject(valuesCreator = true, tableInfo = true)
@JsonObject
@ParcelablePlease
public class ParcelableTrend implements Parcelable {

    @CursorField(value = CachedTrends._ID, excludeWrite = true, type = TwidereDataStore.TYPE_PRIMARY_KEY)
    long _id;
    @ParcelableThisPlease
    @JsonField(name = "account_id", typeConverter = UserKeyConverter.class)
    @CursorField(value = CachedTrends.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;
    @ParcelableThisPlease
    @JsonField(name = "woe_id")
    @CursorField(CachedTrends.WOEID)
    public int woe_id;
    @ParcelableThisPlease
    @JsonField(name = "timestamp")
    @CursorField(CachedTrends.TIMESTAMP)
    public long timestamp;
    @ParcelableThisPlease
    @JsonField(name = "trend_order")
    @CursorField(CachedTrends.TREND_ORDER)
    public int trend_order;
    @JsonField(name = "name")
    @CursorField(value = CachedTrends.NAME)
    public String name;

    @Override
    public String toString() {
        return "ParcelableTrend{" +
                "_id=" + _id +
                ", account_key=" + account_key +
                ", woe_id=" + woe_id +
                ", timestamp=" + timestamp +
                ", trend_order=" + trend_order +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableTrendParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ParcelableTrend> CREATOR = new Creator<ParcelableTrend>() {
        public ParcelableTrend createFromParcel(Parcel source) {
            ParcelableTrend target = new ParcelableTrend();
            ParcelableTrendParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ParcelableTrend[] newArray(int size) {
            return new ParcelableTrend[size];
        }
    };
}

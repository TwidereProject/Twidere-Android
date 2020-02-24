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

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;

import java.lang.reflect.ParameterizedType;

@ParcelablePlease
@JsonObject
public class ParcelableLocation implements Parcelable {

    @ParcelableThisPlease
    @JsonField(name = "latitude")
    public double latitude;
    @ParcelableThisPlease
    @JsonField(name = "longitude")
    public double longitude;

    public ParcelableLocation() {
    }

    public ParcelableLocation(final double latitude, final double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof ParcelableLocation)) return false;
        final ParcelableLocation other = (ParcelableLocation) obj;
        if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
            return false;
        return Double.doubleToLongBits(longitude) == Double.doubleToLongBits(other.longitude);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = prime * result + (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(longitude);
        result = prime * result + (int) (temp ^ temp >>> 32);
        return result;
    }


    @Override
    public String toString() {
        return latitude + "," + longitude;
    }

    @Nullable
    public static ParcelableLocation valueOf(@NonNull final String locationString) {
        if (locationString == null) return null;
        final String[] longlat = locationString.split(",");
        if (longlat.length != 2) {
            return null;
        }

        ParcelableLocation obj = new ParcelableLocation();
        try {
            obj.latitude = Double.parseDouble(longlat[0]);
            obj.longitude = Double.parseDouble(longlat[1]);
        } catch (NumberFormatException e) {
            return null;
        }
        if (Double.isNaN(obj.latitude) || Double.isNaN(obj.longitude)) return null;
        return obj;
    }

    public static class Converter implements CursorFieldConverter<ParcelableLocation> {
        @Override
        public ParcelableLocation parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) {
            final String locationString = cursor.getString(columnIndex);
            if (locationString == null) return null;
            return valueOf(locationString);
        }

        @Override
        public void writeField(ContentValues values, ParcelableLocation object, String columnName, ParameterizedType fieldType) {
            if (object == null) return;
            values.put(columnName, object.toString());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableLocationParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ParcelableLocation> CREATOR = new Creator<ParcelableLocation>() {
        @Override
        public ParcelableLocation createFromParcel(Parcel source) {
            ParcelableLocation target = new ParcelableLocation();
            ParcelableLocationParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public ParcelableLocation[] newArray(int size) {
            return new ParcelableLocation[size];
        }
    };
}

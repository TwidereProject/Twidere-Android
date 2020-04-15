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

package org.mariotaku.microblog.library.twitter.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 15/5/7.
 */
@ParcelablePlease
@JsonObject
public class Place extends TwitterResponseObject implements TwitterResponse, Comparable<Place>, Parcelable {

    @JsonField(name = "full_name")
    String fullName;

    public GeoLocation[][] getBoundingBoxCoordinates() {
        throw new UnsupportedOperationException();
    }

    public String getBoundingBoxType() {
        throw new UnsupportedOperationException();
    }

    public Place[] getContainedWithIn() {
        throw new UnsupportedOperationException();
    }

    public String getCountry() {
        throw new UnsupportedOperationException();
    }

    public String getCountryCode() {
        throw new UnsupportedOperationException();
    }

    public String getFullName() {
        return fullName;
    }

    public GeoLocation[][] getGeometryCoordinates() {
        throw new UnsupportedOperationException();
    }

    public String getGeometryType() {
        throw new UnsupportedOperationException();
    }

    public String getId() {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        throw new UnsupportedOperationException();
    }

    public String getPlaceType() {
        throw new UnsupportedOperationException();
    }

    public String getStreetAddress() {
        throw new UnsupportedOperationException();
    }

    public String getUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(@NonNull Place another) {
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        PlaceParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel source) {
            Place target = new Place();
            PlaceParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
}

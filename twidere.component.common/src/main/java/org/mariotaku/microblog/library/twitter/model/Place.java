/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.microblog.library.twitter.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

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

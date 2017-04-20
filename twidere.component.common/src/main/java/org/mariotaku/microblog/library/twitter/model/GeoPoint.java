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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 15/5/7.
 */
@ParcelablePlease
@JsonObject
public class GeoPoint implements Parcelable {

    @ParcelableNoThanks
    private volatile GeoLocation geoLocation;

    @JsonField(name = "coordinates")
    double[] coordinates;

    @JsonField(name = "type")
    String type;

    public GeoLocation getGeoLocation() {
        if (coordinates == null) return null;
        if (geoLocation != null) return geoLocation;
        return geoLocation = new GeoLocation(coordinates[0], coordinates[1]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        GeoPointParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<GeoPoint> CREATOR = new Creator<GeoPoint>() {
        @Override
        public GeoPoint createFromParcel(Parcel source) {
            GeoPoint target = new GeoPoint();
            GeoPointParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public GeoPoint[] newArray(int size) {
            return new GeoPoint[size];
        }
    };
}

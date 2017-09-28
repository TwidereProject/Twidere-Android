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
import android.support.annotation.NonNull;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.restfu.http.ValueMap;

/**
 * A data class representing geo location.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
@ParcelablePlease
public class GeoLocation implements ValueMap, Parcelable {

    double latitude;
    double longitude;

    /**
     * Creates a GeoLocation instance
     *
     * @param latitude  the latitude
     * @param longitude the longitude
     */
    public GeoLocation(final double latitude, final double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GeoLocation() {

    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof GeoLocation)) return false;

        final GeoLocation that = (GeoLocation) o;

        if (Double.compare(that.getLatitude(), latitude) != 0) return false;
        if (Double.compare(that.getLongitude(), longitude) != 0) return false;

        return true;
    }

    /**
     * returns the latitude of the geo location
     *
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * returns the longitude of the geo location
     *
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = latitude != +0.0d ? Double.doubleToLongBits(latitude) : 0L;
        result = (int) (temp ^ temp >>> 32);
        temp = longitude != +0.0d ? Double.doubleToLongBits(longitude) : 0L;
        result = 31 * result + (int) (temp ^ temp >>> 32);
        return result;
    }

    @Override
    public String toString() {
        return "GeoLocation{" + "latitude=" + latitude + ", longitude=" + longitude + '}';
    }

    @Override
    public boolean has(@NonNull String key) {
        return "lat".equals(key) || "long".equals(key);
    }

    @Override
    public Object get(@NonNull String key) {
        if ("lat".equals(key)) return latitude;
        if ("long".equals(key)) return longitude;
        return null;
    }

    @NonNull
    @Override
    public String[] keys() {
        return new String[]{"lat", "long"};
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        GeoLocationParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<GeoLocation> CREATOR = new Creator<GeoLocation>() {
        @Override
        public GeoLocation createFromParcel(Parcel source) {
            GeoLocation target = new GeoLocation();
            GeoLocationParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public GeoLocation[] newArray(int size) {
            return new GeoLocation[size];
        }
    };
}

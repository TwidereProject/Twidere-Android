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

package org.mariotaku.microblog.library.model.microblog;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease
@JsonObject
public class Location implements Parcelable {

    @JsonField(name = "woeid")
    int woeid;
    @JsonField(name = "parentid")
    int parentId;
    @JsonField(name = "country")
    String countryName;
    @JsonField(name = "countryCode")
    String countryCode;
    @JsonField(name = "placeType")
    PlaceType placeType;
    @JsonField(name = "name")
    String name;
    @JsonField(name = "url")
    String url;

    public int getWoeid() {
        return woeid;
    }

    public long getParentId() {
        return parentId;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public PlaceType getPlaceType() {
        return placeType;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        return woeid == location.woeid;

    }

    @Override
    public int hashCode() {
        return woeid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        LocationParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel source) {
            Location target = new Location();
            LocationParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    @ParcelablePlease
    @JsonObject
    public static class PlaceType implements Parcelable {
        @JsonField(name = "name")
        String name;

        @JsonField(name = "code")
        int code;

        public int getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "PlaceType{" +
                    "name='" + name + '\'' +
                    ", code=" + code +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Location$PlaceTypeParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<PlaceType> CREATOR = new Creator<PlaceType>() {
            @Override
            public PlaceType createFromParcel(Parcel source) {
                PlaceType target = new PlaceType();
                Location$PlaceTypeParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public PlaceType[] newArray(int size) {
                return new PlaceType[size];
            }
        };
    }
}

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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 15/5/10.
 */
@JsonObject
public class Location {

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

    @JsonObject
    public static class PlaceType {
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
    }
}

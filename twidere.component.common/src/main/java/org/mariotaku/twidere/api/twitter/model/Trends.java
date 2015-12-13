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

package org.mariotaku.twidere.api.twitter.model;

import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.api.twitter.util.TwitterTrendsDateConverter;

import java.util.Date;

/**
 * Created by mariotaku on 15/5/10.
 */
@JsonObject
public class Trends extends TwitterResponseObject implements TwitterResponse, Comparable<Trends> {

    @JsonField(name = "as_of", typeConverter = TwitterTrendsDateConverter.class)
    Date asOf;
    @JsonField(name = "created_at", typeConverter = TwitterTrendsDateConverter.class)
    Date createdAt;
    @JsonField(name = "trends")
    Trend[] trends;
    @JsonField(name = "locations")
    Location[] locations;

    public Date getAsOf() {
        return asOf;
    }

    public Trend[] getTrends() {
        return trends;
    }

    public Location[] getLocations() {
        return locations;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public int compareTo(@NonNull Trends another) {
        return asOf.compareTo(another.getAsOf());
    }
}

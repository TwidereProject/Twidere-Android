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

import org.mariotaku.microblog.library.twitter.util.TwitterTrendsDateConverter;

import java.util.Date;

/**
 * Created by mariotaku on 15/5/10.
 */
@ParcelablePlease
@JsonObject
public class Trends extends TwitterResponseObject implements TwitterResponse, Comparable<Trends>,
        Parcelable {

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TrendsParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Trends> CREATOR = new Creator<Trends>() {
        public Trends createFromParcel(Parcel source) {
            Trends target = new Trends();
            TrendsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public Trends[] newArray(int size) {
            return new Trends[size];
        }
    };
}

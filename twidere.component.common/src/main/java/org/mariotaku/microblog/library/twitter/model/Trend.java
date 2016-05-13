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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 15/5/10.
 */
@ParcelablePlease
@JsonObject
public class Trend implements Parcelable {

    @JsonField(name = "name")
    String name;
    @JsonField(name = "url")
    String url;
    @JsonField(name = "query")
    String query;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return "Trend{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", query='" + query + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TrendParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Trend> CREATOR = new Creator<Trend>() {
        @Override
        public Trend createFromParcel(Parcel source) {
            Trend target = new Trend();
            TrendParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public Trend[] newArray(int size) {
            return new Trend[size];
        }
    };
}

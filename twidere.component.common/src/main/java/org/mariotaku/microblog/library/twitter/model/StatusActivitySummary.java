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
 * Created by mariotaku on 15/5/13.
 */
@ParcelablePlease
@JsonObject
public class StatusActivitySummary extends TwitterResponseObject implements TwitterResponse, Parcelable {

    @JsonField(name = "favoriters", typeConverter = IDs.Converter.class)
    IDs favoriters;
    @JsonField(name = "repliers", typeConverter = IDs.Converter.class)
    IDs repliers;
    @JsonField(name = "retweeters", typeConverter = IDs.Converter.class)
    IDs retweeters;

    @JsonField(name = "favoriters_count")
    long favoritersCount;
    @JsonField(name = "repliers_count")
    long repliersCount;
    @JsonField(name = "retweeters_count")
    long retweetersCount;
    @JsonField(name = "descendent_reply_count")
    long descendentReplyCount;

    public IDs getFavoriters() {
        return favoriters;
    }

    public IDs getRepliers() {
        return repliers;
    }

    public IDs getRetweeters() {
        return retweeters;
    }

    public long getFavoritersCount() {
        return favoritersCount;
    }

    public long getRepliersCount() {
        return repliersCount;
    }

    public long getRetweetersCount() {
        return retweetersCount;
    }

    public long getDescendentReplyCount() {
        return descendentReplyCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        StatusActivitySummaryParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<StatusActivitySummary> CREATOR = new Creator<StatusActivitySummary>() {
        public StatusActivitySummary createFromParcel(Parcel source) {
            StatusActivitySummary target = new StatusActivitySummary();
            StatusActivitySummaryParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public StatusActivitySummary[] newArray(int size) {
            return new StatusActivitySummary[size];
        }
    };
}

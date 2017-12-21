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

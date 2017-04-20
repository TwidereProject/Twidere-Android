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
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 15/5/13.
 */
@ParcelablePlease
@JsonObject
public class AccountSettings extends TwitterResponseObject implements Parcelable {

    @JsonField(name = "geo_enabled")
    boolean geoEnabled;
    @JsonField(name = "trend_location")
    Location[] trendLocations;
    @JsonField(name = "language")
    String language;
    @JsonField(name = "always_use_https")
    boolean alwaysUseHttps;
    @JsonField(name = "time_zone")
    TimeZone timezone;

    public boolean isAlwaysUseHttps() {
        return alwaysUseHttps;
    }

    public String getLanguage() {
        return language;
    }

    public TimeZone getTimeZone() {
        return timezone;
    }

    public Location[] getTrendLocations() {
        return trendLocations;
    }

    public boolean isGeoEnabled() {
        return geoEnabled;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        AccountSettingsParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<AccountSettings> CREATOR = new Creator<AccountSettings>() {
        public AccountSettings createFromParcel(Parcel source) {
            AccountSettings target = new AccountSettings();
            AccountSettingsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public AccountSettings[] newArray(int size) {
            return new AccountSettings[size];
        }
    };
}

/*
 *         Twidere - Twitter client for Android
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.mariotaku.microblog.library.statusnet.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 16/2/29.
 */
@ParcelablePlease
@JsonObject
public class StatusNetConfig implements Parcelable {

    @JsonField(name = "site")
    Site site;

    public Site getSite() {
        return site;
    }

    @ParcelablePlease
    @JsonObject
    public static class Site implements Parcelable {
        @JsonField(name = "textlimit")
        int textLimit;

        public int getTextLimit() {
            return textLimit;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            StatusNetConfig$SiteParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<Site> CREATOR = new Creator<Site>() {
            @Override
            public Site createFromParcel(Parcel source) {
                Site target = new Site();
                StatusNetConfig$SiteParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public Site[] newArray(int size) {
                return new Site[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        StatusNetConfigParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<StatusNetConfig> CREATOR = new Creator<StatusNetConfig>() {
        @Override
        public StatusNetConfig createFromParcel(Parcel source) {
            StatusNetConfig target = new StatusNetConfig();
            StatusNetConfigParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public StatusNetConfig[] newArray(int size) {
            return new StatusNetConfig[size];
        }
    };
}

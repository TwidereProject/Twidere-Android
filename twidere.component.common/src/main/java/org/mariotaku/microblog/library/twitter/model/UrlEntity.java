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

package org.mariotaku.microblog.library.twitter.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 15/3/31.
 */
@ParcelablePlease
@JsonObject
public class UrlEntity implements Parcelable {
    @JsonField(name = "indices", typeConverter = IndicesConverter.class)
    Indices indices;
    @JsonField(name = "display_url")
    String displayUrl;
    @JsonField(name = "expanded_url")
    String expandedUrl;

    @JsonField(name = "url")
    String url;

    public String getDisplayUrl() {
        return displayUrl;
    }

    @Override
    public String toString() {
        return "UrlEntity{" +
                "indices=" + indices +
                ", displayUrl='" + displayUrl + '\'' +
                ", expandedUrl='" + expandedUrl + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    public int getEnd() {
        return indices.getEnd();
    }

    public int getStart() {
        return indices.getStart();
    }

    public String getExpandedUrl() {
        return expandedUrl;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UrlEntityParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<UrlEntity> CREATOR = new Creator<UrlEntity>() {
        @Override
        public UrlEntity createFromParcel(Parcel source) {
            UrlEntity target = new UrlEntity();
            UrlEntityParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public UrlEntity[] newArray(int size) {
            return new UrlEntity[size];
        }
    };
}

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

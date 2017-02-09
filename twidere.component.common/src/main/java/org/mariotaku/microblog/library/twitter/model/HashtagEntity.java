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
public class HashtagEntity implements Parcelable {

    @JsonField(name = "text")
    String text;
    @JsonField(name = "indices", typeConverter = IndicesConverter.class)
    Indices indices;

    public int getEnd() {
        return indices.getEnd();
    }

    public int getStart() {
        return indices.getStart();
    }

    public String getText() {
        return text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "HashtagEntity{" +
                "text='" + text + '\'' +
                ", indices=" + indices +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        HashtagEntityParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<HashtagEntity> CREATOR = new Creator<HashtagEntity>() {
        @Override
        public HashtagEntity createFromParcel(Parcel source) {
            HashtagEntity target = new HashtagEntity();
            HashtagEntityParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public HashtagEntity[] newArray(int size) {
            return new HashtagEntity[size];
        }
    };
}

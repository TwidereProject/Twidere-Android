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

package org.mariotaku.twidere.model.message;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

/**
 * Created by mariotaku on 2017/2/9.
 */

@ParcelablePlease
@JsonObject
public class StickerExtras extends MessageExtras implements Parcelable {

    @JsonField(name = "url")
    @ParcelableThisPlease
    String url;

    StickerExtras() {

    }

    public StickerExtras(@NonNull String url) {
        setUrl(url);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        StickerExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<StickerExtras> CREATOR = new Creator<StickerExtras>() {
        public StickerExtras createFromParcel(Parcel source) {
            StickerExtras target = new StickerExtras();
            StickerExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public StickerExtras[] newArray(int size) {
            return new StickerExtras[size];
        }
    };
}

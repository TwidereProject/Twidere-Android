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

package org.mariotaku.twidere.model.account;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 2017/4/19.
 */
@ParcelablePlease
@JsonObject
public class MastodonAccountExtras implements AccountExtras, Parcelable {

    @JsonField(name = "status_text_limit")
    int statusTextLimit = 500;

    public int getStatusTextLimit() {
        return statusTextLimit;
    }

    public void setStatusTextLimit(int statusTextLimit) {
        this.statusTextLimit = statusTextLimit;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        MastodonAccountExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<MastodonAccountExtras> CREATOR = new Creator<MastodonAccountExtras>() {
        public MastodonAccountExtras createFromParcel(Parcel source) {
            MastodonAccountExtras target = new MastodonAccountExtras();
            MastodonAccountExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public MastodonAccountExtras[] newArray(int size) {
            return new MastodonAccountExtras[size];
        }
    };
}

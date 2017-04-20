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
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

/**
 * Created by mariotaku on 16/2/26.
 */
@ParcelablePlease
@JsonObject
public class TwitterAccountExtras implements Parcelable, AccountExtras {

    public static final Creator<TwitterAccountExtras> CREATOR = new Creator<TwitterAccountExtras>() {
        @Override
        public TwitterAccountExtras createFromParcel(Parcel source) {
            TwitterAccountExtras target = new TwitterAccountExtras();
            TwitterAccountExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public TwitterAccountExtras[] newArray(int size) {
            return new TwitterAccountExtras[size];
        }
    };

    @JsonField(name = "official_credentials")
    @ParcelableThisPlease
    boolean officialCredentials;

    public boolean isOfficialCredentials() {
        return officialCredentials;
    }

    public void setIsOfficialCredentials(boolean officialCredentials) {
        this.officialCredentials = officialCredentials;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TwitterAccountExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public String toString() {
        return "TwitterAccountExtras{" +
                "officialCredentials=" + officialCredentials +
                '}';
    }
}

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

package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@JsonObject
@ParcelablePlease
public class CustomEmoji implements Parcelable {

    @JsonField(name = "url")
    public String url;
    @JsonField(name = "static_url")
    public String static_url;

    @Override
    public String toString() {
        return "CustomEmoji{" +
                "url='" + url + '\'' +
                ", static_url='" + static_url + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        CustomEmojiParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<CustomEmoji> CREATOR = new Creator<CustomEmoji>() {
        public CustomEmoji createFromParcel(Parcel source) {
            CustomEmoji target = new CustomEmoji();
            CustomEmojiParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public CustomEmoji[] newArray(int size) {
            return new CustomEmoji[size];
        }
    };

}

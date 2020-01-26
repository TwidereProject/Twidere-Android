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

package org.mariotaku.twidere.model.message;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

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
    @JsonField(name = "display_name")
    @ParcelableThisPlease
    String displayName;

    StickerExtras() {

    }

    public StickerExtras(@NonNull String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
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

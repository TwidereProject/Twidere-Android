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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@JsonObject
@ParcelablePlease
public class ParcelableMediaUpdate implements Parcelable {

    @NonNull
    @JsonField(name = "uri")
    public String uri;
    @ParcelableMedia.Type
    @JsonField(name = "type")
    public int type;

    @JsonField(name = "alt_text")
    @Nullable
    public String alt_text;

    @JsonField(name = "delete_on_success")
    public boolean delete_on_success;
    @JsonField(name = "delete_always")
    public boolean delete_always;

    public ParcelableMediaUpdate() {
    }

    public ParcelableMediaUpdate(@NonNull final String uri, final int type) {
        this.uri = uri;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParcelableMediaUpdate that = (ParcelableMediaUpdate) o;

        if (type != that.type) return false;
        if (!uri.equals(that.uri)) return false;
        return alt_text != null ? alt_text.equals(that.alt_text) : that.alt_text == null;

    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + type;
        result = 31 * result + (alt_text != null ? alt_text.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ParcelableMediaUpdate{" +
                "uri='" + uri + '\'' +
                ", type=" + type +
                ", alt_text='" + alt_text + '\'' +
                ", delete_on_success=" + delete_on_success +
                ", delete_always=" + delete_always +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableMediaUpdateParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ParcelableMediaUpdate> CREATOR = new Creator<ParcelableMediaUpdate>() {
        @Override
        public ParcelableMediaUpdate createFromParcel(Parcel source) {
            ParcelableMediaUpdate target = new ParcelableMediaUpdate();
            ParcelableMediaUpdateParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public ParcelableMediaUpdate[] newArray(int size) {
            return new ParcelableMediaUpdate[size];
        }
    };
}
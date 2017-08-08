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

package org.mariotaku.twidere.model.draft;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.twidere.model.ParcelableStatus;

/**
 * Created by mariotaku on 2017/2/7.
 */

@ParcelablePlease
@JsonObject
public class StatusObjectActionExtras implements ActionExtras, Parcelable {
    @JsonField(name = "status")
    ParcelableStatus status;

    public ParcelableStatus getStatus() {
        return status;
    }

    public void setStatus(final ParcelableStatus status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        StatusObjectActionExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<StatusObjectActionExtras> CREATOR = new Creator<StatusObjectActionExtras>() {
        public StatusObjectActionExtras createFromParcel(Parcel source) {
            StatusObjectActionExtras target = new StatusObjectActionExtras();
            StatusObjectActionExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public StatusObjectActionExtras[] newArray(int size) {
            return new StatusObjectActionExtras[size];
        }
    };
}

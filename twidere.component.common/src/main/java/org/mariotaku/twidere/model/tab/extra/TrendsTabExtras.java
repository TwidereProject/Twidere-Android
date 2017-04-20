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

package org.mariotaku.twidere.model.tab.extra;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_WOEID;

/**
 * Created by mariotaku on 2017/2/2.
 */

@JsonObject
@ParcelablePlease
public class TrendsTabExtras extends TabExtras implements Parcelable {
    @JsonField(name = "woeid")
    int woeId;
    @JsonField(name = "place_name")
    String placeName;

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(final String placeName) {
        this.placeName = placeName;
    }

    public int getWoeId() {
        return woeId;
    }

    public void setWoeId(final int woeId) {
        this.woeId = woeId;
    }

    @Override
    public void copyToBundle(final Bundle bundle) {
        super.copyToBundle(bundle);
        bundle.putInt(EXTRA_WOEID, woeId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TrendsTabExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<TrendsTabExtras> CREATOR = new Creator<TrendsTabExtras>() {
        public TrendsTabExtras createFromParcel(Parcel source) {
            TrendsTabExtras target = new TrendsTabExtras();
            TrendsTabExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public TrendsTabExtras[] newArray(int size) {
            return new TrendsTabExtras[size];
        }
    };

}

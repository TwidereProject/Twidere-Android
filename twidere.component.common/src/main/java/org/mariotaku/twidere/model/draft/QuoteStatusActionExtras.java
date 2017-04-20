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
public class QuoteStatusActionExtras implements ActionExtras, Parcelable {

    @JsonField(name = "status")
    ParcelableStatus status;
    @JsonField(name = "quote_original_status")
    boolean quoteOriginalStatus;

    public ParcelableStatus getStatus() {
        return status;
    }

    public void setStatus(final ParcelableStatus status) {
        this.status = status;
    }

    public boolean isQuoteOriginalStatus() {
        return quoteOriginalStatus;
    }

    public void setQuoteOriginalStatus(final boolean quoteOriginalStatus) {
        this.quoteOriginalStatus = quoteOriginalStatus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        QuoteStatusActionExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<QuoteStatusActionExtras> CREATOR = new Creator<QuoteStatusActionExtras>() {
        public QuoteStatusActionExtras createFromParcel(Parcel source) {
            QuoteStatusActionExtras target = new QuoteStatusActionExtras();
            QuoteStatusActionExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public QuoteStatusActionExtras[] newArray(int size) {
            return new QuoteStatusActionExtras[size];
        }
    };
}

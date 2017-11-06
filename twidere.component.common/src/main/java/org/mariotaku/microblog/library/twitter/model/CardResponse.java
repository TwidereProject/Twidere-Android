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

package org.mariotaku.microblog.library.twitter.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.io.IOException;

@ParcelablePlease
@JsonObject
public class CardResponse implements Parcelable {
    @JsonField(name = "card")
    CardEntity card;

    public CardEntity getCard() {
        return card;
    }

    @OnJsonParseComplete
    void onParseComplete() throws IOException {
        if (card == null) throw new IOException("Empty card result");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        CardResponseParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<CardResponse> CREATOR = new Creator<CardResponse>() {
        public CardResponse createFromParcel(Parcel source) {
            CardResponse target = new CardResponse();
            CardResponseParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public CardResponse[] newArray(int size) {
            return new CardResponse[size];
        }
    };
}

/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2018 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.twidere.model.attachment.QuotedStatus;
import org.mariotaku.twidere.model.attachment.SummaryCard;

@ParcelablePlease
@JsonObject
public class ParcelableStatusAttachment implements Parcelable {

    @JsonField(name = "media")
    @Nullable
    public ParcelableMedia[] media;

    @JsonField(name = "card")
    @Nullable
    public ParcelableCardEntity card;

    @JsonField(name = "summary_card")
    @Nullable
    public SummaryCard summary_card;

    @JsonField(name = "quoted")
    @Nullable
    public QuotedStatus quoted;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableStatusAttachmentParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ParcelableStatusAttachment> CREATOR = new Creator<ParcelableStatusAttachment>() {
        public ParcelableStatusAttachment createFromParcel(Parcel source) {
            ParcelableStatusAttachment target = new ParcelableStatusAttachment();
            ParcelableStatusAttachmentParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ParcelableStatusAttachment[] newArray(int size) {
            return new ParcelableStatusAttachment[size];
        }
    };
}

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

import org.mariotaku.twidere.model.util.UserKeyConverter;

@ParcelablePlease
@JsonObject
public class ParcelableStatusAttachment implements Parcelable {

    @JsonField(name = "media")
    @Nullable
    public ParcelableMedia[] media;

    @JsonField(name = "card")
    @Nullable
    public ParcelableCardEntity card;

    @JsonField(name = "quoted")
    @Nullable
    public QuotedStatus quoted;

    @ParcelablePlease
    @JsonObject
    public static class QuotedStatus implements Parcelable {

        @JsonField(name = "id")
        public String id;

        @JsonField(name = "timestamp")
        public long timestamp;

        @JsonField(name = "account_id", typeConverter = UserKeyConverter.class)
        public UserKey account_key;

        @JsonField(name = "user_id", typeConverter = UserKeyConverter.class)
        @Nullable
        public UserKey user_key;

        @JsonField(name = "user_is_protected")
        public boolean user_is_protected;

        @JsonField(name = "user_is_verified")
        public boolean user_is_verified;

        @JsonField(name = "text_plain")
        public String text_plain;

        @JsonField(name = "text_unescaped")
        public String text_unescaped;

        @JsonField(name = "source")
        public String source;

        @JsonField(name = "user_name")
        public String user_name;

        @JsonField(name = "user_screen_name")
        public String user_screen_name;

        @JsonField(name = "user_profile_image")
        public String user_profile_image;

        @JsonField(name = "media")
        @Nullable
        public ParcelableMedia[] media;

        @JsonField(name = "spans")
        public SpanItem[] spans;

        @JsonField(name = "external_url")
        public String external_url;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            ParcelableStatusAttachment$QuotedStatusParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<QuotedStatus> CREATOR = new Creator<QuotedStatus>() {
            public QuotedStatus createFromParcel(Parcel source) {
                QuotedStatus target = new QuotedStatus();
                ParcelableStatusAttachment$QuotedStatusParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public QuotedStatus[] newArray(int size) {
                return new QuotedStatus[size];
            }
        };
    }

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

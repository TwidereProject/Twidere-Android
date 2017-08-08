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
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 16/3/30.
 */
@ParcelablePlease
@JsonObject
public class NewMediaMetadata implements Parcelable {
    @JsonField(name = "media_id")
    String mediaId;
    @JsonField(name = "alt_text")
    AltText altText;

    NewMediaMetadata() {

    }

    public NewMediaMetadata(String mediaId, String altText) {
        this.mediaId = mediaId;
        this.altText = new AltText(altText);
    }

    @Override
    public String toString() {
        return "NewMediaMetadata{" +
                "mediaId='" + mediaId + '\'' +
                ", altText=" + altText +
                '}';
    }

    @ParcelablePlease
    @JsonObject
    public static class AltText implements Parcelable {
        @JsonField(name = "text")
        String text;

        AltText() {

        }

        public AltText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "AltText{" +
                    "text='" + text + '\'' +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            NewMediaMetadata$AltTextParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<AltText> CREATOR = new Creator<AltText>() {
            public AltText createFromParcel(Parcel source) {
                AltText target = new AltText();
                NewMediaMetadata$AltTextParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public AltText[] newArray(int size) {
                return new AltText[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        NewMediaMetadataParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<NewMediaMetadata> CREATOR = new Creator<NewMediaMetadata>() {
        public NewMediaMetadata createFromParcel(Parcel source) {
            NewMediaMetadata target = new NewMediaMetadata();
            NewMediaMetadataParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public NewMediaMetadata[] newArray(int size) {
            return new NewMediaMetadata[size];
        }
    };
}

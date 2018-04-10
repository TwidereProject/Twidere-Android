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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@JsonObject
@ParcelablePlease
public class ParcelableMedia implements Parcelable {

    /**
     * Link to media info, or its associated page (status info)
     */
    @SuppressWarnings("NullableProblems")
    @NonNull
    @JsonField(name = "url")
    @ParcelableThisPlease
    public String url;

    /**
     * URL to full size image or video
     */
    @SuppressWarnings("NullableProblems")
    @NonNull
    @JsonField(name = "media_url")
    @ParcelableThisPlease
    public String media_url;

    /**
     * URL to preview image
     */
    @SuppressWarnings("NullableProblems")
    @NonNull
    @JsonField(name = "preview_url")
    @ParcelableThisPlease
    public String preview_url;

    /**
     * URL to its associated page
     */
    @JsonField(name = "page_url")
    @Nullable
    public String page_url;

    @Type
    @JsonField(name = "type")
    @ParcelableThisPlease
    public int type;

    @JsonField(name = "width")
    @ParcelableThisPlease
    public int width;
    @JsonField(name = "height")
    @ParcelableThisPlease
    public int height;

    @JsonField(name = "duration")
    @ParcelableThisPlease
    public long duration;

    @ParcelableThisPlease
    @JsonField(name = "mime_type")
    public String mime_type;

    @ParcelableThisPlease
    @JsonField(name = "alt_text")
    public String alt_text;

    @ParcelableThisPlease
    @JsonField(name = "open_browser")
    public boolean open_browser;

    public static final Creator<ParcelableMedia> CREATOR = new Creator<ParcelableMedia>() {
        @Override
        public ParcelableMedia createFromParcel(Parcel source) {
            ParcelableMedia target = new ParcelableMedia();
            ParcelableMediaParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public ParcelableMedia[] newArray(int size) {
            return new ParcelableMedia[size];
        }
    };

    public ParcelableMedia() {

    }


    public ParcelableMedia(ParcelableMediaUpdate update) {
        media_url = update.uri;
        url = update.uri;
        preview_url = update.uri;
        type = update.type;
    }


    @OnJsonParseComplete
    void onParseComplete() {
        if (this.page_url != null) {
            this.url = this.page_url;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParcelableMedia that = (ParcelableMedia) o;

        if (!url.equals(that.url)) return false;
        return media_url.equals(that.media_url);
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + media_url.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ParcelableMedia{" +
                "url='" + url + '\'' +
                ", media_url='" + media_url + '\'' +
                ", preview_url='" + preview_url + '\'' +
                ", page_url='" + page_url + '\'' +
                ", type=" + type +
                ", width=" + width +
                ", height=" + height +
                ", duration=" + duration +
                ", mime_type='" + mime_type + '\'' +
                ", alt_text='" + alt_text + '\'' +
                ", open_browser=" + open_browser +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableMediaParcelablePlease.writeToParcel(this, dest, flags);
    }

    @IntDef({Type.UNKNOWN, Type.IMAGE, Type.VIDEO, Type.ANIMATED_GIF, Type.CARD_ANIMATED_GIF,
            Type.EXTERNAL_PLAYER, Type.VARIABLE_TYPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {

        int UNKNOWN = 0;
        int IMAGE = 1;
        int VIDEO = 2;
        int ANIMATED_GIF = 3;
        int CARD_ANIMATED_GIF = 4;
        int EXTERNAL_PLAYER = 5;
        int VARIABLE_TYPE = 6;
    }

}

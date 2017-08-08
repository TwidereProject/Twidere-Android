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

package org.mariotaku.microblog.library.gnusocial.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * GNUSocial attachment model
 * <p/>
 * Created by mariotaku on 16/1/26.
 */
@ParcelablePlease
@JsonObject
public class Attachment implements Parcelable {
    @JsonField(name = "width")
    int width;
    @JsonField(name = "height")
    int height;
    @JsonField(name = "url")
    String url;
    @JsonField(name = "thumb_url")
    String thumbUrl;
    @JsonField(name = "large_thumb_url")
    String largeThumbUrl;
    @JsonField(name = "mimetype")
    String mimetype;
    @JsonField(name = "id")
    long id;
    @JsonField(name = "oembed")
    boolean oembed;
    @JsonField(name = "size")
    long size;
    @JsonField(name = "version")
    String version;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getUrl() {
        return url;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public String getLargeThumbUrl() {
        return largeThumbUrl;
    }

    public String getMimetype() {
        return mimetype;
    }

    public long getId() {
        return id;
    }

    public boolean isOembed() {
        return oembed;
    }

    public long getSize() {
        return size;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "width=" + width +
                ", height=" + height +
                ", url='" + url + '\'' +
                ", thumbUrl='" + thumbUrl + '\'' +
                ", largeThumbUrl='" + largeThumbUrl + '\'' +
                ", mimetype='" + mimetype + '\'' +
                ", id=" + id +
                ", oembed=" + oembed +
                ", size=" + size +
                ", version='" + version + '\'' +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        AttachmentParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
        @Override
        public Attachment createFromParcel(Parcel source) {
            Attachment target = new Attachment();
            AttachmentParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };
}

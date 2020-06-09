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

package org.mariotaku.microblog.library.mastodon.model;

import androidx.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

/**
 * {@see https://github.com/tootsuite/documentation/blob/master/Using-the-API/API.md#attachment}
 *
 * Created by mariotaku on 2017/4/17.
 */
@JsonObject
public class Attachment {
    /**
     * ID of the attachment
     */
    @JsonField(name = "id")
    String id;
    /**
     * One of: {@code image}, {@code video}, {@code gifv}
     */
    @JsonField(name = "type")
    String type;
    /**
     * URL of the locally hosted version of the image
     */
    @JsonField(name = "url")
    String url;
    /**
     * For remote images, the remote URL of the original image
     */
    @JsonField(name = "remote_url")
    String remoteUrl;
    /**
     * URL of the preview image
     */
    @JsonField(name = "preview_url")
    String previewUrl;
    /**
     * Shorter URL for the image, for insertion into text (only present on local images)
     */
    @JsonField(name = "text_url")
    String textUrl;

    @JsonField(name = "meta")
    MetaInfo meta;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public String getTextUrl() {
        return textUrl;
    }

    public MetaInfo getMeta() {
        return meta;
    }

    @JsonObject
    public static class MetaInfo {

        @ParcelableThisPlease
        @JsonField(name = "focus")
        @Nullable
        public FocusInfo focus;
        @ParcelableThisPlease
        @JsonField(name = "original")
        @Nullable
        public MetaDataInfo original;
        @ParcelableThisPlease
        @JsonField(name = "small")
        @Nullable
        public MetaDataInfo small;

        @JsonObject
        public static class FocusInfo {
            @ParcelableThisPlease
            @JsonField(name = "x")
            public float x;
            @ParcelableThisPlease
            @JsonField(name = "y")
            public float y;
        }

        @JsonObject
        public static class MetaDataInfo {
            @ParcelableThisPlease
            @JsonField(name = "width")
            public long width;
            @ParcelableThisPlease
            @JsonField(name = "height")
            public long height;
            @ParcelableThisPlease
            @JsonField(name = "aspect")
            public float aspect;
        }


    }
    @Override
    public String toString() {
        return "Attachment{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", remoteUrl='" + remoteUrl + '\'' +
                ", previewUrl='" + previewUrl + '\'' +
                ", textUrl='" + textUrl + '\'' +
                '}';
    }
}

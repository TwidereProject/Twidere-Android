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
import com.hannesdorfmann.parcelableplease.annotation.Bagger;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.microblog.library.twitter.model.util.ParcelMapBagger;

import java.util.Map;

/**
 * Created by mariotaku on 2017/2/12.
 */
@ParcelablePlease
@JsonObject
public class StickerEntity implements Parcelable {

    @JsonField(name = "display_name")
    String displayName;

    @JsonField(name = "id")
    String id;

    @JsonField(name = "annotation_id")
    String annotationId;

    @JsonField(name = "sticker_set_annotation_id")
    String stickerSetAnnotationId;

    @JsonField(name = "group_annotation_id")
    String groupAnnotationId;

    @JsonField(name = "type")
    String type;

    @JsonField(name = "author_id")
    String authorId;

    @JsonField(name = "start_time")
    long startTime;

    @JsonField(name = "end_time")
    long endTime;

    @JsonField(name = "dominant_color")
    String dominantColor;

    @JsonField(name = "background_color")
    String backgroundColor;

    @JsonField(name = "card_image")
    Image cardImage;

    @JsonField(name = "images")
    @Bagger(ImageMapBagger.class)
    Map<String, Image> images;

    @JsonField(name = "variant_name")
    String variantName;

    public String getAnnotationId() {
        return annotationId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public Image getCardImage() {
        return cardImage;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDominantColor() {
        return dominantColor;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getGroupAnnotationId() {
        return groupAnnotationId;
    }

    public String getId() {
        return id;
    }

    public Map<String, Image> getImages() {
        return images;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getStickerSetAnnotationId() {
        return stickerSetAnnotationId;
    }

    public String getType() {
        return type;
    }

    public String getVariantName() {
        return variantName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        StickerEntityParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<StickerEntity> CREATOR = new Creator<StickerEntity>() {
        public StickerEntity createFromParcel(Parcel source) {
            StickerEntity target = new StickerEntity();
            StickerEntityParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public StickerEntity[] newArray(int size) {
            return new StickerEntity[size];
        }
    };

    @ParcelablePlease
    @JsonObject
    public static class Image implements Parcelable {
        @JsonField(name = "type")
        String type;
        @JsonField(name = "width")
        int width;
        @JsonField(name = "height")
        int height;
        @JsonField(name = "byte_count")
        long byteCount;
        @JsonField(name = "url")
        String url;

        public long getByteCount() {
            return byteCount;
        }

        public int getHeight() {
            return height;
        }

        public String getType() {
            return type;
        }

        public String getUrl() {
            return url;
        }

        public int getWidth() {
            return width;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            StickerEntity$ImageParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<Image> CREATOR = new Creator<Image>() {
            public Image createFromParcel(Parcel source) {
                Image target = new Image();
                StickerEntity$ImageParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public Image[] newArray(int size) {
                return new Image[size];
            }
        };
    }

    public static class ImageMapBagger extends ParcelMapBagger<Image> {
        public ImageMapBagger() {
            super(Image.class);
        }
    }
}

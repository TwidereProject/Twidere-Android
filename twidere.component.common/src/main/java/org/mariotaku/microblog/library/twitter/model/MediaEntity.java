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
import androidx.annotation.IntDef;
import androidx.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.Bagger;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.microblog.library.twitter.model.util.ParcelMapBagger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by mariotaku on 15/3/31.
 */
@ParcelablePlease
@JsonObject
public class MediaEntity extends UrlEntity implements Parcelable {
    @JsonField(name = "id")
    long id;

    @JsonField(name = "indices", typeConverter = IndicesConverter.class)
    Indices indices;

    @JsonField(name = "media_url")
    String mediaUrl;

    @JsonField(name = "media_url_https")
    String mediaUrlHttps;
    @JsonField(name = "url")
    String url;
    @JsonField(name = "display_url")
    String displayUrl;
    @JsonField(name = "expanded_url")
    String expandedUrl;
    @JsonField(name = "type")
    @Type
    String type;
    @JsonField(name = "sizes")
    @Bagger(SizeMapBagger.class)
    Map<String, Size> sizes;
    @JsonField(name = "source_status_id")
    long sourceStatusId;
    @JsonField(name = "source_user_id")
    long sourceUserId;
    @JsonField(name = "video_info")
    VideoInfo videoInfo;
    @JsonField(name = "features")
    @Bagger(FeaturesMapBagger.class)
    Map<String, Feature> features;
    @JsonField(name = "ext_alt_text")
    String altText;
    @JsonField(name = "ext")
    ExtInfo extInfo;

    public Map<String, Feature> getFeatures() {
        return features;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    public String getMediaUrlHttps() {
        return mediaUrlHttps;
    }

    @Override
    public String getExpandedUrl() {
        return expandedUrl;
    }

    @Override
    public String getDisplayUrl() {
        return displayUrl;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Type
    public String getType() {
        return type;
    }

    public Map<String, Size> getSizes() {
        return sizes;
    }

    @Override
    public int getEnd() {
        return indices.getEnd();
    }

    @Override
    public int getStart() {
        return indices.getStart();
    }

    public long getId() {
        return id;
    }

    public String getAltText() {
        return altText;
    }

    public ExtInfo getExtInfo() {
        return extInfo;
    }

    @Override
    public String toString() {
        return "MediaEntity{" +
                "id=" + id +
                ", indices=" + indices +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", mediaUrlHttps='" + mediaUrlHttps + '\'' +
                ", url='" + url + '\'' +
                ", displayUrl='" + displayUrl + '\'' +
                ", expandedUrl='" + expandedUrl + '\'' +
                ", type=" + type +
                ", sizes=" + sizes +
                ", sourceStatusId=" + sourceStatusId +
                ", sourceUserId=" + sourceUserId +
                ", videoInfo=" + videoInfo +
                ", features=" + features +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        MediaEntityParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<MediaEntity> CREATOR = new Creator<MediaEntity>() {
        @Override
        public MediaEntity createFromParcel(Parcel source) {
            MediaEntity target = new MediaEntity();
            MediaEntityParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public MediaEntity[] newArray(int size) {
            return new MediaEntity[size];
        }
    };

    public @interface Type {
        String PHOTO = "photo";
        String VIDEO = "video";
        String ANIMATED_GIF = "animated_gif";
    }

    @ParcelablePlease
    @JsonObject
    public static class Feature implements Parcelable {
        @JsonField(name = "faces")
        Face[] faces;

        @Override
        public String toString() {
            return "Feature{" +
                    "faces=" + Arrays.toString(faces) +
                    '}';
        }

        @ParcelablePlease
        @JsonObject
        public static class Face implements Parcelable {
            @JsonField(name = "x")
            int x;
            @JsonField(name = "y")
            int y;
            @JsonField(name = "h")
            int height;
            @JsonField(name = "w")
            int width;

            public int getX() {
                return x;
            }

            public int getY() {
                return y;
            }

            @Override
            public String toString() {
                return "Face{" +
                        "x=" + x +
                        ", y=" + y +
                        ", height=" + height +
                        ", width=" + width +
                        '}';
            }

            public int getHeight() {
                return height;
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
                MediaEntity$Feature$FaceParcelablePlease.writeToParcel(this, dest, flags);
            }

            public static final Creator<Face> CREATOR = new Creator<Face>() {
                @Override
                public Face createFromParcel(Parcel source) {
                    Face target = new Face();
                    MediaEntity$Feature$FaceParcelablePlease.readFromParcel(target, source);
                    return target;
                }

                @Override
                public Face[] newArray(int size) {
                    return new Face[size];
                }
            };
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            MediaEntity$FeatureParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<Feature> CREATOR = new Creator<Feature>() {
            @Override
            public Feature createFromParcel(Parcel source) {
                Feature target = new Feature();
                MediaEntity$FeatureParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public Feature[] newArray(int size) {
                return new Feature[size];
            }
        };
    }

    @ParcelablePlease
    @JsonObject
    public static class VideoInfo implements Parcelable {

        @JsonField(name = "duration")
        long duration;
        @JsonField(name = "variants")
        Variant[] variants;
        @JsonField(name = "aspect_ratio")
        long[] aspectRatio;

        public Variant[] getVariants() {
            return variants;
        }

        public long[] getAspectRatio() {
            return aspectRatio;
        }

        @Override
        public String toString() {
            return "VideoInfo{" +
                    "duration=" + duration +
                    ", variants=" + Arrays.toString(variants) +
                    ", aspectRatio=" + Arrays.toString(aspectRatio) +
                    '}';
        }

        public long getDuration() {
            return duration;
        }

        @ParcelablePlease
        @JsonObject
        public static class Variant implements Parcelable {
            @JsonField(name = "bitrate")
            long bitrate;
            @JsonField(name = "content_type")
            String contentType;
            @JsonField(name = "url")
            String url;

            @Override
            public String toString() {
                return "Variant{" +
                        "bitrate=" + bitrate +
                        ", contentType='" + contentType + '\'' +
                        ", url='" + url + '\'' +
                        '}';
            }

            public String getContentType() {
                return contentType;
            }

            public String getUrl() {
                return url;
            }

            public long getBitrate() {
                return bitrate;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                MediaEntity$VideoInfo$VariantParcelablePlease.writeToParcel(this, dest, flags);
            }

            public static final Creator<Variant> CREATOR = new Creator<Variant>() {
                @Override
                public Variant createFromParcel(Parcel source) {
                    Variant target = new Variant();
                    MediaEntity$VideoInfo$VariantParcelablePlease.readFromParcel(target, source);
                    return target;
                }

                @Override
                public Variant[] newArray(int size) {
                    return new Variant[size];
                }
            };
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            MediaEntity$VideoInfoParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<VideoInfo> CREATOR = new Creator<VideoInfo>() {
            @Override
            public VideoInfo createFromParcel(Parcel source) {
                VideoInfo target = new VideoInfo();
                MediaEntity$VideoInfoParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public VideoInfo[] newArray(int size) {
                return new VideoInfo[size];
            }
        };
    }

    @StringDef({ScaleType.THUMB, ScaleType.SMALL, ScaleType.MEDIUM, ScaleType.LARGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SizeType {
    }

    @IntDef({ScaleType.FIT, ScaleType.CROP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScaleType {
        int CROP = 101;
        int FIT = 100;
        String THUMB = "thumb";
        String SMALL = "small";
        String MEDIUM = "medium";
        String LARGE = "large";
    }

    @ParcelablePlease
    @JsonObject
    public static class ExtInfo implements Parcelable {

        @JsonField(name = "stickerInfo")
        Item stickerInfo;
        @JsonField(name = "altText")
        Item altText;

        public Item getStickerInfo() {
            return stickerInfo;
        }

        public Item getAltText() {
            return altText;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            MediaEntity$ExtInfoParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<ExtInfo> CREATOR = new Creator<ExtInfo>() {
            public ExtInfo createFromParcel(Parcel source) {
                ExtInfo target = new ExtInfo();
                MediaEntity$ExtInfoParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public ExtInfo[] newArray(int size) {
                return new ExtInfo[size];
            }
        };

        @ParcelablePlease
        @JsonObject
        public static class Item implements Parcelable {
            @JsonField(name = "ttl")
            long ttl;

            public long getTtl() {
                return ttl;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                MediaEntity$ExtInfo$ItemParcelablePlease.writeToParcel(this, dest, flags);
            }

            public static final Creator<Item> CREATOR = new Creator<Item>() {
                public Item createFromParcel(Parcel source) {
                    Item target = new Item();
                    MediaEntity$ExtInfo$ItemParcelablePlease.readFromParcel(target, source);
                    return target;
                }

                public Item[] newArray(int size) {
                    return new Item[size];
                }
            };
        }
    }

    @ParcelablePlease
    @JsonObject
    public static class Size implements Parcelable {

        @JsonField(name = "w")
        int width;
        @JsonField(name = "h")
        int height;
        @JsonField(name = "resize")
        String resize;


        public int getHeight() {
            return height;
        }

        public String getResize() {
            return resize;
        }

        public int getWidth() {
            return width;
        }

        @Override
        public String toString() {
            return "Size{" +
                    "width=" + width +
                    ", height=" + height +
                    ", resize='" + resize + '\'' +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            MediaEntity$SizeParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<Size> CREATOR = new Creator<Size>() {
            @Override
            public Size createFromParcel(Parcel source) {
                Size target = new Size();
                MediaEntity$SizeParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public Size[] newArray(int size) {
                return new Size[size];
            }
        };
    }

    public static class SizeMapBagger extends ParcelMapBagger<Size> {
        public SizeMapBagger() {
            super(Size.class);
        }
    }

    public static class FeaturesMapBagger extends ParcelMapBagger<Feature> {
        public FeaturesMapBagger() {
            super(Feature.class);
        }
    }
}

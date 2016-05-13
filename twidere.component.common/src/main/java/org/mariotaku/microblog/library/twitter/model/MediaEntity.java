/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *   
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.microblog.library.twitter.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

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

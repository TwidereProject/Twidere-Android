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

package org.mariotaku.twidere.api.twitter.model.impl;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.api.twitter.model.MediaEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mariotaku on 15/3/31.
 */
@JsonObject
public class MediaEntityImpl implements MediaEntity {
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
    Type type;
    @JsonField(name = "sizes")
    HashMap<String, Size> sizes;
    @JsonField(name = "source_status_id")
    long sourceStatusId;
    @JsonField(name = "source_user_id")
    long sourceUserId;
    @JsonField(name = "video_info")
    VideoInfo videoInfo;
    @JsonField(name = "features")
    HashMap<String, Feature> features;

    @Override
    public Map<String, Feature> getFeatures() {
        return features;
    }

    @Override
    public String toString() {
        return "MediaEntityImpl{" +
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
    public String getMediaUrl() {
        return mediaUrl;
    }

    @Override
    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    @Override
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

    @Override
    public Type getType() {
        return type;
    }

    @Override
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

    @Override
    public long getId() {
        return id;
    }

    @JsonObject
    public static class FeatureImpl implements Feature {
        @JsonField(name = "faces")
        Face[] faces;

        @Override
        public String toString() {
            return "FeatureImpl{" +
                    "faces=" + Arrays.toString(faces) +
                    '}';
        }

        @JsonObject
        public static class FaceImpl implements Face {
            @JsonField(name = "x")
            int x;
            @JsonField(name = "y")
            int y;
            @JsonField(name = "h")
            int height;
            @JsonField(name = "w")
            int width;

            @Override
            public int getX() {
                return x;
            }

            @Override
            public int getY() {
                return y;
            }

            @Override
            public String toString() {
                return "FaceImpl{" +
                        "x=" + x +
                        ", y=" + y +
                        ", height=" + height +
                        ", width=" + width +
                        '}';
            }

            @Override
            public int getHeight() {
                return height;
            }

            @Override
            public int getWidth() {
                return width;
            }
        }

    }

    @JsonObject
    public static class VideoInfoImpl implements VideoInfo {

        @JsonField(name = "duration")
        long duration;
        @JsonField(name = "variants")
        Variant[] variants;
        @JsonField(name = "aspect_ratio")
        long[] aspectRatio;

        @Override
        public Variant[] getVariants() {
            return variants;
        }

        @Override
        public long[] getAspectRatio() {
            return aspectRatio;
        }

        @Override
        public String toString() {
            return "VideoInfoImpl{" +
                    "duration=" + duration +
                    ", variants=" + Arrays.toString(variants) +
                    ", aspectRatio=" + Arrays.toString(aspectRatio) +
                    '}';
        }

        @Override
        public long getDuration() {
            return duration;
        }

        @JsonObject
        public static class VariantImpl implements Variant {
            @JsonField(name = "bitrate")
            long bitrate;
            @JsonField(name = "content_type")
            String contentType;
            @JsonField(name = "url")
            String url;

            @Override
            public String toString() {
                return "VariantImpl{" +
                        "bitrate=" + bitrate +
                        ", contentType='" + contentType + '\'' +
                        ", url='" + url + '\'' +
                        '}';
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public String getUrl() {
                return url;
            }

            @Override
            public long getBitrate() {
                return bitrate;
            }
        }
    }


    @JsonObject
    public static class SizeImpl implements Size {

        @JsonField(name = "w")
        int width;
        @JsonField(name = "h")
        int height;
        @JsonField(name = "resize")
        String resize;

        @Override
        public String toString() {
            return "SizeImpl{" +
                    "width=" + width +
                    ", height=" + height +
                    ", resize='" + resize + '\'' +
                    '}';
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public String getResize() {
            return resize;
        }

        @Override
        public int getWidth() {
            return width;
        }
    }
}

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

package org.mariotaku.twidere.api.twitter.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mariotaku on 15/3/31.
 */
@JsonObject
public class MediaEntity extends UrlEntity {
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

    public Map<String, Feature> getFeatures() {
        return features;
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

    public String getMediaUrl() {
        return mediaUrl;
    }

    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    public String getMediaUrlHttps() {
        return mediaUrlHttps;
    }

    public String getExpandedUrl() {
        return expandedUrl;
    }

    public String getDisplayUrl() {
        return displayUrl;
    }

    public String getUrl() {
        return url;
    }

    public Type getType() {
        return type;
    }

    public Map<String, Size> getSizes() {
        return sizes;
    }

    public int getEnd() {
        return indices.getEnd();
    }

    public int getStart() {
        return indices.getStart();
    }

    public long getId() {
        return id;
    }

    public enum Type {
        PHOTO, VIDEO, ANIMATED_GIF, UNKNOWN;

        public static Type parse(String typeString) {
            if ("photo".equalsIgnoreCase(typeString)) {
                return PHOTO;
            } else if ("video".equalsIgnoreCase(typeString)) {
                return VIDEO;
            } else if ("animated_gif".equalsIgnoreCase(typeString)) {
                return ANIMATED_GIF;
            }
            return UNKNOWN;
        }
    }


    @JsonObject
    public static class Feature {
        @JsonField(name = "faces")
        Face[] faces;

        @Override
        public String toString() {
            return "Feature{" +
                    "faces=" + Arrays.toString(faces) +
                    '}';
        }

        @JsonObject
        public static class Face {
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
        }

    }

    @JsonObject
    public static class VideoInfo {

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

        @JsonObject
        public static class Variant {
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
        }
    }


    @JsonObject
    public static class Size {

        public static final String THUMB = "thumb";
        public static final String SMALL = "small";
        public static final String MEDIUM = "medium";
        public static final String LARGE = "large";
        public static final int FIT = 100;
        public static final int CROP = 101;
        @JsonField(name = "w")
        int width;
        @JsonField(name = "h")
        int height;
        @JsonField(name = "resize")
        String resize;

        @Override
        public String toString() {
            return "Size{" +
                    "width=" + width +
                    ", height=" + height +
                    ", resize='" + resize + '\'' +
                    '}';
        }

        public int getHeight() {
            return height;
        }

        public String getResize() {
            return resize;
        }

        public int getWidth() {
            return width;
        }
    }
}

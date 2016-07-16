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

import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 15/5/7.
 */
@JsonObject
public class MediaUploadResponse extends TwitterResponseObject implements TwitterResponse {

    @JsonField(name = "media_id")
    String mediaId;
    @JsonField(name = "size")
    long size;
    @JsonField(name = "image")
    Image image;
    @JsonField(name = "video")
    Video video;
    @JsonField(name = "processing_info")
    @Nullable
    ProcessingInfo processingInfo;

    public String getId() {
        return mediaId;
    }

    public Image getImage() {
        return image;
    }

    public long getSize() {
        return size;
    }

    public Video getVideo() {
        return video;
    }

    @Nullable
    public ProcessingInfo getProcessingInfo() {
        return processingInfo;
    }

    @Override
    public String toString() {
        return "MediaUploadResponse{" +
                "mediaId='" + mediaId + '\'' +
                ", size=" + size +
                ", image=" + image +
                ", video=" + video +
                ", processingInfo=" + processingInfo +
                "} " + super.toString();
    }

    @JsonObject
    public static class Video {
        @JsonField(name = "video_type")
        String videoType;

        public String getVideoType() {
            return videoType;
        }

        @Override
        public String toString() {
            return "Video{" +
                    "videoType='" + videoType + '\'' +
                    '}';
        }
    }

    @JsonObject
    public static class Image {

        @JsonField(name = "width")
        int width;
        @JsonField(name = "height")
        int height;
        @JsonField(name = "image_type")
        String imageType;

        public int getHeight() {
            return height;
        }

        public String getImageType() {
            return imageType;
        }

        public int getWidth() {
            return width;
        }

        @Override
        public String toString() {
            return "Image{" +
                    "width=" + width +
                    ", height=" + height +
                    ", imageType='" + imageType + '\'' +
                    '}';
        }
    }

    @JsonObject
    public static class ProcessingInfo {
        @JsonField(name = "state")
        @State
        String state;
        @JsonField(name = "check_after_secs")
        long checkAfterSecs;
        @JsonField(name = "progress_percent")
        int progressPercent;
        @JsonField(name = "error")
        ErrorInfo error;

        @State
        public String getState() {
            return state;
        }

        public long getCheckAfterSecs() {
            return checkAfterSecs;
        }

        public int getProgressPercent() {
            return progressPercent;
        }

        public ErrorInfo getError() {
            return error;
        }

        @Override
        public String toString() {
            return "ProcessingInfo{" +
                    "state='" + state + '\'' +
                    ", checkAfterSecs=" + checkAfterSecs +
                    '}';
        }


        @StringDef({State.PENDING, State.IN_PROGRESS, State.FAILED, State.SUCCEEDED})
        public @interface State {
            String PENDING = "pending";
            String IN_PROGRESS = "in_progress";
            String FAILED = "failed";
            String SUCCEEDED = "succeeded";

        }
    }
}

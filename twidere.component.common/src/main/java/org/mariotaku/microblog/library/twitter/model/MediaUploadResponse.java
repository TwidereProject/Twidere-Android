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

import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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

        @JsonField(name = "w")
        int width;
        @JsonField(name = "h")
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
        @Retention(RetentionPolicy.SOURCE)
        public @interface State {
            String PENDING = "pending";
            String IN_PROGRESS = "in_progress";
            String FAILED = "failed";
            String SUCCEEDED = "succeeded";

        }
    }
}

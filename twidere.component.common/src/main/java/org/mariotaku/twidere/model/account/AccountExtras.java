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

package org.mariotaku.twidere.model.account;

import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.microblog.library.twitter.annotation.MediaCategory;

/**
 * Created by mariotaku on 16/2/26.
 */
public interface AccountExtras extends Parcelable {

    @JsonObject
    class ImageLimit {

        @JsonField(name = "max_width")
        int maxWidth;
        @JsonField(name = "max_height")
        int maxHeight;

        @JsonField(name = "max_size_sync")
        long maxSizeSync;
        @JsonField(name = "max_size_async")
        long maxSizeAsync;

        public int getMaxWidth() {
            return maxWidth;
        }

        public void setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
        }

        public int getMaxHeight() {
            return maxHeight;
        }

        public void setMaxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
        }

        public long getMaxSizeSync() {
            return maxSizeSync;
        }

        public void setMaxSizeSync(long maxSizeSync) {
            this.maxSizeSync = maxSizeSync;
        }

        public long getMaxSizeAsync() {
            return maxSizeAsync;
        }

        public void setMaxSizeAsync(long maxSizeAsync) {
            this.maxSizeAsync = maxSizeAsync;
        }

        public boolean checkSize(long size, boolean async) {
            final long limit = async ? getMaxSizeAsync() : getMaxSizeSync();
            if (limit <= 0 || size <= 0) return true;
            return size <= limit;
        }

        @Override
        public String toString() {
            return "ImageLimit{" +
                    "maxWidth=" + maxWidth +
                    ", maxHeight=" + maxHeight +
                    ", maxSizeSync=" + maxSizeSync +
                    ", maxSizeAsync=" + maxSizeAsync +
                    '}';
        }

        @NonNull
        public static ImageLimit ofGeometry(int width, int height) {
            final ImageLimit limit = new ImageLimit();
            limit.setMaxWidth(width);
            limit.setMaxHeight(height);
            return limit;
        }

        @NonNull
        public static ImageLimit ofSize(int size) {
            final ImageLimit limit = new ImageLimit();
            limit.setMaxSizeSync(size);
            limit.setMaxSizeAsync(size);
            return limit;
        }

        @NonNull
        public static ImageLimit twitterDefault(@Nullable @MediaCategory String category) {
            if (MediaCategory.DM_IMAGE.equals(category)) {
                ImageLimit limit = new ImageLimit();
                limit.setMaxSizeSync(5 * 1024 * 1024);
                limit.setMaxSizeAsync(5 * 1024 * 1024);
                return limit;
            }
            final ImageLimit limit = new ImageLimit();
            limit.setMaxSizeSync(3 * 1024 * 1024);
            limit.setMaxSizeAsync(3 * 1024 * 1024);
            return limit;
        }

    }

    @JsonObject
    class VideoLimit {

        boolean supported;

        @JsonField(name = "min_width")
        int minWidth;
        @JsonField(name = "min_height")
        int minHeight;

        @JsonField(name = "max_width")
        int maxWidth;
        @JsonField(name = "max_height")
        int maxHeight;

        @JsonField(name = "can_rotate_geometry_limit")
        boolean canRotateGeometryLimit;

        @JsonField(name = "max_size_sync")
        long maxSizeSync;
        @JsonField(name = "max_size_async")
        long maxSizeAsync;

        @JsonField(name = "min_aspect_ratio")
        double minAspectRatio;
        @JsonField(name = "max_aspect_ratio")
        double maxAspectRatio;

        @JsonField(name = "min_frame_rate")
        double minFrameRate;
        @JsonField(name = "max_frame_rate")
        double maxFrameRate;

        @JsonField(name = "min_duration_sync")
        long minDurationSync;
        @JsonField(name = "min_duration_async")
        long minDurationAsync;
        @JsonField(name = "max_duration_sync")
        long maxDurationSync;
        @JsonField(name = "max_duration_async")
        long maxDurationAsync;

        public boolean isSupported() {
            return supported;
        }

        public void setSupported(boolean supported) {
            this.supported = supported;
        }

        public int getMinWidth() {
            return minWidth;
        }

        public void setMinWidth(int minWidth) {
            this.minWidth = minWidth;
        }

        public int getMinHeight() {
            return minHeight;
        }

        public void setMinHeight(int minHeight) {
            this.minHeight = minHeight;
        }

        public int getMaxWidth() {
            return maxWidth;
        }

        public void setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
        }

        public int getMaxHeight() {
            return maxHeight;
        }

        public void setMaxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
        }

        public boolean canRotateGeometryLimit() {
            return canRotateGeometryLimit;
        }

        public void setCanRotateGeometryLimit(boolean canRotateGeometryLimit) {
            this.canRotateGeometryLimit = canRotateGeometryLimit;
        }

        public long getMaxSizeSync() {
            return maxSizeSync;
        }

        public void setMaxSizeSync(long maxSizeSync) {
            this.maxSizeSync = maxSizeSync;
        }

        public long getMaxSizeAsync() {
            return maxSizeAsync;
        }

        public void setMaxSizeAsync(long maxSizeAsync) {
            this.maxSizeAsync = maxSizeAsync;
        }

        public double getMinAspectRatio() {
            return minAspectRatio;
        }

        public void setMinAspectRatio(double minAspectRatio) {
            this.minAspectRatio = minAspectRatio;
        }

        public double getMaxAspectRatio() {
            return maxAspectRatio;
        }

        public void setMaxAspectRatio(double maxAspectRatio) {
            this.maxAspectRatio = maxAspectRatio;
        }

        public double getMinFrameRate() {
            return minFrameRate;
        }

        public void setMinFrameRate(double minFrameRate) {
            this.minFrameRate = minFrameRate;
        }

        public double getMaxFrameRate() {
            return maxFrameRate;
        }

        public void setMaxFrameRate(double maxFrameRate) {
            this.maxFrameRate = maxFrameRate;
        }

        public long getMinDurationSync() {
            return minDurationSync;
        }

        public void setMinDurationSync(long minDurationSync) {
            this.minDurationSync = minDurationSync;
        }

        public long getMinDurationAsync() {
            return minDurationAsync;
        }

        public void setMinDurationAsync(long minDurationAsync) {
            this.minDurationAsync = minDurationAsync;
        }

        public long getMaxDurationSync() {
            return maxDurationSync;
        }

        public void setMaxDurationSync(long maxDurationSync) {
            this.maxDurationSync = maxDurationSync;
        }

        public long getMaxDurationAsync() {
            return maxDurationAsync;
        }

        public void setMaxDurationAsync(long maxDurationAsync) {
            this.maxDurationAsync = maxDurationAsync;
        }

        @SuppressWarnings("RedundantIfStatement")
        public boolean checkGeometry(int width, int height) {
            if (minWidth <= 0 || maxWidth <= 0) return false;
            // Check w & h
            boolean widthValid = inRange(width, getMinWidth(), getMaxWidth());
            boolean heightValid = inRange(height, getMinHeight(), getMaxHeight());
            if (canRotateGeometryLimit()) {
                widthValid |= inRange(height, getMinWidth(), getMaxWidth());
                heightValid |= inRange(width, getMinHeight(), getMaxHeight());
            }

            if (!widthValid || !heightValid) {
                return false;
            }

            // Check aspect ratio
            double aspectRatio = width / (double) height;
            if (!inRange(aspectRatio, getMinAspectRatio(), getMaxAspectRatio())) {
                return false;
            }

            return true;
        }

        public boolean checkFrameRate(double frameRate) {
            return inRange(frameRate, getMinFrameRate(), getMaxFrameRate());
        }

        public boolean checkMinDuration(long duration, boolean async) {
            final long limit = async ? getMinDurationAsync() : getMinDurationSync();
            if (limit <= 0) return true;
            return duration >= limit;
        }

        public boolean checkMaxDuration(long duration, boolean async) {
            final long limit = async ? getMaxDurationAsync() : getMaxDurationSync();
            if (limit <= 0) return true;
            return duration <= limit;
        }

        public boolean checkSize(long size, boolean async) {
            final long limit = async ? getMaxSizeAsync() : getMaxSizeSync();
            if (limit <= 0 || size <= 0) return true;
            return size <= limit;
        }

        @SuppressWarnings("RedundantIfStatement")
        private boolean inRange(int num, int min, int max) {
            if (min > 0 && num < min) return false;
            if (max > 0 && num > max) return false;
            return true;
        }

        @SuppressWarnings("RedundantIfStatement")
        private boolean inRange(double num, double min, double max) {
            if (min > 0 && num < min) return false;
            if (max > 0 && num > max) return false;
            return true;
        }

        @Override
        public String toString() {
            return "VideoLimit{" +
                    "supported=" + supported +
                    ", minWidth=" + minWidth +
                    ", minHeight=" + minHeight +
                    ", maxWidth=" + maxWidth +
                    ", maxHeight=" + maxHeight +
                    ", canRotateGeometryLimit=" + canRotateGeometryLimit +
                    ", maxSizeSync=" + maxSizeSync +
                    ", maxSizeAsync=" + maxSizeAsync +
                    ", minAspectRatio=" + minAspectRatio +
                    ", maxAspectRatio=" + maxAspectRatio +
                    ", minFrameRate=" + minFrameRate +
                    ", maxFrameRate=" + maxFrameRate +
                    ", minDurationSync=" + minDurationSync +
                    ", minDurationAsync=" + minDurationAsync +
                    ", maxDurationSync=" + maxDurationSync +
                    ", maxDurationAsync=" + maxDurationAsync +
                    '}';
        }

        public static VideoLimit twitterDefault() {
            VideoLimit videoLimit = new VideoLimit();
            videoLimit.setSupported(true);
            videoLimit.setMinWidth(32);
            videoLimit.setMinHeight(32);
            videoLimit.setMaxWidth(1280);
            videoLimit.setMaxHeight(1024);
            videoLimit.setCanRotateGeometryLimit(true);
            videoLimit.setMinAspectRatio(1.0 / 3);
            videoLimit.setMaxAspectRatio(3);
            videoLimit.setMaxSizeSync(15 * 1024 * 1024);
            videoLimit.setMaxSizeAsync(512 * 1024 * 1024);
            videoLimit.setMinFrameRate(0);
            videoLimit.setMaxFrameRate(40);
            videoLimit.setMinDurationSync(500); // 0.5s
            videoLimit.setMinDurationAsync(500); // 0.5s
            videoLimit.setMaxDurationSync(30000); // 30s
            videoLimit.setMaxDurationAsync(140000); // 140s
            return videoLimit;
        }

        public static VideoLimit unsupported() {
            VideoLimit videoLimit = new VideoLimit();
            videoLimit.setSupported(false);
            return videoLimit;
        }
    }
}

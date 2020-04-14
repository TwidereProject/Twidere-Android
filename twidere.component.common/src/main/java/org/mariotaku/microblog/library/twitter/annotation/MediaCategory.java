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

package org.mariotaku.microblog.library.twitter.annotation;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@StringDef({MediaCategory.TWEET_IMAGE, MediaCategory.TWEET_GIF, MediaCategory.TWEET_VIDEO,
        MediaCategory.DM_IMAGE, MediaCategory.DM_GIF, MediaCategory.DM_VIDEO})
public @interface MediaCategory {
    String TWEET_IMAGE = "tweet_image";
    String TWEET_GIF = "tweet_gif";
    String TWEET_VIDEO = "tweet_video";
    String DM_IMAGE = "dm_image";
    String DM_GIF = "dm_gif";
    String DM_VIDEO = "dm_video";
}

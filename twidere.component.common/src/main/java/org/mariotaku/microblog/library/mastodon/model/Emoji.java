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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class Emoji {
    @JsonField(name = "shortcode")
    String shortCode;
    @JsonField(name = "url")
    String url;
    @JsonField(name = "static_url")
    String staticUrl;

    public String getShortCode() {
        return shortCode;
    }

    public String getUrl() {
        return url;
    }

    public String getStaticUrl() {
        return staticUrl;
    }

    @Override
    public String toString() {
        return "Emoji{" +
                "shortCode='" + shortCode + '\'' +
                ", url='" + url + '\'' +
                ", staticUrl='" + staticUrl + '\'' +
                '}';
    }
}

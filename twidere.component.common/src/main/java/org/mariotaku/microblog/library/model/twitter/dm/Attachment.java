/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2018 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.microblog.library.model.twitter.dm;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.microblog.library.model.microblog.MediaEntity;

@JsonObject
public class Attachment {
    @JsonField(name = "type")
    String type;

    @JsonField(name = "media")
    Media media;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    @JsonObject
    public static class Media extends MediaEntity {

        public void setId(String id) {
            this.id = id;
        }
    }
}

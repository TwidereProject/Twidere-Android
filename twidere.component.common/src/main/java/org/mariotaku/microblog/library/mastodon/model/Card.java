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

import android.support.annotation.StringDef;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * {@see https://github.com/tootsuite/documentation/blob/master/Using-the-API/API.md#card}
 *
 * Created by mariotaku on 2017/4/17.
 */
@JsonObject
public class Card {
    /**
     * The url associated with the card
     */
    @JsonField(name = "url")
    String url;
    /**
     * The title of the card
     */
    @JsonField(name = "title")
    String title;
    /**
     * The card description
     */
    @JsonField(name = "description")
    String description;
    /**
     * The image associated with the card, if any
     */
    @JsonField(name = "image")
    String image;
    /**
     * One of: {@code link}, {@code photo}, {@code video}, {@code rich}
     */
    @JsonField(name = "type")
    String type;

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Card{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @StringDef({Type.LINK, Type.PHOTO, Type.VIDEO, Type.RICH})
    public @interface Type {
        String LINK = "link";
        String PHOTO = "photo";
        String VIDEO = "video";
        String RICH = "rich";
    }
}

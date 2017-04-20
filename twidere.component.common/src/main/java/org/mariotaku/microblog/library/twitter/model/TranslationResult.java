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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 15/5/7.
 */
@JsonObject
public class TranslationResult extends TwitterResponseObject implements TwitterResponse {

    @JsonField(name = "id")
    String id;
    @JsonField(name = "lang")
    String lang;
    @JsonField(name = "translated_lang")
    String translatedLang;
    @JsonField(name = "translation_type")
    String translationType;
    @JsonField(name = "text")
    String text;

    public String getId() {
        return id;
    }

    public String getLang() {
        return lang;
    }

    public String getText() {
        return text;
    }

    public String getTranslatedLang() {
        return translatedLang;
    }

    public String getTranslationType() {
        return translationType;
    }

    @Override
    public String toString() {
        return "TranslationResult{" +
                "id='" + id + '\'' +
                ", lang='" + lang + '\'' +
                ", translatedLang='" + translatedLang + '\'' +
                ", translationType='" + translationType + '\'' +
                ", text='" + text + '\'' +
                "} " + super.toString();
    }
}

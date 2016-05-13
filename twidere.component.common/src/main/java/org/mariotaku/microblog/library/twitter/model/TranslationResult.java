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

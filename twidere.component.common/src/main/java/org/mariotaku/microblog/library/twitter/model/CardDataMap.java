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

import androidx.annotation.NonNull;

import com.bluelinelabs.logansquare.LoganSquare;
import com.fasterxml.jackson.core.JsonGenerator;

import org.mariotaku.restfu.RestConverter;
import org.mariotaku.restfu.http.ValueMap;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.restfu.http.mime.StringBody;
import org.mariotaku.microblog.library.MicroBlogException;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by mariotaku on 15/12/30.
 */
public class CardDataMap implements ValueMap {
    protected final Map<String, String> map = new LinkedHashMap<>();

    public void putString(String key, String value) {
        map.put("twitter:string:" + key, value);
    }

    public void putLong(String key, long value) {
        map.put("twitter:long:" + key, String.valueOf(value));
    }

    @Override
    public boolean has(@NonNull String key) {
        return map.containsKey(key);
    }

    @Override
    public Object get(@NonNull String key) {
        return map.get(key);
    }

    @NonNull
    @Override
    public String[] keys() {
        final Set<String> keySet = map.keySet();
        return keySet.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return "CardDataMap{" +
                "map=" + map +
                '}';
    }

    public static class BodyConverter implements RestConverter<CardDataMap, Body, MicroBlogException> {
        @NonNull
        @Override
        public Body convert(@NonNull CardDataMap obj) throws ConvertException, IOException, MicroBlogException {
            final StringWriter sw = new StringWriter();
            final JsonGenerator generator = LoganSquare.JSON_FACTORY.createGenerator(sw);
            generator.writeStartObject();
            for (Map.Entry<String, String> entry : obj.map.entrySet()) {
                generator.writeStringField(entry.getKey(), entry.getValue());
            }
            generator.writeEndObject();
            generator.flush();
            return new StringBody(sw.toString(), Charset.defaultCharset());
        }
    }
}

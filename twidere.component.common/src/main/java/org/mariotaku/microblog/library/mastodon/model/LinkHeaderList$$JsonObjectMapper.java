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

import androidx.annotation.Keep;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.ParameterizedType;
import com.bluelinelabs.logansquare.util.SimpleArrayMap;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

@SuppressWarnings("unused")
@Keep
public final class LinkHeaderList$$JsonObjectMapper<T> extends JsonMapper<LinkHeaderList<T>> {
    private final JsonMapper<T> m69ClassJsonMapper;

    public LinkHeaderList$$JsonObjectMapper(ParameterizedType type, ParameterizedType TType, SimpleArrayMap<ParameterizedType, JsonMapper> partialMappers) {
        partialMappers.put(type, this);
        //noinspection unchecked
        m69ClassJsonMapper = LoganSquare.mapperFor(TType, partialMappers);
    }

    @Override
    public LinkHeaderList<T> parse(JsonParser jsonParser) throws IOException {
        if (jsonParser.getCurrentToken() == null) {
            jsonParser.nextToken();
        }
        LinkHeaderList<T> instance = new LinkHeaderList<>();
        final JsonToken currentToken = jsonParser.getCurrentToken();
        if (currentToken == JsonToken.START_ARRAY) {
            instance.addAll(m69ClassJsonMapper.parseList(jsonParser));
        } else {
            jsonParser.skipChildren();
        }
        return instance;
    }

    @Override
    public void parseField(LinkHeaderList<T> instance, String fieldName, JsonParser jsonParser) throws IOException {

    }

    @Override
    public void serialize(LinkHeaderList<T> object, JsonGenerator jsonGenerator, boolean writeStartAndEnd) throws IOException {
        throw new UnsupportedOperationException();
    }
}

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

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.ParameterizedType;
import com.bluelinelabs.logansquare.util.SimpleArrayMap;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

@SuppressWarnings("unused")
public final class ResponseList$$JsonObjectMapper<T> extends JsonMapper<ResponseList<T>> {
    private final JsonMapper<T> m84ClassJsonMapper;

    public ResponseList$$JsonObjectMapper(ParameterizedType type, ParameterizedType TType, SimpleArrayMap<ParameterizedType, JsonMapper> partialMappers) {
        partialMappers.put(type, this);
        //noinspection unchecked
        m84ClassJsonMapper = LoganSquare.mapperFor(TType, partialMappers);
    }

    @Override
    public ResponseList<T> parse(JsonParser jsonParser) throws IOException {
        if (jsonParser.getCurrentToken() == null) {
            jsonParser.nextToken();
        }
        if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
            ResponseList<T> instance = new ResponseList<>();
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonParser.getCurrentName();
                jsonParser.nextToken();
                parseField(instance, fieldName, jsonParser);
                jsonParser.skipChildren();
            }
            return instance;
        } else if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
            return new ResponseList<>(m84ClassJsonMapper.parseList(jsonParser));
        }
        jsonParser.skipChildren();
        throw new IOException("Unsupported object");
    }

    @Override
    public void parseField(ResponseList<T> instance, String fieldName, JsonParser jsonParser) throws IOException {
        if ("results".equals(fieldName)) {
            instance.addAll(m84ClassJsonMapper.parseList(jsonParser));
        }
    }

    @Override
    public void serialize(ResponseList<T> object, JsonGenerator jsonGenerator, boolean writeStartAndEnd) throws IOException {
        if (object == null) {
            jsonGenerator.writeNull();
            return;
        }
        if (writeStartAndEnd) {
            jsonGenerator.writeStartArray();
        }
        for (T t : object) {
            m84ClassJsonMapper.serialize(t, jsonGenerator, true);
        }
        if (writeStartAndEnd) {
            jsonGenerator.writeEndArray();
        }
    }
}

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

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.ParameterizedType;
import com.bluelinelabs.logansquare.util.SimpleArrayMap;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

@SuppressWarnings("unused")
public final class PageableResponseList$$JsonObjectMapper<T> extends JsonMapper<PageableResponseList<T>> {
    private final JsonMapper<T> m84ClassJsonMapper;

    public PageableResponseList$$JsonObjectMapper(ParameterizedType type, ParameterizedType TType, SimpleArrayMap<ParameterizedType, JsonMapper> partialMappers) {
        partialMappers.put(type, this);
        //noinspection unchecked
        m84ClassJsonMapper = LoganSquare.mapperFor(TType, partialMappers);
    }

    @Override
    public PageableResponseList<T> parse(JsonParser jsonParser) throws IOException {
        if (jsonParser.getCurrentToken() == null) {
            jsonParser.nextToken();
        }
        PageableResponseList<T> instance = new PageableResponseList<>();
        final JsonToken currentToken = jsonParser.getCurrentToken();
        if (currentToken == JsonToken.START_OBJECT) {
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonParser.getCurrentName();
                jsonParser.nextToken();
                parseField(instance, fieldName, jsonParser);
                jsonParser.skipChildren();
            }
        } else if (currentToken == JsonToken.START_ARRAY) {
            instance.addAll(m84ClassJsonMapper.parseList(jsonParser));
        }
        return instance;
    }

    @Override
    public void parseField(PageableResponseList<T> instance, String fieldName, JsonParser jsonParser) throws IOException {
        switch (fieldName) {
            case "users":
            case "statuses":
            case "lists": {
                instance.addAll(m84ClassJsonMapper.parseList(jsonParser));
                break;
            }
            case "previous_cursor": {
                instance.previousCursor = jsonParser.getValueAsLong();
                break;
            }
            case "next_cursor": {
                instance.nextCursor = jsonParser.getValueAsLong();
                break;
            }
        }
    }

    @Override
    public void serialize(PageableResponseList<T> object, JsonGenerator jsonGenerator, boolean writeStartAndEnd) throws IOException {
        throw new UnsupportedOperationException();
    }
}

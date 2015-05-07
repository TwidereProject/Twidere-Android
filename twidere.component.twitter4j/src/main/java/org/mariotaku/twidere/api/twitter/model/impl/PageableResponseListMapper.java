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

package org.mariotaku.twidere.api.twitter.model.impl;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.mariotaku.twidere.api.twitter.TwitterConverter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import twitter4j.Status;
import twitter4j.User;

public final class PageableResponseListMapper<T> extends JsonMapper<PageableResponseListImpl<T>> {

    private static final Map<Class<?>, PageableResponseListMapper<?>> instanceCache = new HashMap<>();

    private final String listField;
    private final Class<T> elementType;

    public PageableResponseListMapper(String listField, Class<T> elementType) {
        this.listField = listField;
        this.elementType = elementType;
    }

    @Override
    public PageableResponseListImpl<T> parse(JsonParser jsonParser) throws IOException {
        PageableResponseListImpl<T> instance = new PageableResponseListImpl<>();
        if (jsonParser.getCurrentToken() == null) {
            jsonParser.nextToken();
        }
        if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
            jsonParser.skipChildren();
            return null;
        }
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonParser.getCurrentName();
            jsonParser.nextToken();
            parseField(instance, fieldName, jsonParser);
            jsonParser.skipChildren();
        }
        return instance;
    }

    public void parseField(PageableResponseListImpl<T> instance, String fieldName, JsonParser jsonParser) throws IOException {
        if (listField.equals(fieldName)) {
            instance.addAll(LoganSquare.mapperFor(elementType).parseList(jsonParser));
        } else if ("previous_cursor".equals(fieldName)) {
            instance.previousCursor = jsonParser.getValueAsLong(0);
        } else if ("next_cursor".equals(fieldName)) {
            instance.previousCursor = jsonParser.getValueAsLong(0);
        }
    }

    @Override
    public void serialize(PageableResponseListImpl object, JsonGenerator jsonGenerator, boolean writeStartAndEnd) throws IOException {
        throw new UnsupportedOperationException();
    }

    public synchronized static <C> PageableResponseListMapper<C> get(Class<C> cls) {
        @SuppressWarnings("unchecked")
        final PageableResponseListMapper<C> cache = (PageableResponseListMapper<C>) instanceCache.get(cls);
        if (cache != null) return cache;
        final String listField;
        if (User.class.isAssignableFrom(cls)) {
            listField = "users";
        } else if (Status.class.isAssignableFrom(cls)) {
            listField = "statuses";
        } else {
            throw new TwitterConverter.UnsupportedTypeException(cls);
        }
        final PageableResponseListMapper<C> created = new PageableResponseListMapper<>(listField, cls);
        instanceCache.put(cls, created);
        return created;
    }

}

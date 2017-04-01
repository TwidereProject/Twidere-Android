/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.microblog.library.twitter.util;

import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;

import com.bluelinelabs.logansquare.Commons_ParameterizedTypeAccessor;
import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.ParameterizedType;
import com.fasterxml.jackson.core.JsonParseException;

import org.mariotaku.commons.logansquare.LoganSquareMapperFinder;
import org.mariotaku.microblog.library.twitter.model.TwitterResponse;
import org.mariotaku.restfu.RestConverter;
import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.restfu.http.mime.SimpleBody;
import org.mariotaku.restfu.http.mime.StringBody;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by mariotaku on 2017/3/23.
 */

public class LoganSquareConverterFactory<E extends Exception> extends RestConverter.SimpleFactory<E> {

    protected SimpleArrayMap<Type, RestConverter<HttpResponse, ?, E>> responseConverters = new SimpleArrayMap<>();
    protected SimpleArrayMap<Type, RestConverter<?, Body, E>> sBodyConverters = new SimpleArrayMap<>();

    @Override
    public RestConverter<HttpResponse, ?, E> forResponse(Type type) throws RestConverter.ConvertException {
        RestConverter<HttpResponse, ?, E> converter = responseConverters.get(type);
        if (converter != null) {
            return converter;
        }
        return new JsonResponseConverter<>(type);
    }

    @Override
    public RestConverter<?, Body, E> forRequest(Type type) throws RestConverter.ConvertException {
        final RestConverter<?, Body, E> converter = sBodyConverters.get(type);
        if (converter != null) {
            return converter;
        }
        if (SimpleBody.supports(type)) {
            return new SimpleBodyConverter<>(type);
        }
        return new JsonRequestConverter<>(type);
    }

    @NonNull
    private static Object parseOrThrow(HttpResponse response, ParameterizedType<?> type)
            throws IOException, RestConverter.ConvertException {
        try {
            final Object parsed;
            if (type.rawType == List.class) {
                final JsonMapper mapper = LoganSquareMapperFinder.mapperFor(type.typeParameters.get(0).rawType);
                parsed = mapper.parseList(response.getBody().stream());
            } else if (type.rawType == Map.class) {
                final JsonMapper mapper = LoganSquareMapperFinder.mapperFor(type.typeParameters.get(1).rawType);
                parsed = mapper.parseMap(response.getBody().stream());
            } else {
                final JsonMapper mapper = LoganSquareMapperFinder.mapperFor(type);
                parsed = mapper.parse(response.getBody().stream());
            }
            if (parsed == null) {
                throw new IOException("Empty data");
            }
            return parsed;
        } catch (JsonParseException e) {
            throw new RestConverter.ConvertException("Malformed JSON Data");
        }
    }

    private static class JsonResponseConverter<E extends Exception> implements RestConverter<HttpResponse, Object, E> {
        private final ParameterizedType<?> type;

        JsonResponseConverter(Type type) {
            this.type = Commons_ParameterizedTypeAccessor.create(type);
        }

        @Override
        public Object convert(HttpResponse httpResponse) throws IOException, ConvertException, E {
            final Object object = parseOrThrow(httpResponse, type);
            if (object instanceof TwitterResponse) {
                ((TwitterResponse) object).processResponseHeader(httpResponse);
            }
            return object;
        }
    }

    private static class JsonRequestConverter<E extends Exception> implements RestConverter<Object, Body, E> {
        private final ParameterizedType<?> type;

        JsonRequestConverter(Type type) {
            this.type = Commons_ParameterizedTypeAccessor.create(type);
        }

        @Override
        public Body convert(Object request) throws IOException, ConvertException, E {
            final String json;
            if (type.rawType == List.class) {
                final JsonMapper mapper = LoganSquareMapperFinder.mapperFor(type.typeParameters.get(0).rawType);
                //noinspection unchecked
                json = mapper.serialize((List) request);
            } else if (type.rawType == Map.class) {
                final JsonMapper mapper = LoganSquareMapperFinder.mapperFor(type.typeParameters.get(1).rawType);
                //noinspection unchecked
                json = mapper.serialize((Map) request);
            } else {
                final JsonMapper mapper = LoganSquareMapperFinder.mapperFor(type);
                //noinspection unchecked
                json = mapper.serialize(request);
            }
            return new StringBody(json, ContentType.parse("application/json"));
        }
    }
}

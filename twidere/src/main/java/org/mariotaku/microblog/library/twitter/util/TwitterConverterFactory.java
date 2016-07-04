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

package org.mariotaku.microblog.library.twitter.util;

import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;

import com.bluelinelabs.logansquare.JsonMapper;
import com.fasterxml.jackson.core.JsonParseException;

import org.mariotaku.commons.logansquare.LoganSquareMapperFinder;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.ResponseCode;
import org.mariotaku.microblog.library.twitter.model.TwitterResponse;
import org.mariotaku.restfu.RestConverter;
import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.restfu.http.mime.SimpleBody;
import org.mariotaku.restfu.http.mime.StringBody;
import org.mariotaku.restfu.oauth.OAuthToken;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by mariotaku on 15/5/5.
 */
public class TwitterConverterFactory extends RestConverter.SimpleFactory<MicroBlogException> {

    private static SimpleArrayMap<Type, RestConverter<HttpResponse, ?, MicroBlogException>> sResponseConverters = new SimpleArrayMap<>();
    private static SimpleArrayMap<Type, RestConverter<?, Body, MicroBlogException>> sBodyConverters = new SimpleArrayMap<>();


    static {
        sResponseConverters.put(ResponseCode.class, new ResponseCode.ResponseConverter());
        sResponseConverters.put(OAuthToken.class, new OAuthTokenResponseConverter());

    }

    @NonNull
    private static Object parseOrThrow(HttpResponse response, JsonMapper<?> mapper)
            throws IOException, MicroBlogException, RestConverter.ConvertException {
        try {
            final Object parsed = mapper.parse(response.getBody().stream());
            if (parsed == null) {
                final MicroBlogException exception = new MicroBlogException("Empty data");
                exception.setHttpResponse(response);
                throw exception;
            }
            return parsed;
        } catch (JsonParseException e) {
            throw new RestConverter.ConvertException("Malformed JSON Data");
        }
    }

    @Override
    public RestConverter<HttpResponse, ?, MicroBlogException> forResponse(Type type) throws RestConverter.ConvertException {
        RestConverter<HttpResponse, ?, MicroBlogException> converter = sResponseConverters.get(type);
        if (converter != null) {
            return converter;
        }
        final JsonMapper<?> mapper;
        try {
            mapper = LoganSquareMapperFinder.mapperFor(type);
        } catch (LoganSquareMapperFinder.ClassLoaderDeadLockException e) {
            throw new RestConverter.ConvertException(e);
        }
        return new JsonResponseConverter(mapper);
    }

    @Override
    public RestConverter<?, Body, MicroBlogException> forRequest(Type type) throws RestConverter.ConvertException {
        final RestConverter<?, Body, MicroBlogException> converter = sBodyConverters.get(type);
        if (converter != null) {
            return converter;
        }
        if (SimpleBody.supports(type)) {
            return new SimpleBodyConverter<>(type);
        }
        try {
            return new JsonRequestConverter(LoganSquareMapperFinder.mapperFor(type));
        } catch (LoganSquareMapperFinder.ClassLoaderDeadLockException e) {
            throw new RestConverter.ConvertException(e);
        }
    }

    public static class UnsupportedTypeException extends UnsupportedOperationException {
        public UnsupportedTypeException(Type type) {
            super("Unsupported type " + type);
        }
    }

    public static class JsonResponseConverter implements RestConverter<HttpResponse, Object, MicroBlogException> {
        private final JsonMapper<?> mapper;

        public JsonResponseConverter(JsonMapper<?> mapper) {
            this.mapper = mapper;
        }

        @Override
        public Object convert(HttpResponse httpResponse) throws IOException, ConvertException, MicroBlogException {
            final Object object = parseOrThrow(httpResponse, mapper);
            if (object instanceof TwitterResponse) {
                ((TwitterResponse) object).processResponseHeader(httpResponse);
            }
            return object;
        }
    }

    public static class JsonRequestConverter implements RestConverter<Object, Body, MicroBlogException> {
        private final JsonMapper<Object> mapper;

        public JsonRequestConverter(JsonMapper<Object> mapper) {
            this.mapper = mapper;
        }

        @Override
        public Body convert(Object request) throws IOException, ConvertException, MicroBlogException {
            return new StringBody(mapper.serialize(request), ContentType.parse("application/json"));
        }
    }

}

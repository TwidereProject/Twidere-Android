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

package org.mariotaku.twidere.api.twitter.util;

import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;

import com.bluelinelabs.logansquare.JsonMapper;
import com.fasterxml.jackson.core.JsonParseException;

import org.mariotaku.restfu.RestConverter;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;
import org.mariotaku.twidere.api.twitter.model.ResponseCode;
import org.mariotaku.twidere.api.twitter.model.TwitterResponse;
import org.mariotaku.twidere.util.LoganSquareMapperFinder;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by mariotaku on 15/5/5.
 */
public class TwitterConverterFactory extends RestConverter.SimpleFactory<TwitterException> {

    private static SimpleArrayMap<Type, RestConverter<HttpResponse, ?, TwitterException>> sResponseConverters = new SimpleArrayMap<>();
    private static SimpleArrayMap<Type, RestConverter<?, Body, TwitterException>> sBodyConverters = new SimpleArrayMap<>();


    static {
        sResponseConverters.put(ResponseCode.class, new ResponseCode.ResponseConverter());
        sResponseConverters.put(OAuthToken.class, new OAuthToken.ResponseConverter());

//        sBodyConverters.put(CardDataMap.class, new CardDataMap.BodyConverter());
    }

    @NonNull
    private static Object parseOrThrow(HttpResponse response, JsonMapper<?> mapper)
            throws IOException, TwitterException, RestConverter.ConvertException {
        try {
            final Object parsed = mapper.parse(response.getBody().stream());
            if (parsed == null) {
                final TwitterException exception = new TwitterException("Empty data");
                exception.setHttpResponse(response);
                throw exception;
            }
            return parsed;
        } catch (JsonParseException e) {
            throw new RestConverter.ConvertException("Malformed JSON Data");
        }
    }

    @Override
    public RestConverter<HttpResponse, ?, TwitterException> forResponse(Type type) throws RestConverter.ConvertException {
        RestConverter<HttpResponse, ?, TwitterException> converter = sResponseConverters.get(type);
        if (converter != null) {
            return converter;
        }
        final JsonMapper<?> mapper;
        try {
            mapper = LoganSquareMapperFinder.mapperFor(type);
        } catch (LoganSquareMapperFinder.ClassLoaderDeadLockException e) {
            throw new RestConverter.ConvertException(e);
        }
        return new JsonConverter(mapper);
    }

    @Override
    public RestConverter<?, Body, TwitterException> forRequest(Type type) throws RestConverter.ConvertException {
        final RestConverter<?, Body, TwitterException> converter = sBodyConverters.get(type);
        if (converter != null) {
            return converter;
        }
        return super.forRequest(type);
    }

    public static class UnsupportedTypeException extends UnsupportedOperationException {
        public UnsupportedTypeException(Type type) {
            super("Unsupported type " + type);
        }
    }

    public static class JsonConverter implements RestConverter<HttpResponse, Object, TwitterException> {
        private final JsonMapper<?> mapper;

        public JsonConverter(JsonMapper<?> mapper) {
            this.mapper = mapper;
        }

        @Override
        public Object convert(HttpResponse httpResponse) throws IOException, ConvertException, TwitterException {
            final Object object = parseOrThrow(httpResponse, mapper);
            if (object instanceof TwitterResponse) {
                ((TwitterResponse) object).processResponseHeader(httpResponse);
            }
            return object;
        }
    }

}

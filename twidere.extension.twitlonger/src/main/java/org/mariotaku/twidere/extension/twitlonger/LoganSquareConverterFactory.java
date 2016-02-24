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

package org.mariotaku.twidere.extension.twitlonger;

import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.JsonMapper;
import com.fasterxml.jackson.core.JsonParseException;

import org.mariotaku.restfu.RestConverter;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.twidere.util.LoganSquareMapperFinder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mariotaku on 15/5/5.
 */
public class LoganSquareConverterFactory extends RestConverter.SimpleFactory<TwitLongerException> {

    private static Map<Type, RestConverter<HttpResponse, ?, TwitLongerException>> sResponseConverters = new HashMap<>();
    private static Map<Type, RestConverter<?, Body, TwitLongerException>> sBodyConverters = new HashMap<>();


    static {
    }

    @NonNull
    private static Object parseOrThrow(Body body, JsonMapper<?> mapper)
            throws IOException, TwitLongerException, RestConverter.ConvertException {
        try {
            final Object parsed = mapper.parse(body.stream());
            if (parsed == null) {
                throw new TwitLongerException("Empty data");
            }
            return parsed;
        } catch (JsonParseException e) {
            throw new RestConverter.ConvertException("Malformed JSON Data");
        }
    }

    @Override
    public RestConverter<HttpResponse, ?, TwitLongerException> forResponse(Type type) throws RestConverter.ConvertException {
        RestConverter<HttpResponse, ?, TwitLongerException> converter = sResponseConverters.get(type);
        if (converter != null) {
            return converter;
        }
        final JsonMapper<?> mapper;
        try {
            mapper = LoganSquareMapperFinder.mapperFor(type);
        } catch (LoganSquareMapperFinder.ClassLoaderDeadLockException e) {
            throw new RestConverter.ConvertException(e);
        }
        return new LoganSquareResponseConverter(mapper);
    }

    @Override
    public RestConverter<?, Body, TwitLongerException> forRequest(Type type) throws RestConverter.ConvertException {
        final RestConverter<?, Body, TwitLongerException> converter = sBodyConverters.get(type);
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

    public static class LoganSquareResponseConverter implements RestConverter<HttpResponse, Object, TwitLongerException> {
        private final JsonMapper<?> mapper;

        public LoganSquareResponseConverter(JsonMapper<?> mapper) {
            this.mapper = mapper;
        }

        @Override
        public Object convert(HttpResponse httpResponse) throws IOException, ConvertException, TwitLongerException {
            final Body body = httpResponse.getBody();
            final Object object = parseOrThrow(body, mapper);
            return object;
        }
    }

}

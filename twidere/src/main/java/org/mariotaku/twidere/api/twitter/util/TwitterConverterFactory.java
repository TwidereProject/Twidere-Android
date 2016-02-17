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
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.ParameterizedType;
import com.bluelinelabs.logansquare.ParameterizedTypeAccessor;
import com.fasterxml.jackson.core.JsonParseException;

import org.mariotaku.restfu.RestConverter;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;
import org.mariotaku.twidere.api.twitter.model.ResponseCode;
import org.mariotaku.twidere.api.twitter.model.TwitterResponse;
import org.mariotaku.twidere.util.BugReporter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by mariotaku on 15/5/5.
 */
public class TwitterConverterFactory extends RestConverter.SimpleFactory<TwitterException> {

    private static SimpleArrayMap<Type, RestConverter<HttpResponse, ?, TwitterException>> sResponseConverters = new SimpleArrayMap<>();
    private static SimpleArrayMap<Type, RestConverter<?, Body, TwitterException>> sBodyConverters = new SimpleArrayMap<>();

    private static final ExecutorService pool = Executors.newSingleThreadExecutor();

    static {
        sResponseConverters.put(ResponseCode.class, new ResponseCode.Converter());
        sResponseConverters.put(OAuthToken.class, new OAuthToken.Converter());
    }

    @NonNull
    private static Object parseOrThrow(Body body, JsonMapper<?> mapper)
            throws IOException, TwitterException, RestConverter.ConvertException {
        try {
            final Object parsed = mapper.parse(body.stream());
            if (parsed == null) {
                throw new TwitterException("Empty data");
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
        final ParameterizedType<?> parameterizedType = ParameterizedTypeAccessor.create(type);
        final Future<JsonMapper<?>> future = pool.submit(new Callable<JsonMapper<?>>() {
            @Override
            public JsonMapper<?> call() {
                return LoganSquare.mapperFor(parameterizedType);
            }
        });
        final JsonMapper<?> mapper;
        //noinspection TryWithIdenticalCatches
        try {
            mapper = future.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            } else if (cause instanceof Error) {
                throw ((Error) cause);
            } else {
                throw new RuntimeException(e);
            }
        } catch (TimeoutException e) {
            if (BuildConfig.DEBUG) {
                throw new RuntimeException(e);
            } else {
                BugReporter.logException(e);
                throw new RestConverter.ConvertException(e);
            }
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
            final Body body = httpResponse.getBody();
            final Object object = parseOrThrow(body, mapper);
            if (object instanceof TwitterResponse) {
                ((TwitterResponse) object).processResponseHeader(httpResponse);
            }
            return object;
        }
    }

}

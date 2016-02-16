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
import android.util.Log;
import android.util.TimingLogger;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.ParameterizedType;
import com.bluelinelabs.logansquare.ParameterizedTypeAccessor;
import com.fasterxml.jackson.core.JsonParseException;

import org.mariotaku.restfu.RestConverter;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.restfu.http.mime.SimpleBody;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;
import org.mariotaku.twidere.api.twitter.model.ResponseCode;
import org.mariotaku.twidere.api.twitter.model.TwitterResponse;
import org.mariotaku.twidere.util.TwidereTypeUtils;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;

/**
 * Created by mariotaku on 15/5/5.
 */
public class TwitterConverterFactory extends RestConverter.SimpleFactory<TwitterException> {

    private static SimpleArrayMap<Type, RestConverter<HttpResponse, ?, TwitterException>> sResponseConverters = new SimpleArrayMap<>();
    private static SimpleArrayMap<Type, RestConverter<?, Body, TwitterException>> sBodyConverters = new SimpleArrayMap<>();

    static {
        sResponseConverters.put(ResponseCode.class, new ResponseCode.Converter());
        sResponseConverters.put(OAuthToken.class, new OAuthToken.Converter());
    }

    public static TwitterException parseTwitterException(HttpResponse resp) {
        try {
            final Body body = resp.getBody();
            if (body == null) return new TwitterException(resp);
            final JsonMapper<TwitterException> mapper = LoganSquare.mapperFor(TwitterException.class);
            final Reader reader = SimpleBody.reader(body);
            final TwitterException parse = mapper.parse(LoganSquare.JSON_FACTORY.createParser(reader));
            if (parse != null) return parse;
            return new TwitterException(resp);
        } catch (JsonParseException e) {
            return new TwitterException("Malformed JSON Data", e, resp);
        } catch (IOException e) {
            return new TwitterException("IOException while throwing exception", e, resp);
        }
    }

    @NonNull
    private static <T> T parseOrThrow(Body body, Type type)
            throws IOException, TwitterException, RestConverter.ConvertException {
        try {
            if (BuildConfig.DEBUG) {
                Log.d("TwitterConverter", TwidereTypeUtils.toSimpleName(type) + " <---");
            }
            final ParameterizedType<T> parameterizedType = ParameterizedTypeAccessor.create(type);
            final JsonMapper<T> mapper = LoganSquare.mapperFor(parameterizedType);
            if (BuildConfig.DEBUG) {
                Log.d("TwitterConverter", TwidereTypeUtils.toSimpleName(type) + " ---> " + TwidereTypeUtils.toSimpleName(mapper.getClass()));
            }
            final Reader reader = SimpleBody.reader(body);
            final T parsed = mapper.parse(LoganSquare.JSON_FACTORY.createParser(reader));
            if (BuildConfig.DEBUG) {
                Log.d("TwitterConverter", TwidereTypeUtils.toSimpleName(type) + " Finished");
            }
            if (parsed == null) {
                throw new TwitterException("Empty data");
            }
            return parsed;
        } catch (JsonParseException e) {
            throw new RestConverter.ConvertException("Malformed JSON Data");
        }
    }

    @Override
    public RestConverter<HttpResponse, ?, TwitterException> forResponse(Type type) {
        RestConverter<HttpResponse, ?, TwitterException> converter = sResponseConverters.get(type);
        if (converter != null) {
            return converter;
        }
        return new TwitterConverter(type);
    }

    @Override
    public RestConverter<?, Body, TwitterException> forRequest(Type type) {
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

    public static class TwitterConverter implements RestConverter<HttpResponse, Object, TwitterException> {
        private final Type type;

        public TwitterConverter(Type type) {
            this.type = type;
        }

        @Override
        public Object convert(HttpResponse httpResponse) throws IOException, ConvertException, TwitterException {
            final TimingLogger logger = new TimingLogger("TwitterConverter", TwidereTypeUtils.toSimpleName(type));
            logger.addSplit("Status code: " + httpResponse.getStatus());
            final Body body = httpResponse.getBody();
            logger.addSplit("Start parsing");
            final Object object = parseOrThrow(body, type);
            logger.addSplit("End parsing");
            if (object instanceof TwitterResponse) {
                ((TwitterResponse) object).processResponseHeader(httpResponse);
            }
            if (BuildConfig.DEBUG) {
                logger.dumpToLog();
            }
            return object;
        }
    }
}

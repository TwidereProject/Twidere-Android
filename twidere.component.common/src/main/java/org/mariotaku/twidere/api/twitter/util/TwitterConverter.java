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

import android.support.v4.util.SimpleArrayMap;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.ParameterizedType;
import com.bluelinelabs.logansquare.ParameterizedTypeTrojan;
import com.fasterxml.jackson.core.JsonParseException;

import org.mariotaku.restfu.Converter;
import org.mariotaku.restfu.Utils;
import org.mariotaku.restfu.http.RestHttpResponse;
import org.mariotaku.restfu.http.mime.TypedData;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;
import org.mariotaku.twidere.api.twitter.model.ResponseCode;
import org.mariotaku.twidere.api.twitter.model.TwitterResponseObject;
import org.mariotaku.twidere.api.twitter.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by mariotaku on 15/5/5.
 */
public class TwitterConverter implements Converter {

    private static SimpleArrayMap<Type, Converter> sConverters = new SimpleArrayMap<>();

    static {
        sConverters.put(ResponseCode.class, new ResponseCode.Converter());
        sConverters.put(OAuthToken.class, new OAuthToken.Converter());
    }

    public static TwitterException parseTwitterException(RestHttpResponse resp) {
        try {
            final TypedData body = resp.getBody();
            if (body == null) return new TwitterException(resp);
            final TwitterException parse = LoganSquare.parse(body.stream(), TwitterException.class);
            if (parse != null) return parse;
            return new TwitterException(resp);
        } catch (JsonParseException e) {
            return new TwitterException("Malformed JSON Data", e, resp);
        } catch (IOException e) {
            return new TwitterException("IOException while throwing exception", e, resp);
        }
    }

    private static <T> T parseOrThrow(RestHttpResponse resp, InputStream stream, Type type) throws IOException, TwitterException {
        try {
            final ParameterizedType<T> parameterizedType = ParameterizedTypeTrojan.create(type);
            final T parse = LoganSquare.parse(stream, parameterizedType);
            if (TwitterException.class == type && parse == null) {
                throw new TwitterException();
            }
            return parse;
        } catch (JsonParseException e) {
            throw new TwitterException("Malformed JSON Data", resp);
        }
    }

    private static <T> List<T> parseListOrThrow(RestHttpResponse resp, InputStream stream, Class<T> elementCls) throws IOException, TwitterException {
        try {
            return LoganSquare.parseList(stream, elementCls);
        } catch (JsonParseException e) {
            throw new TwitterException("Malformed JSON Data", e, resp);
        }
    }

    @Override
    public Object convert(RestHttpResponse response, Type type) throws Exception {
        final TypedData body = response.getBody();
        if (!response.isSuccessful()) {
            throw TwitterConverter.<TwitterException>parseOrThrow(response, body.stream(), TwitterException.class);
        }
        try {
            Converter converter = sConverters.get(type);
            if (converter != null) {
                return converter.convert(response, type);
            }
            final InputStream stream = body.stream();
            final Object object = parseOrThrow(response, stream, type);
            checkResponse(type, object, response);
            if (object instanceof TwitterResponseObject) {
                ((TwitterResponseObject) object).processResponseHeader(response);
            }
            return object;
        } finally {
            Utils.closeSilently(body);
        }
    }

    private void checkResponse(Type type, Object object, RestHttpResponse response) throws TwitterException {
        if (User.class.equals(type)) {
            if (object == null) throw new TwitterException("User is null");
        }
    }

    public static class UnsupportedTypeException extends UnsupportedOperationException {
        public UnsupportedTypeException(Type type) {
            super("Unsupported type " + type);
        }
    }
}

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

import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

import org.mariotaku.library.logansquare.extension.LoganSquareWrapper;
import org.mariotaku.restfu.Converter;
import org.mariotaku.restfu.Utils;
import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.RestHttpResponse;
import org.mariotaku.restfu.http.mime.TypedData;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;
import org.mariotaku.twidere.api.twitter.model.ResponseCode;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.impl.ResponseListImpl;
import org.mariotaku.twidere.api.twitter.model.impl.TwitterModelWrapper;
import org.mariotaku.twidere.api.twitter.model.impl.TwitterResponseImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.List;

/**
 * Created by mariotaku on 15/5/5.
 */
public class TwitterConverter implements Converter {

    public static TwitterException parseTwitterException(RestHttpResponse resp) {
        try {
            final TypedData body = resp.getBody();
            if (body == null) return new TwitterException(resp);
            final TwitterException parse = LoganSquareWrapper.parse(body.stream(), TwitterException.class);
            if (parse != null) return parse;
            return new TwitterException(resp);
        } catch (JsonParseException e) {
            return new TwitterException("Malformed JSON Data", e, resp);
        } catch (IOException e) {
            return new TwitterException("IOException while throwing exception", e, resp);
        }
    }

    private static <T> T parseOrThrow(RestHttpResponse resp, InputStream stream, Class<T> cls) throws IOException, TwitterException {
        try {
            final T parse = LoganSquareWrapper.parse(stream, cls);
            if (TwitterException.class.isAssignableFrom(cls) && parse == null) {
                throw new TwitterException();
            }
            return parse;
        } catch (JsonParseException e) {
            throw new TwitterException("Malformed JSON Data", resp);
        }
    }

    private static <T> List<T> parseListOrThrow(RestHttpResponse resp, InputStream stream, Class<T> elementCls) throws IOException, TwitterException {
        try {
            return LoganSquareWrapper.parseList(stream, elementCls);
        } catch (JsonParseException e) {
            throw new TwitterException("Malformed JSON Data", resp);
        }
    }

    @Override
    public Object convert(RestHttpResponse response, Type type) throws Exception {
        final TypedData body = response.getBody();
        if (!response.isSuccessful()) {
            throw parseOrThrow(response, body.stream(), TwitterException.class);
        }
        final ContentType contentType = body.contentType();
        final InputStream stream = body.stream();
        try {
            if (type instanceof Class<?>) {
                final Class<?> cls = (Class<?>) type;
                final Class<?> wrapperCls = LoganSquareWrapper.getWrapperClass(cls);
                if (wrapperCls != null) {
                    final TwitterModelWrapper<?> wrapper = (TwitterModelWrapper<?>) parseOrThrow(response, stream, wrapperCls);
                    wrapper.processResponseHeader(response);
                    return wrapper.getWrapped(null);
                } else if (OAuthToken.class.isAssignableFrom(cls)) {
                    final ByteArrayOutputStream os = new ByteArrayOutputStream();
                    body.writeTo(os);
                    Charset charset = contentType != null ? contentType.getCharset() : null;
                    if (charset == null) {
                        charset = Charset.defaultCharset();
                    }
                    try {
                        return new OAuthToken(os.toString(charset.name()), charset);
                    } catch (ParseException e) {
                        throw new IOException(e);
                    }
                } else if (ResponseCode.class.isAssignableFrom(cls)) {
                    return new ResponseCode(response);
                }
                final Object object = parseOrThrow(response, stream, cls);
                checkResponse(cls, object, response);
                if (object instanceof TwitterResponseImpl) {
                    ((TwitterResponseImpl) object).processResponseHeader(response);
                }
                return object;
            } else if (type instanceof ParameterizedType) {
                final Type rawType = ((ParameterizedType) type).getRawType();
                if (rawType instanceof Class<?>) {
                    final Class<?> rawClass = (Class<?>) rawType;
                    final Class<?> wrapperCls = LoganSquareWrapper.getWrapperClass(rawClass);
                    if (wrapperCls != null) {
                        final TwitterModelWrapper<?> wrapper = (TwitterModelWrapper<?>) parseOrThrow(response, stream, wrapperCls);
                        wrapper.processResponseHeader(response);
                        return wrapper.getWrapped(((ParameterizedType) type).getActualTypeArguments());
                    } else if (ResponseList.class.isAssignableFrom(rawClass)) {
                        final Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
                        final ResponseListImpl<?> responseList = new ResponseListImpl<>(parseListOrThrow(response, stream, (Class<?>) elementType));
                        responseList.processResponseHeader(response);
                        return responseList;
                    }
                }
            }
            throw new UnsupportedTypeException(type);
        } finally {
            Utils.closeSilently(stream);
        }
    }

    private void checkResponse(Class<?> cls, Object object, RestHttpResponse response) throws TwitterException {
        if (User.class.isAssignableFrom(cls)) {
            if (object == null) throw new TwitterException("User is null");
        }
    }

    private static class EnumConverter<T extends Enum<T>> implements TypeConverter<T> {
        private final Class<T> cls;

        EnumConverter(Class<T> cls) {
            this.cls = cls;
        }

        public static <T extends Enum<T>> EnumConverter<T> get(Class<T> cls) {
            return new EnumConverter<>(cls);
        }

        @SuppressWarnings({"unchecked", "TryWithIdenticalCatches"})
        @Override
        public T parse(JsonParser jsonParser) throws IOException {
            try {
                final Method method = cls.getMethod("parse", String.class);
                return (T) method.invoke(null, jsonParser.getValueAsString());
            } catch (NoSuchMethodException e) {
                return Enum.valueOf(cls, jsonParser.getValueAsString());
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void serialize(T object, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) {
            throw new UnsupportedOperationException();
        }
    }

    public static class UnsupportedTypeException extends UnsupportedOperationException {
        public UnsupportedTypeException(Type type) {
            super("Unsupported type " + type);
        }
    }
}

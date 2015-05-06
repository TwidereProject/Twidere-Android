/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.api.twitter;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import org.mariotaku.simplerestapi.Converter;
import org.mariotaku.simplerestapi.http.ContentType;
import org.mariotaku.simplerestapi.http.RestResponse;
import org.mariotaku.simplerestapi.http.mime.TypedData;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;
import org.mariotaku.twidere.api.twitter.model.impl.HashtagEntityImpl;
import org.mariotaku.twidere.api.twitter.model.impl.Indices;
import org.mariotaku.twidere.api.twitter.model.impl.MediaEntityImpl;
import org.mariotaku.twidere.api.twitter.model.impl.PlaceImpl;
import org.mariotaku.twidere.api.twitter.model.impl.ResponseListImpl;
import org.mariotaku.twidere.api.twitter.model.impl.SavedSearchImpl;
import org.mariotaku.twidere.api.twitter.model.impl.StatusImpl;
import org.mariotaku.twidere.api.twitter.model.impl.TypeConverterMapper;
import org.mariotaku.twidere.api.twitter.model.impl.UrlEntityImpl;
import org.mariotaku.twidere.api.twitter.model.impl.UserImpl;
import org.mariotaku.twidere.api.twitter.model.impl.UserMentionEntityImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.text.ParseException;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.ResponseList;
import twitter4j.SavedSearch;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.UrlEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

/**
 * Created by mariotaku on 15/5/5.
 */
public class TwitterConverter implements Converter {

    static {
        TypeConverterMapper.register(Status.class, StatusImpl.class);
        TypeConverterMapper.register(User.class, UserImpl.class);
        TypeConverterMapper.register(SavedSearch.class, SavedSearchImpl.class);
        TypeConverterMapper.register(UrlEntity.class, UrlEntityImpl.class);
        TypeConverterMapper.register(MediaEntity.class, MediaEntityImpl.class);
        TypeConverterMapper.register(UserMentionEntity.class, UserMentionEntityImpl.class);
        TypeConverterMapper.register(HashtagEntity.class, HashtagEntityImpl.class);
        TypeConverterMapper.register(Place.class, PlaceImpl.class);

        LoganSquare.registerTypeConverter(Indices.class, Indices.CONVERTER);
        LoganSquare.registerTypeConverter(GeoLocation.class, GeoLocation.CONVERTER);
        LoganSquare.registerTypeConverter(MediaEntity.Type.class, new EnumConverter(MediaEntity.Type.class));
//        TypeConverterMapper.register(DirectMessage.class, DirectMessageImpl.class);
    }

    @Override
    public Object convert(RestResponse response, Type type) throws Exception {
        final TypedData body = response.getBody();
        if (!response.isSuccessful()) {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            body.writeTo(os);
            os.flush();
            final ContentType contentType = body.contentType();
            Charset charset = null;
            if (contentType != null) {
                charset = contentType.getCharset();
            }
            if (charset == null) charset = Charset.defaultCharset();
            throw new TwitterException(os.toString(charset.name()));
        }
        final ContentType contentType = body.contentType();
        final InputStream stream = body.stream();
        if (type instanceof Class<?>) {
            final Class<?> cls = (Class<?>) type;
            if (OAuthToken.class.isAssignableFrom(cls)) {
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
            }
            LoganSquare.parse(stream, cls);
        } else if (type instanceof ParameterizedType) {
            final Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class<?>) {
                final Class<?> rawClass = (Class<?>) rawType;
                if (ResponseList.class.isAssignableFrom(rawClass)) {
                    final Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
                    return new ResponseListImpl<>(LoganSquare.parseList(stream, (Class<?>) elementType));
                }
            }
        }
        throw new UnsupportedTypeException(type);
    }

    private static class EnumConverter<T extends Enum<T>> implements TypeConverter<T> {
        private final Class<T> cls;

        EnumConverter(Class<T> cls) {
            this.cls = cls;
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

    private class UnsupportedTypeException extends UnsupportedOperationException {
        public UnsupportedTypeException(Type type) {
            super("Unsupported type " + type);
        }
    }
}

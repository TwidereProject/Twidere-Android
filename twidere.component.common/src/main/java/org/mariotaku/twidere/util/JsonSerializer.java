/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.ParameterizedType;

import org.mariotaku.commons.logansquare.LoganSquareMapperFinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by mariotaku on 15/8/6.
 */
public class JsonSerializer {

    private JsonSerializer() {
    }

    @Nullable
    public static <T> String serializeList(@Nullable final List<T> list, final Class<T> cls) {
        if (list == null) return null;
        try {
            return LoganSquareMapperFinder.mapperFor(cls).serialize(list);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static <T> String serializeMap(@Nullable final Map<String, T> list, final Class<T> cls) {
        if (list == null) return null;
        try {
            return LoganSquareMapperFinder.mapperFor(cls).serialize(list);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static <T> String serialize(@Nullable final T[] array, final Class<T> cls) {
        if (array == null) return null;
        try {
            return LoganSquareMapperFinder.mapperFor(cls).serialize(Arrays.asList(array));
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static <T> String serialize(@Nullable final T object, final Class<T> cls) {
        if (object == null) return null;
        try {
            return LoganSquareMapperFinder.mapperFor(cls).serialize(object);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static <T> String serialize(@Nullable final T object, final ParameterizedType<T> cls) {
        if (object == null) return null;
        try {
            return LoganSquareMapperFinder.mapperFor(cls).serialize(object);
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> void serialize(@Nullable final T object, final OutputStream st,
            final Class<T> cls) throws IOException {
        LoganSquareMapperFinder.mapperFor(cls).serialize(object, st);
    }

    public static <T> void serialize(@Nullable final List<T> list, final OutputStream st,
            final Class<T> cls) throws IOException {
        LoganSquareMapperFinder.mapperFor(cls).serialize(list, st);
    }

    @NonNull
    public static <T> String serialize(@Nullable final T object) throws IOException {
        if (object == null) throw new IOException();
        //noinspection unchecked
        final Class<T> cls = (Class<T>) object.getClass();
        final JsonMapper<T> mapper = LoganSquareMapperFinder.mapperFor(cls);
        return mapper.serialize(object);
    }

    @NonNull
    public static <T> T[] parseArray(@Nullable final String string, final Class<T> cls) throws IOException {
        if (string == null) throw new IOException();
        final List<T> list = LoganSquareMapperFinder.mapperFor(cls).parseList(string);
        //noinspection unchecked
        return list.toArray((T[]) Array.newInstance(cls, list.size()));
    }

    @NonNull
    public static <T> T parse(@Nullable final String string, final Class<T> cls) throws IOException {
        if (string == null) throw new IOException();
        return LoganSquareMapperFinder.mapperFor(cls).parse(string);
    }

    @NonNull
    public static <T> T parse(@Nullable final String string, final ParameterizedType<T> cls) throws IOException {
        if (string == null) throw new IOException();
        return LoganSquareMapperFinder.mapperFor(cls).parse(string);
    }

    @NonNull
    public static <T> T parse(@Nullable final InputStream stream, final Class<T> cls) throws IOException {
        if (stream == null) throw new IOException();
        return LoganSquareMapperFinder.mapperFor(cls).parse(stream);
    }


    @NonNull
    public static <T> T parse(@Nullable final InputStream stream, final ParameterizedType<T> cls) throws IOException {
        if (stream == null) throw new IOException();
        return LoganSquareMapperFinder.mapperFor(cls).parse(stream);
    }

    @NonNull
    public static <E> List<E> parseList(@Nullable InputStream stream, Class<E> cls) throws IOException {
        if (stream == null) throw new IOException();
        return LoganSquareMapperFinder.mapperFor(cls).parseList(stream);
    }

    @NonNull
    public static <E> Map<String, E> parseMap(@Nullable InputStream stream, Class<E> cls) throws IOException {
        if (stream == null) throw new IOException();
        return LoganSquareMapperFinder.mapperFor(cls).parseMap(stream);
    }

    @NonNull
    public static <E> List<E> parseList(@Nullable String json, Class<E> cls) throws IOException {
        if (json == null) throw new IOException();
        return LoganSquareMapperFinder.mapperFor(cls).parseList(json);
    }

}

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

package org.mariotaku.twidere.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    static {
    }

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

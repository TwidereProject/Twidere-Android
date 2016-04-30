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

import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.JsonMapper;

import org.mariotaku.commons.logansquare.LoganSquareMapperFinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    public static <T> String serialize(@Nullable final List<T> list, final Class<T> cls) {
        if (list == null) return null;
        try {
            return LoganSquareMapperFinder.mapperFor(cls).serialize(list);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static <T> String serialize(@Nullable final Map<String, T> list, final Class<T> cls) {
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
    public static <T> String serialize(@Nullable final T object) {
        if (object == null) return null;
        try {
            //noinspection unchecked
            final JsonMapper<T> mapper = (JsonMapper<T>)
                    LoganSquareMapperFinder.mapperFor(object.getClass());
            return mapper.serialize(object);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T[] parseArray(@Nullable final String string, final Class<T> cls) {
        if (string == null) return null;
        try {
            final List<T> list = LoganSquareMapperFinder.mapperFor(cls).parseList(string);
            //noinspection unchecked
            return list.toArray((T[]) Array.newInstance(cls, list.size()));
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T parse(@Nullable final String string, final Class<T> cls) {
        if (string == null) return null;
        try {
            return LoganSquareMapperFinder.mapperFor(cls).parse(string);
        } catch (IOException e) {
            return null;
        }
    }

    public static <E> List<E> parseList(File file, Class<E> cls) {
        FileInputStream is = null;
        //noinspection TryFinallyCanBeTryWithResources
        try {
            is = new FileInputStream(file);
            return parseList(is, cls);
        } catch (IOException e) {
            return null;
        } finally {
            Utils.closeSilently(is);
        }
    }

    public static <E> List<E> parseList(InputStream stream, Class<E> cls) {
        try {
            return LoganSquareMapperFinder.mapperFor(cls).parseList(stream);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static <E> List<E> parseList(@Nullable String json, Class<E> cls) {
        if (json == null) return null;
        //noinspection TryFinallyCanBeTryWithResources
        try {
            return LoganSquareMapperFinder.mapperFor(cls).parseList(json);
        } catch (IOException e) {
            return null;
        }
    }

}

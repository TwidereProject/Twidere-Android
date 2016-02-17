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

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

/**
 * Created by mariotaku on 15/8/6.
 */
public class JsonSerializer {

    @Nullable
    public static <T> String serialize(@Nullable final List<T> list, final Class<T> cls) {
        if (list == null) return null;
        try {
            return LoganSquare.serialize(list, cls);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static <T> String serialize(@Nullable final T object, final Class<T> cls) {
        if (object == null) return null;
        try {
            return LoganSquare.mapperFor(cls).serialize(object);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T[] parseArray(@Nullable final String string, final Class<T> cls) {
        if (string == null) return null;
        try {
            final List<T> list = LoganSquare.mapperFor(cls).parseList(string);
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
            return LoganSquare.mapperFor(cls).parse(string);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static <T> String serialize(@Nullable final T obj) {
        if (obj == null) return null;
        //noinspection unchecked
        return serialize(obj, (Class<T>) obj.getClass());
    }

    public static <E> List<E> parseList(File file, Class<E> jsonObjectClass) throws IOException {
        final FileInputStream is = new FileInputStream(file);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            return LoganSquare.parseList(is, jsonObjectClass);
        } finally {
            is.close();
        }
    }

}

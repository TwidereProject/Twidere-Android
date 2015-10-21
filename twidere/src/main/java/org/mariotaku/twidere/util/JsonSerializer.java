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

import android.content.Context;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.library.logansquare.extension.LoganSquareWrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mariotaku on 15/8/6.
 */
public class JsonSerializer {
    public static final String JSON_CACHE_DIR = "json_cache";

    @Nullable
    public static <T> String serialize(@Nullable final List<T> list, final Class<T> cls) {
        if (list == null) return null;
        try {
            return LoganSquareWrapper.serialize(list, cls);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static <T> String serialize(@Nullable final T object, final Class<T> cls) {
        if (object == null) return null;
        try {
            return LoganSquareWrapper.mapperFor(cls).serialize(object);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static <T> String serializeArray(@Nullable final T[] object, final Class<T> cls) {
        if (object == null) return null;
        try {
            return LoganSquareWrapper.mapperFor(cls).serialize(Arrays.asList(object));
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static <T> List<T> parseList(@Nullable final String string, final Class<T> cls) {
        if (string == null) return null;
        try {
            return LoganSquareWrapper.mapperFor(cls).parseList(string);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T[] parseArray(@Nullable final String string, final Class<T> cls) {
        if (string == null) return null;
        try {
            final List<T> list = LoganSquareWrapper.mapperFor(cls).parseList(string);
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
            return LoganSquareWrapper.mapperFor(cls).parse(string);
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
            return LoganSquareWrapper.parseList(is, jsonObjectClass);
        } finally {
            is.close();
        }
    }

    public static JSONObject convertJSONObject(final InputStream stream) throws IOException {
        final String string = convertString(stream);
        try {
            return new JSONObject(string);
        } catch (final JSONException e) {
            throw new IOException(e);
        }
    }

    public static String convertString(final InputStream stream) throws IOException {
        if (stream == null) throw new FileNotFoundException();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset()));
        final StringBuilder buf = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            buf.append(line);
            buf.append('\n');
        }
        reader.close();
        return buf.toString();
    }

    public static File getSerializationFile(final Context context, final Object... args) throws IOException {
        if (context == null || args == null || args.length == 0) return null;
        final File cacheDir = Utils.getBestCacheDir(context, JSON_CACHE_DIR);
        if (!cacheDir.exists()) {
            AbsLogger.logIfFalse(cacheDir.mkdirs(), "Unable to create cache dir " + cacheDir);
        }
        final String filename = Utils.encodeQueryParams(TwidereArrayUtils.toString(args, '.', false));
        return new File(cacheDir, filename + ".json");
    }
}

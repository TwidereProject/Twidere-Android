package org.mariotaku.twidere.util;

import android.content.Context;

import com.bluelinelabs.logansquare.LoganSquare;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

public class LoganSquareWrapper extends LoganSquare {

    public static final String JSON_CACHE_DIR = "json_cache";

    public static JSONObject convertJSONObject(final InputStream stream) throws IOException {
        final String string = convertString(stream);
        try {
            return new JSONObject(string);
        } catch (final JSONException e) {
            throw new IOException(e);
        }
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
            AbsLogger.logIfFalse(cacheDir.mkdirs(), "Unable to create cache dir");
        }
        final String filename = Utils.encodeQueryParams(TwidereArrayUtils.toString(args, '.', false));
        return new File(cacheDir, filename + ".json");
    }


}

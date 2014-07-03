package org.mariotaku.jsonserializer;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class JSONFileIO extends JSONSerializer {

	public static final String JSON_CACHE_DIR = "json_cache";
	private static final String KEY_OBJECT = "object";

	private static final String KEY_CLASS = "class";

	public static JSONArray convertJSONArray(final InputStream stream) throws IOException {
		final String string = convertString(stream);
		try {
			return new JSONArray(string);
		} catch (final JSONException e) {
			throw new IOException(e);
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
		final StringBuffer buf = new StringBuffer();
		String line = null;
		while ((line = reader.readLine()) != null) {
			buf.append(line);
			buf.append('\n');
		}
		reader.close();
		return buf.toString();
	}

	public static File getSerializationFile(final Context context, final Object... args) throws IOException {
		if (context == null || args == null || args.length == 0) return null;
		final File cache_dir = Utils.getBestCacheDir(context, JSON_CACHE_DIR);
		if (!cache_dir.exists()) {
			cache_dir.mkdirs();
		}
		final String filename = Utils.encodeQueryParams(ArrayUtils.toString(args, '.', false));
		final File cache_file = new File(cache_dir, filename + ".json");
		return cache_file;
	}

	public static <T extends JSONParcelable> T[] readArray(final File file) throws IOException {
		if (file == null) throw new FileNotFoundException();
		return readArray(new FileInputStream(file));
	}

	public static <T extends JSONParcelable> T[] readArray(final InputStream stream) throws IOException {
		try {
			final JSONObject json = new JSONObject(convertString(stream));
			final JSONParcelable.Creator<T> creator = getCreator(json.getString(KEY_CLASS));
			return createArray(creator, json.getJSONArray(KEY_OBJECT));
		} catch (final JSONException e) {
			throw new IOException(e);
		}
	}

	public static <T extends JSONParcelable> ArrayList<T> readArrayList(final File file) throws IOException {
		if (file == null) throw new FileNotFoundException();
		return readArrayList(new FileInputStream(file));
	}

	public static <T extends JSONParcelable> ArrayList<T> readArrayList(final InputStream stream) throws IOException {
		try {
			final JSONObject json = new JSONObject(convertString(stream));
			final JSONParcelable.Creator<T> creator = getCreator(json.getString(KEY_CLASS));
			return createArrayList(creator, json.getJSONArray(KEY_OBJECT));
		} catch (final JSONException e) {
			throw new IOException(e);
		}
	}

	public static <T extends JSONParcelable> T readObject(final File file) throws IOException {
		if (file == null) throw new FileNotFoundException();
		return readObject(new FileInputStream(file));
	}

	public static <T extends JSONParcelable> T readObject(final InputStream stream) throws IOException {
		try {
			final JSONObject json = new JSONObject(convertString(stream));
			final JSONParcelable.Creator<T> creator = getCreator(json.optString(KEY_CLASS));
			return createObject(creator, json.optJSONObject(KEY_OBJECT));
		} catch (final JSONException e) {
			throw new IOException(e);
		}
	}

	public static <T extends JSONParcelable> void writeArray(final File file, final T[] array) throws IOException {
		writeArray(new FileOutputStream(file), array);
	}

	public static <T extends JSONParcelable> void writeArray(final OutputStream stream, final T[] array)
			throws IOException {
		if (stream == null || array == null) return;
		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, Charset.defaultCharset()));
		final JSONObject json = new JSONObject();
		try {
			json.put(KEY_CLASS, array.getClass().getComponentType().getName());
			json.put(KEY_OBJECT, toJSONArray(array));
			writer.write(jsonToString(json));
			writer.flush();
		} catch (final JSONException e) {
			throw new IOException(e);
		} finally {
			writer.close();
		}
	}

	public static <T extends JSONParcelable> void writeObject(final File file, final T parcelable) throws IOException {
		writeObject(new FileOutputStream(file), parcelable);
	}

	public static <T extends JSONParcelable> void writeObject(final OutputStream stream, final T parcelable)
			throws IOException {
		if (stream == null || parcelable == null) return;
		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, Charset.defaultCharset()));
		final JSONObject json = new JSONObject();
		try {
			json.put(KEY_CLASS, parcelable.getClass().getName());
			json.put(KEY_OBJECT, toJSONObject(parcelable));
			writer.write(jsonToString(json));
			writer.flush();
		} catch (final JSONException e) {
			throw new IOException(e);
		} finally {
			Utils.closeSilently(writer);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends JSONParcelable> JSONParcelable.Creator<T> getCreator(final String name)
			throws IOException {
		try {
			final Class<?> cls = Class.forName(name);
			return (JSONParcelable.Creator<T>) cls.getField("JSON_CREATOR").get(null);
		} catch (final Exception e) {
			throw new IOException(e);
		}
	}
}

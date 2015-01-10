package org.mariotaku.twidere.extension.streaming.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.twidere.TwidereConstants;

import android.os.Bundle;
import android.util.Log;

public final class ParseUtils implements TwidereConstants {

	public static String bundleToJSON(final Bundle args) {
		final Set<String> keys = args.keySet();
		final JSONObject json = new JSONObject();
		for (final String key : keys) {
			final Object value = args.get(key);
			if (value == null) {
				continue;
			}
			try {
				if (value instanceof Boolean) {
					json.put(key, args.getBoolean(key));
				} else if (value instanceof Integer) {
					json.put(key, args.getInt(key));
				} else if (value instanceof Long) {
					json.put(key, args.getLong(key));
				} else if (value instanceof String) {
					json.put(key, args.getString(key));
				} else {
					Log.w(LOGTAG, "Unknown type " + (value != null ? value.getClass().getSimpleName() : null)
							+ " in arguments key " + key);
				}
			} catch (final JSONException e) {
				e.printStackTrace();
			}
		}
		return json.toString();
	}

	public static Bundle jsonToBundle(final String string) {
		final Bundle bundle = new Bundle();
		if (string != null) {
			try {
				final JSONObject json = new JSONObject(string);
				final Iterator<?> it = json.keys();
				while (it.hasNext()) {
					final Object key_obj = it.next();
					if (key_obj == null) {
						continue;
					}
					final String key = key_obj.toString();
					final Object value = json.get(key);
					if (value instanceof Boolean) {
						bundle.putBoolean(key, json.optBoolean(key));
					} else if (value instanceof Integer) {
						// Simple workaround for account_id
						if (shouldPutLong(key)) {
							bundle.putLong(key, json.optLong(key));
						} else {
							bundle.putInt(key, json.optInt(key));
						}
					} else if (value instanceof Long) {
						bundle.putLong(key, json.optLong(key));
					} else if (value instanceof String) {
						bundle.putString(key, json.optString(key));
					} else {
						Log.w(LOGTAG, "Unknown type " + value.getClass().getSimpleName() + " in arguments key " + key);
					}
				}
			} catch (final JSONException e) {
				e.printStackTrace();
			} catch (final ClassCastException e) {
				e.printStackTrace();
			}
		}
		return bundle;
	}

	public static double parseDouble(final String source) {
		if (source == null) return -1;
		try {
			return Double.parseDouble(source);
		} catch (final NumberFormatException e) {
			// Wrong number format? Ignore them.
		}
		return -1;
	}

	public static int parseInt(final String source) {
		return parseInt(source, -1);
	}

	public static int parseInt(final String source, final int def) {
		if (source == null) return def;
		try {
			return Integer.valueOf(source);
		} catch (final NumberFormatException e) {
			// Wrong number format? Ignore them.
		}
		return def;
	}

	public static long parseLong(final String source) {
		return parseLong(source, -1);
	}

	public static long parseLong(final String source, final long def) {
		if (source == null) return def;
		try {
			return Long.parseLong(source);
		} catch (final NumberFormatException e) {
			// Wrong number format? Ignore them.
		}
		return def;
	}

	public static String parseString(final Object object) {
		return parseString(object, null);
	}

	public static String parseString(final Object object, final String def) {
		if (object == null) return def;
		return String.valueOf(object);
	}

	public static URL parseURL(final String url_string) {
		if (url_string == null) return null;
		try {
			return new URL(url_string);
		} catch (final MalformedURLException e) {
			// This should not happen.
		}
		return null;
	}

	private static boolean shouldPutLong(final String key) {
		return EXTRA_ACCOUNT_ID.equals(key) || EXTRA_USER_ID.equals(key) || EXTRA_STATUS_ID.equals(key);
	}

}

/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.graphics.Color;
import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.restfu.Utils;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.constant.IntentConstants;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import static android.text.TextUtils.isEmpty;

public final class ParseUtils {

    public static String bundleToJSON(final Bundle args) {
        final Set<String> keys = args.keySet();
        final StringWriter sw = new StringWriter();
        final JsonWriter json = new JsonWriter(sw);
        try {
            json.beginObject();
            for (final String key : keys) {
                json.name(key);
                final Object value = args.get(key);
                if (value == null) {
                    json.nullValue();
                } else if (value instanceof Boolean) {
                    json.value((Boolean) value);
                } else if (value instanceof Integer) {
                    json.value((Integer) value);
                } else if (value instanceof Long) {
                    json.value((Long) value);
                } else if (value instanceof String) {
                    json.value((String) value);
                } else if (value instanceof Float) {
                    json.value((Float) value);
                } else if (value instanceof Double) {
                    json.value((Double) value);
                } else {
                    Log.w(TwidereConstants.LOGTAG, "Unknown type " + value.getClass().getSimpleName() + " in arguments key " + key);
                }
            }
            json.endObject();
            json.flush();
            sw.flush();
            return sw.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            Utils.closeSilently(json);
        }
    }

    public static Bundle jsonToBundle(final String string) {
        final Bundle bundle = new Bundle();
        if (string == null) return bundle;
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
                    Log.w(TwidereConstants.LOGTAG, "Unknown type " + value.getClass().getSimpleName() + " in arguments key " + key);
                }
            }
        } catch (final JSONException e) {
            e.printStackTrace();
        } catch (final ClassCastException e) {
            e.printStackTrace();
        }
        return bundle;
    }

    public static double parseDouble(final String source) {
        return parseDouble(source, -1);
    }

    public static double parseDouble(final String source, final double def) {
        if (source == null) return def;
        try {
            return Double.parseDouble(source);
        } catch (final NumberFormatException e) {
            // Wrong number format? Ignore them.
        }
        return def;
    }

    public static float parseFloat(final String source) {
        return parseFloat(source, -1);
    }

    public static float parseFloat(final String source, final float def) {
        if (source == null) return def;
        try {
            return Float.parseFloat(source);
        } catch (final NumberFormatException e) {
            // Wrong number format? Ignore them.
        }
        return def;
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

    @Deprecated
    public static String parseString(final String object) {
        return object;
    }

    public static String parseString(final Object object) {
        return parseString(object, null);
    }

    public static String parseString(final int object) {
        return String.valueOf(object);
    }

    public static String parseString(final long object) {
        return String.valueOf(object);
    }

    public static String parseString(final Object object, final String def) {
        if (object == null) return def;
        return String.valueOf(object);
    }

    public static URI parseURI(final String uriString) {
        if (uriString == null) return null;
        try {
            return new URI(uriString);
        } catch (final URISyntaxException e) {
            // This should not happen.
        }
        return null;
    }

    public static URL parseURL(final String urlString) {
        if (urlString == null) return null;
        try {
            return new URL(urlString);
        } catch (final MalformedURLException e) {
            // This should not happen.
        }
        return null;
    }

    private static boolean shouldPutLong(final String key) {
        return IntentConstants.EXTRA_ACCOUNT_ID.equals(key) || IntentConstants.EXTRA_USER_ID.equals(key) || IntentConstants.EXTRA_STATUS_ID.equals(key)
                || IntentConstants.EXTRA_LIST_ID.equals(key);
    }

    public static String parsePrettyDecimal(double num, int decimalDigits) {
        String result = String.format(Locale.US, "%." + decimalDigits + "f", num);
        int dotIdx = result.lastIndexOf('.');
        if (dotIdx == -1) return result;
        int i;
        for (i = result.length() - 1; i >= 0; i--) {
            if (result.charAt(i) != '0') break;
        }
        return result.substring(0, i == dotIdx ? dotIdx : i + 1);
    }

    public static int parseColor(String str, int def) {
        if (isEmpty(str)) return def;
        try {
            return Color.parseColor(str);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

}

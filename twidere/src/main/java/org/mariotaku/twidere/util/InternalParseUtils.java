package org.mariotaku.twidere.util;

import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.restfu.RestFuUtils;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.constant.CompatibilityConstants;
import org.mariotaku.twidere.constant.IntentConstants;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * Created by mariotaku on 16/3/8.
 */
public class InternalParseUtils {
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
            RestFuUtils.closeSilently(json);
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
        } catch (final JSONException | ClassCastException e) {
            e.printStackTrace();
        }
        return bundle;
    }

    private static boolean shouldPutLong(final String key) {
        return CompatibilityConstants.EXTRA_ACCOUNT_ID.equals(key) || IntentConstants.EXTRA_USER_ID.equals(key) || IntentConstants.EXTRA_STATUS_ID.equals(key)
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
}

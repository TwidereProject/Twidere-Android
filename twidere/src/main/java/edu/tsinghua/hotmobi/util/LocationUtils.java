package edu.tsinghua.hotmobi.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;

import org.mariotaku.twidere.util.JsonSerializer;
import org.mariotaku.twidere.util.Utils;

import edu.tsinghua.hotmobi.HotMobiConstants;
import edu.tsinghua.hotmobi.model.LatLng;

/**
 * Created by mariotaku on 16/1/29.
 */
public class LocationUtils implements HotMobiConstants {
    public static LatLng getCachedLatLng(final Context context) {
        final Context appContext = context.getApplicationContext();
        final Location location = Utils.getCachedLocation(appContext);
        if (location == null) {
            return getFallbackCachedLocation(appContext);
        }
        final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences prefs = appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putString(KEY_FALLBACK_CACHED_LOCATION, JsonSerializer.serialize(latLng, LatLng.class));
                editor.apply();
            }
        });
        return latLng;
    }

    private static LatLng getFallbackCachedLocation(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return JsonSerializer.parse(prefs.getString(KEY_FALLBACK_CACHED_LOCATION, null), LatLng.class);
    }
}

package edu.tsinghua.hotmobi.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import org.mariotaku.twidere.util.JsonSerializer;
import org.mariotaku.twidere.util.Utils;

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.LatLng;

/**
 * Created by mariotaku on 16/1/29.
 */
public class LocationUtils {
    public static LatLng getCachedLatLng(Context context) {
        final Location location = Utils.getCachedLocation(context);
        if (location == null) {
            return getFallbackCachedLocation(context);
        }
        final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        final SharedPreferences prefs = context.getSharedPreferences("spice_data_profiling", Context.MODE_PRIVATE);
        prefs.edit().putString(HotMobiLogger.FALLBACK_CACHED_LOCATION, JsonSerializer.serialize(latLng, LatLng.class)).apply();
        return latLng;
    }

    private static LatLng getFallbackCachedLocation(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences("spice_data_profiling", Context.MODE_PRIVATE);
        return JsonSerializer.parse(prefs.getString(HotMobiLogger.FALLBACK_CACHED_LOCATION, null), LatLng.class);
    }
}

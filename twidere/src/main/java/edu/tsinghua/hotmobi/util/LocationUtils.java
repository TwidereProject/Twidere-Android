package edu.tsinghua.hotmobi.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.DebugLog;
import org.mariotaku.twidere.util.JsonSerializer;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.DependencyHolder;

import java.io.IOException;

import edu.tsinghua.hotmobi.HotMobiConstants;
import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.LatLng;

/**
 * Created by mariotaku on 16/1/29.
 */
public class LocationUtils implements HotMobiConstants, Constants {
    private LocationUtils() {
    }

    public static LatLng getCachedLatLng(@NonNull final Context context) {
        final Context appContext = context.getApplicationContext();
        final SharedPreferences prefs = DependencyHolder.Companion.get(context).getPreferences();
        if (!prefs.getBoolean(KEY_USAGE_STATISTICS, false)) return null;
        DebugLog.d(HotMobiLogger.LOGTAG, "getting cached location", null);
        final Location location = Utils.getCachedLocation(appContext);
        if (location == null) {
            try {
                return JsonSerializer.parse(prefs.getString(KEY_FALLBACK_CACHED_LOCATION, null), LatLng.class);
            } catch (IOException e) {
                return null;
            }
        }
        final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putString(KEY_FALLBACK_CACHED_LOCATION, JsonSerializer.serialize(latLng, LatLng.class));
                editor.apply();
            }
        });
        return latLng;
    }

}

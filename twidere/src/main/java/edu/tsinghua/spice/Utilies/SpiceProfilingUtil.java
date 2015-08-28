package edu.tsinghua.spice.Utilies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import org.mariotaku.twidere.BuildConfig;

/**
 * Created by Denny C. Ng on 2/20/15.
 */

public class SpiceProfilingUtil {

    public static final String FILE_NAME_PROFILE = "Profile_SPICE";
    public static final String FILE_NAME_LOCATION = "Location_SPICE";
    public static final String FILE_NAME_APP = "App_SPICE";
    public static final String FILE_NAME_NETWORK = "Network_SPICE";
    public static final String FILE_NAME_ONWIFI = "onWifi_SPICE";
    public static final String FILE_NAME_ONLAUNCH = "onLaunch_SPICE";
    public static final String FILE_NAME_SCREEN = "Screen_SPICE";

    @SuppressLint("InlinedApi")
    public static boolean isCharging(final Context context) {
        if (context == null) return false;
        final Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (intent == null) return false;
        final int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC
                || plugged == BatteryManager.BATTERY_PLUGGED_USB
                || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
    }

    public static boolean log(final String msg) {
        if (BuildConfig.DEBUG) {
            final StackTraceElement ste = new Throwable().fillInStackTrace().getStackTrace()[1];
            final String fullName = ste.getClassName();
            final String name = fullName.substring(fullName.lastIndexOf('.'));
            final String tag = name + "." + ste.getMethodName();
            Log.d(tag, msg);
            return true;
        } else
            return false;
    }
}

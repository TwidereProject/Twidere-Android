package edu.tsinghua.spice.Utilies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

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
    public static final String FILE_NAME_IP = "IP_SPICE";

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

    public static void profile(final Context context, final long account_id, final String text) {
        profile(context, account_id + "_" + FILE_NAME_PROFILE, text);
    }

    public static void profile(final Context context, final String name, final String text) {
        if (context == null) return;
        final SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        if (!prefs.getBoolean(Constants.KEY_USAGE_STATISTICS, false)) return;
        final String persistedDeviceId = prefs.getString(Constants.KEY_DEVICE_SERIAL, null);
        final String serial = String.valueOf(Build.SERIAL).replaceAll("[^\\w\\d]", "");
        final String uuid;
        if (!TextUtils.isEmpty(persistedDeviceId)) {
            uuid = persistedDeviceId.replaceAll("[^\\w\\d]", "");
        } else if (!TextUtils.isEmpty(serial)) {
            uuid = serial;
            prefs.edit().putString(Constants.KEY_DEVICE_SERIAL, serial).apply();
        } else {
            uuid = UUID.randomUUID().toString().replaceAll("[^\\w\\d]", "");
            prefs.edit().putString(Constants.KEY_DEVICE_SERIAL, uuid).apply();
        }
        final String filename = uuid + "_" + name + ".spi";
        new Thread() {
            @Override
            public void run() {
                try {
                    final FileOutputStream fos = context.openFileOutput(filename, Context.MODE_APPEND);
                    if (fos == null) return;
                    final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                    bw.write(text + "," + System.currentTimeMillis() + "\n");
                    bw.flush();
                    fos.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}

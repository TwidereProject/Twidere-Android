package edu.ucdavis.earlybird;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.Utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class ProfilingUtil {

	public static final String FILE_NAME_PROFILE = "Profile";
	public static final String FILE_NAME_LOCATION = "Location";
	public static final String FILE_NAME_APP = "App";
	public static final String FILE_NAME_WIFI = "Wifi";

	public static boolean isCharging(final Context context) {
		if (context == null) return false;
		final Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		if (intent == null) return false;
		final int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
	}

	public static boolean log(final Context context, final String msg) {
		if (Utils.isDebuggable(context)) {
			final StackTraceElement ste = new Throwable().fillInStackTrace().getStackTrace()[1];
			final String fullname = ste.getClassName();
			final String name = fullname.substring(fullname.lastIndexOf('.'));
			final String tag = name + "." + ste.getMethodName();
			Log.d(tag, msg);
			return true;
		} else
			return false;
	}

	public static void profile(final Context context, final long accountID, final String text) {
		profile(context, accountID + "_" + FILE_NAME_PROFILE, text);
	}

	public static void profile(final Context context, final String name, final String text) {
		if (context == null) return;
		final SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		if (!prefs.getBoolean(Constants.KEY_UCD_DATA_PROFILING, false)) return;
		final String filename = name + ".csv";
		new Thread() {
			@Override
			public void run() {
				try {
					final FileOutputStream fos = context.openFileOutput(filename, Context.MODE_APPEND);
					if (fos == null) return;
					final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
					bw.write("[" + System.currentTimeMillis() + "], " + text + "\n");
					bw.flush();
					fos.close();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}
}

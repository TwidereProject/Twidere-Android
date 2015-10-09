/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package edu.tsinghua.hotmobi;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.BatteryManager;
import android.text.TextUtils;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.ApplicationModule;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.tsinghua.hotmobi.model.BatteryRecord;
import edu.tsinghua.hotmobi.model.LatLng;
import edu.tsinghua.hotmobi.model.LinkEvent;
import edu.tsinghua.hotmobi.model.MediaEvent;
import edu.tsinghua.hotmobi.model.NetworkEvent;
import edu.tsinghua.hotmobi.model.RefreshEvent;
import edu.tsinghua.hotmobi.model.ScrollRecord;
import edu.tsinghua.hotmobi.model.SessionEvent;
import edu.tsinghua.hotmobi.model.TweetEvent;

/**
 * Created by mariotaku on 15/8/10.
 */
public class HotMobiLogger {

    public static final long ACCOUNT_ID_NOT_NEEDED = -1;

    public static final String LOGTAG = "HotMobiLogger";
    public static final long UPLOAD_INTERVAL_MILLIS = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
    public static final String LAST_UPLOAD_TIME = "last_upload_time";
    final static SimpleDateFormat DATE_FORMAT;

    static {
        DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final Application mApplication;
    private final Executor mExecutor;

    public HotMobiLogger(Application application) {
        mApplication = application;
        mExecutor = Executors.newSingleThreadExecutor();
    }

    public static String getLogFilename(Object event) {
        if (event instanceof RefreshEvent) {
            return "refresh";
        } else if (event instanceof SessionEvent) {
            return "session";
        } else if (event instanceof TweetEvent) {
            return "tweet";
        } else if (event instanceof MediaEvent) {
            return "media";
        } else if (event instanceof LinkEvent) {
            return "link";
        } else if (event instanceof NetworkEvent) {
            return "network";
        } else if (event instanceof ScrollRecord) {
            return "scroll";
        } else if (event instanceof BatteryRecord) {
            return "battery";
        }
        throw new UnsupportedOperationException("Unknown event type " + event);
    }

    public static String getInstallationSerialId(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        final String persistedDeviceId = prefs.getString(Constants.KEY_DEVICE_SERIAL, null);
        final String uuid;
        if (!TextUtils.isEmpty(persistedDeviceId)) {
            uuid = persistedDeviceId.replaceAll("[^\\w\\d]", "");
        } else {
            uuid = UUID.randomUUID().toString().replaceAll("[^\\w\\d]", "");
            prefs.edit().putString(Constants.KEY_DEVICE_SERIAL, uuid).apply();
        }
        return uuid;
    }

    public static HotMobiLogger getInstance(Context context) {
        return ApplicationModule.get(context).getHotMobiLogger();
    }

    public static LatLng getCachedLatLng(Context context) {
        final Location location = Utils.getCachedLocation(context);
        if (location == null) return null;
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public static File getLogFile(Context context, long accountId, String type) {
        final File logsDir = getLogsDir(context);
        final File todayLogDir = new File(logsDir, DATE_FORMAT.format(new Date()));
        if (!todayLogDir.exists()) {
            todayLogDir.mkdirs();
        }
        final String logFilename;
        if (accountId > 0) {
            logFilename = type + "_" + accountId + ".log";
        } else {
            logFilename = type + ".log";
        }
        return new File(todayLogDir, logFilename);
    }

    public static File getLogsDir(Context context) {
        return new File(context.getFilesDir(), "hotmobi");
    }

    public static long getLastUploadTime(final Context context) {
        final SharedPreferences prefs = context.getSharedPreferences("spice_data_profiling", Context.MODE_PRIVATE);
        return prefs.getLong(LAST_UPLOAD_TIME, -1);
    }

    public static void logPowerBroadcast(Context context) {
        logPowerBroadcast(context, context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)));
    }

    public static void logPowerBroadcast(Context context, Intent intent) {
        if (intent == null) return;
        if (!intent.hasExtra(BatteryManager.EXTRA_LEVEL) || !intent.hasExtra(BatteryManager.EXTRA_SCALE) ||
                !intent.hasExtra(BatteryManager.EXTRA_STATUS)) return;
        final BatteryRecord record = new BatteryRecord();
        record.setLevel(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) / (float)
                intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1));
        record.setState(intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1));
        record.setTimestamp(System.currentTimeMillis());
        record.setTimeOffset(TimeZone.getDefault().getRawOffset());
        getInstance(context).log(record);
    }

    public void log(long accountId, final Object event) {
        mExecutor.execute(new WriteLogTask(mApplication, accountId, event));
    }

    public void log(Object event) {
        log(ACCOUNT_ID_NOT_NEEDED, event);
    }

    public void logList(List<?> events, long accountId, String type) {
        mExecutor.execute(new WriteLogTask(mApplication, accountId, type, events));
    }
}

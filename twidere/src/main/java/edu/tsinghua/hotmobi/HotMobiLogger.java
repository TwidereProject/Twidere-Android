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
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.dagger.DependencyHolder;

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

import javax.inject.Singleton;

import edu.tsinghua.hotmobi.model.LogModel;

/**
 * Created by mariotaku on 15/8/10.
 */
@Singleton
public class HotMobiLogger implements HotMobiConstants {

    public static final String LOGTAG = "HotMobiLogger";
    public static final long UPLOAD_INTERVAL_MILLIS = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
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

    public static String getLogFilename(LogModel logModel) {
        return logModel.getLogFileName();
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
        return DependencyHolder.Companion.get(context).getHotMobiLogger();
    }

    public static File getLogFile(Context context, @Nullable UserKey accountKey, String type) {
        final File logsDir = getLogsDir(context);
        final File todayLogDir = new File(logsDir, DATE_FORMAT.format(new Date()));
        if (!todayLogDir.exists()) {
            todayLogDir.mkdirs();
        }
        final String logFilename;
        if (accountKey != null) {
            logFilename = type + "_" + accountKey + ".log";
        } else {
            logFilename = type + ".log";
        }
        return new File(todayLogDir, logFilename);
    }

    public static File getLogsDir(Context context) {
        return new File(context.getFilesDir(), "hotmobi");
    }

    public static long getLastUploadTime(final Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_LAST_UPLOAD_TIME, -1);
    }

    public static boolean printLog(final String msg) {
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

    public <T extends LogModel> void log(UserKey accountId, final T event, final PreProcessing<T> preProcessing) {
        if (!BuildConfig.HOTMOBI_LOG_ENABLED) return;
        mExecutor.execute(new WriteLogTask<>(mApplication, accountId, event, preProcessing));
    }

    public <T extends LogModel> void log(UserKey accountId, final T event) {
        log(accountId, event, null);
    }

    public <T extends LogModel> void log(final T event) {
        log(event, null);
    }

    public void log(final LogModel event, final PreProcessing preProcessing) {
        log(null, event, preProcessing);
    }

    public <T extends LogModel> void logList(List<T> events, UserKey accountId, String type) {
        logList(events, accountId, type, null);
    }

    public <T extends LogModel> void logList(List<T> events, UserKey accountId, String type, final PreProcessing<T> preProcessing) {
        if (!BuildConfig.HOTMOBI_LOG_ENABLED) return;
        mExecutor.execute(new WriteLogTask<>(mApplication, accountId, type, events, preProcessing));
    }

}

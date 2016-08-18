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

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import org.mariotaku.commons.io.StreamUtils;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Locale;

import edu.tsinghua.hotmobi.model.UploadLogEvent;

/**
 * Upload logs to target server
 * Created by mariotaku on 15/8/27.
 */
public class UploadLogsTask implements Runnable {

    private final Context context;

    public UploadLogsTask(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        if (!BuildConfig.HOTMOBI_LOG_ENABLED) return;
        final SharedPreferences prefs = context.getSharedPreferences(HotMobiConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        if (prefs.contains(HotMobiConstants.KEY_LAST_UPLOAD_TIME)) {
            final long lastUpload = prefs.getLong(HotMobiConstants.KEY_LAST_UPLOAD_TIME, System.currentTimeMillis());
            final double deltaDays = (System.currentTimeMillis() - lastUpload) /
                    (double) HotMobiLogger.UPLOAD_INTERVAL_MILLIS;
            if (deltaDays < 1) {
                HotMobiLogger.printLog("Last uploaded was conducted in 1 day ago.");
                return;
            }
        }

        try {
            context.getContentResolver().call(Uri.parse("content://edu.tsinghua.research.steel"),
                    "upload_logs", null, null);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.w(HotMobiLogger.LOGTAG, e);
            }
        }
        if (uploadLogs()) {
            prefs.edit().putLong(HotMobiConstants.KEY_LAST_UPLOAD_TIME, System.currentTimeMillis()).apply();
        }
    }

    private boolean uploadLogs() {
        final String uuid = HotMobiLogger.getInstallationSerialId(context);
        final File logsDir = HotMobiLogger.getLogsDir(context);
        boolean hasErrors = false;
        final String todayDir = HotMobiLogger.DATE_FORMAT.format(new Date());
        final FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return !filename.equalsIgnoreCase(todayDir);
            }
        };
        File[] dayLogsDirs = logsDir.listFiles(filter);
        if (dayLogsDirs == null) return false;
        for (File dayLogsDir : dayLogsDirs) {
            File[] logFiles = dayLogsDir.listFiles();
            if (logFiles == null) continue;
            boolean succeeded = true;
            for (File logFile : logFiles) {
                OutputStream os = null;
                InputStream is = null;
                try {
                    final UploadLogEvent uploadLogEvent = UploadLogEvent.create(context, logFile);
                    HttpURLConnection conn = (HttpURLConnection) new URL("http://www.dnext.xyz/usage/upload").openConnection();
                    conn.setRequestProperty("X-HotMobi-UUID", uuid);
                    conn.setRequestProperty("X-HotMobi-Date", dayLogsDir.getName());
                    conn.setRequestProperty("X-HotMobi-FileName", logFile.getName());
                    conn.setRequestProperty("User-Agent", String.format(Locale.ROOT,
                            "HotMobi (Twidere %s %d)", BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE));
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    os = conn.getOutputStream();
                    is = new FileInputStream(logFile);
                    StreamUtils.copy(is, os, null, null);
                    final int responseCode = conn.getResponseCode();
                    if (responseCode >= 200 && responseCode < 300) {
                        uploadLogEvent.finish(conn);
                        if (!uploadLogEvent.shouldSkip()) {
                            HotMobiLogger.getInstance(context).log(uploadLogEvent);
                        }
                        succeeded &= logFile.delete();
                    }
                } catch (IOException e) {
                    Log.w(HotMobiLogger.LOGTAG, e);
                    succeeded = false;
                    hasErrors = true;
                } finally {
                    Utils.closeSilently(is);
                    Utils.closeSilently(os);
                }
            }
            if (succeeded) {
                //noinspection ResultOfMethodCallIgnored
                dayLogsDir.delete();
            }
        }
        return hasErrors;
    }
}

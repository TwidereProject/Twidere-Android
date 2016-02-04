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
import android.util.Log;


import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.util.BugReporter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import edu.tsinghua.hotmobi.model.UploadLogEvent;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Upload logs to target server
 * Created by mariotaku on 15/8/27.
 */
public class UploadLogsTask implements Runnable {

    private final Context context;
    private final OkHttpClient client;

    public UploadLogsTask(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
    }

    @Override
    public void run() {

        final SharedPreferences prefs = context.getSharedPreferences("spice_data_profiling", Context.MODE_PRIVATE);

        if (prefs.contains(HotMobiConstants.KEY_LAST_UPLOAD_TIME)) {
            final long lastUpload = prefs.getLong(HotMobiConstants.KEY_LAST_UPLOAD_TIME, System.currentTimeMillis());
            final double deltaDays = (System.currentTimeMillis() - lastUpload) /
                    (double) HotMobiLogger.UPLOAD_INTERVAL_MILLIS;
            if (deltaDays < 1) {
                HotMobiLogger.log("Last uploaded was conducted in 1 day ago.");
                return;
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
        for (Object dayLogsDirObj : ArrayUtils.nullToEmpty(logsDir.listFiles(filter))) {
            final File dayLogsDir = (File) dayLogsDirObj;
            boolean succeeded = true;
            for (Object logFileObj : ArrayUtils.nullToEmpty(dayLogsDir.listFiles())) {
                File logFile = (File) logFileObj;
                try {
                    final Request.Builder builder = new Request.Builder();
                    builder.url("http://www.dnext.xyz/usage/upload");
                    builder.header("X-HotMobi-UUID", uuid);
                    builder.header("X-HotMobi-Date", dayLogsDir.getName());
                    builder.header("X-HotMobi-FileName", logFile.getName());
                    builder.header("User-Agent", String.format(Locale.ROOT,
                            "HotMobi (Twidere %s %d)", BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE));
                    builder.method("POST", RequestBody.create(MediaType.parse("text/plain"), logFile));
                    final UploadLogEvent uploadLogEvent = UploadLogEvent.create(context, logFile);
                    final Response response = client.newCall(builder.build()).execute();
                    if (response.isSuccessful()) {
                        uploadLogEvent.finish(response);
                        if (!uploadLogEvent.shouldSkip()) {
                            HotMobiLogger.getInstance(context).log(uploadLogEvent);
                        }
                        succeeded &= logFile.delete();
                    }
                } catch (IOException e) {
                    Log.w(HotMobiLogger.LOGTAG, e);
                    succeeded = false;
                    hasErrors = true;
                }
            }
            if (succeeded) {
                BugReporter.logIfFalse(dayLogsDir.delete(), "Unable to delete log dir");
            }
        }
        return hasErrors;
    }
}

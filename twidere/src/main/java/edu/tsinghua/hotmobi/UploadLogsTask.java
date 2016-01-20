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
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.MultiValueMap;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.http.mime.FileBody;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.util.BugReporter;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import edu.tsinghua.hotmobi.model.UploadLogEvent;

/**
 * Upload logs to target server
 * Created by mariotaku on 15/8/27.
 */
public class UploadLogsTask implements Runnable {

    private final Context context;
    private final RestHttpClient client;

    public UploadLogsTask(Context context) {
        this.context = context;
        this.client = TwitterAPIFactory.getDefaultHttpClient(context);
    }

    @Override
    public void run() {

        final SharedPreferences prefs = context.getSharedPreferences("spice_data_profiling", Context.MODE_PRIVATE);

        if (prefs.contains(HotMobiLogger.LAST_UPLOAD_TIME)) {
            final long lastUpload = prefs.getLong(HotMobiLogger.LAST_UPLOAD_TIME, System.currentTimeMillis());
            final double deltaDays = (System.currentTimeMillis() - lastUpload) /
                    (double) HotMobiLogger.UPLOAD_INTERVAL_MILLIS;
            if (deltaDays < 1) {
                HotMobiLogger.log("Last uploaded was conducted in 1 day ago.");
                return;
            }
        }

        if (uploadLogs()) {
            prefs.edit().putLong(HotMobiLogger.LAST_UPLOAD_TIME, System.currentTimeMillis()).apply();
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
                FileBody body = null;
                HttpResponse response = null;
                try {
                    final HttpRequest.Builder builder = new HttpRequest.Builder();
                    builder.method(POST.METHOD);
                    builder.url("http://www.dnext.xyz/usage/upload");
                    final MultiValueMap<String> headers = new MultiValueMap<>();
                    headers.add("X-HotMobi-UUID", uuid);
                    headers.add("X-HotMobi-Date", dayLogsDir.getName());
                    headers.add("X-HotMobi-FileName", logFile.getName());
                    headers.add("User-Agent", String.format(Locale.ROOT,
                            "HotMobi (Twidere %s %d)", BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE));
                    builder.headers(headers);
                    body = new FileBody(logFile);
                    builder.body(body);
                    final UploadLogEvent uploadLogEvent = UploadLogEvent.create(context, logFile);
                    response = client.newCall(builder.build()).execute();
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
                } finally {
                    Utils.closeSilently(body);
                    Utils.closeSilently(response);
                }
            }
            if (succeeded) {
                BugReporter.logIfFalse(dayLogsDir.delete(), "Unable to delete log dir");
            }
        }
        return hasErrors;
    }
}

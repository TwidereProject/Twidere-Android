/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.util;

import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Scanner;
import java.util.zip.CRC32;

/**
 * Created by mariotaku on 15/3/28.
 */
public class BugReporter implements Constants {
    public static void init(TwidereApplication application) {
        ACRA.init(application);
        final ErrorReporter reporter = ACRA.getErrorReporter();
        reporter.setReportSender(new GitHubIssueReportSender());
    }

    public static class GitHubIssueReportSender implements ReportSender {

        private static final String AUTH_TOKEN = "be96f826b8d1947e3a988cace182b57bf8b2cd00";
        private static final String USER_REPO = "mariotaku-bugreport/Twidere-Android.bugreport.test";

        @Override
        public void send(CrashReportData report) throws ReportSenderException {
            final String stackTrace = report.getProperty(ReportField.STACK_TRACE);
            final StringBuilder bodyBuilder = new StringBuilder();
            buildIssueBody(report, bodyBuilder);
            final Scanner scanner = new Scanner(stackTrace);
            final String messageLine = scanner.nextLine();
            final String recentLine = scanner.nextLine();
            final String titleContent = messageLine + " " + recentLine;
            scanner.close();
            final StringBuilder titleBuilder = new StringBuilder();
            final CRC32 crc32 = new CRC32();
            crc32.update(titleContent.getBytes(Charset.defaultCharset()));
            final String checksum = Long.toHexString(crc32.getValue());
            titleBuilder.append(checksum);
            titleBuilder.append(" ");
            titleBuilder.append(titleContent);
            final String query = String.format(Locale.ROOT, "%s repo:%s", checksum, USER_REPO);
            final Uri.Builder searchIssueUrlBuilder = Uri.parse("https://api.github.com/search/issues").buildUpon();
            searchIssueUrlBuilder.appendQueryParameter("q", query);
            HttpURLConnection searchIssueConnection = null;
            int searchIssueConnectionStatus = -1;
            try {
                searchIssueConnection = (HttpURLConnection) new URL(searchIssueUrlBuilder.build().toString()).openConnection();
                searchIssueConnection.setRequestMethod("GET");
                authorizeRequest(searchIssueConnection);
                searchIssueConnection.setDoInput(true);
                searchIssueConnectionStatus = searchIssueConnection.getResponseCode();
                final InputStreamReader reader = new InputStreamReader(searchIssueConnection.getInputStream());
                final JsonReader jsonReader = new JsonReader(reader);
                boolean isDuplicate = false;
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    if ("total_count".equals(jsonReader.nextName())) {
                        isDuplicate = jsonReader.nextInt() > 0;
                    } else {
                        jsonReader.skipValue();
                    }
                }
                jsonReader.endObject();
                if (isDuplicate) {
                    Log.d(LOGTAG, "Issue already exists");
                    return;
                }
            } catch (IOException e) {
                final String msg = "Network error when searching issues, code " + searchIssueConnectionStatus;
                Log.w(LOGTAG, msg, e);
                throw new ReportSenderException(msg, e);
            } finally {
                if (searchIssueConnection != null) {
                    searchIssueConnection.disconnect();
                }
            }

            createIssue(bodyBuilder.toString(), titleBuilder.toString());
        }

        private void createIssue(String body, String title) throws ReportSenderException {
            HttpURLConnection createIssueConnection;
            try {
                createIssueConnection = (HttpURLConnection) new URL(String.format(Locale.ROOT, "https://api.github.com/repos/%s/issues", USER_REPO)).openConnection();
                createIssueConnection.setRequestMethod("POST");
                authorizeRequest(createIssueConnection);
                createIssueConnection.setRequestProperty("Content-Type", "application/json");
                createIssueConnection.setDoOutput(true);
                final JsonWriter writer = new JsonWriter(new OutputStreamWriter(createIssueConnection.getOutputStream()));
                writer.beginObject();
                writer.name("title").value(title);
                writer.name("body").value(body);
                writer.endObject();
                writer.flush();
                createIssueConnection.connect();
                final String location = createIssueConnection.getHeaderField("Location");
                if (!TextUtils.isEmpty(location)) {
                    Log.d(LOGTAG, "Issue created at " + location);
                }
            } catch (IOException e) {
                final String msg = "Network error when sending error report";
                Log.w(LOGTAG, msg, e);
                throw new ReportSenderException(msg, e);
            }
        }

        private void buildIssueBody(CrashReportData report, StringBuilder bodyBuilder) {
            bodyBuilder.append("**Application Version**: ");
            bodyBuilder.append(report.getProperty(ReportField.APP_VERSION_NAME));
            bodyBuilder.append(" (");
            bodyBuilder.append(report.getProperty(ReportField.APP_VERSION_CODE));
            bodyBuilder.append(")\n\n");
            bodyBuilder.append("**Device Information**: ");
            bodyBuilder.append(Build.MODEL);
            bodyBuilder.append(" (");
            bodyBuilder.append(Build.PRODUCT);
            bodyBuilder.append(", ");
            bodyBuilder.append(Build.BRAND);
            bodyBuilder.append(")");
            bodyBuilder.append(", ");
            bodyBuilder.append("Android ");
            bodyBuilder.append(VERSION.RELEASE);
            bodyBuilder.append(" (");
            bodyBuilder.append(VERSION.SDK_INT);
            bodyBuilder.append(")\n\n");
            bodyBuilder.append("**User comment**: \n");
            bodyBuilder.append("````\n");
            bodyBuilder.append(report.getProperty(ReportField.USER_COMMENT));
            bodyBuilder.append("\n````\n");
            bodyBuilder.append("**Stack trace**: \n");
            bodyBuilder.append("````\n");
            bodyBuilder.append(report.getProperty(ReportField.STACK_TRACE));
            bodyBuilder.append("\n````");
        }

        private void authorizeRequest(HttpURLConnection builder) {
            builder.setRequestProperty("Authorization", String.format(Locale.ROOT, "token %s", AUTH_TOKEN));
            builder.setRequestProperty("Accept", "application/vnd.github.v3+json");
            builder.setRequestProperty("User-Agent", "Twidere (" + BuildConfig.VERSION_NAME + ")");
        }
    }
}

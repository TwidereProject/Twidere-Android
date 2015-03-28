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
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;

import java.io.IOException;
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

        private static final String AUTH_TOKEN = "208aacee0f51338a85ba5e9e3da54859981456ca";
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
            final JSONObject json = new JSONObject();
            try {
                json.put("title", titleBuilder.toString());
                json.put("body", bodyBuilder.toString());
            } catch (JSONException e) {
                throw new ReportSenderException("Error processing report json", e);
            }
            final OkHttpClient client = new OkHttpClient();
            final Request.Builder searchIssueBuilder = new Request.Builder();
            final String query = String.format(Locale.ROOT, "%s repo:%s", checksum, USER_REPO);
            final Uri.Builder searchIssueUrlBuilder = Uri.parse("https://api.github.com/search/issues").buildUpon();
            searchIssueUrlBuilder.appendQueryParameter("q", query);
            searchIssueBuilder.url(searchIssueUrlBuilder.build().toString());
            authorizeRequest(searchIssueBuilder);
            try {
                final Response response = client.newCall(searchIssueBuilder.build()).execute();
                final JsonReader jsonReader = new JsonReader(response.body().charStream());
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
                final String msg = "Network error when searching issues";
                Log.w(LOGTAG, msg, e);
                throw new ReportSenderException(msg, e);
            }
            final RequestBody issueBody = RequestBody.create(MediaType.parse("application/json"), json.toString());
            final Request.Builder createIssueBuilder = new Request.Builder();
            createIssueBuilder.url(String.format(Locale.ROOT, "https://api.github.com/repos/%s/issues", USER_REPO));
            createIssueBuilder.post(issueBody);
            authorizeRequest(createIssueBuilder);
            try {
                final Response response = client.newCall(createIssueBuilder.build()).execute();
                final String location = response.header("Location");
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

        private void authorizeRequest(Builder builder) {
            builder.header("Authorization", "token " + AUTH_TOKEN);
            builder.header("Accept", "application/vnd.github.v3+json");
            builder.header("User-Agent", "Twidere (" + BuildConfig.VERSION_NAME + ")");
        }
    }
}

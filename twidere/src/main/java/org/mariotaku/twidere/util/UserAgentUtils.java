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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.lang.reflect.Constructor;

/**
 * Created by mariotaku on 15/4/12.
 */
public class UserAgentUtils {
    private UserAgentUtils() {
    }

    // You may uncomment next line if using Android Annotations library, otherwise just be sure to run it in on the UI thread
    @UiThread
    public static String getDefaultUserAgentString(Context context) {
        if (Looper.myLooper() != Looper.getMainLooper())
            throw new IllegalStateException("User-Agent cannot be fetched from worker thread");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return NewApiWrapper.getDefaultUserAgent(context);
            }
            final Constructor<WebSettings> constructor = WebSettings.class.getDeclaredConstructor(Context.class, WebView.class);
            constructor.setAccessible(true);
            try {
                WebSettings settings = constructor.newInstance(context, null);
                return settings.getUserAgentString();
            } finally {
                constructor.setAccessible(false);
            }
        } catch (Exception e) {
            WebView webView = null;
            try {
                webView = new WebView(context);
                return webView.getSettings().getUserAgentString();
            } catch (Exception e2) {
                return System.getProperty("http.agent");
            } finally {
                if (webView != null) {
                    webView.destroy();
                }
            }
        }
    }

    @WorkerThread
    @Nullable
    public static String getDefaultUserAgentStringSafe(Context context) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            //noinspection ResourceType
            return getDefaultUserAgentString(context);
        }
        final Handler handler = new Handler(Looper.getMainLooper());
        try {
            final FetchUserAgentRunnable runnable = new FetchUserAgentRunnable(context);
            handler.post(runnable);
            runnable.waitForExecution();
            return runnable.getUserAgent();
        } finally {
            handler.removeCallbacksAndMessages(null);
        }
    }

    private static class FetchUserAgentRunnable implements Runnable {

        private final Context context;
        private String userAgent;
        private boolean userAgentSet;

        public FetchUserAgentRunnable(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            userAgent = getDefaultUserAgentString(context);
            userAgentSet = true;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void waitForExecution() {
            //noinspection StatementWithEmptyBody
            while (!userAgentSet) ;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static class NewApiWrapper {
        private NewApiWrapper() {
        }

        @UiThread
        static String getDefaultUserAgent(Context context) {
            return WebSettings.getDefaultUserAgent(context);
        }
    }
}

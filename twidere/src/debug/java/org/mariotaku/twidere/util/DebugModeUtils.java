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

package org.mariotaku.twidere.util;

import android.app.Application;
import android.os.Build;
import android.webkit.WebView;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.mariotaku.stethoext.bsh.BshRuntimeReplFactoryBuilder;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.util.net.NoIntercept;
import org.mariotaku.twidere.util.stetho.AccountsDumperPlugin;
import org.mariotaku.twidere.util.stetho.UserStreamDumperPlugin;

import okhttp3.OkHttpClient;

/**
 * Created by mariotaku on 15/5/27.
 */
public class DebugModeUtils {

    private static RefWatcher sRefWatcher;

    private DebugModeUtils() {
    }

    public static void initForOkHttpClient(final OkHttpClient.Builder builder) {
        final StethoInterceptor interceptor = new StethoInterceptor();

        builder.addNetworkInterceptor(chain -> {
            if (chain.request().tag() == NoIntercept.INSTANCE) {
                return chain.proceed(chain.request());
            }
            return interceptor.intercept(chain);
        });
    }

    public static void initForApplication(final Application application) {
        Stetho.initialize(Stetho.newInitializerBuilder(application)
                .enableDumpapp(() -> new Stetho.DefaultDumperPluginsBuilder(application)
                        .provide(new AccountsDumperPlugin(application))
                        .provide(new UserStreamDumperPlugin(application))
                        .finish())
                .enableWebKitInspector(() -> new Stetho.DefaultInspectorModulesBuilder(application)
                        .runtimeRepl(new BshRuntimeReplFactoryBuilder(application).build())
                        .finish())
                .build());
        initLeakCanary(application);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    static void initLeakCanary(Application application) {
        if (!BuildConfig.LEAK_CANARY_ENABLED) return;
        LeakCanary.enableDisplayLeakActivity(application);
        if (LeakCanary.isInAnalyzerProcess(application)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        sRefWatcher = LeakCanary.install(application);
    }

    public static void watchReferenceLeak(final Object object) {
        if (sRefWatcher == null) return;
        sRefWatcher.watch(object);
    }
}

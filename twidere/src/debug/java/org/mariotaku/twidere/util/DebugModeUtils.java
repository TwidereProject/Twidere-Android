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

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.squareup.leakcanary.AndroidExcludedRefs;
import com.squareup.leakcanary.DisplayLeakService;
import com.squareup.leakcanary.ExcludedRefs;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.squareup.leakcanary.ServiceHeapDumpListener;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.activity.ComposeActivity;
import org.mariotaku.twidere.util.net.NoIntercept;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by mariotaku on 15/5/27.
 */
public class DebugModeUtils {

    private static RefWatcher sRefWatcher;

    private DebugModeUtils() {
    }

    public static void initForOkHttpClient(final OkHttpClient.Builder builder) {
        final StethoInterceptor interceptor = new StethoInterceptor();

        builder.addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                if (chain.request().tag() == NoIntercept.INSTANCE) {
                    return chain.proceed(chain.request());
                }
                return interceptor.intercept(chain);
            }
        });
    }

    public static void initForApplication(final Application application) {
        Stetho.initialize(Stetho.newInitializerBuilder(application)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(application))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(application))
                .build());
        initLeakCanary(application);
    }

    static void initLeakCanary(Application application) {
        if (!BuildConfig.LEAK_CANARY_ENABLED) return;
        ExcludedRefs.Builder excludedRefsBuilder = AndroidExcludedRefs.createAppDefaults();
        LeakCanary.enableDisplayLeakActivity(application);
        ServiceHeapDumpListener heapDumpListener = new ServiceHeapDumpListener(application, DisplayLeakService.class);
        final RefWatcher refWatcher = LeakCanary.androidWatcher(application, heapDumpListener, excludedRefsBuilder.build());
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                // Ignore memory leak caused by LocationManager
                if (activity.getClass() == ComposeActivity.class) return;
                refWatcher.watch(activity);
            }
        });
        sRefWatcher = refWatcher;
    }

    public static void watchReferenceLeak(final Object object) {
        if (sRefWatcher == null) return;
        sRefWatcher.watch(object);
    }
}

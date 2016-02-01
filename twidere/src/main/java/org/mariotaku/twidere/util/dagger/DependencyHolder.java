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

package org.mariotaku.twidere.util.dagger;

import android.content.Context;

import com.squareup.okhttp.Dns;

import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.twidere.util.ActivityTracker;
import org.mariotaku.twidere.util.ExternalThemeManager;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.TwidereValidator;

import javax.inject.Inject;

import edu.tsinghua.hotmobi.HotMobiLogger;

/**
 * Created by mariotaku on 15/12/31.
 */
public class DependencyHolder {

    private static DependencyHolder sInstance;
    @Inject
    HotMobiLogger mHotMobiLogger;
    @Inject
    ReadStateManager mReadStateManager;
    @Inject
    RestHttpClient mRestHttpClient;
    @Inject
    ExternalThemeManager mExternalThemeManager;
    @Inject
    ActivityTracker mActivityTracker;
    @Inject
    Dns mDns;
    @Inject
    TwidereValidator mValidator;

    DependencyHolder(Context context) {
        GeneralComponentHelper.build(context).inject(this);
    }

    public static DependencyHolder get(Context context) {
        if (sInstance != null) return sInstance;
        return sInstance = new DependencyHolder(context);
    }

    public HotMobiLogger getHotMobiLogger() {
        return mHotMobiLogger;
    }

    public ReadStateManager getReadStateManager() {
        return mReadStateManager;
    }

    public RestHttpClient getRestHttpClient() {
        return mRestHttpClient;
    }

    public ExternalThemeManager getExternalThemeManager() {
        return mExternalThemeManager;
    }

    public ActivityTracker getActivityTracker() {
        return mActivityTracker;
    }

    public Dns getDns() {
        return mDns;
    }

    public TwidereValidator getValidator() {
        return mValidator;
    }
}

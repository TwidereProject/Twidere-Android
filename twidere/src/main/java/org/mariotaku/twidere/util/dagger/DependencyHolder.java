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

import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.twidere.util.ActivityTracker;
import org.mariotaku.twidere.util.ExternalThemeManager;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwidereValidator;
import org.mariotaku.twidere.util.net.TwidereDns;

import javax.inject.Inject;

import okhttp3.ConnectionPool;

/**
 * Created by mariotaku on 15/12/31.
 */
public class DependencyHolder {

    private static DependencyHolder sInstance;
    @Inject
    ReadStateManager mReadStateManager;
    @Inject
    RestHttpClient mRestHttpClient;
    @Inject
    ExternalThemeManager mExternalThemeManager;
    @Inject
    ActivityTracker mActivityTracker;
    @Inject
    TwidereDns mDns;
    @Inject
    TwidereValidator mValidator;
    @Inject
    SharedPreferencesWrapper mPreferences;
    @Inject
    ConnectionPool mConnectionPoll;

    DependencyHolder(Context context) {
        GeneralComponentHelper.build(context).inject(this);
    }

    public static DependencyHolder get(Context context) {
        if (sInstance != null) return sInstance;
        return sInstance = new DependencyHolder(context);
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

    public TwidereDns getDns() {
        return mDns;
    }

    public TwidereValidator getValidator() {
        return mValidator;
    }

    public SharedPreferencesWrapper getPreferences() {
        return mPreferences;
    }

    public ConnectionPool getConnectionPoll() {
        return mConnectionPoll;
    }
}

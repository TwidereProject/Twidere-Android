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
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.Utils;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import edu.tsinghua.hotmobi.model.BaseEvent;
import edu.tsinghua.hotmobi.model.LatLng;
import edu.tsinghua.hotmobi.model.MediaEvent;
import edu.tsinghua.hotmobi.model.RefreshEvent;
import edu.tsinghua.hotmobi.model.SessionEvent;
import edu.tsinghua.hotmobi.model.TweetEvent;

/**
 * Created by mariotaku on 15/8/10.
 */
public class HotMobiLogger {

    public static final long ACCOUNT_ID_NOT_NEEDED = -1;

    private static final String LOGTAG = "HotMobiLogger";

    private final Executor mExecutor;

    public HotMobiLogger() {
        mExecutor = Executors.newSingleThreadExecutor();
    }

    private static String getLogFilename(BaseEvent event) {
        if (event instanceof RefreshEvent) {
            return "refresh";
        } else if (event instanceof SessionEvent) {
            return "session";
        } else if (event instanceof TweetEvent) {
            return "tweet";
        } else if (event instanceof MediaEvent) {
            return "media";
        }
        return null;
    }

    public static String getInstallationSerialId(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        final String persistedDeviceId = prefs.getString(Constants.KEY_DEVICE_SERIAL, null);
        final String uuid;
        if (!TextUtils.isEmpty(persistedDeviceId)) {
            uuid = persistedDeviceId.replaceAll("[^\\w\\d]", "");
        } else {
            uuid = UUID.randomUUID().toString().replaceAll("[^\\w\\d]", "");
            prefs.edit().putString(Constants.KEY_DEVICE_SERIAL, uuid).apply();
        }
        return uuid;
    }

    public static HotMobiLogger getInstance(Context context) {
        return ((TwidereApplication) context.getApplicationContext()).getHotMobiLogger();
    }

    public static LatLng getCachedLatLng(Context context) {
        final Location location = Utils.getCachedLocation(context);
        if (location == null) return null;
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public void log(long accountId, final Object event) {

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(LOGTAG, LoganSquare.serialize(event));
                } catch (IOException e) {
                    Log.w(LOGTAG, e);
                }
            }
        });
    }

    public void log(Object event) {
        log(ACCOUNT_ID_NOT_NEEDED, event);
    }
}

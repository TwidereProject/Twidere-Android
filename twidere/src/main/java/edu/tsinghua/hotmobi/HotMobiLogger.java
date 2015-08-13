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
import android.location.Location;

import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.Utils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import edu.tsinghua.hotmobi.model.BaseEvent;
import edu.tsinghua.hotmobi.model.FirstLaunchEvent;
import edu.tsinghua.hotmobi.model.LatLng;
import edu.tsinghua.hotmobi.model.RefreshEvent;
import edu.tsinghua.hotmobi.model.SessionEvent;
import edu.tsinghua.hotmobi.model.TweetEvent;
import edu.tsinghua.hotmobi.model.TweetType;

/**
 * Created by mariotaku on 15/8/10.
 */
public class HotMobiLogger {

    private final Executor mExecutor;

    public HotMobiLogger() {
        mExecutor = Executors.newSingleThreadExecutor();
    }

    public void log(BaseEvent event) {

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private static String getLogFilename(BaseEvent event) {
        if (event instanceof FirstLaunchEvent) {
            return "first_launch";
        } else if (event instanceof RefreshEvent) {
            return "refresh";
        } else if (event instanceof SessionEvent) {
            return "session";
        } else if (event instanceof TweetEvent) {
            return "tweet";
        }
        return null;
    }

    public static int getTweetType(ParcelableStatus status) {
        return TweetType.TEXT;
    }

    public static HotMobiLogger getInstance(Context context) {
        return ((TwidereApplication) context.getApplicationContext()).getHotMobiLogger();
    }

    public static LatLng getCachedLatLng(Context context) {
        final Location location = Utils.getCachedLocation(context);
        if (location == null) return null;
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
}

/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.v4.net.ConnectivityManagerCompat;
import android.util.Log;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.Utils;

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.UploadLogsService;
import edu.tsinghua.hotmobi.model.NetworkEvent;

public class ConnectivityStateReceiver extends BroadcastReceiver implements Constants {

    private static final String RECEIVER_LOGTAG = LOGTAG + "." + "Connectivity";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (BuildConfig.DEBUG) {
            Log.d(RECEIVER_LOGTAG, String.format("Received Broadcast %s", intent));
        }
        if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) return;
        final TwidereApplication application = TwidereApplication.Companion.getInstance(context);
//        application.reloadConnectivitySettings();
        Utils.startRefreshServiceIfNeeded(application);
        final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_USAGE_STATISTICS, false) && prefs.getBoolean(KEY_SETTINGS_WIZARD_COMPLETED, false)) {
            // BEGIN HotMobi
            final NetworkEvent event = NetworkEvent.create(context);
            HotMobiLogger.getInstance(context).log(event);
            // END HotMobi
        }

        final Context appContext = context.getApplicationContext();
        final ConnectivityManager cm = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        final boolean isNetworkMetered = ConnectivityManagerCompat.isActiveNetworkMetered(cm);
        final boolean isCharging = Utils.isCharging(appContext);
        if (!isNetworkMetered && isCharging) {
            final long currentTime = System.currentTimeMillis();
            final long lastSuccessfulTime = HotMobiLogger.getLastUploadTime(appContext);
            if ((currentTime - lastSuccessfulTime) > HotMobiLogger.UPLOAD_INTERVAL_MILLIS) {
                appContext.startService(new Intent(appContext, UploadLogsService.class));
            }
        }

    }
}

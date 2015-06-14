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
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.util.Log;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;

import edu.tsinghua.spice.Task.SpiceAsyIPTask;
import edu.tsinghua.spice.Utilies.NetworkStateUtil;
import edu.tsinghua.spice.Utilies.SpiceProfilingUtil;

import static org.mariotaku.twidere.util.Utils.startRefreshServiceIfNeeded;
import static org.mariotaku.twidere.util.Utils.startUsageStatisticsServiceIfNeeded;

public class ConnectivityStateReceiver extends BroadcastReceiver implements Constants {

	private static final String RECEIVER_LOGTAG = LOGTAG + "." + "Connectivity";
    private LocationManager mLocationManager;

	@Override
	public void onReceive(final Context context, final Intent intent) {
        if (BuildConfig.DEBUG) {
			Log.d(RECEIVER_LOGTAG, String.format("Received Broadcast %s", intent));
		}
		if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) return;
		startUsageStatisticsServiceIfNeeded(context);
		startRefreshServiceIfNeeded(context);
        //spice
        SpiceProfilingUtil.profile(context,SpiceProfilingUtil.FILE_NAME_ONWIFI, NetworkStateUtil.getConnectedType(context));
        new SpiceAsyIPTask(context).execute();
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager == null) return;
        final String provider = LocationManager.NETWORK_PROVIDER;
        if (mLocationManager.isProviderEnabled(provider)) {
            final Location location = mLocationManager.getLastKnownLocation(provider);
            if (location != null) {
                SpiceProfilingUtil.profile(context, SpiceProfilingUtil.FILE_NAME_LOCATION, location.getTime() + ","
                        + location.getLatitude() + "," + location.getLongitude() + "," + location.getProvider());
            }
        }
	}
}

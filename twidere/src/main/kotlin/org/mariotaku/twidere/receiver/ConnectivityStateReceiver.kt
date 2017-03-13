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

package org.mariotaku.twidere.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v4.net.ConnectivityManagerCompat
import edu.tsinghua.hotmobi.HotMobiLogger
import edu.tsinghua.hotmobi.UploadLogsService
import edu.tsinghua.hotmobi.model.NetworkEvent
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.TwidereConstants.SHARED_PREFERENCES_NAME
import org.mariotaku.twidere.constant.usageStatisticsKey
import org.mariotaku.twidere.service.StreamingService
import org.mariotaku.twidere.util.DebugLog
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.dagger.DependencyHolder

class ConnectivityStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        DebugLog.d(RECEIVER_LOGTAG, String.format("Received Broadcast %s", intent), null)
        if (ConnectivityManager.CONNECTIVITY_ACTION != intent.action) return
        val prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (prefs[usageStatisticsKey]) {
            // BEGIN HotMobi
            val event = NetworkEvent.create(context)
            HotMobiLogger.getInstance(context).log(event)
            // END HotMobi
        }

        val appContext = context.applicationContext
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isNetworkMetered = ConnectivityManagerCompat.isActiveNetworkMetered(cm)
        val holder = DependencyHolder.get(context)
        holder.mediaPreloader.isNetworkMetered = isNetworkMetered
        val isCharging = Utils.isCharging(appContext)
        if (!isNetworkMetered && isCharging) {
            val currentTime = System.currentTimeMillis()
            val lastSuccessfulTime = HotMobiLogger.getLastUploadTime(appContext)
            if (currentTime - lastSuccessfulTime > HotMobiLogger.UPLOAD_INTERVAL_MILLIS) {
                appContext.startService(Intent(appContext, UploadLogsService::class.java))
            }
        }
        StreamingService.startOrStopService(context)
    }

    companion object {

        private const val RECEIVER_LOGTAG = "TwidereConnectivity"
    }
}

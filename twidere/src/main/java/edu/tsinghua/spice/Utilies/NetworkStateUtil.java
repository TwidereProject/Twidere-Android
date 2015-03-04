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

package edu.tsinghua.spice.Utilies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by Denny C. Ng on 2/28/15.
 */

public class NetworkStateUtil {

    private static int getConnectedTypeValue(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                return mNetworkInfo.getType();
            }
        }
        return -1;
    }

    public static String getConnectedType (Context context) {
        int type = -1;
        type = getConnectedTypeValue(context);
        String network = "unknown";
        switch (type) {
            case 0:
                network = "cellular";
                break;
            case 1:
                network = "wifi";
                break;
            case 2:
                network = "wimax";
                break;
            case 3:
                network = "ethernet";
                break;
            case 4:
                network = "bluetooth";
                break;
            case -1:
                network = "ERROR";
                break;
            default:
                network = "unknown";
                break;
        }
        return network;
    }
}

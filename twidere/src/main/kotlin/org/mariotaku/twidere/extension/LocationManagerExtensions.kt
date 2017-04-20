/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.extension

import android.location.Location
import android.location.LocationManager
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.util.DebugLog

/**
 * Created by mariotaku on 2017/4/20.
 */


fun LocationManager.getCachedLocation(): Location? {
    DebugLog.v(TwidereConstants.LOGTAG, "Fetching cached location", Exception())
    var location: Location? = null
    try {
        location = getLastKnownLocation(LocationManager.GPS_PROVIDER)
    } catch (ignore: SecurityException) {

    }

    if (location != null) return location
    try {
        location = getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    } catch (ignore: SecurityException) {

    }

    return location
}

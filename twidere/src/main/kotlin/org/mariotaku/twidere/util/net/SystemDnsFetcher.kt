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

package org.mariotaku.twidere.util.net

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import androidx.annotation.RequiresApi
import org.mariotaku.twidere.extension.activateNetworkCompat
import java.net.InetAddress

object SystemDnsFetcher {
    val impl = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        DnsFetcherBase()
    } else {
        DnsFetcherLollipop()
    }

    fun get(context: Context): List<String>? {
        return impl.get(context)
    }

    open class DnsFetcherBase {
        @SuppressLint("PrivateApi")
        open fun get(context: Context): List<String>? {
            try {
                val systemProperties = Class.forName("android.os.SystemProperties")
                val method = systemProperties.getMethod("get", String::class.java)
                val netdns = arrayOf("net.dns1", "net.dns2", "net.dns3", "net.dns4")
                return netdns.mapNotNull { key ->
                    return@mapNotNull method(null, key) as? String
                }
            } catch (e: Exception) {
                return null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    class DnsFetcherLollipop : DnsFetcherBase() {

        override fun get(context: Context): List<String>? {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activateNetworkCompat ?: return null
            return cm.getLinkProperties(activeNetwork)?.dnsServers?.map(InetAddress::getHostAddress)
        }
    }

}
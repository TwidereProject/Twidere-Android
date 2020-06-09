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

import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*

/**
 * Created by mariotaku on 15/11/23.
 */
class SystemHosts {

    @Throws(IOException::class)
    fun resolve(hostToResolve: String): List<InetAddress> {
        File(HOSTS_PATH).bufferedReader().use {
            var line: String
            while (true) {
                line = it.readLine() ?: break
                val scanner = Scanner(line)
                if (!scanner.hasNext()) continue
                val address = scanner.next()
                if (address.startsWith("#")) continue
                while (scanner.hasNext()) {
                    val host = scanner.next()
                    if (host.startsWith("#")) break
                    if (hostToResolve == host) {
                        val resolved = TwidereDns.getResolvedIPAddress(host, address) ?: continue
                        return listOf(resolved)
                    }
                }
            }
        }
        throw UnknownHostException(hostToResolve)
    }

    companion object {

        private const val HOSTS_PATH = "/system/etc/hosts"
    }

}

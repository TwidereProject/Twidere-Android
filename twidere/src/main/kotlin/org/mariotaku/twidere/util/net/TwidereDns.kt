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

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.util.TimingLogger
import okhttp3.Dns
import org.mariotaku.ktextension.toIntOr
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.TwidereConstants.HOST_MAPPING_PREFERENCES_NAME
import org.mariotaku.twidere.constant.SharedPreferenceConstants.*
import org.xbill.DNS.*
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*
import javax.inject.Singleton

@Singleton
class TwidereDns(val context: Context, private val preferences: SharedPreferences) : Dns {

    private val hostMapping = context.getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME,
            Context.MODE_PRIVATE)
    private val systemHosts = SystemHosts()

    private var resolver: Resolver? = null
    private var useResolver: Boolean = false

    init {
        reloadDnsSettings()
    }

    @Throws(UnknownHostException::class)
    override fun lookup(hostname: String): List<InetAddress> {
        try {
            return resolveInternal(hostname, hostname, 0, useResolver)
        } catch (e: IOException) {
            if (e is UnknownHostException) throw e
            throw UnknownHostException("Unable to resolve address " + e.message)
        } catch (e: SecurityException) {
            throw UnknownHostException("Security exception" + e.message)
        }

    }

    @Throws(UnknownHostException::class)
    fun lookupResolver(hostname: String): List<InetAddress> {
        try {
            return resolveInternal(hostname, hostname, 0, true)
        } catch (e: IOException) {
            if (e is UnknownHostException) throw e
            throw UnknownHostException("Unable to resolve address " + e.message)
        } catch (e: SecurityException) {
            throw UnknownHostException("Security exception" + e.message)
        }

    }

    fun reloadDnsSettings() {
        this.resolver = null
        useResolver = preferences.getBoolean(KEY_BUILTIN_DNS_RESOLVER, false)
    }

    fun putMapping(host: String, address: String) {
        beginMappingTransaction {
            this[host] = address
        }
    }

    fun beginMappingTransaction(action: MappingTransaction.() -> Unit) {
        hostMapping.edit().apply { action(MappingTransaction(this)) }.apply()
    }

    @Throws(IOException::class, SecurityException::class)
    private fun resolveInternal(originalHost: String, host: String, depth: Int,
            useResolver: Boolean): List<InetAddress> {
        val logger = TimingLogger(RESOLVER_LOGTAG, "resolve")
        // Return if host is an address
        val fromAddressString = fromAddressString(originalHost, host)
        if (fromAddressString != null) {
            addLogSplit(logger, host, "valid ip address", depth)
            dumpLog(logger, fromAddressString)
            return fromAddressString
        }
        // Load from custom mapping
        addLogSplit(logger, host, "start custom mapping resolve", depth)
        val fromMapping = getFromMapping(host)
        addLogSplit(logger, host, "end custom mapping resolve", depth)
        if (fromMapping != null) {
            dumpLog(logger, fromMapping)
            return fromMapping
        }
        if (useResolver) {
            // Load from /etc/hosts, since Dnsjava doesn't support hosts entry lookup
            addLogSplit(logger, host, "start /etc/hosts resolve", depth)
            val fromSystemHosts = fromSystemHosts(host)
            addLogSplit(logger, host, "end /etc/hosts resolve", depth)
            if (fromSystemHosts != null) {
                dumpLog(logger, fromSystemHosts)
                return fromSystemHosts
            }

            // Use DNS resolver
            addLogSplit(logger, host, "start resolver resolve", depth)
            val fromResolver = fromResolver(originalHost, host)
            addLogSplit(logger, host, "end resolver resolve", depth)
            if (fromResolver != null) {
                dumpLog(logger, fromResolver)
                return fromResolver
            }
        }
        addLogSplit(logger, host, "start system default resolve", depth)
        val fromDefault = Arrays.asList(*InetAddress.getAllByName(host))
        addLogSplit(logger, host, "end system default resolve", depth)
        dumpLog(logger, fromDefault)
        return fromDefault
    }

    private fun dumpLog(logger: TimingLogger, addresses: List<InetAddress>) {
        if (BuildConfig.DEBUG) return
        Log.v(RESOLVER_LOGTAG, "Resolved $addresses")
        logger.dumpToLog()
    }

    private fun addLogSplit(logger: TimingLogger, host: String, message: String, depth: Int) {
        if (BuildConfig.DEBUG) return
        val sb = StringBuilder()
        for (i in 0 until depth) {
            sb.append(">")
        }
        sb.append(" ")
        sb.append(host)
        sb.append(": ")
        sb.append(message)
        logger.addSplit(sb.toString())
    }

    private fun fromSystemHosts(host: String): List<InetAddress>? {
        return try {
            systemHosts.resolve(host)
        } catch (e: IOException) {
            null
        }

    }

    @Throws(IOException::class)
    private fun fromResolver(originalHost: String, host: String): List<InetAddress>? {
        val resolver = this.getResolver()
        val records = lookupHostName(resolver, host, true)
        val addrs = ArrayList<InetAddress>(records.size)
        for (record in records) {
            addrs.add(addrFromRecord(originalHost, record))
        }
        if (addrs.isEmpty()) return null
        return addrs
    }

    @Throws(UnknownHostException::class)
    private fun getFromMapping(host: String): List<InetAddress>? {
        return getFromMappingInternal(host, host, false)
    }

    @Throws(UnknownHostException::class)
    private fun getFromMappingInternal(host: String, origHost: String, checkRecursive: Boolean): List<InetAddress>? {
        if (checkRecursive && hostMatches(host, origHost)) {
            // Recursive resolution, stop this call
            return null
        }
        for ((key, value1) in hostMapping.all) {
            if (hostMatches(host, key)) {
                val value = value1 as String
                val resolved = getResolvedIPAddress(origHost, value) ?: // Maybe another hostname
                        return getFromMappingInternal(value, origHost, true)
                return listOf(resolved)
            }
        }
        return null
    }

    private fun getResolver(): Resolver {
        return this.resolver ?: run {
            val tcp = preferences.getBoolean(KEY_TCP_DNS_QUERY, false)
            val servers = preferences.getString(KEY_DNS_SERVER, null)?.split(';', ',', ' ') ?:
                    SystemDnsFetcher.get(context)
            val resolvers = servers?.mapNotNull {
                val segs = it.split("#", limit = 2)
                if (segs.isEmpty()) return@mapNotNull null
                if (!isValidIpAddress(segs[0])) return@mapNotNull null
                return@mapNotNull SimpleResolver(segs[0]).apply {
                    if (segs.size == 2) {
                        val port = segs[1].toIntOr(-1)
                        if (port in 0..65535) {
                            setPort(port)
                        }
                    }
                }
            }
            val resolver: Resolver
            resolver = if (resolvers != null && resolvers.isNotEmpty()) {
                ExtendedResolver(resolvers.toTypedArray())
            } else {
                SimpleResolver()
            }
            resolver.setTCP(tcp)
            this.resolver = resolver
            return@run resolver
        }
    }


    @Throws(UnknownHostException::class)
    private fun fromAddressString(host: String, address: String): List<InetAddress>? {
        val resolved = getResolvedIPAddress(host, address) ?: return null
        return listOf(resolved)
    }

    companion object {

        private const val RESOLVER_LOGTAG = "TwidereDns"


        private fun hostMatches(host: String?, rule: String?): Boolean {
            if (rule == null || host == null) return false
            if (rule.startsWith(".")) return host.endsWith(rule, ignoreCase = true)
            return host.equals(rule, ignoreCase = true)
        }

        @Throws(UnknownHostException::class)
        fun getResolvedIPAddress(host: String,
                address: String): InetAddress? {
            var bytes = Address.toByteArray(address, Address.IPv4)
            if (bytes != null)
                return InetAddress.getByAddress(host, bytes)
            bytes = Address.toByteArray(address, Address.IPv6)
            if (bytes != null)
                return InetAddress.getByAddress(host, bytes)
            return null
        }

        private fun getInetAddressType(address: String): Int {
            var bytes = Address.toByteArray(address, Address.IPv4)
            if (bytes != null)
                return Address.IPv4
            bytes = Address.toByteArray(address, Address.IPv6)
            if (bytes != null)
                return Address.IPv6
            return 0
        }

        fun isValidIpAddress(address: String): Boolean {
            return getInetAddressType(address) != 0
        }

        @Throws(UnknownHostException::class)
        private fun lookupHostName(resolver: Resolver, name: String, all: Boolean): Array<Record> {
            try {
                val lookup = newLookup(resolver, name, Type.A)
                val a = lookup.run()
                if (a == null) {
                    if (lookup.result == Lookup.TYPE_NOT_FOUND) {
//                        val aaaa = newLookup(resolver, name, Type.AAAA).run()
//                        if (aaaa != null) return aaaa
                    }
                    throw UnknownHostException("unknown host")
                }
                if (!all)
                    return a
//                val aaaa = newLookup(resolver, name, Type.AAAA).run() ?: return a
//                return a + aaaa
                return a
            } catch (e: TextParseException) {
                throw UnknownHostException("invalid name")
            }

        }

        @Throws(TextParseException::class)
        private fun newLookup(resolver: Resolver, name: String, type: Int): Lookup {
            val lookup = Lookup(name, type)
            lookup.setResolver(resolver)
            return lookup
        }

        @Throws(UnknownHostException::class)
        private fun addrFromRecord(name: String, r: Record): InetAddress {
            val addr: InetAddress = if (r is ARecord) {
                r.address
            } else {
                (r as AAAARecord).address
            }
            return InetAddress.getByAddress(name, addr.address)
        }
    }

    class MappingTransaction(private val editor: SharedPreferences.Editor) {
        operator fun set(host: String, address: String) {
            editor.putString(host, address)
        }

        fun remove(host: String) {
            editor.remove(host)
        }
    }


}

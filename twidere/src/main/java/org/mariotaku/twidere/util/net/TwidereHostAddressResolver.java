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

package org.mariotaku.twidere.util.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.HostsFileParser;
import org.mariotaku.twidere.util.Utils;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedHashMap;

import twitter4j.http.HostAddressResolver;

import static android.text.TextUtils.isEmpty;

public class TwidereHostAddressResolver implements Constants, HostAddressResolver {

    private static final String RESOLVER_LOGTAG = "TwidereHostAddressResolver";

    private static final String DEFAULT_DNS_SERVER_ADDRESS = "8.8.8.8";

    private final SharedPreferences mHostMapping, mPreferences;
    private final HostsFileParser mSystemHosts = new HostsFileParser();
    private final HostCache mHostCache = new HostCache(512);
    private final boolean mLocalMappingOnly;
    private final String mDnsAddress;

    private Resolver mDns;

    public TwidereHostAddressResolver(final Context context) {
        this(context, false);
    }

    public TwidereHostAddressResolver(final Context context, final boolean localOnly) {
        mHostMapping = context.getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final String address = mPreferences.getString(KEY_DNS_SERVER, DEFAULT_DNS_SERVER_ADDRESS);
        mDnsAddress = isValidIpAddress(address) ? address : DEFAULT_DNS_SERVER_ADDRESS;
        mLocalMappingOnly = localOnly;
    }

    public synchronized void removeCachedHost(final String host) {
        mHostCache.remove(host);
    }

    @Override
    public String resolve(final String host) throws IOException {
        if (host == null) return null;
        if (isValidIpAddress(host)) return null;
        // First, I'll try to load address cached.
        if (mHostCache.containsKey(host)) {
            if (Utils.isDebugBuild()) {
                Log.d(RESOLVER_LOGTAG, "Got cached address " + mHostCache.get(host) + " for host " + host);
            }
            return mHostCache.get(host);
        }
        // Then I'll try to load from custom host mapping.
        // Stupid way to find top domain, but really fast.
        if (mHostMapping.contains(host)) {
            final String mappedAddr = mHostMapping.getString(host, null);
            mHostCache.put(host, mappedAddr);
            if (Utils.isDebugBuild()) {
                Log.d(RESOLVER_LOGTAG, "Got mapped address " + mappedAddr + " for host " + host);
            }
            return mappedAddr;
        }
        mSystemHosts.reloadIfNeeded();
        if (mSystemHosts.contains(host)) {
            final String hostAddr = mSystemHosts.getAddress(host);
            mHostCache.put(host, hostAddr);
            if (Utils.isDebugBuild()) {
                Log.d(RESOLVER_LOGTAG, "Got mapped address " + hostAddr + " for host " + host);
            }
            return hostAddr;
        }
        final String customMappedHost = findHost(host);
        if (customMappedHost != null) {
            mHostCache.put(host, customMappedHost);
            if (Utils.isDebugBuild()) {
                Log.d(RESOLVER_LOGTAG, "Got mapped address " + customMappedHost + " for host " + host);
            }
            return customMappedHost;
        }
        initDns();
        // Use TCP DNS Query if enabled.
        if (mDns != null && mPreferences.getBoolean(KEY_TCP_DNS_QUERY, false)) {
            final Lookup lookup = new Lookup(new Name(host), Type.A, DClass.IN);
            final Record[] records;
            lookup.setResolver(mDns);
            lookup.run();
            final int result = lookup.getResult();
            if (result != Lookup.SUCCESSFUL) {
                throw new IOException("Could not find " + host);
            }
            records = lookup.getAnswers();
            String hostAddr = null;
            // Test each IP address resolved.
            for (final Record record : records) {
                if (record instanceof ARecord) {
                    final InetAddress ipv4Addr = ((ARecord) record).getAddress();
                    if (ipv4Addr.isReachable(300)) {
                        hostAddr = ipv4Addr.getHostAddress();
                    }
                } else if (record instanceof AAAARecord) {
                    final InetAddress ipv6Addr = ((AAAARecord) record).getAddress();
                    if (ipv6Addr.isReachable(300)) {
                        hostAddr = ipv6Addr.getHostAddress();
                    }
                }
                if (hostAddr != null) {
                    mHostCache.put(host, hostAddr);
                    if (Utils.isDebugBuild()) {
                        Log.d(RESOLVER_LOGTAG, "Resolved address " + hostAddr + " for host " + host);
                    }
                    return hostAddr;
                }
            }
            // No address is reachable, but I believe the IP is correct.
            final Record record = records[0];
            if (record instanceof ARecord) {
                final InetAddress ipv4Addr = ((ARecord) record).getAddress();
                hostAddr = ipv4Addr.getHostAddress();
            } else if (record instanceof AAAARecord) {
                final InetAddress ipv6Addr = ((AAAARecord) record).getAddress();
                hostAddr = ipv6Addr.getHostAddress();
            } else if (record instanceof CNAMERecord)
                return resolve(((CNAMERecord) record).getTarget().toString());
            mHostCache.put(host, hostAddr);
            if (Utils.isDebugBuild()) {
                Log.d(RESOLVER_LOGTAG, "Resolved address " + hostAddr + " for host " + host);
            }
            return hostAddr;
        }
        if (Utils.isDebugBuild()) {
            Log.w(RESOLVER_LOGTAG, "Resolve address " + host + " failed, using original host");
        }
        return host;
    }

    private String findHost(final String host) {
        for (final String rule : mHostMapping.getAll().keySet()) {
            if (hostMatches(host, rule)) return mHostMapping.getString(rule, null);
        }
        return null;
    }

    private void initDns() throws IOException {
        if (mDns != null) return;
        mDns = new SimpleResolver(mDnsAddress);
        mDns.setTCP(true);
    }

    private static boolean hostMatches(final String host, final String rule) {
        if (rule == null || host == null) return false;
        if (rule.startsWith(".")) return host.toLowerCase().endsWith(rule.toLowerCase());
        return host.equalsIgnoreCase(rule);
    }

    private static boolean isValidIpAddress(final String address) {
        if (isEmpty(address)) return false;
        return InetAddressUtils.isIPv4Address(address) || InetAddressUtils.isIPv6Address(address);
    }

    private static class HostCache extends LinkedHashMap<String, String> {

        private static final long serialVersionUID = -9216545511009449147L;

        HostCache(final int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public String put(final String key, final String value) {
            if (value == null) return null;
            return super.put(key, value);
        }
    }
}

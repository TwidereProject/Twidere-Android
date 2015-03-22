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
import android.support.annotation.NonNull;
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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import twitter4j.http.HostAddressResolver;

import static android.text.TextUtils.isEmpty;

public class TwidereHostAddressResolver implements Constants, HostAddressResolver {

    private static final String RESOLVER_LOGTAG = "Twidere.Host";

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

    @SuppressWarnings("unused")
    public synchronized void removeCachedHost(final String host) {
        mHostCache.remove(host);
    }

    @NonNull
    @Override
    public InetAddress[] resolve(@NonNull final String host) throws IOException {
        return resolveInternal(host, host);
    }

    private InetAddress[] resolveInternal(String originalHost, String host) throws IOException {
        if (isValidIpAddress(host)) return fromAddressString(originalHost, host);
        // First, I'll try to load address cached.
        if (mHostCache.containsKey(host)) {
            final InetAddress[] hostAddr = mHostCache.get(host);
            if (Utils.isDebugBuild()) {
                Log.d(RESOLVER_LOGTAG, "Got cached " + Arrays.toString(hostAddr));
            }
            return hostAddr;
        }
        // Then I'll try to load from custom host mapping.
        // Stupid way to find top domain, but really fast.
        if (mHostMapping.contains(host)) {
            final String mappedAddr = mHostMapping.getString(host, null);
            if (mappedAddr != null) {
                final InetAddress[] hostAddr = fromAddressString(originalHost, mappedAddr);
                mHostCache.put(originalHost, hostAddr);
                if (Utils.isDebugBuild()) {
                    Log.d(RESOLVER_LOGTAG, "Got mapped " + Arrays.toString(hostAddr));
                }
                return hostAddr;
            }
        }
        mSystemHosts.reloadIfNeeded();
        if (mSystemHosts.contains(host)) {
            final InetAddress[] hostAddr = fromAddressString(originalHost, mSystemHosts.getAddress(host));
            mHostCache.put(originalHost, hostAddr);
            if (Utils.isDebugBuild()) {
                Log.d(RESOLVER_LOGTAG, "Got hosts " + Arrays.toString(hostAddr));
            }
            return hostAddr;
        }
        final String customMappedHost = findHost(host);
        if (customMappedHost != null) {
            final InetAddress[] hostAddr = fromAddressString(originalHost, customMappedHost);
            mHostCache.put(originalHost, hostAddr);
            if (Utils.isDebugBuild()) {
                Log.d(RESOLVER_LOGTAG, "Got mapped address " + customMappedHost + " for host " + host);
            }
            return hostAddr;
        }
        // Use TCP DNS Query if enabled.
        final Resolver dns = getResolver();
        if (dns != null && mPreferences.getBoolean(KEY_TCP_DNS_QUERY, false)) {
            final Lookup lookup = new Lookup(new Name(host), Type.A, DClass.IN);
            final Record[] records;
            lookup.setResolver(dns);
            lookup.run();
            final int result = lookup.getResult();
            if (result != Lookup.SUCCESSFUL) {
                throw new UnknownHostException("Unable to resolve " + host + ", " + lookup.getErrorString());
            }
            records = lookup.getAnswers();
            final ArrayList<InetAddress> resolvedAddresses = new ArrayList<>();
            // Test each IP address resolved.
            for (final Record record : records) {
                if (record instanceof ARecord) {
                    final InetAddress ipv4Addr = ((ARecord) record).getAddress();
                    resolvedAddresses.add(InetAddress.getByAddress(originalHost, ipv4Addr.getAddress()));
                } else if (record instanceof AAAARecord) {
                    final InetAddress ipv6Addr = ((AAAARecord) record).getAddress();
                    resolvedAddresses.add(InetAddress.getByAddress(originalHost, ipv6Addr.getAddress()));
                }
            }
            if (!resolvedAddresses.isEmpty()) {
                final InetAddress[] hostAddr = resolvedAddresses.toArray(new InetAddress[resolvedAddresses.size()]);
                mHostCache.put(originalHost, hostAddr);
                if (Utils.isDebugBuild()) {
                    Log.d(RESOLVER_LOGTAG, "Resolved " + Arrays.toString(hostAddr));
                }
                return hostAddr;
            }
            // No address is reachable, but I believe the IP is correct.

            for (final Record record : records) {
                if (record instanceof CNAMERecord)
                    return resolveInternal(originalHost, ((CNAMERecord) record).getTarget().toString());
            }
        }
        if (Utils.isDebugBuild()) {
            Log.w(RESOLVER_LOGTAG, "Resolve address " + host + " failed, using original host");
        }
        final InetAddress[] defaultAddresses = InetAddress.getAllByName(host);
        mHostCache.put(host, defaultAddresses);
        return defaultAddresses;
    }

    private InetAddress[] fromAddressString(String host, String address) throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getByName(address);
        if (inetAddress instanceof Inet4Address) {
            return new InetAddress[]{Inet4Address.getByAddress(host, inetAddress.getAddress())};
        } else if (inetAddress instanceof Inet6Address) {
            return new InetAddress[]{Inet6Address.getByAddress(host, inetAddress.getAddress())};
        }
        throw new UnknownHostException("Bad address " + host + " = " + address);
    }

    private String findHost(final String host) {
        for (final Entry<String, ?> entry : mHostMapping.getAll().entrySet()) {
            if (hostMatches(host, entry.getKey())) return (String) entry.getValue();
        }
        return null;
    }

    private Resolver getResolver() throws IOException {
        if (mDns != null) return mDns;
        mDns = new SimpleResolver(mDnsAddress);
        mDns.setTCP(true);
        return mDns;
    }

    private static boolean hostMatches(final String host, final String rule) {
        if (rule == null || host == null) return false;
        if (rule.startsWith(".")) return host.matches("(?i).*" + Pattern.quote(rule));
        return host.equalsIgnoreCase(rule);
    }

    private static boolean isValidIpAddress(final String address) {
        if (isEmpty(address)) return false;
        return InetAddressUtils.isIPv4Address(address) || InetAddressUtils.isIPv6Address(address);
    }

    private static class HostCache extends LinkedHashMap<String, InetAddress[]> {

        private static final long serialVersionUID = -9216545511009449147L;

        HostCache(final int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public InetAddress[] put(final String key, final InetAddress... value) {
            if (value == null) return null;
            return super.put(key, value);
        }
    }
}

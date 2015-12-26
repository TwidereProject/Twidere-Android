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
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.LruCache;
import android.util.TimingLogger;

import com.squareup.okhttp.Dns;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

public class TwidereDns implements Constants, Dns {

    private static final String RESOLVER_LOGTAG = "TwidereDns";

    private static final String DEFAULT_DNS_SERVER_ADDRESS = "8.8.8.8";

    private final SharedPreferences mHostMapping, mPreferences;
    private final LruCache<String, InetAddress[]> mHostCache = new LruCache<>(512);
    private final String mDnsAddress;
    private final SystemHosts mSystemHosts;

    private Resolver mDns;
    private TimingLogger mLogger;

    public TwidereDns(final Context context) {
        mLogger = new TimingLogger(RESOLVER_LOGTAG, "resolve");
        mHostMapping = SharedPreferencesWrapper.getInstance(context, HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final String address = mPreferences.getString(KEY_DNS_SERVER, DEFAULT_DNS_SERVER_ADDRESS);
        mDnsAddress = isValidIpAddress(address) ? address : DEFAULT_DNS_SERVER_ADDRESS;
        mSystemHosts = new SystemHosts();
    }

    @SuppressWarnings("unused")
    public synchronized void removeCachedHost(final String host) {
        mHostCache.remove(host);
    }


    @NonNull
    private InetAddress[] resolveInternal(String originalHost, String host) throws IOException {
        mLogger.reset();
        // Return if host is an address
        final InetAddress[] fromAddressString = fromAddressString(originalHost, host);
        if (fromAddressString != null) {
            if (BuildConfig.DEBUG) {
                mLogger.dumpToLog();
            }
            return fromAddressString;
        }
        // Find from cache
        final InetAddress[] fromCache = getCached(host);
        if (fromCache != null) {
            if (BuildConfig.DEBUG) {
                mLogger.dumpToLog();
            }
            return fromCache;
        }
        // Load from custom mapping
        mLogger.addSplit("start custom mappong resolve");
        final InetAddress[] fromMapping = getFromMapping(host);
        mLogger.addSplit("end custom mappong resolve");
        if (fromMapping != null) {
            putCache(originalHost, fromMapping);
            if (BuildConfig.DEBUG) {
                mLogger.dumpToLog();
            }
            return fromMapping;
        }
        // Load from /etc/hosts
        mLogger.addSplit("start /etc/hosts resolve");
        final InetAddress[] fromSystemHosts = fromSystemHosts(host);
        mLogger.addSplit("end /etc/hosts resolve");
        if (fromSystemHosts != null) {
            putCache(originalHost, fromSystemHosts);
            if (BuildConfig.DEBUG) {
                mLogger.dumpToLog();
            }
            return fromSystemHosts;
        }
        // Use TCP DNS Query if enabled.
//        final Resolver dns = getResolver();
//        if (dns != null && mPreferences.getBoolean(KEY_TCP_DNS_QUERY, false)) {
//            final InetAddress[] hostAddr = resolveDns(originalHost, host, dns);
//            if (hostAddr != null) return hostAddr;
//        }
        mLogger.addSplit("start system default resolve");
        final InetAddress[] defaultAddresses = InetAddress.getAllByName(host);
        mLogger.addSplit("end system default resolve");
        putCache(host, defaultAddresses);
        if (BuildConfig.DEBUG) {
            mLogger.dumpToLog();
        }
        return defaultAddresses;
    }

    private InetAddress[] fromSystemHosts(String host) {
        try {
            return mSystemHosts.resolve(host);
        } catch (IOException e) {
            return null;
        }
    }

    private InetAddress[] getCached(String host) {
        return mHostCache.get(host);
    }

    private InetAddress[] resolveDns(String originalHost, String host, Resolver dns) throws IOException {
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
            putCache(originalHost, hostAddr);
            if (BuildConfig.DEBUG) {
                Log.v(RESOLVER_LOGTAG, "Resolved " + Arrays.toString(hostAddr));
            }
            return hostAddr;
        }
        // No address is reachable, but I believe the IP is correct.

        for (final Record record : records) {
            if (record instanceof CNAMERecord)
                return resolveInternal(originalHost, ((CNAMERecord) record).getTarget().toString());
        }
        return null;
    }

    private void putCache(String host, InetAddress[] addresses) {
        if (ArrayUtils.isEmpty(addresses) || ArrayUtils.contains(addresses, null)) return;
        mHostCache.put(host, addresses);
    }

    private InetAddress[] fromAddressString(String host, String address) throws UnknownHostException {
        final InetAddress resolved = InetAddressUtils.getResolvedIPAddress(host, address);
        if (resolved == null) return null;
        return new InetAddress[]{resolved};
    }

    @Nullable
    private InetAddress[] getFromMapping(final String host) {
        return getFromMappingInternal(host, host, false);
    }

    @Nullable
    private InetAddress[] getFromMappingInternal(String host, String origHost, boolean checkRecursive) {
        if (checkRecursive && hostMatches(host, origHost)) {
            // Recursive resolution, stop this call
            return null;
        }
        for (final Entry<String, ?> entry : mHostMapping.getAll().entrySet()) {
            if (hostMatches(host, entry.getKey())) {
                final String value = (String) entry.getValue();
                final InetAddress resolved = InetAddressUtils.getResolvedIPAddress(origHost, value);
                if (resolved == null) {
                    // Maybe another hostname
                    return getFromMappingInternal(value, origHost, true);
                }
                return new InetAddress[]{resolved};
            }
        }
        return null;
    }

    private Resolver getResolver() throws IOException {
        if (mDns != null) return mDns;
        return mDns = new SimpleResolver(mDnsAddress);
    }

    private static boolean hostMatches(final String host, final String rule) {
        if (rule == null || host == null) return false;
        if (rule.startsWith(".")) return StringUtils.endsWithIgnoreCase(host, rule);
        return host.equalsIgnoreCase(rule);
    }

    private static boolean isValidIpAddress(final String address) {
        return InetAddressUtils.getInetAddressType(address) != 0;
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        try {
            return Arrays.asList(resolveInternal(hostname, hostname));
        } catch (IOException e) {
            if (e instanceof UnknownHostException) throw (UnknownHostException) e;
            throw new UnknownHostException("Unable to resolve address " + e.getMessage());
        }
    }
}

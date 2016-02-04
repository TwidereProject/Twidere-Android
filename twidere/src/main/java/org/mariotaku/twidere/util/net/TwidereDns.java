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
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.util.LruCache;
import android.util.TimingLogger;

import com.squareup.okhttp.Dns;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.mariotaku.inetaddrjni.library.InetAddressUtils;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwidereMathUtils;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
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
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

@Singleton
public class TwidereDns implements Constants, Dns {

    private static final String RESOLVER_LOGTAG = "TwidereDns";

    private final SharedPreferences mHostMapping, mPreferences;
    private final HostCache mHostCache = new HostCache(512);
    private final SystemHosts mSystemHosts;

    private Resolver mResolver;
    private TimingLogger mLogger;
    private long mConnnectTimeout;

    public TwidereDns(final Context context) {
        mLogger = new TimingLogger(RESOLVER_LOGTAG, "resolve");
        mHostMapping = SharedPreferencesWrapper.getInstance(context, HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mSystemHosts = new SystemHosts();
        reloadDnsSettings();
    }

    private static boolean hostMatches(final String host, final String rule) {
        if (rule == null || host == null) return false;
        if (rule.startsWith(".")) return StringUtils.endsWithIgnoreCase(host, rule);
        return host.equalsIgnoreCase(rule);
    }

    private static boolean isValidIpAddress(final String address) {
        return InetAddressUtils.getInetAddressType(address) != 0;
    }

    @SuppressWarnings("unused")
    public synchronized void removeCachedHost(final String host) {
        mHostCache.remove(host);
    }

    @NonNull
    private InetAddress[] resolveInternal(String originalHost, String host, int depth, boolean putCache) throws IOException {
        resetLog(originalHost);
        // Return if host is an address
        final InetAddress[] fromAddressString = fromAddressString(originalHost, host);
        if (fromAddressString != null) {
            if (BuildConfig.DEBUG) {
                addLogSplit(originalHost, host, "valid ip address", depth);
                dumpLog(fromAddressString);
            }
            return fromAddressString;
        }
        // Find from cache
        final InetAddress[] fromCache = getCached(host);
        if (fromCache != null) {
            if (BuildConfig.DEBUG) {
                addLogSplit(originalHost, host, "hit cache", depth);
                dumpLog(fromCache);
            }
            return fromCache;
        }
        // Load from custom mapping
        addLogSplit(originalHost, host, "start custom mapping resolve", depth);
        final InetAddress[] fromMapping = getFromMapping(host);
        addLogSplit(originalHost, host, "end custom mapping resolve", depth);
        if (fromMapping != null) {
            putCache(originalHost, fromMapping, -1, TimeUnit.SECONDS);
            if (BuildConfig.DEBUG) {
                dumpLog(fromMapping);
            }
            return fromMapping;
        }
        // Load from /etc/hosts
        addLogSplit(originalHost, host, "start /etc/hosts resolve", depth);
        final InetAddress[] fromSystemHosts = fromSystemHosts(host);
        addLogSplit(originalHost, host, "end /etc/hosts resolve", depth);
        if (fromSystemHosts != null) {
            putCache(originalHost, fromSystemHosts, 60, TimeUnit.SECONDS);
            if (BuildConfig.DEBUG) {
                dumpLog(fromSystemHosts);
            }
            return fromSystemHosts;
        }
        // Use TCP DNS Query if enabled.
        addLogSplit(originalHost, host, "start resolver resolve", depth);
        final InetAddress[] fromResolver = fromResolver(originalHost, host, depth);
        addLogSplit(originalHost, host, "end resolver resolve", depth);
        if (!ArrayUtils.isEmpty(fromResolver)) {
            if (BuildConfig.DEBUG) {
                dumpLog(fromResolver);
            }
            return fromResolver;
        }
        addLogSplit(originalHost, host, "start system default resolve", depth);
        final InetAddress[] fromDefault = InetAddress.getAllByName(host);
        addLogSplit(originalHost, host, "end system default resolve", depth);
        putCache(host, fromDefault, 60, TimeUnit.SECONDS);
        if (BuildConfig.DEBUG) {
            dumpLog(fromDefault);
        }
        return fromDefault;
    }

    private void dumpLog(@NonNull InetAddress[] addresses) {
        Log.v(RESOLVER_LOGTAG, "Resolved " + Arrays.toString(addresses));
        mLogger.dumpToLog();
    }

    private void resetLog(String originalHost) {
        mLogger.reset(RESOLVER_LOGTAG, originalHost);
    }

    private void addLogSplit(String originalHost, String host, String message, int depth) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append(">");
        }
        sb.append(" ");
        sb.append(host);
        sb.append(": ");
        sb.append(message);
        mLogger.addSplit(sb.toString());
    }

    private InetAddress[] fromSystemHosts(String host) {
        try {
            return mSystemHosts.resolve(host);
        } catch (IOException e) {
            return null;
        }
    }

    private InetAddress[] getCached(String host) {
        return mHostCache.getValid(host);
    }

    @Nullable
    private InetAddress[] fromResolver(String originalHost, String host, int depth) throws IOException {
        final Resolver dns = getResolver();
        final Lookup lookup = new Lookup(new Name(host), Type.A, DClass.IN);
        lookup.setResolver(dns);
        lookup.run();
        final int result = lookup.getResult();
        if (result != Lookup.SUCCESSFUL) {
            throw new UnknownHostException("Unable to resolve " + host + ", " + lookup.getErrorString());
        }
        final Record[] records = lookup.getAnswers();
        final ArrayList<InetAddress> resolvedAddresses = new ArrayList<>();
        // Test each IP address resolved.
        long ttl = -1;
        for (final Record record : records) {
            if (ttl == -1) {
                ttl = record.getTTL();
            }
            final InetAddress inetAddress;
            if (record instanceof ARecord) {
                inetAddress = ((ARecord) record).getAddress();
            } else if (record instanceof AAAARecord) {
                inetAddress = ((AAAARecord) record).getAddress();
            } else {
                continue;
            }
            if (mConnnectTimeout == 0 || inetAddress.isReachable(TwidereMathUtils.clamp((int) mConnnectTimeout / 2, 1000, 3000))) {
                resolvedAddresses.add(InetAddress.getByAddress(originalHost, inetAddress.getAddress()));
            }
        }
        if (!resolvedAddresses.isEmpty()) {
            final InetAddress[] hostAddr = resolvedAddresses.toArray(new InetAddress[resolvedAddresses.size()]);
            putCache(originalHost, hostAddr, ttl, TimeUnit.SECONDS);
            return hostAddr;
        }
        // No address is reachable, but I believe the IP is correct.
        return null;
//        final List<InetAddress> addresses = new ArrayList<>();
//        for (final Record record : records) {
//            if (!(record instanceof CNAMERecord)) {
//                continue;
//            }
//            Collections.addAll(addresses, resolveInternal(originalHost,
//                    ((CNAMERecord) record).getTarget().toString(), depth + 1, false));
//        }
//        return addresses.toArray(new InetAddress[addresses.size()]);
    }

    private void putCache(String host, InetAddress[] addresses, long ttl, TimeUnit unit) {
        if (ArrayUtils.isEmpty(addresses) || ArrayUtils.contains(addresses, null)) return;
        mHostCache.put(host, addresses, ttl, unit);
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

    @NonNull
    private Resolver getResolver() throws IOException {
        if (mResolver != null) return mResolver;
        final boolean tcp = mPreferences.getBoolean(KEY_TCP_DNS_QUERY, false);
        final String address = mPreferences.getString(KEY_DNS_SERVER, null);
        final SimpleResolver resolver;
        if (isValidIpAddress(address)) {
            resolver = new SimpleResolver(address);
        } else {
            resolver = new SimpleResolver();
        }
        resolver.setTCP(tcp);
        return mResolver = resolver;
    }

    public void reloadDnsSettings() {
        mResolver = null;
        mConnnectTimeout = TimeUnit.SECONDS.toMillis(mPreferences.getInt(KEY_CONNECTION_TIMEOUT, 10));
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        try {
            return Arrays.asList(resolveInternal(hostname, hostname, 0, true));
        } catch (IOException e) {
            if (e instanceof UnknownHostException) throw (UnknownHostException) e;
            throw new UnknownHostException("Unable to resolve address " + e.getMessage());
        }
    }

    private static class HostCache extends LruCache<String, InetAddress[]> {

        private SimpleArrayMap<String, Long> ttlMap = new SimpleArrayMap<>();

        public HostCache(int maxSize) {
            super(maxSize);
        }

        public synchronized void put(String host, InetAddress[] addresses, long ttl, TimeUnit unit) {
            // Don't cache this entry if ttl == 0
            if (ttl == 0) return;
            put(host, addresses);
            // ttl < 0 means permanent entry
            if (ttl > 0) {
                ttlMap.put(host, SystemClock.elapsedRealtime() + unit.toMillis(ttl));
            }
        }

        public InetAddress[] getValid(String host) {
            cleanExpired();
            return get(host);
        }


        private synchronized void cleanExpired() {
            for (int i = ttlMap.size() - 1; i >= 0; i--) {
                if (ttlMap.valueAt(i) < SystemClock.elapsedRealtime()) {
                    remove(ttlMap.keyAt(i));
                    ttlMap.removeAt(i);
                }
            }
        }
    }
}

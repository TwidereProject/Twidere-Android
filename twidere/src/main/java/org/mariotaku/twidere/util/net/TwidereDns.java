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
import android.text.TextUtils;
import android.util.Log;
import android.util.TimingLogger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwidereMathUtils;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Address;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import okhttp3.Dns;

@Singleton
public class TwidereDns implements Constants, Dns {

    private static final String RESOLVER_LOGTAG = "TwidereDns";
    private static final boolean CHECK_ADDRESS = Boolean.parseBoolean("false");

    private final SharedPreferences mHostMapping, mPreferences;
    private final SystemHosts mSystemHosts;

    private Resolver mResolver;
    private TimingLogger mLogger;
    private long mConnectTimeout;
    private boolean mUseResolver;

    public TwidereDns(final Context context) {
        mLogger = new TimingLogger(RESOLVER_LOGTAG, "resolve");
        mHostMapping = SharedPreferencesWrapper.getInstance(context, HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mSystemHosts = new SystemHosts();
        reloadDnsSettings();
    }

    @Override
    public synchronized List<InetAddress> lookup(String hostname) throws UnknownHostException {
        try {
            return Arrays.asList(resolveInternal(hostname, hostname, 0, true, mUseResolver));
        } catch (IOException e) {
            if (e instanceof UnknownHostException) throw (UnknownHostException) e;
            throw new UnknownHostException("Unable to resolve address " + e.getMessage());
        }
    }

    public synchronized List<InetAddress> lookupResolver(String hostname) throws UnknownHostException {
        try {
            return Arrays.asList(resolveInternal(hostname, hostname, 0, true, true));
        } catch (IOException e) {
            if (e instanceof UnknownHostException) throw (UnknownHostException) e;
            throw new UnknownHostException("Unable to resolve address " + e.getMessage());
        }
    }

    public synchronized void reloadDnsSettings() {
        mResolver = null;
        mUseResolver = !TextUtils.isEmpty(mPreferences.getString(KEY_DNS_SERVER, null));
        mConnectTimeout = TimeUnit.SECONDS.toMillis(mPreferences.getInt(KEY_CONNECTION_TIMEOUT, 10));
    }

    @NonNull
    private InetAddress[] resolveInternal(final String originalHost, final String host, final int depth,
                                          final boolean putCache, final boolean useResolver) throws IOException {
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
        // Load from custom mapping
        addLogSplit(originalHost, host, "start custom mapping resolve", depth);
        final InetAddress[] fromMapping = getFromMapping(host);
        addLogSplit(originalHost, host, "end custom mapping resolve", depth);
        if (fromMapping != null) {
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
            if (BuildConfig.DEBUG) {
                dumpLog(fromSystemHosts);
            }
            return fromSystemHosts;
        }
        // Use DNS resolver
        if (useResolver) {
            addLogSplit(originalHost, host, "start resolver resolve", depth);
            final InetAddress[] fromResolver = fromResolver(originalHost, host, depth);
            addLogSplit(originalHost, host, "end resolver resolve", depth);
            if (!ArrayUtils.isEmpty(fromResolver)) {
                if (BuildConfig.DEBUG) {
                    dumpLog(fromResolver);
                }
                return fromResolver;
            }
        }
        addLogSplit(originalHost, host, "start system default resolve", depth);
        final InetAddress[] fromDefault = InetAddress.getAllByName(host);
        addLogSplit(originalHost, host, "end system default resolve", depth);
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

    @Nullable
    private InetAddress[] fromResolver(String originalHost, String host, int depth) throws IOException {
        addLogSplit(originalHost, host, "start get resolver", depth);
        final Resolver resolver = getResolver();
        addLogSplit(originalHost, host, "end get resolver", depth);
        addLogSplit(originalHost, host, "start lookup host name", depth);
        final Record[] records = lookupHostName(resolver, host, true);
        addLogSplit(originalHost, host, "end lookup host name", depth);
        addLogSplit(originalHost, host, "start convert record", depth);
        InetAddress[] addrs = new InetAddress[records.length];
        for (int i = 0; i < records.length; i++) {
            addrs[i] = addrFromRecord(originalHost, records[i]);
        }
        addLogSplit(originalHost, host, "end convert record", depth);
        return addrs;
    }

    private boolean checkAddress(InetAddress inetAddress) throws IOException {
        if (!CHECK_ADDRESS) return true;
        return inetAddress.isReachable(TwidereMathUtils.clamp((int) mConnectTimeout / 2, 1000, 3000));
    }

    @Nullable
    private InetAddress[] getFromMapping(final String host) throws UnknownHostException {
        return getFromMappingInternal(host, host, false);
    }

    @Nullable
    private InetAddress[] getFromMappingInternal(String host, String origHost, boolean checkRecursive) throws UnknownHostException {
        if (checkRecursive && hostMatches(host, origHost)) {
            // Recursive resolution, stop this call
            return null;
        }
        for (final Entry<String, ?> entry : mHostMapping.getAll().entrySet()) {
            if (hostMatches(host, entry.getKey())) {
                final String value = (String) entry.getValue();
                final InetAddress resolved = getResolvedIPAddress(origHost, value);
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


    private static boolean hostMatches(final String host, final String rule) {
        if (rule == null || host == null) return false;
        if (rule.startsWith(".")) return StringUtils.endsWithIgnoreCase(host, rule);
        return host.equalsIgnoreCase(rule);
    }


    private InetAddress[] fromAddressString(String host, String address) throws UnknownHostException {
        final InetAddress resolved = getResolvedIPAddress(host, address);
        if (resolved == null) return null;
        return new InetAddress[]{resolved};
    }

    public static InetAddress getResolvedIPAddress(String host, String address) throws UnknownHostException {
        byte[] bytes;
        bytes = Address.toByteArray(address, Address.IPv4);
        if (bytes != null)
            return InetAddress.getByAddress(host, bytes);
        bytes = Address.toByteArray(address, Address.IPv6);
        if (bytes != null)
            return InetAddress.getByAddress(host, bytes);
        return null;
    }

    private static int getInetAddressType(String address) {
        byte[] bytes;
        bytes = Address.toByteArray(address, Address.IPv4);
        if (bytes != null)
            return Address.IPv4;
        bytes = Address.toByteArray(address, Address.IPv6);
        if (bytes != null)
            return Address.IPv6;
        return 0;
    }

    public static boolean isValidIpAddress(final String address) {
        return getInetAddressType(address) != 0;
    }

    private static Record[] lookupHostName(Resolver resolver, String name, boolean all) throws UnknownHostException {
        try {
            Lookup lookup = newLookup(resolver, name, Type.A);
            Record[] a = lookup.run();
            if (a == null) {
                if (lookup.getResult() == Lookup.TYPE_NOT_FOUND) {
                    Record[] aaaa = newLookup(resolver, name, Type.AAAA).run();
                    if (aaaa != null)
                        return aaaa;
                }
                throw new UnknownHostException("unknown host");
            }
            if (!all)
                return a;
            Record[] aaaa = newLookup(resolver, name, Type.AAAA).run();
            if (aaaa == null)
                return a;
            Record[] merged = new Record[a.length + aaaa.length];
            System.arraycopy(a, 0, merged, 0, a.length);
            System.arraycopy(aaaa, 0, merged, a.length, aaaa.length);
            return merged;
        } catch (TextParseException e) {
            throw new UnknownHostException("invalid name");
        }
    }

    private static Lookup newLookup(Resolver resolver, String name, int type) throws TextParseException {
        final Lookup lookup = new Lookup(name, type);
        lookup.setResolver(resolver);
        return lookup;
    }

    private static InetAddress addrFromRecord(String name, Record r) throws UnknownHostException {
        InetAddress addr;
        if (r instanceof ARecord) {
            addr = ((ARecord) r).getAddress();
        } else {
            addr = ((AAAARecord) r).getAddress();
        }
        return InetAddress.getByAddress(name, addr.getAddress());
    }


}

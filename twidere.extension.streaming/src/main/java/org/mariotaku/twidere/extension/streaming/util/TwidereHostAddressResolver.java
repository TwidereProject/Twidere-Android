/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.extension.streaming.util;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.internal.Network;

import org.mariotaku.twidere.Twidere;
import org.mariotaku.twidere.extension.streaming.BuildConfig;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class TwidereHostAddressResolver implements Network {

    private static final String RESOLVER_LOGTAG = "Twidere.Streaming.Host";
    private static TwidereHostAddressResolver sInstance;

    private final HostCache mHostCache = new HostCache(512);
    private final Context mContext;

    public TwidereHostAddressResolver(final Context context) {
        mContext = context;
    }

    @Override
    public InetAddress[] resolveInetAddresses(final String host) throws UnknownHostException {
        if (host == null) return null;
        // First, I'll try to load address cached.
        final InetAddress[] cached = mHostCache.get(host);
        if (cached != null) {
            if (BuildConfig.DEBUG) {
                Log.d(RESOLVER_LOGTAG, "Got cached " + Arrays.toString(cached));
            }
            return cached;
        }
        final InetAddress[] resolved = Twidere.resolveHost(mContext, host);
        mHostCache.put(host, resolved);
        return resolved;
    }

    public static Network getInstance(final Context context) {
        if (sInstance != null) return sInstance;
        return sInstance = new TwidereHostAddressResolver(context);
    }

    private static class HostCache extends LinkedHashMap<String, InetAddress[]> {

        private static final long serialVersionUID = -9216545511009449147L;

        HostCache(final int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public InetAddress[] put(final String key, final InetAddress[] value) {
            if (value == null) return null;
            return super.put(key, value);
        }
    }
}

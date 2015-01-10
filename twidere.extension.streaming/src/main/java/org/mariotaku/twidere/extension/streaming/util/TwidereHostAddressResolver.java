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

import static android.text.TextUtils.isEmpty;

import java.util.LinkedHashMap;

import org.mariotaku.twidere.Twidere;
import org.mariotaku.twidere.extension.streaming.BuildConfig;

import twitter4j.http.HostAddressResolver;
import android.content.Context;
import android.util.Log;

public class TwidereHostAddressResolver implements HostAddressResolver {

	private static final String RESOLVER_LOGTAG = "Twidere.Streaming.HostAddressResolver";

	private final HostCache mHostCache = new HostCache(512);
	private final Context mContext;

	public TwidereHostAddressResolver(final Context context) {
		mContext = context;
	}

	@Override
	public String resolve(final String host) {
		if (host == null) return null;
		// First, I'll try to load address cached.
		if (mHostCache.containsKey(host)) {
			if (BuildConfig.DEBUG) {
				Log.d(RESOLVER_LOGTAG, "Got cached address " + mHostCache.get(host) + " for host " + host);
			}
			return mHostCache.get(host);
		}
		final String address = Twidere.resolveHost(mContext, host);
		if (isValidIpAddress(address)) {
			if (BuildConfig.DEBUG) {
				Log.d(RESOLVER_LOGTAG, "Resolved address " + address + " for host " + host);
			}
			return address;
		}
		if (BuildConfig.DEBUG) {
			Log.w(RESOLVER_LOGTAG, "Resolve address " + host + " failed, using original host");
		}
		return host;
	}

	static boolean isValidIpAddress(final String address) {
		return !isEmpty(address);
	}

	private static class HostCache extends LinkedHashMap<String, String> {

		private static final long serialVersionUID = -9216545511009449147L;

		HostCache(final int initialCapacity) {
			super(initialCapacity);
		}

		@Override
		public String put(final String key, final String value) {
			if (value == null) return value;
			return super.put(key, value);
		}
	}
}

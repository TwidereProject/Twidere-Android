/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.os.Looper;
import android.util.Log;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import okhttp3.Dns;

/**
 * Created by mariotaku on 15/12/31.
 */
public class TwidereProxySelector extends ProxySelector {
    @Inject
    Dns dns;
    private final Proxy.Type type;
    private final String host;
    private final int port;
    private List<Proxy> proxy;

    public TwidereProxySelector(Context context, Proxy.Type type, String host, int port) {
        GeneralComponentHelper.build(context).inject(this);
        this.type = type;
        this.host = host;
        this.port = port;
    }

    @Override
    public List<Proxy> select(URI uri) {
        if (proxy != null) return proxy;
        final InetSocketAddress address;
        if (Looper.myLooper() != Looper.getMainLooper()) {
            address = createResolved(host, port);
        } else {
            // If proxy host is an IP address, create unresolved directly.
            if (TwidereDns.isValidIpAddress(host)) {
                address = InetSocketAddress.createUnresolved(host, port);
            } else {
                address = new InetSocketAddress(host, port);
            }
        }
        return proxy = Collections.singletonList(new Proxy(type, address));
    }

    private InetSocketAddress createResolved(String host, int port) {
        try {
            //noinspection LoopStatementThatDoesntLoop
            for (InetAddress inetAddress : dns.lookup(host)) {
                return new InetSocketAddress(inetAddress, port);
            }
        } catch (IOException e) {
            return InetSocketAddress.createUnresolved(host, port);
        }
        return InetSocketAddress.createUnresolved(host, port);
    }

    @Override
    public void connectFailed(URI uri, SocketAddress address, IOException failure) {
        if (BuildConfig.DEBUG) {
            Log.w("TwidereProxy", String.format("%s: proxy %s connect failed", uri, address), failure);
        }
    }
}

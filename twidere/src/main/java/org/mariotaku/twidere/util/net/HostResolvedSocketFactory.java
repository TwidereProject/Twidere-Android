/*
 * Twidere - Twitter client for Android
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

import org.apache.http.conn.util.InetAddressUtils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import twitter4j.http.HostAddressResolver;

/**
 * Created by mariotaku on 15/1/31.
 */
public class HostResolvedSocketFactory extends SocketFactory {

    private final SocketFactory defaultFactory;
    private final HostAddressResolver resolver;

    public HostResolvedSocketFactory(HostAddressResolver resolver) {
        defaultFactory = SocketFactory.getDefault();
        this.resolver = resolver;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        final String resolvedHost = resolver.resolve(host);
        if (resolvedHost != null && !resolvedHost.equals(host)) {
            if (InetAddressUtils.isIPv6Address(resolvedHost)) {
                final byte[] resolvedAddress = Inet6Address.getByName(resolvedHost).getAddress();
                return new Socket(InetAddress.getByAddress(host, resolvedAddress), port);
            } else if (InetAddressUtils.isIPv4Address(resolvedHost)) {
                final byte[] resolvedAddress = Inet4Address.getByName(resolvedHost).getAddress();
                return new Socket(InetAddress.getByAddress(host, resolvedAddress), port);
            }
        }
        return defaultFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket() throws IOException {
        return new Socket();
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        final String resolvedHost = resolver.resolve(host);
        if (resolvedHost != null && !resolvedHost.equals(host)) {
            if (InetAddressUtils.isIPv6Address(resolvedHost)) {
                final byte[] resolvedAddress = Inet6Address.getByName(resolvedHost).getAddress();
                return new Socket(InetAddress.getByAddress(host, resolvedAddress), port);
            } else if (InetAddressUtils.isIPv4Address(resolvedHost)) {
                final byte[] resolvedAddress = Inet4Address.getByName(resolvedHost).getAddress();
                return new Socket(InetAddress.getByAddress(host, resolvedAddress), port);
            }
        }
        return defaultFactory.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        final String hostName = host.getHostName();
        final String resolvedHost = resolver.resolve(hostName);
        if (resolvedHost != null && !resolvedHost.equals(hostName)) {
            if (InetAddressUtils.isIPv6Address(resolvedHost)) {
                final byte[] resolvedAddress = Inet6Address.getByName(resolvedHost).getAddress();
                return new Socket(InetAddress.getByAddress(hostName, resolvedAddress), port);
            } else if (InetAddressUtils.isIPv4Address(resolvedHost)) {
                final byte[] resolvedAddress = Inet4Address.getByName(resolvedHost).getAddress();
                return new Socket(InetAddress.getByAddress(hostName, resolvedAddress), port);
            }
        }
        return defaultFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        final String hostName = address.getHostName();
        final String resolvedHost = resolver.resolve(hostName);
        if (resolvedHost != null && !resolvedHost.equals(hostName)) {
            if (InetAddressUtils.isIPv6Address(resolvedHost)) {
                final byte[] resolvedAddress = Inet6Address.getByName(resolvedHost).getAddress();
                return new Socket(InetAddress.getByAddress(hostName, resolvedAddress), port, localAddress, localPort);
            } else if (InetAddressUtils.isIPv4Address(resolvedHost)) {
                final byte[] resolvedAddress = Inet4Address.getByName(resolvedHost).getAddress();
                return new Socket(InetAddress.getByAddress(hostName, resolvedAddress), port, localAddress, localPort);
            }
        }
        return defaultFactory.createSocket(address, port, localAddress, localPort);
    }

    protected HostAddressResolver getResolver() {
        return resolver;
    }
}

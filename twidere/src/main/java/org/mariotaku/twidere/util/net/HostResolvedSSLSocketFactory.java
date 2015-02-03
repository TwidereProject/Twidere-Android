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

import android.net.SSLCertificateSocketFactory;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import twitter4j.http.HostAddressResolver;

/**
 * Created by mariotaku on 15/1/31.
 */
public class HostResolvedSSLSocketFactory extends SSLSocketFactory {

    private final SSLSocketFactory defaultFactory;
    private final HostAddressResolver resolver;

    public HostResolvedSSLSocketFactory(HostAddressResolver resolver, boolean ignoreError) {
        if (ignoreError) {
            defaultFactory = SSLCertificateSocketFactory.getInsecure(0, null);
        } else {
            defaultFactory = SSLCertificateSocketFactory.getDefault(0, null);
        }
        this.resolver = resolver;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return defaultFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket() throws IOException {
        return defaultFactory.createSocket();
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return defaultFactory.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return defaultFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return defaultFactory.createSocket(address, port, localAddress, localPort);
    }


    @Override
    public String[] getDefaultCipherSuites() {
        return defaultFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return defaultFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return defaultFactory.createSocket(s, host, port, autoClose);
    }

}

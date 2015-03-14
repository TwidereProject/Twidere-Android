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

package com.squareup.okhttp.internal;

import android.util.Log;

import com.squareup.okhttp.Protocol;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import javax.net.ssl.SSLSocket;

/**
 * Created by mariotaku on 15/3/13.
 */
public class TwidereOkHttpPlatform extends Platform implements Constants {

    // setUseSessionTickets(boolean)
    private static final OptionalMethod<Socket> SET_USE_SESSION_TICKETS =
            new OptionalMethod<>(null, "setUseSessionTickets", Boolean.TYPE);
    // setHostname(String)
    private static final OptionalMethod<Socket> SET_HOSTNAME =
            new OptionalMethod<>(null, "setHostname", String.class);
    // byte[] getAlpnSelectedProtocol()
    private static final OptionalMethod<Socket> GET_ALPN_SELECTED_PROTOCOL =
            new OptionalMethod<>(byte[].class, "getAlpnSelectedProtocol");
    // setAlpnSelectedProtocol(byte[])
    private static final OptionalMethod<Socket> SET_ALPN_PROTOCOLS =
            new OptionalMethod<>(null, "setAlpnProtocols", byte[].class);

    private final TwidereApplication application;

    // Non-null on Android 4.0+.
    private final Method trafficStatsTagSocket;
    private final Method trafficStatsUntagSocket;

    private TwidereOkHttpPlatform(TwidereApplication application, Method trafficStatsTagSocket, Method trafficStatsUntagSocket) {
        this.application = application;
        this.trafficStatsTagSocket = trafficStatsTagSocket;
        this.trafficStatsUntagSocket = trafficStatsUntagSocket;
    }

    public static void applyHack(TwidereApplication application) {
        final TwidereOkHttpPlatform platform = get(application);
        try {
            final Field field = Platform.class.getDeclaredField("PLATFORM");
            field.setAccessible(true);
            field.set(null, platform);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            //Unable to change default platform
            Log.w(LOGTAG, e);
        }
    }


    public static TwidereOkHttpPlatform get(TwidereApplication application) {
        // Attempt to find Android 4.0+ APIs.
        Method trafficStatsTagSocket = null;
        Method trafficStatsUntagSocket = null;
        try {
            Class<?> trafficStats = Class.forName("android.net.TrafficStats");
            trafficStatsTagSocket = trafficStats.getMethod("tagSocket", Socket.class);
            trafficStatsUntagSocket = trafficStats.getMethod("untagSocket", Socket.class);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }
        return new TwidereOkHttpPlatform(application, trafficStatsTagSocket, trafficStatsUntagSocket);
    }

    @Override
    public void connectSocket(Socket socket, InetSocketAddress address,
                              int connectTimeout) throws IOException {
        try {
//            final HostAddressResolver resolver = application.getHostAddressResolver();
            socket.connect(address, connectTimeout);
        } catch (SecurityException se) {
            // Before android 4.3, socket.connect could throw a SecurityException
            // if opening a socket resulted in an EACCES error.
            IOException ioException = new IOException("Exception in connect");
            ioException.initCause(se);
            throw ioException;
        }
    }

    @Override
    public void configureTlsExtensions(
            SSLSocket sslSocket, String hostname, List<Protocol> protocols) {
        // Enable SNI and session tickets.
        if (hostname != null) {
            SET_USE_SESSION_TICKETS.invokeOptionalWithoutCheckedException(sslSocket, true);
            SET_HOSTNAME.invokeOptionalWithoutCheckedException(sslSocket, hostname);
        }

        // Enable ALPN.
        boolean alpnSupported = SET_ALPN_PROTOCOLS.isSupported(sslSocket);
        if (!alpnSupported) {
            return;
        }

        Object[] parameters = {concatLengthPrefixed(protocols)};
        SET_ALPN_PROTOCOLS.invokeWithoutCheckedException(sslSocket, parameters);
    }

    @Override
    public String getSelectedProtocol(SSLSocket socket) {
        boolean alpnSupported = GET_ALPN_SELECTED_PROTOCOL.isSupported(socket);
        if (!alpnSupported) {
            return null;
        }

        byte[] alpnResult =
                (byte[]) GET_ALPN_SELECTED_PROTOCOL.invokeWithoutCheckedException(socket);
        if (alpnResult != null) {
            return new String(alpnResult, Util.UTF_8);
        }
        return null;
    }

    @Override
    public void tagSocket(Socket socket) throws SocketException {
        if (trafficStatsTagSocket == null) return;

        try {
            trafficStatsTagSocket.invoke(null, socket);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public void untagSocket(Socket socket) throws SocketException {
        if (trafficStatsUntagSocket == null) return;

        try {
            trafficStatsUntagSocket.invoke(null, socket);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}

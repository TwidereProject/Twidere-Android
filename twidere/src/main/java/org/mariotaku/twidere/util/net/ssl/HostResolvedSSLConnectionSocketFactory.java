/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.mariotaku.twidere.util.net.ssl;

import android.net.SSLCertificateSocketFactory;
import android.os.Build;
import android.util.Log;

import org.apache.http.HttpHost;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

@ThreadSafe
public class HostResolvedSSLConnectionSocketFactory implements LayeredConnectionSocketFactory {

	private static final String TAG = "HttpClient";

	public static final String HTTP_CONTEXT_KEY_ORIGINAL_HOST = "original_host";

	private final javax.net.ssl.SSLSocketFactory socketfactory;

	private final X509HostnameVerifier hostnameVerifier;

	private final String[] supportedProtocols;

	private final String[] supportedCipherSuites;

	public HostResolvedSSLConnectionSocketFactory(final javax.net.ssl.SSLSocketFactory socketfactory,
			final String[] supportedProtocols, final String[] supportedCipherSuites,
			final X509HostnameVerifier hostnameVerifier) {
		this.socketfactory = Args.notNull(socketfactory, "SSL socket factory");
		this.supportedProtocols = supportedProtocols;
		this.supportedCipherSuites = supportedCipherSuites;
		this.hostnameVerifier = hostnameVerifier != null ? hostnameVerifier
				: SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
	}

	public HostResolvedSSLConnectionSocketFactory(final javax.net.ssl.SSLSocketFactory socketfactory,
			final X509HostnameVerifier hostnameVerifier) {
		this(socketfactory, null, null, hostnameVerifier);
	}

	public HostResolvedSSLConnectionSocketFactory(final SSLContext sslContext) {
		this(sslContext, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
	}

	public HostResolvedSSLConnectionSocketFactory(final SSLContext sslContext, final String[] supportedProtocols,
			final String[] supportedCipherSuites, final X509HostnameVerifier hostnameVerifier) {
		this(Args.notNull(sslContext, "SSL context").getSocketFactory(), supportedProtocols, supportedCipherSuites,
				hostnameVerifier);
	}

	public HostResolvedSSLConnectionSocketFactory(final SSLContext sslContext,
			final X509HostnameVerifier hostnameVerifier) {
		this(Args.notNull(sslContext, "SSL context").getSocketFactory(), null, null, hostnameVerifier);
	}

	@Override
	public Socket connectSocket(final int connectTimeout, final Socket socket, final HttpHost host,
			final InetSocketAddress remoteAddress, final InetSocketAddress localAddress, final HttpContext context)
			throws IOException {
		Args.notNull(host, "HTTP host");
		Args.notNull(remoteAddress, "Remote address");
		final Socket sock = socket != null ? socket : createSocket(context);
		if (localAddress != null) {
			sock.bind(localAddress);
		}
		try {
			sock.connect(remoteAddress, connectTimeout);
		} catch (final IOException ex) {
			try {
				sock.close();
			} catch (final IOException ignore) {
			}
			throw ex;
		}
		// Setup SSL layering if necessary
		if (sock instanceof SSLSocket) {
			final SSLSocket sslsock = (SSLSocket) sock;
			sslsock.startHandshake();
			verifyHostname(sslsock, host.getHostName(), context);
			return sock;
		} else
			return createLayeredSocket(sock, host.getHostName(), remoteAddress.getPort(), context);
	}

	@Override
	public Socket createLayeredSocket(final Socket socket, final String target, final int port,
			final HttpContext context) throws IOException {
		final SSLSocket sslsock = (SSLSocket) socketfactory.createSocket(socket, target, port, true);
		if (supportedProtocols != null) {
			sslsock.setEnabledProtocols(supportedProtocols);
		}
		if (supportedCipherSuites != null) {
			sslsock.setEnabledCipherSuites(supportedCipherSuites);
		}
		prepareSocket(sslsock);

		// Android specific code to enable SNI
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

			if (socketfactory instanceof SSLCertificateSocketFactory) {
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.d(TAG, "Enabling SNI for " + target);
				}
				((SSLCertificateSocketFactory) socketfactory).setHostname(sslsock, target);
			}
		}
		// End of Android specific code

		sslsock.startHandshake();
		verifyHostname(sslsock, target, context);
		return sslsock;
	}

	@Override
	public Socket createSocket(final HttpContext context) throws IOException {
		return SocketFactory.getDefault().createSocket();
	}

	/**
	 * Performs any custom initialization for a newly created SSLSocket (before
	 * the SSL handshake happens).
	 * 
	 * The default implementation is a no-op, but could be overridden to, e.g.,
	 * call {@link javax.net.ssl.SSLSocket#setEnabledCipherSuites(String[])}.
	 */
	protected void prepareSocket(final SSLSocket socket) throws IOException {
	}

	private String getHostname(final String hostname, final HttpContext context) {
		if (context == null) return hostname;
		final Object attr = context.getAttribute(HTTP_CONTEXT_KEY_ORIGINAL_HOST);
		if (attr instanceof String) return (String) attr;
		return hostname;
	}

	private void verifyHostname(final SSLSocket sslsock, final String hostname, final HttpContext context)
			throws IOException {
		try {
			hostnameVerifier.verify(getHostname(hostname, context), sslsock);
			// verifyHostName() didn't blowup - good!
		} catch (final IOException iox) {
			// close the socket before re-throwing the exception
			try {
				sslsock.close();
			} catch (final Exception x) { /* ignore */
			}
			throw iox;
		}
	}

	X509HostnameVerifier getHostnameVerifier() {
		return hostnameVerifier;
	}

}

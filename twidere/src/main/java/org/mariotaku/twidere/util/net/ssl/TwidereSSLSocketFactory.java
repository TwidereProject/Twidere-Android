package org.mariotaku.twidere.util.net.ssl;

import android.content.Context;

import org.apache.http.HttpHost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public final class TwidereSSLSocketFactory implements LayeredConnectionSocketFactory {

	private final Context context;
	private final boolean ignoreSSLErrors;
	private final HostResolvedSSLConnectionSocketFactory delegated;

	private TwidereSSLSocketFactory(final Context context, final boolean ignoreSSLErrors)
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		this.context = context;
		this.ignoreSSLErrors = ignoreSSLErrors;
		final TrustManager[] tm = { new TrustAllX509TrustManager() };
		final SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, tm, null);
		final X509HostnameVerifier hostnameVerifier = new TwidereHostnameVerifier(context, ignoreSSLErrors);
		delegated = new HostResolvedSSLConnectionSocketFactory(sslContext, hostnameVerifier);
	}

	@Override
	public Socket connectSocket(final int connectTimeout, final Socket socket, final HttpHost host,
			final InetSocketAddress remoteAddress, final InetSocketAddress localAddress, final HttpContext httpContext)
			throws IOException {
		return delegated.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, httpContext);
	}

	@Override
	public Socket createLayeredSocket(final Socket socket, final String target, final int port,
			final HttpContext httpContext) throws IOException {
		return delegated.createLayeredSocket(socket, target, port, httpContext);
	}

	@Override
	public Socket createSocket(final HttpContext httpContext) throws IOException {
		return delegated.createSocket(httpContext);
	}

	public static LayeredConnectionSocketFactory getSocketFactory(final Context context, final boolean ignoreSSLErrors)
			throws SSLInitializationException {
		try {
			return new TwidereSSLSocketFactory(context, ignoreSSLErrors);
		} catch (final GeneralSecurityException e) {
			throw new SSLInitializationException("Cannot create socket factory", e);
		}
	}

}
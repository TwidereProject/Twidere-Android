package org.mariotaku.twidere.util.net.ssl;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public final class TrustAllX509TrustManager implements X509TrustManager {
	@Override
	public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
	}

	@Override
	public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}
}
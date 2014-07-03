package org.mariotaku.twidere.util.net;

import android.content.Context;

import twitter4j.http.HttpClient;
import twitter4j.http.HttpClientConfiguration;
import twitter4j.http.HttpClientFactory;

public class TwidereHttpClientFactory implements HttpClientFactory {

	private final Context context;

	public TwidereHttpClientFactory(final Context context) {
		this.context = context;
	}

	@Override
	public HttpClient getInstance(final HttpClientConfiguration conf) {
		return new TwidereHttpClientImpl(context, conf);
	}

}

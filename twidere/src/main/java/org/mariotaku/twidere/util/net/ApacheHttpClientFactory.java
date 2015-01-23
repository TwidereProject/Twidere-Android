package org.mariotaku.twidere.util.net;

import android.content.Context;

import twitter4j.http.HttpClient;
import twitter4j.http.HttpClientConfiguration;
import twitter4j.http.HttpClientFactory;

public class ApacheHttpClientFactory implements HttpClientFactory {

	private final Context context;

	public ApacheHttpClientFactory(final Context context) {
		this.context = context;
	}

	@Override
	public HttpClient getInstance(final HttpClientConfiguration conf) {
		return new ApacheHttpClientImpl(context, conf);
	}

}

package org.mariotaku.twidere.util.net;

import org.mariotaku.twidere.app.TwidereApplication;

import twitter4j.http.HostAddressResolver;
import twitter4j.http.HostAddressResolverFactory;
import twitter4j.http.HttpClientConfiguration;

public class TwidereHostResolverFactory implements HostAddressResolverFactory {

	private final TwidereApplication mApplication;

	public TwidereHostResolverFactory(final TwidereApplication application) {
		mApplication = application;
	}

	@Override
	public HostAddressResolver getInstance(final HttpClientConfiguration conf) {
		return mApplication.getHostAddressResolver();
	}

}

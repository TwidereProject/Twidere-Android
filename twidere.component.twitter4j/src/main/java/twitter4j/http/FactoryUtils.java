package twitter4j.http;

public class FactoryUtils {
	public static HostAddressResolver getHostAddressResolver(final HttpClientConfiguration conf) {
		final HostAddressResolverFactory factory = conf.getHostAddressResolverFactory();
		if (factory == null) return null;
		return factory.getInstance(conf);
	}

	public static HttpClient getHttpClient(final HttpClientConfiguration conf) {
		final HttpClientFactory factory = conf.getHttpClientFactory();
		if (factory == null) return new HttpClientImpl(conf);
		final HttpClient client = factory.getInstance(conf);
		if (client == null) return new HttpClientImpl(conf);
		return client;
	}
}

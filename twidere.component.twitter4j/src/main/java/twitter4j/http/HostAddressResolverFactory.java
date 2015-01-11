package twitter4j.http;

public interface HostAddressResolverFactory {

	public HostAddressResolver getInstance(HttpClientConfiguration conf);

}

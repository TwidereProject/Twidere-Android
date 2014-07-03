package twitter4j.http;

import twitter4j.internal.logging.Logger;

import java.io.DataOutputStream;
import java.io.IOException;

public class HttpClientBase {

	private static final Logger logger = Logger.getLogger(HttpClientBase.class);
	protected final HttpClientConfiguration CONF;

	public HttpClientBase(final HttpClientConfiguration conf) {
		CONF = conf;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof HttpClientBase)) return false;

		final HttpClientBase that = (HttpClientBase) o;

		if (!CONF.equals(that.CONF)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return CONF.hashCode();
	}

	public void shutdown() {
	}

	@Override
	public String toString() {
		return "HttpClientBase{" + "CONF=" + CONF + '}';
	}

	public void write(final DataOutputStream out, final String outStr) throws IOException {
		out.writeBytes(outStr);
		logger.debug(outStr);
	}

	protected boolean isProxyConfigured() {
		return CONF.getHttpProxyHost() != null && !CONF.getHttpProxyHost().equals("");
	}
}

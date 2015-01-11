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

package twitter4j.http;

import java.io.DataOutputStream;
import java.io.IOException;

import twitter4j.internal.logging.Logger;

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

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

import java.util.Arrays;
import java.util.Map;

import twitter4j.auth.Authorization;

/**
 * HTTP Request parameter object
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class HttpRequest {

	private final RequestMethod method;

	private final String url, signUrl;

	private final HttpParameter[] parameters;

	private final Authorization authorization;

	private final Map<String, String> requestHeaders;

	private static final HttpParameter[] NULL_PARAMETERS = new HttpParameter[0];

	/**
	 * @param method Specifies the HTTP method
	 * @param url the request to request
	 * @param parameters parameters
	 * @param authorization Authentication implementation. Currently
	 *            BasicAuthentication, OAuthAuthentication and
	 *            NullAuthentication are supported.
	 * @param requestHeaders
	 */
	public HttpRequest(final RequestMethod method, final String url, final String signUrl,
			final HttpParameter[] parameters, final Authorization authorization,
			final Map<String, String> requestHeaders) {
		this.method = method;
		if (method != RequestMethod.POST && parameters != null && parameters.length != 0) {
			final String paramString = HttpParameter.encodeParameters(parameters);
			this.url = url + "?" + paramString;
			this.signUrl = signUrl + "?" + paramString;
			this.parameters = NULL_PARAMETERS;
		} else {
			this.url = url;
			this.signUrl = signUrl;
			this.parameters = parameters;
		}
		this.authorization = authorization;
		this.requestHeaders = requestHeaders;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final HttpRequest that = (HttpRequest) o;

		if (authorization != null ? !authorization.equals(that.authorization) : that.authorization != null)
			return false;
		if (!Arrays.equals(parameters, that.parameters)) return false;
		if (requestHeaders != null ? !requestHeaders.equals(that.requestHeaders) : that.requestHeaders != null)
			return false;
		if (method != null ? !method.equals(that.method) : that.method != null) return false;
		if (url != null ? !url.equals(that.url) : that.url != null) return false;

		return true;
	}

	public Authorization getAuthorization() {
		return authorization;
	}

	public RequestMethod getMethod() {
		return method;
	}

	public HttpParameter[] getParameters() {
		return parameters;
	}

	public Map<String, String> getRequestHeaders() {
		return requestHeaders;
	}

	public String getSignURL() {
		return signUrl != null ? signUrl : url;
	}

	public String getURL() {
		return url;
	}

	@Override
	public int hashCode() {
		int result = method != null ? method.hashCode() : 0;
		result = 31 * result + (url != null ? url.hashCode() : 0);
		result = 31 * result + (signUrl != null ? signUrl.hashCode() : 0);
		result = 31 * result + (parameters != null ? Arrays.hashCode(parameters) : 0);
		result = 31 * result + (authorization != null ? authorization.hashCode() : 0);
		result = 31 * result + (requestHeaders != null ? requestHeaders.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "HttpRequest{" + "requestMethod=" + method + ", url='" + url + '\'' + ", signUrl='" + signUrl + '\''
				+ ", postParams=" + (parameters == null ? null : Arrays.asList(parameters)) + ", authentication="
				+ authorization + ", requestHeaders=" + requestHeaders + '}';
	}
}

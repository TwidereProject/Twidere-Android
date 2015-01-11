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

import java.util.HashMap;
import java.util.Map;

import twitter4j.TwitterException;
import twitter4j.auth.Authorization;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;

import static twitter4j.http.RequestMethod.DELETE;
import static twitter4j.http.RequestMethod.GET;
import static twitter4j.http.RequestMethod.HEAD;
import static twitter4j.http.RequestMethod.POST;
import static twitter4j.http.RequestMethod.PUT;

/**
 * HTTP Client wrapper with handy request methods, ResponseListener mechanism
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class HttpClientWrapper {
	private final Configuration wrapperConf;
	private final HttpClient http;

	private final Map<String, String> requestHeaders;

	private HttpResponseListener httpResponseListener;

	// never used with this project. Just for handiness for those using this
	// class.
	public HttpClientWrapper() {
		wrapperConf = ConfigurationContext.getInstance();
		requestHeaders = wrapperConf.getRequestHeaders();
		http = FactoryUtils.getHttpClient(wrapperConf);
	}

	public HttpClientWrapper(final Configuration wrapperConf) {
		this.wrapperConf = wrapperConf;
		requestHeaders = wrapperConf.getRequestHeaders();
		http = FactoryUtils.getHttpClient(wrapperConf);
	}

	public HttpResponse delete(final String url, final String signUrl) throws TwitterException {
		return delete(url, signUrl, null, null);
	}

	public HttpResponse delete(final String url, final String signUrl, final Authorization authorization)
			throws TwitterException {
		return delete(url, signUrl, null, authorization);
	}

	public HttpResponse delete(final String url, final String signUrl, final HttpParameter[] parameters)
			throws TwitterException {
		return delete(url, signUrl, parameters, null);
	}

	public HttpResponse delete(final String url, final String signUrl, final HttpParameter[] parameters,
			final Authorization authorization) throws TwitterException {
		return request(new HttpRequest(DELETE, url, signUrl, parameters, authorization, requestHeaders));
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final HttpClientWrapper that = (HttpClientWrapper) o;

		if (!http.equals(that.http)) return false;
		if (!requestHeaders.equals(that.requestHeaders)) return false;
		if (!wrapperConf.equals(that.wrapperConf)) return false;

		return true;
	}

	public HttpResponse get(final String url, final String signUrl) throws TwitterException {
		return get(url, signUrl, null, null);
	}

	public HttpResponse get(final String url, final String signUrl, final Authorization authorization)
			throws TwitterException {
		return get(url, signUrl, null, authorization);
	}

	public HttpResponse get(final String url, final String signUrl, final HttpParameter[] parameters)
			throws TwitterException {
		return get(url, signUrl, parameters, null);
	}

	public HttpResponse get(final String url, final String signUrl, final HttpParameter[] parameters,
			final Authorization authorization) throws TwitterException {
		return request(new HttpRequest(GET, url, signUrl, parameters, authorization, requestHeaders));
	}

	@Override
	public int hashCode() {
		int result = wrapperConf.hashCode();
		result = 31 * result + http.hashCode();
		result = 31 * result + requestHeaders.hashCode();
		return result;
	}

	public HttpResponse head(final String url, final String signUrl) throws TwitterException {
		return head(url, signUrl, null, null);
	}

	public HttpResponse head(final String url, final String signUrl, final Authorization authorization)
			throws TwitterException {
		return head(url, signUrl, null, authorization);
	}

	public HttpResponse head(final String url, final String signUrl, final HttpParameter[] parameters)
			throws TwitterException {
		return head(url, signUrl, parameters, null);
	}

	public HttpResponse head(final String url, final String signUrl, final HttpParameter[] parameters,
			final Authorization authorization) throws TwitterException {
		return request(new HttpRequest(HEAD, url, signUrl, parameters, authorization, requestHeaders));
	}

	public HttpResponse post(final String url, final String signUrl) throws TwitterException {
		return post(url, signUrl, null, null, null);
	}

	public HttpResponse post(final String url, final String signUrl, final Authorization authorization)
			throws TwitterException {
		return post(url, signUrl, null, authorization, null);
	}

	public HttpResponse post(final String url, final String signUrl, final HttpParameter[] parameters)
			throws TwitterException {
		return post(url, signUrl, parameters, null, null);
	}

	public HttpResponse post(final String url, final String signUrl, final HttpParameter[] parameters,
			final Authorization authorization) throws TwitterException {
		return post(url, signUrl, parameters, authorization, null);
	}

	public HttpResponse post(final String url, final String signUrl, final HttpParameter[] parameters,
			final Authorization authorization, final Map<String, String> requestHeaders) throws TwitterException {
		final Map<String, String> headers = new HashMap<String, String>(this.requestHeaders);
		if (requestHeaders != null) {
			headers.putAll(requestHeaders);
		}
		return request(new HttpRequest(POST, url, signUrl, parameters, authorization, headers));
	}

	public HttpResponse post(final String url, final String signUrl, final HttpParameter[] parameters,
			final Map<String, String> requestHeaders) throws TwitterException {
		return post(url, signUrl, parameters, null, requestHeaders);
	}

	public HttpResponse put(final String url, final String signUrl) throws TwitterException {
		return put(url, signUrl, null, null);
	}

	public HttpResponse put(final String url, final String signUrl, final Authorization authorization)
			throws TwitterException {
		return put(url, signUrl, null, authorization);
	}

	public HttpResponse put(final String url, final String signUrl, final HttpParameter[] parameters)
			throws TwitterException {
		return put(url, signUrl, parameters, null);
	}

	public HttpResponse put(final String url, final String signUrl, final HttpParameter[] parameters,
			final Authorization authorization) throws TwitterException {
		return request(new HttpRequest(PUT, url, signUrl, parameters, authorization, requestHeaders));
	}

	public void setHttpResponseListener(final HttpResponseListener listener) {
		httpResponseListener = listener;
	}

	public void shutdown() {
		http.shutdown();
	}

	@Override
	public String toString() {
		return "HttpClientWrapper{" + "wrapperConf=" + wrapperConf + ", http=" + http + ", requestHeaders="
				+ requestHeaders + ", httpResponseListener=" + httpResponseListener + '}';
	}

	private HttpResponse request(final HttpRequest req) throws TwitterException {
		HttpResponse res;
		try {
			res = http.request(req);
			// fire HttpResponseEvent
			if (httpResponseListener != null) {
				httpResponseListener.httpResponseReceived(new HttpResponseEvent(req, res, null));
			}
		} catch (final TwitterException te) {
			if (httpResponseListener != null) {
				httpResponseListener.httpResponseReceived(new HttpResponseEvent(req, null, te));
			}
			throw te;
		}
		return res;
	}
}

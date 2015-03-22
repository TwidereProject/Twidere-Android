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
import twitter4j.conf.ConfigurationContext;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.2
 */
public class HttpClientImpl extends HttpClientBase implements HttpClient, HttpResponseCode {

    private static final Map<HttpClientConfiguration, HttpClient> instanceMap = new HashMap<>(
            1);

    public HttpClientImpl() {
        super(ConfigurationContext.getInstance());
    }

    public HttpClientImpl(final HttpClientConfiguration conf) {
        super(conf);
    }

    public HttpResponse get(final String url, final String sign_url) throws TwitterException {
        return request(new HttpRequest(RequestMethod.GET, url, sign_url, null, null, null));
    }

    public HttpResponse post(final String url, final String sign_url, final HttpParameter[] params)
            throws TwitterException {
        return request(new HttpRequest(RequestMethod.POST, url, sign_url, params, null, null));
    }

    @Override
    public HttpResponse request(final HttpRequest req) throws TwitterException {
        throw new UnsupportedOperationException();
    }

    public static HttpClient getInstance(final HttpClientConfiguration conf) {
        HttpClient client = instanceMap.get(conf);
        if (null == client) {
            client = new HttpClientImpl(conf);
            instanceMap.put(conf, client);
        }
        return client;
    }

}

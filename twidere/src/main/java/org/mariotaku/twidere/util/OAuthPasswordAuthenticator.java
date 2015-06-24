/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util;

import android.text.TextUtils;
import android.util.Pair;
import android.util.Xml;

import com.nostra13.universalimageloader.utils.IoUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.restfu.RestAPIFactory;
import org.mariotaku.restfu.RestClient;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.http.RestHttpRequest;
import org.mariotaku.restfu.http.RestHttpResponse;
import org.mariotaku.restfu.http.mime.BaseTypedData;
import org.mariotaku.restfu.http.mime.FormTypedBody;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.TwitterOAuth;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;
import org.mariotaku.twidere.model.RequestType;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.Reader;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

public class OAuthPasswordAuthenticator implements Constants {

    private static final String INPUT_AUTHENTICITY_TOKEN = "authenticity_token";
    private static final String INPUT_REDIRECT_AFTER_LOGIN = "redirect_after_login";

    private final TwitterOAuth oauth;
    private final RestHttpClient client;
    private final Endpoint endpoint;

    public OAuthPasswordAuthenticator(final TwitterOAuth oauth) {
        final RestClient restClient = RestAPIFactory.getRestClient(oauth);
        this.oauth = oauth;
        this.client = restClient.getRestClient();
        this.endpoint = restClient.getEndpoint();
    }

    public OAuthToken getOAuthAccessToken(final String username, final String password) throws AuthenticationException {
        final OAuthToken requestToken;
        try {
            requestToken = oauth.getRequestToken(OAUTH_CALLBACK_OOB);
        } catch (final TwitterException e) {
            if (e.isCausedByNetworkIssue()) throw new AuthenticationException(e);
            throw new AuthenticityTokenException(e);
        }
        RestHttpResponse authorizePage = null, authorizeResult = null;
        try {
            final String oauthToken = requestToken.getOauthToken();
            final HashMap<String, String> inputMap = new HashMap<>();
            final RestHttpRequest.Builder authorizePageBuilder = new RestHttpRequest.Builder();
            authorizePageBuilder.method(GET.METHOD);
            authorizePageBuilder.url(endpoint.construct("/oauth/authorize", Pair.create("oauth_token",
                    requestToken.getOauthToken())));
            authorizePageBuilder.extra(RequestType.API);
            final RestHttpRequest authorizePageRequest = authorizePageBuilder.build();
            authorizePage = client.execute(authorizePageRequest);
            final String[] cookieHeaders = authorizePage.getHeaders("Set-Cookie");
            readInputFromHtml(BaseTypedData.reader(authorizePage.getBody()), inputMap,
                    INPUT_AUTHENTICITY_TOKEN, INPUT_REDIRECT_AFTER_LOGIN);
            final List<Pair<String, String>> params = new ArrayList<>();
            params.add(Pair.create("oauth_token", oauthToken));
            params.add(Pair.create(INPUT_AUTHENTICITY_TOKEN, inputMap.get(INPUT_AUTHENTICITY_TOKEN)));
            if (inputMap.containsKey(INPUT_REDIRECT_AFTER_LOGIN)) {
                params.add(Pair.create(INPUT_REDIRECT_AFTER_LOGIN, inputMap.get(INPUT_REDIRECT_AFTER_LOGIN)));
            }
            params.add(Pair.create("session[username_or_email]", username));
            params.add(Pair.create("session[password]", password));
            final FormTypedBody authorizationResultBody = new FormTypedBody(params);
            final ArrayList<Pair<String, String>> requestHeaders = new ArrayList<>();
            requestHeaders.add(Pair.create("Origin", "https://twitter.com"));
            requestHeaders.add(Pair.create("Referer", Endpoint.constructUrl("https://twitter.com/oauth/authorize",
                    Pair.create("oauth_token", requestToken.getOauthToken()))));

            final String host = parseUrlHost(endpoint.getUrl());
            for (String cookieHeader : cookieHeaders) {
                for (HttpCookie cookie : HttpCookie.parse(cookieHeader)) {
                    if (HttpCookie.domainMatches(cookie.getDomain(), host)) {
                        cookie.setVersion(1);
                        cookie.setDomain("twitter.com");
                    }
                    requestHeaders.add(Pair.create("Cookie", cookie.toString()));
                }
            }
            final RestHttpRequest.Builder authorizeResultBuilder = new RestHttpRequest.Builder();
            authorizeResultBuilder.method(POST.METHOD);
            authorizeResultBuilder.url(endpoint.construct("/oauth/authorize"));
            authorizeResultBuilder.headers(requestHeaders);
            authorizeResultBuilder.body(authorizationResultBody);
            authorizeResultBuilder.extra(RequestType.API);
            authorizeResult = client.execute(authorizeResultBuilder.build());
            final String oauthPin = readOAuthPINFromHtml(BaseTypedData.reader(authorizeResult.getBody()));
            if (isEmpty(oauthPin)) throw new WrongUserPassException();
            return oauth.getAccessToken(requestToken, oauthPin);
        } catch (final IOException | NullPointerException | XmlPullParserException | TwitterException e) {
            throw new AuthenticationException(e);
        } finally {
            if (authorizePage != null) {
                IoUtils.closeSilently(authorizePage);
            }
            if (authorizeResult != null) {
                IoUtils.closeSilently(authorizeResult);
            }
        }
    }

    private static void readInputFromHtml(final Reader in, Map<String, String> map, String... desiredNames) throws IOException, XmlPullParserException {
        final XmlPullParserFactory f = XmlPullParserFactory.newInstance();
        final XmlPullParser parser = f.newPullParser();
        parser.setFeature(Xml.FEATURE_RELAXED, true);
        parser.setInput(in);
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            final String tag = parser.getName();
            switch (parser.getEventType()) {
                case XmlPullParser.START_TAG: {
                    final String name = parser.getAttributeValue(null, "name");
                    if ("input".equalsIgnoreCase(tag) && ArrayUtils.contains(desiredNames, name)) {
                        map.put(name, parser.getAttributeValue(null, "value"));
                    }
                    break;
                }
            }
        }
    }

    public static String readOAuthPINFromHtml(final Reader in) throws XmlPullParserException, IOException {
        boolean start_div = false, start_code = false;
        final XmlPullParserFactory f = XmlPullParserFactory.newInstance();
        final XmlPullParser parser = f.newPullParser();
        parser.setFeature(Xml.FEATURE_RELAXED, true);
        parser.setInput(in);
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            final String tag = parser.getName();
            final int type = parser.getEventType();
            if (type == XmlPullParser.START_TAG) {
                if ("div".equalsIgnoreCase(tag)) {
                    start_div = "oauth_pin".equals(parser.getAttributeValue(null, "id"));
                } else if ("code".equalsIgnoreCase(tag)) {
                    if (start_div) {
                        start_code = true;
                    }
                }
            } else if (type == XmlPullParser.END_TAG) {
                if ("div".equalsIgnoreCase(tag)) {
                    start_div = false;
                } else if ("code".equalsIgnoreCase(tag)) {
                    start_code = false;
                }
            } else if (type == XmlPullParser.TEXT) {
                final String text = parser.getText();
                if (start_code && !TextUtils.isEmpty(text) && TextUtils.isDigitsOnly(text))
                    return text;
            }
        }
        return null;
    }

    private static String parseUrlHost(String url) {
        final int startOfHost = url.indexOf("://") + 3, endOfHost = url.indexOf('/', startOfHost);
        return url.substring(startOfHost, endOfHost);
    }

    public static class AuthenticationException extends Exception {

        private static final long serialVersionUID = -5629194721838256378L;

        AuthenticationException() {
        }

        AuthenticationException(final Exception cause) {
            super(cause);
        }

        AuthenticationException(final String message) {
            super(message);
        }
    }

    public static final class AuthenticityTokenException extends AuthenticationException {

        private static final long serialVersionUID = -1840298989316218380L;

        public AuthenticityTokenException(Exception e) {
            super(e);
        }
    }

    public static final class WrongUserPassException extends AuthenticationException {

        private static final long serialVersionUID = -4880737459768513029L;

    }

}

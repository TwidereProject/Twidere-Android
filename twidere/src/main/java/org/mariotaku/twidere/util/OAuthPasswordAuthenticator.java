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
import android.util.Xml;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.Constants;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.http.HeaderMap;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpParameter;
import twitter4j.http.HttpResponse;

import static android.text.TextUtils.isEmpty;

public class OAuthPasswordAuthenticator implements Constants {

    private static final String INPUT_AUTHENTICITY_TOKEN = "authenticity_token";
    private static final String INPUT_REDIRECT_AFTER_LOGIN = "redirect_after_login";

    private final Twitter twitter;
    private final HttpClientWrapper client;

    public OAuthPasswordAuthenticator(final Twitter twitter) {
        final Configuration conf = twitter.getConfiguration();
        this.twitter = twitter;
        client = new HttpClientWrapper(conf);
    }

    public AccessToken getOAuthAccessToken(final String username, final String password) throws AuthenticationException {
        final RequestToken requestToken;
        try {
            requestToken = twitter.getOAuthRequestToken(OAUTH_CALLBACK_OOB);
        } catch (final TwitterException e) {
            if (e.isCausedByNetworkIssue()) throw new AuthenticationException(e);
            throw new AuthenticityTokenException();
        }
        try {
            final String oauthToken = requestToken.getToken();
            final String authorizationUrl = requestToken.getAuthorizationURL();
            final HashMap<String, String> inputMap = new HashMap<>();
            final HttpResponse authorizePage = client.get(authorizationUrl, authorizationUrl, null, null, null);
            final List<String> cookieHeaders = authorizePage.getResponseHeaders("Set-Cookie");
            readInputFromHtml(authorizePage.asReader(),
                    inputMap, INPUT_AUTHENTICITY_TOKEN, INPUT_REDIRECT_AFTER_LOGIN);
            final Configuration conf = twitter.getConfiguration();
            final List<HttpParameter> params = new ArrayList<>();
            params.add(new HttpParameter("oauth_token", oauthToken));
            params.add(new HttpParameter(INPUT_AUTHENTICITY_TOKEN, inputMap.get(INPUT_AUTHENTICITY_TOKEN)));
            if (inputMap.containsKey(INPUT_REDIRECT_AFTER_LOGIN)) {
                params.add(new HttpParameter(INPUT_REDIRECT_AFTER_LOGIN, inputMap.get(INPUT_REDIRECT_AFTER_LOGIN)));
            }
            params.add(new HttpParameter("session[username_or_email]", username));
            params.add(new HttpParameter("session[password]", password));
            final HeaderMap requestHeaders = new HeaderMap();
            requestHeaders.addHeader("Origin", "https://twitter.com");
            requestHeaders.addHeader("Referer", "https://twitter.com/oauth/authorize?oauth_token=" + requestToken.getToken());
            requestHeaders.put("Cookie", cookieHeaders);
            final String oAuthAuthorizationUrl = conf.getOAuthAuthorizationURL();
            final String oauthPin = readOAuthPINFromHtml(client.post(oAuthAuthorizationUrl, oAuthAuthorizationUrl,
                    params.toArray(new HttpParameter[params.size()]), requestHeaders).asReader());
            if (isEmpty(oauthPin)) throw new WrongUserPassException();
            return twitter.getOAuthAccessToken(requestToken, oauthPin);
        } catch (final IOException | TwitterException | NullPointerException | XmlPullParserException e) {
            throw new AuthenticationException(e);
        }
    }

    public static void readInputFromHtml(final Reader in, Map<String, String> map, String... desiredNames) throws IOException, XmlPullParserException {
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

    }

    public static final class WrongUserPassException extends AuthenticationException {

        private static final long serialVersionUID = -4880737459768513029L;

    }

}

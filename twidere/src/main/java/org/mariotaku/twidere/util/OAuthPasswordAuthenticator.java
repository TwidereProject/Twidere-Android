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

import static android.text.TextUtils.isEmpty;

import android.text.TextUtils;
import android.util.Xml;

import org.mariotaku.twidere.Constants;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpParameter;

import java.io.IOException;
import java.io.Reader;

public class OAuthPasswordAuthenticator implements Constants {

	private final Twitter twitter;
	private final HttpClientWrapper client;

	private static final String REFRESH_URL_PREFIX = "url=";

	public OAuthPasswordAuthenticator(final Twitter twitter) {
		final Configuration conf = twitter.getConfiguration();
		this.twitter = twitter;
		client = new HttpClientWrapper(conf);
	}

	public AccessToken getOAuthAccessToken(final String username, final String password) throws AuthenticationException {
		if (twitter == null) return null;
		final RequestToken requestToken;
		try {
			requestToken = twitter.getOAuthRequestToken(OAUTH_CALLBACK_OOB);
		} catch (final TwitterException e) {
			if (e.isCausedByNetworkIssue()) throw new AuthenticationException(e);
			throw new AuthenticityTokenException();
		}
		try {
			final String oauthToken = requestToken.getToken();
			final String authorizationUrl = requestToken.getAuthorizationURL().toString();
			final String authenticityToken = readAuthenticityTokenFromHtml(client.get(authorizationUrl,
					authorizationUrl, null, null).asReader());
			if (authenticityToken == null) throw new AuthenticityTokenException();
			final Configuration conf = twitter.getConfiguration();
			final HttpParameter[] params = new HttpParameter[4];
			params[0] = new HttpParameter("authenticity_token", authenticityToken);
			params[1] = new HttpParameter("oauth_token", oauthToken);
			params[2] = new HttpParameter("session[username_or_email]", username);
			params[3] = new HttpParameter("session[password]", password);
			final String oAuthAuthorizationUrl = conf.getOAuthAuthorizationURL().toString();
			final String oauthPin = readOAuthPINFromHtml(client.post(oAuthAuthorizationUrl, oAuthAuthorizationUrl,
					params).asReader());
			if (isEmpty(oauthPin)) throw new WrongUserPassException();
			return twitter.getOAuthAccessToken(requestToken, oauthPin);
		} catch (final IOException e) {
			throw new AuthenticationException(e);
		} catch (final TwitterException e) {
			throw new AuthenticationException(e);
		} catch (final NullPointerException e) {
			throw new AuthenticationException(e);
		} catch (final XmlPullParserException e) {
			throw new AuthenticationException(e);
		}
	}

	public static String readAuthenticityTokenFromHtml(final Reader in) throws IOException, XmlPullParserException {
		final XmlPullParserFactory f = XmlPullParserFactory.newInstance();
		final XmlPullParser parser = f.newPullParser();
		parser.setFeature(Xml.FEATURE_RELAXED, true);
		parser.setInput(in);
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			final String tag = parser.getName();
			switch (parser.getEventType()) {
				case XmlPullParser.START_TAG: {
					if ("input".equals(tag) && "authenticity_token".equals(parser.getAttributeValue(null, "name")))
						return parser.getAttributeValue(null, "value");
				}
			}
		}
		return null;
	}

	public static String readCallbackUrlFromHtml(final Reader in) throws IOException, XmlPullParserException {
		final XmlPullParserFactory f = XmlPullParserFactory.newInstance();
		final XmlPullParser parser = f.newPullParser();
		parser.setFeature(Xml.FEATURE_RELAXED, true);
		parser.setInput(in);
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			final String tag = parser.getName();
			switch (parser.getEventType()) {
				case XmlPullParser.START_TAG: {
					if ("meta".equals(tag) && "refresh".equals(parser.getAttributeValue(null, "http-equiv"))) {
						final String content = parser.getAttributeValue(null, "content");
						int idx;
						if (!TextUtils.isEmpty(content) && (idx = content.indexOf(REFRESH_URL_PREFIX)) != -1) {
							final String url = content.substring(idx + REFRESH_URL_PREFIX.length());
							if (!TextUtils.isEmpty(url)) return url;
						}
					}
				}
			}
		}
		return null;
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
				if (start_code && !TextUtils.isEmpty(text) && TextUtils.isDigitsOnly(text)) return text;
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

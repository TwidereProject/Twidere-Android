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

package twitter4j.auth;

import javax.crypto.spec.SecretKeySpec;

import twitter4j.TwitterException;
import twitter4j.http.HttpResponse;
import twitter4j.internal.util.InternalStringUtil;

abstract class OAuthToken {

	private final String token;
	private final String tokenSecret;

	private transient SecretKeySpec secretKeySpec;
	String[] responseStr = null;

	public OAuthToken(final String token, final String tokenSecret) {
		this.token = token;
		this.tokenSecret = tokenSecret;
	}

	OAuthToken(final HttpResponse response) throws TwitterException {
		this(response.asString());
	}

	OAuthToken(final String string) {
		responseStr = InternalStringUtil.split(string, "&");
		tokenSecret = getParameter("oauth_token_secret");
		token = getParameter("oauth_token");
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof OAuthToken)) return false;

		final OAuthToken that = (OAuthToken) o;

		if (!token.equals(that.token)) return false;
		if (!tokenSecret.equals(that.tokenSecret)) return false;

		return true;
	}

	public String getParameter(final String parameter) {
		String value = null;
		for (final String str : responseStr) {
			if (str.startsWith(parameter + '=')) {
				value = InternalStringUtil.split(str, "=")[1].trim();
				break;
			}
		}
		return value;
	}

	public String getToken() {
		return token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	@Override
	public int hashCode() {
		int result = token.hashCode();
		result = 31 * result + tokenSecret.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "OAuthToken{" + "token='" + token + '\'' + ", tokenSecret='" + tokenSecret + '\'' + ", secretKeySpec="
				+ secretKeySpec + '}';
	}

	/* package */SecretKeySpec getSecretKeySpec() {
		return secretKeySpec;
	}

	/* package */void setSecretKeySpec(final SecretKeySpec secretKeySpec) {
		this.secretKeySpec = secretKeySpec;
	}
}

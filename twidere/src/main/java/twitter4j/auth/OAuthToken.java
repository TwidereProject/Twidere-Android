/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j.auth;

import twitter4j.TwitterException;
import twitter4j.http.HttpResponse;
import twitter4j.internal.util.InternalStringUtil;

import javax.crypto.spec.SecretKeySpec;

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

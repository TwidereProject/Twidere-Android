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

package org.mariotaku.twidere.api.twitter.auth;

import android.util.Base64;

import org.mariotaku.simplerestapi.RestMethod;
import org.mariotaku.simplerestapi.RestMethodInfo;
import org.mariotaku.simplerestapi.Utils;
import org.mariotaku.simplerestapi.http.Authorization;
import org.mariotaku.simplerestapi.http.Endpoint;
import org.mariotaku.simplerestapi.http.KeyValuePair;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by mariotaku on 15/2/4.
 */
public class OAuthAuthorization implements Authorization {

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String OAUTH_SIGNATURE_METHOD = "HMAC-SHA1";
    private static final String OAUTH_VERSION = "1.0";

    SecureRandom secureRandom = new SecureRandom();

    private final String consumerKey, consumerSecret;
    private final OAuthToken oauthToken;

    public OAuthAuthorization(String consumerKey, String consumerSecret) {
        this(consumerKey, consumerSecret, null);
    }

    public OAuthAuthorization(String consumerKey, String consumerSecret, OAuthToken oauthToken) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.oauthToken = oauthToken;
    }

    private String generateOAuthSignature(RestMethod method, String url,
                                          String oauthNonce, long timestamp,
                                          String oauthToken, String oauthTokenSecret,
                                          List<KeyValuePair> queries,
                                          List<KeyValuePair> forms) {
        final List<String> encodeParams = new ArrayList<>();
        encodeParams.add(encodeParameter("oauth_consumer_key", consumerKey));
        encodeParams.add(encodeParameter("oauth_nonce", oauthNonce));
        encodeParams.add(encodeParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD));
        encodeParams.add(encodeParameter("oauth_timestamp", String.valueOf(timestamp)));
        encodeParams.add(encodeParameter("oauth_version", OAUTH_VERSION));
        if (oauthToken != null) {
            encodeParams.add(encodeParameter("oauth_token", oauthToken));
        }
        if (queries != null) {
            for (KeyValuePair query : queries) {
                encodeParams.add(encodeParameter(query.getKey(), query.getValue()));
            }
        }
        if (forms != null) {
            for (KeyValuePair form : forms) {
                encodeParams.add(encodeParameter(form.getKey(), form.getValue()));
            }
        }
        Collections.sort(encodeParams);
        final StringBuilder paramBuilder = new StringBuilder();
        for (int i = 0, j = encodeParams.size(); i < j; i++) {
            if (i != 0) {
                paramBuilder.append('&');
            }
            paramBuilder.append(encodeParams.get(i));
        }
        final String signingKey;
        if (oauthTokenSecret != null) {
            signingKey = encode(consumerSecret) + '&' + encode(oauthTokenSecret);
        } else {
            signingKey = encode(consumerSecret) + '&';
        }
        try {
            final Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(signingKey.getBytes(), mac.getAlgorithm());
            mac.init(secret);
            String urlNoQuery = url.indexOf('?') != -1 ? url.substring(0, url.indexOf('?')) : url;
            final String baseString = encode(method.value()) + '&' + encode(urlNoQuery) + '&' + encode(paramBuilder.toString());
            final byte[] signature = mac.doFinal(baseString.getBytes(DEFAULT_ENCODING));
            return Base64.encodeToString(signature, Base64.URL_SAFE);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        } catch (InvalidKeyException | UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public String getHeader(Endpoint endpoint, RestMethodInfo request) {
        if (!(endpoint instanceof OAuthEndpoint)) throw new IllegalArgumentException();
        final OAuthEndpoint oauthEndpoint = (OAuthEndpoint) endpoint;
        final RestMethod method = request.getMethod();
        final String url = Endpoint.constructUrl(oauthEndpoint.getSignUrl(), request);
        final String oauthNonce = generateOAuthNonce();
        final long timestamp = System.currentTimeMillis() / 1000;
        final Map<String, Object> extras = request.getExtras();
        final String oauthToken, oauthTokenSecret;
        if (this.oauthToken != null) {
            oauthToken = this.oauthToken.getOauthToken();
            oauthTokenSecret = this.oauthToken.getOauthTokenSecret();
        } else {
            oauthToken = (String) extras.get("oauth_token");
            oauthTokenSecret = (String) extras.get("oauth_token_secret");
        }
        final String oauthSignature = generateOAuthSignature(method, url, oauthNonce, timestamp, oauthToken,
                oauthTokenSecret, request.getQueries(), request.getForms());
        final List<KeyValuePair> encodeParams = new ArrayList<>();
        encodeParams.add(new KeyValuePair("oauth_consumer_key", consumerKey));
        encodeParams.add(new KeyValuePair("oauth_nonce", oauthNonce));
        encodeParams.add(new KeyValuePair("oauth_signature", encode(oauthSignature)));
        encodeParams.add(new KeyValuePair("oauth_signature_method", OAUTH_SIGNATURE_METHOD));
        encodeParams.add(new KeyValuePair("oauth_timestamp", String.valueOf(timestamp)));
        encodeParams.add(new KeyValuePair("oauth_version", OAUTH_VERSION));
        if (oauthToken != null) {
            encodeParams.add(new KeyValuePair("oauth_token", oauthToken));
        }
        Collections.sort(encodeParams);
        final StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append("OAuth ");
        for (int i = 0, j = encodeParams.size(); i < j; i++) {
            if (i != 0) {
                headerBuilder.append(", ");
            }
            final KeyValuePair keyValuePair = encodeParams.get(i);
            headerBuilder.append(keyValuePair.getKey());
            headerBuilder.append("=\"");
            headerBuilder.append(keyValuePair.getValue());
            headerBuilder.append('\"');
        }
        return headerBuilder.toString();
    }

    @Override
    public boolean hasAuthorization() {
        return true;
    }

    private String encodeParameter(String key, String value) {
        return encode(key) + '=' + encode(value);
    }

    private static String encode(final String value) {
        return Utils.encode(value, DEFAULT_ENCODING);
    }


    private String generateOAuthNonce() {
        final byte[] input = new byte[32];
        secureRandom.nextBytes(input);
        return Base64.encodeToString(input, Base64.URL_SAFE | Base64.NO_PADDING);
    }
}

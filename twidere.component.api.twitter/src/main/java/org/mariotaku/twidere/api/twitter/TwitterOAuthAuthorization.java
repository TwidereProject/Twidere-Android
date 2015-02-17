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

package org.mariotaku.twidere.api.twitter;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import retrofit.client.Request;
import retrofit.mime.TypedOutput;

/**
 * Created by mariotaku on 15/2/4.
 */
public class TwitterOAuthAuthorization implements Authorization {

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String OAUTH_SIGNATURE_METHOD = "HMAC-SHA1";
    private static final String OAUTH_VERSION = "1.0";

    SecureRandom secureRandom = new SecureRandom();

    private final String consumerKey, consumerSecret;
    private String oauthToken;
    private String oauthTokenSecret;

    public TwitterOAuthAuthorization(String consumerKey, String consumerSecret) {
        this(consumerKey, consumerSecret, null, null);
    }

    public TwitterOAuthAuthorization(String consumerKey, String consumerSecret, String oauthToken, String oauthTokenSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.oauthToken = oauthToken;
        this.oauthTokenSecret = oauthTokenSecret;
    }

    public void setAccessToken(String oauthToken, String oauthTokenSecret) {
        this.oauthToken = oauthToken;
        this.oauthTokenSecret = oauthTokenSecret;
    }

    private String generateOAuthSignature(String method, String url,
                                          String oauthNonce, long timestamp,
                                          List<KeyValuePair> queryParams) {
        final List<String> encodeParams = new ArrayList<>();
        encodeParams.add(encodeParameter("oauth_consumer_key", consumerKey));
        encodeParams.add(encodeParameter("oauth_nonce", oauthNonce));
        encodeParams.add(encodeParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD));
        encodeParams.add(encodeParameter("oauth_timestamp", String.valueOf(timestamp)));
        encodeParams.add(encodeParameter("oauth_version", OAUTH_VERSION));
        if (oauthToken != null) {
            encodeParams.add(encodeParameter("oauth_token", oauthToken));
        }
        if (queryParams != null) {
            for (KeyValuePair queryParam : queryParams) {
                encodeParams.add(encodeParameter(queryParam.getKey(), queryParam.getValue()));
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
            final String baseString = encode(method) + '&' + encode(url) + '&' + encode(paramBuilder.toString());
            final byte[] signature = mac.doFinal(baseString.getBytes(DEFAULT_ENCODING));
            return Base64.encodeToString(signature, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        } catch (InvalidKeyException | UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public String getHeader(Request request) {
        return getHeader(request.getMethod(), request.getUrl(), request.getBody());
    }

    public String getHeader(String method, String url, TypedOutput body) {
        final String oauthNonce = generateOAuthNonce();
        final long timestamp = System.currentTimeMillis() / 1000;
        final List<KeyValuePair> queryParams = new ArrayList<>();
        parseGetParametersFromUrl(url, queryParams, DEFAULT_ENCODING);
        if (body != null) {
            final ContentType contentType = ContentType.parse(body.mimeType());
            if ("application/x-www-form-urlencoded".equals(contentType.getContentType())) {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream((int) body.length());
                try {
                    body.writeTo(baos);
                    parseGetParameters(baos.toString(DEFAULT_ENCODING), queryParams, DEFAULT_ENCODING);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        baos.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
        final String oauthSignature = generateOAuthSignature(method, url, oauthNonce, timestamp, queryParams);
        final List<KeyValuePair> encodeParams = new ArrayList<>();
        encodeParams.add(new KeyValuePair("oauth_consumer_key", consumerKey));
        encodeParams.add(new KeyValuePair("oauth_signature_method", OAUTH_SIGNATURE_METHOD));
        encodeParams.add(new KeyValuePair("oauth_timestamp", String.valueOf(timestamp)));
        encodeParams.add(new KeyValuePair("oauth_nonce", oauthNonce));
        encodeParams.add(new KeyValuePair("oauth_version", OAUTH_VERSION));
        if (oauthToken != null) {
            encodeParams.add(new KeyValuePair("oauth_token", oauthToken));
        }
        encodeParams.add(new KeyValuePair("oauth_signature", encode(oauthSignature)));
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
        return encode(value, DEFAULT_ENCODING);
    }


    /**
     * @param value string to be encoded
     * @return encoded string
     * @see <a href="http://wiki.oauth.net/TestCases">OAuth / TestCases</a>
     * @see <a
     * href="http://groups.google.com/group/oauth/browse_thread/thread/a8398d0521f4ae3d/9d79b698ab217df2?hl=en&lnk=gst&q=space+encoding#9d79b698ab217df2">Space
     * encoding - OAuth | Google Groups</a>
     * @see <a href="http://tools.ietf.org/html/rfc3986#section-2.1">RFC 3986 -
     * Uniform Resource Identifier (URI): Generic Syntax - 2.1.
     * Percent-Encoding</a>
     */
    private static String encode(final String value, String encoding) {
        String encoded;
        try {
            encoded = URLEncoder.encode(value, encoding);
        } catch (final UnsupportedEncodingException ignore) {
            return null;
        }
        final StringBuilder buf = new StringBuilder(encoded.length());
        char focus;
        for (int i = 0; i < encoded.length(); i++) {
            focus = encoded.charAt(i);
            if (focus == '*') {
                buf.append("%2A");
            } else if (focus == '+') {
                buf.append("%20");
            } else if (focus == '%' && i + 1 < encoded.length() && encoded.charAt(i + 1) == '7'
                    && encoded.charAt(i + 2) == 'E') {
                buf.append('~');
                i += 2;
            } else {
                buf.append(focus);
            }
        }
        return buf.toString();
    }


    private void parseGetParametersFromUrl(final String url, final List<KeyValuePair> params,
                                           final String encoding) {
        final int queryStart = url.indexOf("?");
        if (-1 == queryStart) return;
        parseGetParameters(url.substring(queryStart + 1), params, encoding);
    }

    private void parseGetParameters(final String queryString, final List<KeyValuePair> params,
                                    final String encoding) {
        final String[] queryStrings = Utils.split(queryString, "&");
        try {
            for (final String query : queryStrings) {
                final String[] split = Utils.split(query, "=");
                final String key = URLDecoder.decode(split[0], encoding);
                if (split.length == 2) {
                    params.add(new KeyValuePair(key, URLDecoder.decode(split[1], encoding)));
                } else {
                    params.add(new KeyValuePair(key, ""));
                }
            }
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateOAuthNonce() {
        final byte[] input = new byte[32];
        secureRandom.nextBytes(input);
        return Base64.encodeToString(input, Base64.NO_WRAP).replaceAll("[^\\w]", "");
    }
}

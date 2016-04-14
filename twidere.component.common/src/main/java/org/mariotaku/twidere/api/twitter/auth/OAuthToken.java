/*
 *                 Twidere - Twitter client for Android
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


import org.mariotaku.restfu.RestConverter;
import org.mariotaku.restfu.RestFuUtils;
import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.ValueMap;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.twidere.api.twitter.TwitterException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;

/**
 * Created by mariotaku on 15/2/4.
 */
public class OAuthToken implements ValueMap {

    private static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
    private static final String OAUTH_TOKEN = "oauth_token";
    private String screenName;
    private String userId;

    private String oauthToken, oauthTokenSecret;

    public String getScreenName() {
        return screenName;
    }

    public String getUserId() {
        return userId;
    }

    public String getOauthTokenSecret() {
        return oauthTokenSecret;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public OAuthToken(String oauthToken, String oauthTokenSecret) {
        this.oauthToken = oauthToken;
        this.oauthTokenSecret = oauthTokenSecret;
    }

    public OAuthToken(String body, Charset charset) throws ParseException {
        RestFuUtils.parseQuery(body, charset.name(), new RestFuUtils.KeyValueConsumer() {

            @Override
            public void consume(String key, String value) {
                switch (key) {
                    case OAUTH_TOKEN: {
                        oauthToken = value;
                        break;
                    }
                    case OAUTH_TOKEN_SECRET: {
                        oauthTokenSecret = value;
                        break;
                    }
                    case "user_id": {
                        userId = value;
                        break;
                    }
                    case "screen_name": {
                        screenName = value;
                        break;
                    }
                }
            }
        });
        if (oauthToken == null || oauthTokenSecret == null) {
            throw new ParseException("Unable to parse request token", -1);
        }
    }

    @Override
    public boolean has(String key) {
        return OAUTH_TOKEN.equals(key) || OAUTH_TOKEN_SECRET.equals(key);
    }

    @Override
    public String toString() {
        return "OAuthToken{" +
                "screenName='" + screenName + '\'' +
                ", userId=" + userId +
                ", oauthToken='" + oauthToken + '\'' +
                ", oauthTokenSecret='" + oauthTokenSecret + '\'' +
                '}';
    }

    @Override
    public String get(String key) {
        if (OAUTH_TOKEN.equals(key)) {
            return oauthToken;
        } else if (OAUTH_TOKEN_SECRET.equals(key)) {
            return oauthTokenSecret;
        }
        return null;
    }

    @Override
    public String[] keys() {
        return new String[]{OAUTH_TOKEN, OAUTH_TOKEN_SECRET};
    }

    public static class ResponseConverter implements RestConverter<HttpResponse, OAuthToken, TwitterException> {
        @Override
        public OAuthToken convert(HttpResponse response) throws IOException, ConvertException {
            final Body body = response.getBody();
            try {
                final ContentType contentType = body.contentType();
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                body.writeTo(os);
                Charset charset = contentType != null ? contentType.getCharset() : null;
                if (charset == null) {
                    charset = Charset.defaultCharset();
                }
                try {
                    return new OAuthToken(os.toString(charset.name()), charset);
                } catch (ParseException e) {
                    throw new ConvertException(e);
                }
            } finally {
                RestFuUtils.closeSilently(body);
            }
        }
    }
}

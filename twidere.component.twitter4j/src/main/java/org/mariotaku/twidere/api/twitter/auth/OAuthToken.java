package org.mariotaku.twidere.api.twitter.auth;

import org.apache.commons.lang3.tuple.Pair;
import org.mariotaku.simplerestapi.Utils;
import org.mariotaku.simplerestapi.http.ValueMap;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariotaku on 15/2/4.
 */
public class OAuthToken implements ValueMap {

    private String screenName;
    private long userId;

    private String oauthToken, oauthTokenSecret;

    public String getScreenName() {
        return screenName;
    }

    public long getUserId() {
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
        List<Pair<String, String>> params = new ArrayList<>();
        Utils.parseGetParameters(body, params, charset.name());
        for (Pair<String, String> param : params) {
            switch (param.getKey()) {
                case "oauth_token": {
                    oauthToken = param.getValue();
                    break;
                }
                case "oauth_token_secret": {
                    oauthTokenSecret = param.getValue();
                    break;
                }
                case "user_id": {
                    userId = Long.parseLong(param.getValue());
                    break;
                }
                case "screen_name": {
                    screenName = param.getValue();
                    break;
                }
            }
        }
        if (oauthToken == null || oauthTokenSecret == null) {
            throw new ParseException("Unable to parse request token", -1);
        }
    }

    @Override
    public boolean has(String key) {
        return "oauth_token".equals(key) || "oauth_token_secret".equals(key);
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
        if ("oauth_token".equals(key)) {
            return oauthToken;
        } else if ("oauth_token_secret".equals(key)) {
            return oauthTokenSecret;
        }
        return null;
    }
}

package org.mariotaku.microblog.library.twitter.auth;

import android.util.Base64;

import org.mariotaku.restfu.http.HeaderValue;
import org.mariotaku.restfu.oauth.OAuthToken;

/**
 * Created by mariotaku on 16/1/4.
 */
public class OAuth2GetTokenHeader implements HeaderValue {

    private final OAuthToken token;

    public OAuth2GetTokenHeader(OAuthToken token) {
        this.token = token;
    }

    @Override
    public String toHeaderValue() {
        return "Basic " + Base64.encodeToString((token.getOauthToken() + ":"
                + token.getOauthTokenSecret()).getBytes(), Base64.NO_WRAP);
    }
}

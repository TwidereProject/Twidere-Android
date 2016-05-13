package org.mariotaku.microblog.library.twitter.auth;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/1/4.
 */
@JsonObject
public class OAuth2Token {
    @JsonField(name = "token_type")
    String tokenType;
    @JsonField(name = "access_token")
    String accessToken;

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }
}

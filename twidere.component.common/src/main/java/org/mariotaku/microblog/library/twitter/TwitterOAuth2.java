package org.mariotaku.microblog.library.twitter;

import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Header;
import org.mariotaku.restfu.annotation.param.KeyValue;
import org.mariotaku.restfu.annotation.param.Params;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.auth.OAuth2GetTokenHeader;
import org.mariotaku.microblog.library.twitter.auth.OAuth2Token;

/**
 * Created by mariotaku on 16/1/4.
 */
public interface TwitterOAuth2 {

    @POST("/oauth2/token")
    @Params(@KeyValue(key = "grant_type", value = "client_credentials"))
    OAuth2Token getApplicationOnlyAccessToken(@Header("Authorization") OAuth2GetTokenHeader token)
            throws MicroBlogException;
}

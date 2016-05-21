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

package org.mariotaku.microblog.library.twitter;


import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Extra;
import org.mariotaku.restfu.annotation.param.KeyValue;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Params;
import org.mariotaku.restfu.oauth.OAuthToken;

/**
 * Created by mariotaku on 15/2/4.
 */
public interface TwitterOAuth {

    @POST("/oauth/request_token")
    OAuthToken getRequestToken(@Param("oauth_callback") String oauthCallback) throws MicroBlogException;

    @POST("/oauth/access_token")
    @Params(@KeyValue(key = "x_auth_mode", value = "client_auth"))
    OAuthToken getAccessToken(@Param("x_auth_username") String xauthUsername,
                              @Param("x_auth_password") String xauthPassword) throws MicroBlogException;


    @POST("/oauth/access_token")
    OAuthToken getAccessToken(@Extra({"oauth_token", "oauth_token_secret"}) OAuthToken requestToken,
                              @Param("oauth_verifier") String oauthVerifier) throws MicroBlogException;

    @POST("/oauth/access_token")
    OAuthToken getAccessToken(@Extra({"oauth_token", "oauth_token_secret"}) OAuthToken requestToken)
            throws MicroBlogException;

}

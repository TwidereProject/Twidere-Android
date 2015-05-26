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

package org.mariotaku.twidere.api.twitter;


import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Body;
import org.mariotaku.restfu.annotation.param.Extra;
import org.mariotaku.restfu.annotation.param.Form;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;

/**
 * Created by mariotaku on 15/2/4.
 */
public interface TwitterOAuth {

    @POST("/oauth/request_token")
    @Body(BodyType.FORM)
    OAuthToken getRequestToken(@Form("oauth_callback") String oauthCallback) throws TwitterException;

    @POST("/oauth/access_token")
    @Body(BodyType.FORM)
    OAuthToken getAccessToken(@Form("x_auth_username") String xauthUsername,
                              @Form("x_auth_password") String xauthPassword,
                              @Form("x_auth_mode") XAuthMode xauthMode)throws TwitterException;


    @POST("/oauth/access_token")
    @Body(BodyType.FORM)
    OAuthToken getAccessToken(@Extra({"oauth_token", "oauth_token_secret"}) OAuthToken requestToken, @Form("oauth_verifier") String oauthVerifier)throws TwitterException;

    enum XAuthMode {
        CLIENT("client_auth");

        @Override
        public String toString() {
            return mode;
        }

        private final String mode;

        XAuthMode(String mode) {
            this.mode = mode;
        }
    }
}

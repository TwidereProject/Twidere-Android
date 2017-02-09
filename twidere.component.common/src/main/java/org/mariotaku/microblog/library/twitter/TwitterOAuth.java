/*
 *         Twidere - Twitter client for Android
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

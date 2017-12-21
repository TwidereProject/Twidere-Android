/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.microblog.library.auth;

import android.util.Base64;

import org.mariotaku.restfu.http.HeaderValue;
import org.mariotaku.restfu.oauth.OAuthToken;

public final class OAuth2GetTokenHeader implements HeaderValue {

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

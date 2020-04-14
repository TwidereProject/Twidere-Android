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

package org.mariotaku.microblog.library.twitter.auth;

import androidx.annotation.NonNull;
import android.util.Base64;

import org.mariotaku.restfu.RestRequest;
import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;

/**
 * Created by mariotaku on 15/4/19.
 */
public final class BasicAuthorization implements Authorization {

    private final String user;
    private final String password;

    public BasicAuthorization(String user, String password) {
        this.user = user;
        this.password = password;
    }

    @Override
    public String getHeader(@NonNull Endpoint endpoint, @NonNull RestRequest info) {
        if (!hasAuthorization()) return null;
        return "Basic " + Base64.encodeToString((user + ":" + password).getBytes(), Base64.NO_WRAP);
    }

    @Override
    public boolean hasAuthorization() {
        return user != null && password != null;
    }
}

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

package org.mariotaku.microblog.library.twitter.model;

import androidx.annotation.NonNull;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.restfu.RestConverter;
import org.mariotaku.restfu.http.HttpResponse;

/**
 * Created by mariotaku on 15/6/15.
 */
public class ResponseCode {

    private final int responseCode;

    public ResponseCode(HttpResponse response) {
        responseCode = response.getStatus();
    }

    public int getResponseCode() {
        return responseCode;
    }

    public boolean isSuccessful() {
        return responseCode >= 200 && responseCode < 300;
    }

    public static class ResponseConverter implements RestConverter<HttpResponse, ResponseCode, MicroBlogException> {

        @NonNull
        @Override
        public ResponseCode convert(@NonNull HttpResponse response) {
            return new ResponseCode(response);
        }
    }
}

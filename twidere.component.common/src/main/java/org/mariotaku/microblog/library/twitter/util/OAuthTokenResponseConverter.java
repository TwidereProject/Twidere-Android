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

package org.mariotaku.microblog.library.twitter.util;

import androidx.annotation.NonNull;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.restfu.RestConverter;
import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.restfu.oauth.OAuthToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;

/**
 * Created by mariotaku on 16/5/21.
 */
public class OAuthTokenResponseConverter implements RestConverter<HttpResponse, OAuthToken, MicroBlogException> {
    @NonNull
    @Override
    public OAuthToken convert(@NonNull HttpResponse response) throws IOException, ConvertException {
        final Body body = response.getBody();
        final ContentType contentType = body.contentType();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        body.writeTo(os);
        Charset charset = contentType != null ? contentType.getCharset() : null;
        if (charset == null) {
            charset = Charset.defaultCharset();
        }
        try {
            return new OAuthToken(os.toString(charset.name()), charset);
        } catch (ParseException e) {
            throw new ConvertException(e);
        }
    }
}

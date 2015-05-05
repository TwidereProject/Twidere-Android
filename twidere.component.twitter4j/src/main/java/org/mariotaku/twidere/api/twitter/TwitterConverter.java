/*
 * Twidere - Twitter client for Android
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

import com.bluelinelabs.logansquare.LoganSquare;

import org.mariotaku.simplerestapi.Converter;
import org.mariotaku.simplerestapi.http.ContentType;
import org.mariotaku.simplerestapi.http.RestResponse;
import org.mariotaku.simplerestapi.http.mime.TypedData;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.text.ParseException;

/**
 * Created by mariotaku on 15/5/5.
 */
class TwitterConverter implements Converter {
    @Override
    public Object convert(RestResponse response, Type type) throws IOException {
        final TypedData body = response.getBody();
        final ContentType contentType = body.contentType();
        final InputStream stream = body.stream();
        if (type instanceof Class<?>) {
            final Class<?> cls = (Class<?>) type;
            if (OAuthToken.class.isAssignableFrom(cls)) {
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                body.writeTo(os);
                Charset charset = contentType.getCharset();
                try {
                    return new OAuthToken(os.toString(charset.name()), charset);
                } catch (ParseException e) {
                    throw new IOException(e);
                }
            }
            LoganSquare.parse(stream, cls);
        }
        throw new UnsupportedOperationException();
    }
}

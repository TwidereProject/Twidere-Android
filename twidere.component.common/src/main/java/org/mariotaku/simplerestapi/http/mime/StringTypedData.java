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

package org.mariotaku.simplerestapi.http.mime;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.simplerestapi.Utils;
import org.mariotaku.simplerestapi.http.ContentType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by mariotaku on 15/5/12.
 */
public class StringTypedData implements TypedData {

    private final ContentType contentType;
    private final byte[] data;
    private ByteArrayInputStream is;

    public StringTypedData(String string, Charset charset) {
        this.contentType = ContentType.parse("text/plain").charset(charset);
        this.data = string.getBytes(charset);
    }

    @Nullable
    @Override
    public ContentType contentType() {
        return contentType;
    }

    @Override
    public String contentEncoding() {
        return null;
    }

    @Override
    public long length() throws IOException {
        return data.length;
    }

    @Override
    public void writeTo(@NonNull OutputStream os) throws IOException {
        os.write(data);
    }

    @NonNull
    @Override
    public InputStream stream() throws IOException {
        if (is != null) return is;
        return is = new ByteArrayInputStream(data);
    }

    @Override
    public void close() throws IOException {
        Utils.closeSilently(is);
    }
}

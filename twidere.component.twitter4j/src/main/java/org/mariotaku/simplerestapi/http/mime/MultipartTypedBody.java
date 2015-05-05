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

package org.mariotaku.simplerestapi.http.mime;

import org.mariotaku.simplerestapi.http.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by mariotaku on 15/5/5.
 */
public class MultipartTypedBody implements TypedData {
    private final TypedData[] parts;
    private boolean lengthSet;
    private long length;

    public MultipartTypedBody(List<TypedData> parts) {
        this(parts.toArray(new TypedData[parts.size()]));
    }

    public MultipartTypedBody(TypedData... parts) {
        this.parts = parts;
    }

    @Override
    public ContentType contentType() {
        return null;
    }

    @Override
    public String contentEncoding() {
        return null;
    }

    @Override
    public long length() throws IOException {
        if (!lengthSet) {
            length = 0;
            for (TypedData part : parts) {
                length += part.length();
            }
            lengthSet = true;
        }
        return length;
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        for (TypedData part : parts) {
            part.writeTo(os);
        }
    }

    @Override
    public InputStream stream() throws IOException {
        return null;
    }

    @Override
    public void close() throws IOException {
        for (TypedData part : parts) {
            part.close();
        }
    }
}

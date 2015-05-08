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

import android.support.annotation.NonNull;
import android.util.Pair;

import org.mariotaku.simplerestapi.Utils;
import org.mariotaku.simplerestapi.http.ContentType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by mariotaku on 15/5/5.
 */
public class MultipartTypedBody implements TypedData {
    private final List<Pair<String, TypedData>> parts;
    private final ContentType contentType;


    private static final byte[] COLONSPACE = {':', ' '};
    private static final byte[] CRLF = {'\r', '\n'};
    private static final byte[] DASHDASH = {'-', '-'};
    private final String boundary;

    private boolean lengthSet;
    private long length;

    public MultipartTypedBody(List<Pair<String, TypedData>> parts) {
        this.parts = parts;
        this.contentType = ContentType.parse("multipart/form-data");
        boundary = Utils.bytesToHex(UUID.randomUUID().toString().getBytes());
        contentType.addParameter("boundary", boundary);
    }

    public MultipartTypedBody() {
        this(new ArrayList<Pair<String, TypedData>>());
    }

    public void add(@NonNull String name, @NonNull TypedData data) {
        parts.add(Pair.create(name, data));
    }

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
        if (!lengthSet) {
            length = 0;
            for (Pair<String, TypedData> part : parts) {
                length += part.second.length();
            }
            lengthSet = true;
        }
        return length;
    }

    @Override
    public void writeTo(@NonNull OutputStream os) throws IOException {
        for (Pair<String, TypedData> part : parts) {
            os.write(DASHDASH);
            os.write(boundary.getBytes());
            os.write(CRLF);
            final ContentType contentDisposition = new ContentType("form-data").parameter("name", part.first);
            final ContentType contentType = part.second.contentType();
            final long contentLength = part.second.length();
            if (part.second instanceof FileTypedData) {
                contentDisposition.addParameter("filename", ((FileTypedData) part.second).fileName());
            }
            os.write("Content-Disposition".getBytes());
            os.write(COLONSPACE);
            os.write(contentDisposition.toHeader().getBytes());
            os.write(CRLF);
            if (contentType != null) {
                os.write("Content-Type".getBytes());
                os.write(COLONSPACE);
                os.write(contentType.toHeader().getBytes());
                os.write(CRLF);
            }
            if (contentLength != -1) {
                os.write("Content-Length".getBytes());
                os.write(COLONSPACE);
                os.write(String.valueOf(contentLength).getBytes());
                os.write(CRLF);
            }
            os.write(CRLF);
            part.second.writeTo(os);
            os.write(CRLF);
        }
        os.write(DASHDASH);
        os.write(boundary.getBytes());
        os.write(DASHDASH);
        os.write(CRLF);
    }

    @NonNull
    @Override
    public InputStream stream() throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        writeTo(os);
        return new ByteArrayInputStream(os.toByteArray());
    }

    @Override
    public void close() throws IOException {
        for (Pair<String, TypedData> part : parts) {
            part.second.close();
        }
    }
}

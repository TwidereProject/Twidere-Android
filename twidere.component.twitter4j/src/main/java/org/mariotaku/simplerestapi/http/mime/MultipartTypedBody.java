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

import org.mariotaku.simplerestapi.http.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariotaku on 15/5/5.
 */
public class MultipartTypedBody implements TypedData {
    private List<Pair<String, TypedData>> parts;
    private boolean lengthSet;
    private long length;

    public MultipartTypedBody(List<Pair<String, TypedData>> parts) {
        this.parts = (parts);
    }

    public MultipartTypedBody() {
        this(new ArrayList<Pair<String, TypedData>>());
    }

    public void add(@NonNull String name, @NonNull TypedData data) {
        parts.add(Pair.create(name, data));
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
            part.second.writeTo(os);
        }
    }

    @NonNull
    @Override
    public InputStream stream() throws IOException {
        return null;
    }

    @Override
    public void close() throws IOException {
        for (Pair<String, TypedData> part : parts) {
            part.second.close();
        }
    }
}

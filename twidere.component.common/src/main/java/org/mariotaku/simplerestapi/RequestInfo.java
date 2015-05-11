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

package org.mariotaku.simplerestapi;

import android.support.annotation.Nullable;
import android.util.Pair;

import org.mariotaku.simplerestapi.http.mime.FormTypedBody;
import org.mariotaku.simplerestapi.http.mime.MultipartTypedBody;
import org.mariotaku.simplerestapi.http.mime.TypedData;
import org.mariotaku.simplerestapi.param.Body;

import java.util.List;
import java.util.Map;

/**
 * Created by mariotaku on 15/5/11.
 */
public final class RequestInfo {


    private String method;
    private String path;

    private List<Pair<String, String>> queries, forms, headers;
    private List<Pair<String, TypedData>> parts;
    private Map<String, Object> extras;
    private FileValue file;
    private Body body;

    private TypedData bodyCache;

    public RequestInfo(String method, String path, List<Pair<String, String>> queries,
                       List<Pair<String, String>> forms, List<Pair<String, String>> headers,
                       List<Pair<String, TypedData>> parts, FileValue file, Body body, Map<String, Object> extras) {
        this.method = method;
        this.path = path;
        this.queries = queries;
        this.forms = forms;
        this.headers = headers;
        this.parts = parts;
        this.extras = extras;
        this.file = file;
        this.body = body;
    }

    public List<Pair<String, String>> getQueries() {
        return queries;
    }

    public List<Pair<String, String>> getForms() {
        return forms;
    }

    public List<Pair<String, String>> getHeaders() {
        return headers;
    }

    public List<Pair<String, TypedData>> getParts() {
        return parts;
    }

    public Map<String, Object> getExtras() {
        return extras;
    }

    @Nullable
    public TypedData getBody() {
        if (bodyCache != null) return bodyCache;
        if (body == null) return null;
        switch (body.value()) {
            case FORM: {
                bodyCache = new FormTypedBody(getForms());
                break;
            }
            case MULTIPART: {
                bodyCache = new MultipartTypedBody(getParts());
                break;
            }
            case FILE: {
                bodyCache = file.body();
                break;
            }
        }
        return bodyCache;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public interface Factory {
        RequestInfo create(RestMethodInfo methodInfo);
    }
}

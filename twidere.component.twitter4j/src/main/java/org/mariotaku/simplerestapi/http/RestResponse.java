package org.mariotaku.simplerestapi.http;

import org.apache.commons.lang3.tuple.Pair;
import org.mariotaku.simplerestapi.http.mime.TypedData;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariotaku on 15/2/7.
 */
public abstract class RestResponse implements Closeable {
    public abstract int getStatus();

    public abstract List<Pair<String, String>> getHeaders();

    public abstract TypedData getBody();

    public String getHeader(String name) {
        if (name == null) throw new NullPointerException();
        final List<Pair<String, String>> headers = getHeaders();
        if (headers == null) return null;
        for (Pair<String, String> header : headers) {
            if (header.getKey().equalsIgnoreCase(name)) return header.getValue();
        }
        return null;
    }

    public String[] getHeaders(String name) {
        if (name == null) throw new NullPointerException();
        final List<Pair<String, String>> headers = getHeaders();
        if (headers == null) return new String[0];
        final ArrayList<String> result = new ArrayList<>();
        for (Pair<String, String> header : headers) {
            if (name.equalsIgnoreCase(header.getKey())) {
                result.add(header.getValue());
            }
        }
        return result.toArray(new String[result.size()]);
    }

}

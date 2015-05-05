package org.mariotaku.simplerestapi.http;

import org.mariotaku.simplerestapi.http.mime.TypedData;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariotaku on 15/2/7.
 */
public abstract class RestResponse implements Closeable {
    public abstract int getStatus();

    public abstract List<KeyValuePair> getHeaders();

    public abstract TypedData getBody();

    public String getHeader(String name) {
        if (name == null) throw new NullPointerException();
        final List<KeyValuePair> headers = getHeaders();
        if (headers == null) return null;
        for (KeyValuePair header : headers) {
            if (header.getKey().equalsIgnoreCase(name)) return header.getValue();
        }
        return null;
    }

    public KeyValuePair[] getHeaders(String name) {
        if (name == null) throw new NullPointerException();
        final List<KeyValuePair> headers = getHeaders();
        if (headers == null) return new KeyValuePair[0];
        final ArrayList<KeyValuePair> result = new ArrayList<>();
        for (KeyValuePair header : headers) {
            if (name.equalsIgnoreCase(header.getKey())) {
                result.add(header);
            }
        }
        return result.toArray(new KeyValuePair[result.size()]);
    }
}

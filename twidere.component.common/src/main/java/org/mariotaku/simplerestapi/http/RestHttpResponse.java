package org.mariotaku.simplerestapi.http;

import android.util.Pair;

import org.mariotaku.simplerestapi.http.mime.TypedData;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariotaku on 15/2/7.
 */
public abstract class RestHttpResponse implements Closeable {
    public abstract int getStatus();

    public abstract List<Pair<String, String>> getHeaders();

    public abstract TypedData getBody();

    public String getHeader(String name) {
        if (name == null) throw new NullPointerException();
        final List<Pair<String, String>> headers = getHeaders();
        if (headers == null) return null;
        for (Pair<String, String> header : headers) {
            if (header.first.equalsIgnoreCase(name)) return header.second;
        }
        return null;
    }

    public String[] getHeaders(String name) {
        if (name == null) throw new NullPointerException();
        final List<Pair<String, String>> headers = getHeaders();
        if (headers == null) return new String[0];
        final ArrayList<String> result = new ArrayList<>();
        for (Pair<String, String> header : headers) {
            if (name.equalsIgnoreCase(header.first)) {
                result.add(header.second);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns true if the code is in [200..300), which means the request was
     * successfully received, understood, and accepted.
     */
    public boolean isSuccessful() {
        final int status = getStatus();
        return status >= 200 && status < 300;
    }

}

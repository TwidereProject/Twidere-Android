package org.mariotaku.simplerestapi.http;


import android.util.Pair;

import org.mariotaku.simplerestapi.Utils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariotaku on 15/2/4.
 */
public final class ContentType {

    public static final ContentType OCTET_STREAM = ContentType.parse("application/octet-stream");

    private final String contentType;
    private final List<Pair<String, String>> parameters;

    public ContentType(String contentType, Charset charset) {
        this(contentType, new ArrayList<Pair<String, String>>());
        addParameter("charset", charset.name());
    }

    public ContentType(String contentType) {
        this(contentType, new ArrayList<Pair<String, String>>());
    }

    public ContentType(String contentType, List<Pair<String, String>> parameters) {
        this.contentType = contentType;
        this.parameters = parameters;
    }

    public boolean addParameter(String name, String value) {
        return parameters.add(Pair.create(name, value));
    }

    public ContentType parameter(String name, String value) {
        addParameter(name, value);
        return this;
    }

    public String parameter(String name) {
        for (Pair<String, String> parameter : parameters) {
            if (name.equalsIgnoreCase(parameter.first)) return parameter.second;
        }
        return null;
    }

    @Override
    public String toString() {
        return "ContentType{" +
                "contentType='" + contentType + '\'' +
                ", parameters=" + parameters +
                '}';
    }

    public Charset getCharset() {
        if (parameters == null) return null;
        final String charset = parameter("charset");
        if (charset != null) return Charset.forName(charset);
        return null;
    }

    public String getContentType() {
        return contentType;
    }

    public String toHeader() {
        final StringBuilder sb = new StringBuilder(contentType);
        for (Pair<String, String> parameter : parameters) {
            sb.append("; ");
            sb.append(parameter.first);
            sb.append("=");
            sb.append(parameter.second);
        }
        return sb.toString();
    }

    public static ContentType parse(String string) {
        final List<Pair<String, String>> parameters = new ArrayList<>();
        int previousIndex = string.indexOf(';', 0);
        String contentType;
        if (previousIndex == -1) {
            contentType = string;
        } else {
            contentType = string.substring(0, previousIndex);
        }
        while (previousIndex != -1) {
            final int idx = string.indexOf(';', previousIndex + 1);
            final String[] segs;
            if (idx < 0) {
                segs = Utils.split(string.substring(previousIndex + 1, string.length()).trim(), "=");
            } else {
                segs = Utils.split(string.substring(previousIndex + 1, idx).trim(), "=");
            }
            if (segs.length == 2) {
                parameters.add(Pair.create(segs[0], segs[1]));
            }
            if (idx < 0) {
                break;
            }
            previousIndex = idx;
        }
        return new ContentType(contentType, parameters);
    }

    public ContentType charset(Charset charset) {
        removeParameter("charset");
        return parameter("charset", charset.name());
    }

    private void removeParameter(String name) {
        for (int i = parameters.size() - 1; i >= 0; i++) {
            if (name.equals(parameters.get(i).first)) {
                parameters.remove(i);
            }
        }
    }
}

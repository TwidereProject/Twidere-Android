package org.mariotaku.simplerestapi.http.mime;

import org.mariotaku.simplerestapi.http.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by mariotaku on 15/2/6.
 */
public interface TypedData {
    ContentType contentType();

    String contentEncoding();

    long length() throws IOException;

    void writeTo(OutputStream os) throws IOException;

    InputStream stream() throws IOException;

    void close() throws IOException;
}

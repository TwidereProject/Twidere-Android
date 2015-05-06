package org.mariotaku.simplerestapi.http.mime;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.simplerestapi.http.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by mariotaku on 15/2/6.
 */
public interface TypedData {
    @Nullable
    ContentType contentType();

    String contentEncoding();

    long length() throws IOException;

    void writeTo(@NonNull OutputStream os) throws IOException;

    @NonNull
    InputStream stream() throws IOException;

    void close() throws IOException;
}

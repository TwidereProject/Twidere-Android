package org.mariotaku.simplerestapi.http.mime;

import android.support.annotation.NonNull;

import org.mariotaku.simplerestapi.http.ContentType;
import org.mariotaku.simplerestapi.io.StreamingGZIPInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Created by mariotaku on 15/2/7.
 */
public class BaseTypedData implements TypedData {

    private final ContentType contentType;
    private final long contentLength;
    private final InputStream stream;
    private final String contentEncoding;

    public BaseTypedData(ContentType contentType, String contentEncoding, long contentLength, InputStream stream) throws IOException {
        this.contentType = contentType;
        this.contentEncoding = contentEncoding;
        this.contentLength = contentLength;
        if ("gzip".equals(contentEncoding)) {
            this.stream = new StreamingGZIPInputStream(stream);
        } else {
            this.stream = stream;
        }
    }

    @Override
    public ContentType contentType() {
        return contentType;
    }

    @Override
    public String contentEncoding() {
        return contentEncoding;
    }

    @Override
    public long length() {
        return contentLength;
    }

    @Override
    public String toString() {
        return "BaseTypedData{" +
                "contentType=" + contentType +
                ", contentLength=" + contentLength +
                ", stream=" + stream +
                ", contentEncoding='" + contentEncoding + '\'' +
                '}';
    }

    @Override
    public void writeTo(@NonNull OutputStream os) throws IOException {
        final byte[] buffer = new byte[8192];
        for (int len; (len = stream.read(buffer)) != -1; ) {
            os.write(buffer, 0, len);
        }

    }

    @NonNull
    @Override
    public InputStream stream() {
        return stream;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    public static TypedData wrap(Object value) {
        if (value instanceof TypedData) {
            return (TypedData) value;
        } else if (value instanceof java.io.File) {
            return new FileTypedData((java.io.File) value);
        }
        throw new UnsupportedOperationException();
    }

    public static Reader reader(TypedData data) throws IOException {
        final ContentType contentType = data.contentType();
        final Charset charset = contentType != null ? contentType.getCharset() : null;
        return new InputStreamReader(data.stream(), charset != null ? charset : Charset.defaultCharset());
    }
}

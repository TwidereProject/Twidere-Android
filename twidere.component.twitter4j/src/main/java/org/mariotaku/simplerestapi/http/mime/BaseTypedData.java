package org.mariotaku.simplerestapi.http.mime;

import org.mariotaku.simplerestapi.http.ContentType;
import org.mariotaku.simplerestapi.io.StreamingGZIPInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
    public void writeTo(OutputStream os) throws IOException {
        final byte[] buffer = new byte[8192];
        for (int len; (len = stream.read(buffer)) != -1; ) {
            os.write(buffer, 0, len);
        }

    }

    @Override
    public InputStream stream() {
        return stream;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    public static TypedData wrap(Object value) {
        throw new UnsupportedOperationException();
    }
}

package org.mariotaku.simplerestapi.http.mime;

import android.support.annotation.NonNull;
import android.util.Pair;

import org.mariotaku.simplerestapi.Utils;
import org.mariotaku.simplerestapi.http.ContentType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by mariotaku on 15/2/6.
 */
public class FormTypedBody implements TypedData {

    private final List<Pair<String, String>> forms;
    private final Charset charset;

    public FormTypedBody(List<Pair<String, String>> forms) {
        this.forms = forms;
        this.charset = Charset.forName("UTF-8");
    }

    @Override
    public ContentType contentType() {
        return new ContentType("application/x-www-form-urlencoded", charset);
    }

    @Override
    public String contentEncoding() {
        return null;
    }

    private byte[] bytes;

    private void toRawBytes() {
        if (bytes != null) return;
        final StringBuilder sb = new StringBuilder();
        for (int i = 0, j = forms.size(); i < j; i++) {
            if (i != 0) {
                sb.append('&');
            }
            final Pair<String, String> form = forms.get(i);
            sb.append(Utils.encode(form.first, charset.name()));
            sb.append('=');
            sb.append(Utils.encode(form.second, charset.name()));
        }
        bytes = sb.toString().getBytes(charset);
    }

    @Override
    public long length() {
        toRawBytes();
        return bytes.length;
    }

    @Override
    public void writeTo(@NonNull OutputStream os) throws IOException {
        toRawBytes();
        os.write(bytes);
    }

    @NonNull
    @Override
    public InputStream stream() {
        toRawBytes();
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void close() throws IOException {
        // No-op
    }
}

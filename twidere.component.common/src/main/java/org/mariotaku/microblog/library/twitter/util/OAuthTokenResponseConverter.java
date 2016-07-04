package org.mariotaku.microblog.library.twitter.util;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.restfu.RestConverter;
import org.mariotaku.restfu.RestFuUtils;
import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.restfu.oauth.OAuthToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;

/**
 * Created by mariotaku on 16/5/21.
 */
public class OAuthTokenResponseConverter implements RestConverter<HttpResponse, OAuthToken, MicroBlogException> {
    @Override
    public OAuthToken convert(HttpResponse response) throws IOException, ConvertException {
        final Body body = response.getBody();
        try {
            final ContentType contentType = body.contentType();
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            body.writeTo(os);
            Charset charset = contentType != null ? contentType.getCharset() : null;
            if (charset == null) {
                charset = Charset.defaultCharset();
            }
            try {
                return new OAuthToken(os.toString(charset.name()), charset);
            } catch (ParseException e) {
                throw new ConvertException(e);
            }
        } finally {
            RestFuUtils.closeSilently(body);
        }
    }
}

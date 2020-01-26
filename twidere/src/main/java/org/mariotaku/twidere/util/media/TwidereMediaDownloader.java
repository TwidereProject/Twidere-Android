package org.mariotaku.twidere.util.media;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import org.mariotaku.mediaviewer.library.CacheDownloadLoader;
import org.mariotaku.mediaviewer.library.MediaDownloader;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.MultiValueMap;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.twidere.extension.model.AccountExtensionsKt;
import org.mariotaku.twidere.extension.model.CredentialsExtensionsKt;
import org.mariotaku.twidere.model.CacheMetadata;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.account.cred.Credentials;
import org.mariotaku.twidere.model.util.AccountUtils;
import org.mariotaku.twidere.util.JsonSerializer;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.UserAgentUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.media.preview.PreviewMediaExtractor;
import org.mariotaku.twidere.util.net.NoIntercept;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mariotaku on 16/1/28.
 */
public class TwidereMediaDownloader implements MediaDownloader {

    private final Context context;
    private final RestHttpClient client;
    private final String userAgent;
    private final ThumborWrapper thumbor;

    public TwidereMediaDownloader(final Context context, final RestHttpClient client,
            final ThumborWrapper thumbor) {
        this.context = context;
        this.client = client;
        this.thumbor = thumbor;
        this.userAgent = UserAgentUtils.getDefaultUserAgentStringSafe(context);
    }

    @NonNull
    @Override
    public CacheDownloadLoader.DownloadResult get(@NonNull String url, Object extra) throws IOException {
        try {
            boolean skipUrlReplacing = false;
            if (extra instanceof MediaExtra) {
                skipUrlReplacing = ((MediaExtra) extra).isSkipUrlReplacing();
            }
            if (!skipUrlReplacing) {
                final ParcelableMedia media = PreviewMediaExtractor.fromLink(url, client, extra);
                if (media != null && media.media_url != null) {
                    return getInternal(media.media_url, extra);
                }
            }
            return getInternal(url, extra);
        } catch (IOException e) {
            if (extra instanceof MediaExtra) {
                final String fallbackUrl = ((MediaExtra) extra).getFallbackUrl();
                if (fallbackUrl != null) {
                    final ParcelableMedia media = PreviewMediaExtractor.fromLink(fallbackUrl,
                            client, extra);
                    if (media != null && media.media_url != null) {
                        return getInternal(media.media_url, extra);
                    } else {
                        return getInternal(fallbackUrl, extra);
                    }
                }
            }
            throw e;
        }
    }

    protected CacheDownloadLoader.DownloadResult getInternal(@NonNull String url,
            @Nullable Object extra) throws IOException {
        final Uri uri = Uri.parse(url);
        Credentials credentials = null;
        boolean useThumbor = true;
        if (extra instanceof MediaExtra) {
            useThumbor = ((MediaExtra) extra).isUseThumbor();
            UserKey accountKey = ((MediaExtra) extra).getAccountKey();
            if (accountKey != null) {
                final AccountManager am = AccountManager.get(context);
                Account account = AccountUtils.findByAccountKey(am, accountKey);
                if (account != null) {
                    credentials = AccountExtensionsKt.getCredentials(account, am);
                }
            }
        }
        final Uri modifiedUri = getReplacedUri(uri, credentials != null ? credentials.api_url_format : null);
        final MultiValueMap<String> additionalHeaders = new MultiValueMap<>();
        additionalHeaders.add("User-Agent", userAgent);
        final String method = GET.METHOD;
        final String requestUri;
        if (isAuthRequired(credentials, uri)) {
            additionalHeaders.add("Authorization", CredentialsExtensionsKt.authorizationHeader(credentials,
                    uri, modifiedUri, null));
            requestUri = modifiedUri.toString();
        } else if (thumbor != null && useThumbor) {
            requestUri = thumbor.buildUri(modifiedUri.toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                additionalHeaders.add("Accept", "image/webp, */*");
            }
        } else {
            requestUri = modifiedUri.toString();
        }
        final HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.method(method);
        builder.url(requestUri);
        builder.headers(additionalHeaders);
        builder.tag(NoIntercept.INSTANCE);
        final HttpResponse resp = client.newCall(builder.build()).execute();
        if (!resp.isSuccessful()) {
            final String detailMessage = "Unable to get " + requestUri + ", response code: "
                    + resp.getStatus();
            if (resp.getStatus() == 404) {
                throw new FileNotFoundException(detailMessage);
            }
            throw new IOException(detailMessage);
        }
        final Body body = resp.getBody();
        final CacheMetadata metadata = new CacheMetadata();
        metadata.setContentType(Utils.INSTANCE.sanitizeMimeType(body.contentType().getContentType()));
        return new TwidereDownloadResult(body, metadata);
    }

    public static String getEndpoint(Uri uri) {
        final StringBuilder sb = new StringBuilder();
        sb.append(uri.getScheme());
        sb.append("://");
        sb.append(uri.getHost());
        if (uri.getPort() != -1) {
            sb.append(':');
            sb.append(uri.getPort());
        }
        sb.append("/");
        return sb.toString();
    }

    public static boolean isAuthRequired(@Nullable final Credentials credentials, @NonNull final Uri uri) {
        if (credentials == null) return false;
        final String host = uri.getHost();
        if (credentials.api_url_format != null && credentials.api_url_format.contains(host)) {
            return true;
        }
        return "ton.twitter.com".equalsIgnoreCase(host);
    }

    private static boolean isTwitterUri(final Uri uri) {
        return uri != null && "ton.twitter.com".equalsIgnoreCase(uri.getHost());
    }

    public static Uri getReplacedUri(@NonNull final Uri uri, final String apiUrlFormat) {
        if (apiUrlFormat == null) return uri;
        if (isTwitterUri(uri)) {
            final StringBuilder sb = new StringBuilder();
            final String host = uri.getHost();
            final String domain = host.substring(0, host.lastIndexOf(".twitter.com"));
            final String path = uri.getPath();
            sb.append(MicroBlogAPIFactory.getApiUrl(apiUrlFormat, domain, path));
            final String query = uri.getQuery();
            if (!TextUtils.isEmpty(query)) {
                sb.append("?");
                sb.append(query);
            }
            final String fragment = uri.getFragment();
            if (!TextUtils.isEmpty(fragment)) {
                sb.append("#");
                sb.append(fragment);
            }
            return Uri.parse(sb.toString());
        }
        return uri;
    }

    private static class TwidereDownloadResult implements CacheDownloadLoader.DownloadResult {
        private final Body mBody;
        private final CacheMetadata mMetadata;

        public TwidereDownloadResult(Body body, CacheMetadata metadata) {
            mBody = body;
            mMetadata = metadata;
        }

        @Override
        public void close() throws IOException {
            mBody.close();
        }

        @Override
        public long getLength() throws IOException {
            return mBody.length();
        }

        @NonNull
        @Override
        public InputStream getStream() throws IOException {
            return mBody.stream();
        }

        @Override
        public byte[] getExtra() {
            if (mMetadata == null) return null;
            final String serialize = JsonSerializer.serialize(mMetadata, CacheMetadata.class);
            if (serialize == null) return null;
            return serialize.getBytes();
        }
    }
}

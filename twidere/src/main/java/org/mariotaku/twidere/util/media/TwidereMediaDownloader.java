package org.mariotaku.twidere.util.media;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.squareup.pollexor.Thumbor;
import com.squareup.pollexor.ThumborUrlBuilder;

import org.mariotaku.mediaviewer.library.CacheDownloadLoader;
import org.mariotaku.mediaviewer.library.MediaDownloader;
import org.mariotaku.restfu.RestRequest;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.MultiValueMap;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.restfu.oauth.OAuthAuthorization;
import org.mariotaku.restfu.oauth.OAuthEndpoint;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.CacheMetadata;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.util.JsonSerializer;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.UserAgentUtils;
import org.mariotaku.twidere.util.media.preview.PreviewMediaExtractor;
import org.mariotaku.twidere.util.net.NoIntercept;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mariotaku on 16/1/28.
 */
public class TwidereMediaDownloader implements MediaDownloader, Constants {

    private final Context mContext;
    private final SharedPreferencesWrapper mPreferences;
    private final RestHttpClient mClient;
    private final String mUserAgent;

    private Thumbor mThumbor;

    public TwidereMediaDownloader(final Context context, SharedPreferencesWrapper preferences,
                                  RestHttpClient client) {
        mContext = context;
        mPreferences = preferences;
        mClient = client;
        mUserAgent = UserAgentUtils.getDefaultUserAgentStringSafe(context);
        reloadConnectivitySettings();
    }

    public void reloadConnectivitySettings() {
        if (mPreferences.getBoolean(KEY_THUMBOR_ENABLED)) {
            final String address = mPreferences.getString(KEY_THUMBOR_ADDRESS, null);
            final String securityKey = mPreferences.getString(KEY_THUMBOR_SECURITY_KEY, null);
            if (address != null && URLUtil.isValidUrl(address)) {
                if (TextUtils.isEmpty(securityKey)) {
                    mThumbor = Thumbor.create(address);
                } else {
                    mThumbor = Thumbor.create(address, securityKey);
                }
            } else {
                mThumbor = null;
            }
        } else {
            mThumbor = null;
        }
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
                final ParcelableMedia media = PreviewMediaExtractor.fromLink(url, mClient, extra);
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
                            mClient, extra);
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
        Authorization auth = null;
        ParcelableCredentials account = null;
        boolean useThumbor = true;
        if (extra instanceof MediaExtra) {
            useThumbor = ((MediaExtra) extra).isUseThumbor();
            UserKey accountKey = ((MediaExtra) extra).getAccountKey();
            if (accountKey != null) {
                account = ParcelableCredentialsUtils.getCredentials(mContext, accountKey);
                auth = MicroBlogAPIFactory.getAuthorization(account);
            }
        }
        final Uri modifiedUri = getReplacedUri(uri, account != null ? account.api_url_format : null);
        final MultiValueMap<String> additionalHeaders = new MultiValueMap<>();
        additionalHeaders.add("User-Agent", mUserAgent);
        final String method = GET.METHOD;
        final String requestUri;
        if (isAuthRequired(uri, account) && auth != null && auth.hasAuthorization()) {
            final Endpoint endpoint;
            if (auth instanceof OAuthAuthorization) {
                endpoint = new OAuthEndpoint(getEndpoint(modifiedUri), getEndpoint(uri));
            } else {
                endpoint = new Endpoint(getEndpoint(modifiedUri));
            }
            final MultiValueMap<String> queries = new MultiValueMap<>();
            for (String name : uri.getQueryParameterNames()) {
                for (String value : uri.getQueryParameters(name)) {
                    queries.add(name, value);
                }
            }
            final RestRequest info = new RestRequest(method, false, uri.getPath(), additionalHeaders,
                    queries, null, null, null, null);
            additionalHeaders.add("Authorization", auth.getHeader(endpoint, info));
            requestUri = modifiedUri.toString();
        } else if (mThumbor != null && useThumbor) {
            requestUri = mThumbor.buildImage(modifiedUri.toString()).filter(ThumborUrlBuilder.quality(85)).toUrl();
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
        final HttpResponse resp = mClient.newCall(builder.build()).execute();
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
        metadata.setContentType(body.contentType().getContentType());
        return new TwidereDownloadResult(body, metadata);
    }

    private String getEndpoint(Uri uri) {
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

    private boolean isAuthRequired(final Uri uri, @Nullable final ParcelableCredentials credentials) {
        if (credentials == null) return false;
        final String host = uri.getHost();
        if (credentials.api_url_format != null && credentials.api_url_format.contains(host)) {
            return true;
        }
        return "ton.twitter.com".equalsIgnoreCase(host);
    }

    private boolean isTwitterUri(final Uri uri) {
        return uri != null && "ton.twitter.com".equalsIgnoreCase(uri.getHost());
    }

    private Uri getReplacedUri(@NonNull final Uri uri, final String apiUrlFormat) {
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

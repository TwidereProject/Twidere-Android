package org.mariotaku.twidere.loader.support;

import android.content.Context;
import android.net.Uri;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.http.RestHttpRequest;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

/**
 * Created by mariotaku on 16/1/1.
 */
public class FullImageDownloadLoader extends CacheDownloadLoader {

    @Inject
    RestHttpClient mRestHttpClient;

    private final long mAccountId;

    public FullImageDownloadLoader(Context context, DownloadListener listener, Uri uri, long accountId) {
        super(context, listener, uri);
        GeneralComponentHelper.build(context).inject(this);
        mAccountId = accountId;
    }

    @Override
    protected InputStream getStreamFromNetwork(String url) throws IOException {
        RestHttpRequest.Builder builder = new RestHttpRequest.Builder();
        builder.method(GET.METHOD);
        builder.url(url);
        return mRestHttpClient.execute(builder.build()).getBody().stream();
    }
}

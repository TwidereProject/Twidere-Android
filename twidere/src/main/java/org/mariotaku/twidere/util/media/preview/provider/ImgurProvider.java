package org.mariotaku.twidere.util.media.preview.provider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.util.UriUtils;

/**
 * Created by mariotaku on 16/3/20.
 */
public class ImgurProvider implements Provider {
    @Override
    public boolean supports(@NonNull String link) {
        final String authority = UriUtils.getAuthority(link);
        if (authority == null) return false;
        return "i.imgur.com".equals(authority);
    }

    @Nullable
    @Override
    public ParcelableMedia from(@NonNull String url) {
        final String authority = UriUtils.getAuthority(url);
        if (authority == null) return null;
        if ("i.imgur.com".equals(authority)) {
            final String path = UriUtils.getPath(url);
            if (path == null) return null;
            ParcelableMedia media = new ParcelableMedia();
            media.url = url;
        }
        return null;
    }

    @Nullable
    @Override
    public ParcelableMedia from(@NonNull String link, @NonNull RestHttpClient client, @Nullable Object extra) {
        return from(link);
    }
}

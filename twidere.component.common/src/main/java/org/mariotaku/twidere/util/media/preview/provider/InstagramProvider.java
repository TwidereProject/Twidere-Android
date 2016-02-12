package org.mariotaku.twidere.util.media.preview.provider;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.util.media.preview.PreviewMediaExtractor;

import java.util.List;
import java.util.Locale;

/**
 * Created by mariotaku on 16/1/1.
 */
public class InstagramProvider implements Provider {
    @Override
    public boolean supports(@NonNull String link) {
        final String authority = PreviewMediaExtractor.getAuthority(link);
        return "instagr.am".equals(authority) || "instagram.com".equals(authority) ||
                "www.instagram.com".equals(authority);
    }

    @Override
    @Nullable
    public ParcelableMedia from(@NonNull String link) {
        final Uri uri = Uri.parse(link);
        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() < 2 || !"p".equals(pathSegments.get(0))) return null;
        final String id = pathSegments.get(1);
        final ParcelableMedia media = new ParcelableMedia();
        media.type = ParcelableMedia.Type.IMAGE;
        media.url = link;
        media.preview_url = String.format(Locale.ROOT, "https://instagram.com/p/%s/media/?size=m", id);
        media.media_url = String.format(Locale.ROOT, "https://instagram.com/p/%s/media/?size=l", id);
        media.open_browser = true;
        return media;
    }

    @Override
    @Nullable
    @WorkerThread
    public ParcelableMedia from(@NonNull String link, @NonNull RestHttpClient client, @Nullable Object extra) {
        return from(link);
    }
}

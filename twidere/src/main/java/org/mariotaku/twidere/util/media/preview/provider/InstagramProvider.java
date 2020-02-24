package org.mariotaku.twidere.util.media.preview.provider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.util.UriUtils;

import java.util.Locale;

/**
 * Process instagram image links
 * Created by mariotaku on 16/1/1.
 */
public class InstagramProvider implements Provider {
    @Override
    public boolean supports(@NonNull String link) {
        final String authority = UriUtils.getAuthority(link);
        if (authority == null) return false;
        switch (authority) {
            //noinspection SpellCheckingInspection
            case "instagr.am":
            case "instagram.com":
            case "www.instagram.com": {
                final String path = UriUtils.getPath(link);
                return path != null && path.startsWith("/p/");
            }
        }
        return false;
    }

    @Override
    @Nullable
    public ParcelableMedia from(@NonNull String link) {
        final String path = UriUtils.getPath(link);
        final String prefix = "/p/";
        if (path == null || !path.startsWith(prefix)) {
            return null;
        }
        String lastPath = path.substring(prefix.length());
        if (lastPath.isEmpty()) return null;
        int end = lastPath.indexOf('/');
        if (end < 0) {
            end = lastPath.length();
        }
        final String id = lastPath.substring(0, end);
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

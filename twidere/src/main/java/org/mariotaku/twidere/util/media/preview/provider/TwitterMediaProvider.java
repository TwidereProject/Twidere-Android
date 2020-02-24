package org.mariotaku.twidere.util.media.preview.provider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.util.UriUtils;

/**
 * Created by darkwhite on 1/16/16.
 */
public class TwitterMediaProvider implements Provider {
    @Override
    public boolean supports(@NonNull String link) {
        return isSupported(link);
    }

    @Nullable
    @Override
    public ParcelableMedia from(@NonNull String link) {
        final String path = UriUtils.getPath(link);
        if (path == null) return null;
        final ParcelableMedia media = new ParcelableMedia();
        media.url = link;
        if (path.startsWith("/media/")) {
            media.type = ParcelableMedia.Type.IMAGE;
            media.preview_url = getMediaForSize(link, "medium");
            media.media_url = getMediaForSize(link, "orig");
        } else if (path.startsWith("/tweet_video/")) {
            // Video is not supported yet
            return null;
        } else {
            // Don't display media that not supported yet
            return null;
        }

        return media;
    }

    @Nullable
    @Override
    public ParcelableMedia from(@NonNull String link, @NonNull RestHttpClient client, @Nullable Object extra) {
        return from(link);
    }

    public static String getMediaForSize(@NonNull String link, @NonNull String size) {
        return link + ":" + size;
    }

    public static boolean isSupported(@NonNull String link) {
        final String authority = UriUtils.getAuthority(link);
        if (authority == null || !authority.endsWith(".twimg.com")) {
            return false;
        }
        final String path = UriUtils.getPath(link);
        if (path == null) return false;
        return path.startsWith("/media/");
    }

}

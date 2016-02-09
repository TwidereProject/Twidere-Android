package org.mariotaku.twidere.util.media.preview.provider;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.util.media.preview.PreviewMediaExtractor;

import java.util.Locale;

/**
 * Created by darkwhite on 1/16/16.
 */
public class TwitterPicProvider implements Provider {
    @Override
    public boolean supports(@NonNull String link) {
        final String authority = PreviewMediaExtractor.getAuthority(link);
        if (authority == null) return false;
        return authority.endsWith(".twimg.com") && !link.contains("/tweet_video");
    }

    @Nullable
    @Override
    public ParcelableMedia from(@NonNull String link) {
        final ParcelableMedia media = new ParcelableMedia();
        media.type = ParcelableMedia.Type.TYPE_IMAGE;
        media.url = link;
        if (link.contains("profile")) {
            media.preview_url = link;
            media.media_url = link;
        } else {
            media.preview_url = String.format(Locale.ROOT, "%s:medium", link);
            media.media_url = String.format(Locale.ROOT, "%s:orig", link);
        }

        return media;
    }

    @Nullable
    @Override
    public ParcelableMedia from(@NonNull String link, @NonNull RestHttpClient client, @Nullable Object extra) {
        return from(link);
    }
}

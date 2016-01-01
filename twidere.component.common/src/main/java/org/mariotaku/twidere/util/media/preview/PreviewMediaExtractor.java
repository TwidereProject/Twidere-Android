package org.mariotaku.twidere.util.media.preview;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.util.HtmlLinkExtractor;
import org.mariotaku.twidere.util.media.preview.provider.InstagramProvider;
import org.mariotaku.twidere.util.media.preview.provider.Provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mariotaku on 16/1/1.
 */
public class PreviewMediaExtractor {

    private static final Provider[] sProviders = {
            new InstagramProvider()
    };

    @Nullable
    public static ParcelableMedia fromLink(@Nullable String link) {
        if (TextUtils.isEmpty(link)) return null;
        for (Provider provider : sProviders) {
            if (provider.supportsAuthority(getAuthority(link))) {
                return provider.from(Uri.parse(link));
            }
        }
        return null;
    }

    @Nullable
    private static String getAuthority(@NonNull String link) {
        int start = link.indexOf("://");
        if (start < 0) return null;
        int end = link.indexOf('/', start + 3);
        if (end < 0) {
            end = link.length();
        }
        return link.substring(start + 3, end);
    }

    public static boolean isSupported(@Nullable String link) {
        if (TextUtils.isEmpty(link)) return false;
        for (Provider provider : sProviders) {
            if (provider.supportsAuthority(getAuthority(link))) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getSupportedLinksInStatus(final String statusString) {
        if (statusString == null) return Collections.emptyList();
        final List<String> links = new ArrayList<>();
        final HtmlLinkExtractor extractor = new HtmlLinkExtractor();
        for (final HtmlLinkExtractor.HtmlLink link : extractor.grabLinks(statusString)) {
            final String linkString = link.getLink();
            if (isSupported(linkString)) {
                links.add(linkString);
            }
        }
        return links;
    }
}

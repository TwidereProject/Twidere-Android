package org.mariotaku.twidere.util.media.preview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.util.HtmlLinkExtractor;
import org.mariotaku.twidere.util.media.preview.provider.InstagramProvider;
import org.mariotaku.twidere.util.media.preview.provider.Provider;
import org.mariotaku.twidere.util.media.preview.provider.TwitterMediaProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mariotaku on 16/1/1.
 */
public class PreviewMediaExtractor {

    private static final Provider[] sProviders = {
            new InstagramProvider(),
            new TwitterMediaProvider()
    };

    private PreviewMediaExtractor() {
    }

    @Nullable
    public static ParcelableMedia fromLink(@NonNull String link) {
        final Provider provider = providerFor(link);
        if (provider == null) return null;
        return provider.from(link);
    }

    @Nullable
    @WorkerThread
    public static ParcelableMedia fromLink(@NonNull String link, RestHttpClient client, Object extra) throws IOException {
        final Provider provider = providerFor(link);
        if (provider == null) return null;
        return provider.from(link, client, extra);
    }

    @Nullable
    private static Provider providerFor(String link) {
        if (TextUtils.isEmpty(link)) return null;
        for (Provider provider : sProviders) {
            if (provider.supports(link)) {
                return provider;
            }
        }
        return null;
    }


    public static boolean isSupported(@Nullable String link) {
        return providerFor(link) != null;
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

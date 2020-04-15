package org.mariotaku.twidere.util.media.preview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import android.text.TextUtils;

import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.util.media.preview.provider.InstagramProvider;
import org.mariotaku.twidere.util.media.preview.provider.Provider;
import org.mariotaku.twidere.util.media.preview.provider.TwitterMediaProvider;

import java.io.IOException;

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

}

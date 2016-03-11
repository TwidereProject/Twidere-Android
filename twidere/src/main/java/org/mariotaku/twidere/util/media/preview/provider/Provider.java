package org.mariotaku.twidere.util.media.preview.provider;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.twidere.model.ParcelableMedia;

/**
 * Created by mariotaku on 16/1/1.
 */
public interface Provider {

    boolean supports(@NonNull String link);

    @Nullable
    ParcelableMedia from(@NonNull String url);

    @Nullable
    @WorkerThread
    ParcelableMedia from(@NonNull String link, @NonNull RestHttpClient client, @Nullable Object extra);
}

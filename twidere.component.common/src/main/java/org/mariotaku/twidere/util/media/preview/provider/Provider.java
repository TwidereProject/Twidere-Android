package org.mariotaku.twidere.util.media.preview.provider;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.mariotaku.twidere.model.ParcelableMedia;

/**
 * Created by mariotaku on 16/1/1.
 */
public interface Provider {

    @WorkerThread
    boolean supportsAuthority(@Nullable String authority);

    @WorkerThread
    ParcelableMedia from(Uri uri);

}

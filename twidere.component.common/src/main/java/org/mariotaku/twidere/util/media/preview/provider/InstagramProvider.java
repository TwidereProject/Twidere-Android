package org.mariotaku.twidere.util.media.preview.provider;

import android.net.Uri;
import android.support.annotation.WorkerThread;

import org.mariotaku.twidere.model.ParcelableMedia;

/**
 * Created by mariotaku on 16/1/1.
 */
public class InstagramProvider implements Provider {
    @Override
    @WorkerThread
    public boolean supportsAuthority(String authority) {
        return "instagr.am".equals(authority) || "instagram.com".equals(authority) ||
                "www.instagram.com".equals(authority);
    }

    @Override
    @WorkerThread
    public ParcelableMedia from(Uri uri) {
        return null;
    }
}

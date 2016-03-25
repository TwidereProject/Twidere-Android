package org.mariotaku.twidere.util.media;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.utils.IoUtils;

import org.mariotaku.mediaviewer.library.FileCache;
import org.mariotaku.twidere.provider.CacheProvider;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by mariotaku on 16/1/20.
 */
public class UILFileCache implements FileCache {
    private final DiskCache cache;

    public UILFileCache(final DiskCache cache) {
        this.cache = cache;
    }

    @Override
    public File get(@NonNull final String key) {
        return cache.get(key);
    }

    @Override
    public void remove(@NonNull final String key) {
        cache.remove(key);
    }

    @Override
    public void save(@NonNull final String key, @NonNull final InputStream is, byte[] extra,
                     final CopyListener listener) throws IOException {
        cache.save(key, is, new IoUtils.CopyListener() {
            @Override
            public boolean onBytesCopied(final int current, final int total) {
                return listener == null || listener.onCopied(current);
            }
        });
        if (extra != null) {
            cache.save(CacheProvider.getExtraKey(key), new ByteArrayInputStream(extra), null);
        }
    }

    @NonNull
    @Override
    public Uri toUri(@NonNull final String key) {
        return CacheProvider.getCacheUri(key, null);
    }

    @NonNull
    @Override
    public String fromUri(@NonNull final Uri uri) {
        return CacheProvider.getCacheKey(uri);
    }
}

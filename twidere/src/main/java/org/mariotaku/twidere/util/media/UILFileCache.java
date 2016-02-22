package org.mariotaku.twidere.util.media;

import android.net.Uri;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.utils.IoUtils;

import org.mariotaku.mediaviewer.library.FileCache;
import org.mariotaku.twidere.provider.CacheProvider;

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
    public File get(final String key) {
        return cache.get(key);
    }

    @Override
    public void remove(final String key) {
        cache.remove(key);
    }

    @Override
    public void save(final String key, final InputStream is, final CopyListener listener) throws IOException {
        cache.save(key, is, new IoUtils.CopyListener() {
            @Override
            public boolean onBytesCopied(final int current, final int total) {
                return listener == null || listener.onCopied(current);
            }
        });
    }

    @Override
    public Uri toUri(final String key) {
        return CacheProvider.getCacheUri(key, null);
    }

    @Override
    public String fromUri(final Uri uri) {
        return CacheProvider.getCacheKey(uri);
    }
}

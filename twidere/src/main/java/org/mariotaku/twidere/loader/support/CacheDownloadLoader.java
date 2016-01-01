/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.loader.support;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.AsyncTaskLoader;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.utils.IoUtils;

import org.mariotaku.twidere.util.SimpleDiskCacheUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;
import org.mariotaku.twidere.util.io.ContentLengthInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;


public abstract class CacheDownloadLoader extends AsyncTaskLoader<CacheDownloadLoader.Result> {

    private final Uri mUri;
    private final Handler mHandler;
    private final DownloadListener mListener;

    @Inject
    DiskCache mDiskCache;

    public CacheDownloadLoader(final Context context, final DownloadListener listener, final Uri uri) {
        super(context);
        GeneralComponentHelper.build(context).inject(this);
        mHandler = new Handler(Looper.getMainLooper());
        mUri = uri;
        mListener = listener;
    }

    @Override
    public CacheDownloadLoader.Result loadInBackground() {
        if (mUri == null) {
            return Result.nullInstance();
        }
        final String scheme = mUri.getScheme();
        File cacheFile = null;
        if ("http".equals(scheme) || "https".equals(scheme)) {
            final String uriString = mUri.toString();
            if (uriString == null) return Result.nullInstance();
            cacheFile = mDiskCache.get(uriString);
            if (isValid(cacheFile)) {
                return Result.getInstance(SimpleDiskCacheUtils.getCacheUri(uriString));
            }
            try {
                // from SD cache
                ContentLengthInputStream cis;
                final InputStream is = getStreamFromNetwork(uriString);
                if (is == null) return Result.nullInstance();
                try {
                    final long length = is.available();
                    mHandler.post(new DownloadStartRunnable(this, mListener, length));

                    cis = new ContentLengthInputStream(is, length);
                    mDiskCache.save(uriString, cis, new IoUtils.CopyListener() {
                        @Override
                        public boolean onBytesCopied(int length, int position) {
                            mHandler.post(new ProgressUpdateRunnable(mListener, position, length));
                            return !isAbandoned();
                        }
                    });
                    mHandler.post(new DownloadFinishRunnable(this, mListener));
                } finally {
                    Utils.closeSilently(is);
                }
                cacheFile = mDiskCache.get(uriString);
                if (isValid(cacheFile)) {
                    return Result.getInstance(SimpleDiskCacheUtils.getCacheUri(uriString));
                } else {
                    mDiskCache.remove(uriString);
                    throw new IOException();
                }
            } catch (final Exception e) {
                mHandler.post(new DownloadErrorRunnable(this, mListener, e));
                return Result.getInstance(e);
            }
        }
        return Result.getInstance(mUri);
    }

    protected abstract InputStream getStreamFromNetwork(String url) throws IOException;

    private static boolean isValid(File entry) {
        return entry != null;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }


    public interface DownloadListener {
        void onDownloadError(Throwable t);

        void onDownloadFinished();

        void onDownloadStart(long total);

        void onProgressUpdate(long current, long total);
    }

    public static class Result {
        public final Uri cacheUri;
        public final Exception exception;

        public Result(final Uri cacheUri, final Exception exception) {
            this.cacheUri = cacheUri;
            this.exception = exception;
        }

        public static Result getInstance(final Uri uri) {
            return new Result(uri, null);
        }

        public static Result getInstance(final Exception e) {
            return new Result(null, e);
        }

        public static Result nullInstance() {
            return new Result(null, null);
        }
    }

    private final static class DownloadErrorRunnable implements Runnable {

        private final CacheDownloadLoader loader;
        private final DownloadListener listener;
        private final Throwable t;

        DownloadErrorRunnable(final CacheDownloadLoader loader, final DownloadListener listener, final Throwable t) {
            this.loader = loader;
            this.listener = listener;
            this.t = t;
        }

        @Override
        public void run() {
            if (listener == null || loader.isAbandoned() || loader.isReset()) return;
            listener.onDownloadError(t);
        }
    }

    private final static class DownloadFinishRunnable implements Runnable {

        private final CacheDownloadLoader loader;
        private final DownloadListener listener;

        DownloadFinishRunnable(final CacheDownloadLoader loader, final DownloadListener listener) {
            this.loader = loader;
            this.listener = listener;
        }

        @Override
        public void run() {
            if (listener == null || loader.isAbandoned() || loader.isReset()) return;
            listener.onDownloadFinished();
        }
    }

    private final static class DownloadStartRunnable implements Runnable {

        private final CacheDownloadLoader loader;
        private final DownloadListener listener;
        private final long total;

        DownloadStartRunnable(final CacheDownloadLoader loader, final DownloadListener listener, final long total) {
            this.loader = loader;
            this.listener = listener;
            this.total = total;
        }

        @Override
        public void run() {
            if (listener == null || loader.isAbandoned() || loader.isReset()) return;
            listener.onDownloadStart(total);
        }
    }

    private final static class ProgressUpdateRunnable implements Runnable {

        private final DownloadListener listener;
        private final long current, total;

        ProgressUpdateRunnable(final DownloadListener listener, final long current, final long total) {
            this.listener = listener;
            this.current = current;
            this.total = total;
        }

        @Override
        public void run() {
            if (listener == null) return;
            listener.onProgressUpdate(current, total);
        }
    }
}

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
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;
import android.util.DisplayMetrics;

import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.utils.IoUtils;

import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;
import org.mariotaku.twidere.util.imageloader.AccountFullImageExtra;
import org.mariotaku.twidere.util.io.ContentLengthInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import cz.fhucho.android.util.SimpleDiskCache;
import okio.ByteString;

public class TileImageLoader extends AsyncTaskLoader<TileImageLoader.Result> {

    private final Uri mUri;
    private final Handler mHandler;
    private final DownloadListener mListener;
    @Inject
    ImageDownloader mDownloader;
    @Inject
    SimpleDiskCache mDiskCache;

    private final float mFallbackSize;
    private final long mAccountId;

    public TileImageLoader(final Context context, final DownloadListener listener, final long accountId, final Uri uri) {
        super(context);
        GeneralComponentHelper.build(context).inject(this);
        mHandler = new Handler();
        mAccountId = accountId;
        mUri = uri;
        mListener = listener;
        final Resources res = context.getResources();
        final DisplayMetrics dm = res.getDisplayMetrics();
        mFallbackSize = Math.max(dm.heightPixels, dm.widthPixels);
    }

    @Override
    public TileImageLoader.Result loadInBackground() {
        if (mUri == null) {
            return Result.nullInstance();
        }
        final String scheme = mUri.getScheme();
        SimpleDiskCache.InputStreamEntry cacheEntry = null;
        if ("http".equals(scheme) || "https".equals(scheme)) {
            final String uriString = mUri.toString();
            if (uriString == null) return Result.nullInstance();
            try {
                cacheEntry = mDiskCache.getInputStream(uriString);
            } catch (IOException e) {
                // Ignore
            }
            if (isValid(cacheEntry)) {
                return Result.getInstance(Uri.fromParts("cache",
                        ByteString.encodeUtf8(uriString).base64Url(), null));
            }
            try {
                // from SD cache
                ContentLengthInputStream cis = null;
                final InputStream is = mDownloader.getStream(uriString, new AccountFullImageExtra(mAccountId));
                if (is == null) return Result.nullInstance();
                try {
                    final long length = is.available();
                    mHandler.post(new DownloadStartRunnable(this, mListener, length));

                    final Map<String, String> metadata = new HashMap<>();
                    metadata.put("length", String.valueOf(length));

                    cis = new ContentLengthInputStream(is, length);
                    cis.setReadListener(new ContentLengthInputStream.ReadListener() {
                        @Override
                        public void onRead(long length, long position) {
                            mHandler.post(new ProgressUpdateRunnable(mListener, position));
                        }
                    });
                    mDiskCache.put(uriString, cis, metadata);
                    mHandler.post(new DownloadFinishRunnable(this, mListener));
                } finally {
                    IoUtils.closeSilently(is);
                }
                cacheEntry = mDiskCache.getInputStream(uriString);
                if (isValid(cacheEntry)) {
                    return Result.getInstance(Uri.fromParts("cache",
                            ByteString.encodeUtf8(uriString).base64Url(), null));
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

    private static boolean isValid(SimpleDiskCache.InputStreamEntry entry) {
        if (entry == null) return false;
        final Map<String, String> metadata = entry.getMetadata();
        final long length = NumberUtils.toLong(metadata.get("length"), -1);
        return length == -1 || entry.getLength() == length;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }


    public interface DownloadListener {
        void onDownloadError(Throwable t);

        void onDownloadFinished();

        void onDownloadStart(long total);

        void onProgressUpdate(long downloaded);
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

        private final TileImageLoader loader;
        private final DownloadListener listener;
        private final Throwable t;

        DownloadErrorRunnable(final TileImageLoader loader, final DownloadListener listener, final Throwable t) {
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

        private final TileImageLoader loader;
        private final DownloadListener listener;

        DownloadFinishRunnable(final TileImageLoader loader, final DownloadListener listener) {
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

        private final TileImageLoader loader;
        private final DownloadListener listener;
        private final long total;

        DownloadStartRunnable(final TileImageLoader loader, final DownloadListener listener, final long total) {
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
        private final long current;

        ProgressUpdateRunnable(final DownloadListener listener, final long current) {
            this.listener = listener;
            this.current = current;
        }

        @Override
        public void run() {
            if (listener == null) return;
            listener.onProgressUpdate(current);
        }
    }
}

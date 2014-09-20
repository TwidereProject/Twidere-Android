/*
 * 				Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.gallery3d;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;
import android.util.DisplayMetrics;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import org.mariotaku.gallery3d.util.BitmapUtils;
import org.mariotaku.gallery3d.util.GalleryUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.Exif;
import org.mariotaku.twidere.util.ImageValidator;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.imageloader.AccountExtra;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GLImageLoader extends AsyncTaskLoader<GLImageLoader.Result> implements Constants {

    private final Uri mUri;
    private final Handler mHandler;
    private final DownloadListener mListener;
    private final ImageDownloader mDownloader;
    private final DiskCache mDiskCache;

    private final float mFallbackSize;
    private final long mAccountId;

    public GLImageLoader(final Context context, final DownloadListener listener, final long accountId, final Uri uri) {
        super(context);
        mHandler = new Handler();
        mAccountId = accountId;
        mUri = uri;
        mListener = listener;
        final TwidereApplication app = TwidereApplication.getInstance(context);
        mDownloader = app.getFullImageDownloader();
        mDiskCache = app.getFullDiskCache();
        final Resources res = context.getResources();
        final DisplayMetrics dm = res.getDisplayMetrics();
        mFallbackSize = Math.max(dm.heightPixels, dm.widthPixels);
    }

    @Override
    public GLImageLoader.Result loadInBackground() {
        if (mUri == null) {
            return Result.nullInstance();
        }
        final String scheme = mUri.getScheme();
        if ("http".equals(scheme) || "https".equals(scheme)) {
            final String url = ParseUtils.parseString(mUri.toString());
            if (url == null) return Result.nullInstance();
            final File cacheFile = mDiskCache.get(url);
            if (cacheFile != null) {
                final File cacheDir = cacheFile.getParentFile();
                if (cacheDir != null && !cacheDir.exists()) {
                    cacheDir.mkdirs();
                }
            } else
                return Result.nullInstance();
            try {
                // from SD cache
                if (ImageValidator.checkImageValidity(cacheFile))
                    return decodeImageInternal(cacheFile);

                final InputStream is = mDownloader.getStream(url, new AccountExtra(mAccountId));
                if (is == null) return Result.nullInstance();
                final long length = is.available();
                mHandler.post(new DownloadStartRunnable(this, mListener, length));
                final OutputStream os = new FileOutputStream(cacheFile);
                try {
                    dump(is, os);
                    mHandler.post(new DownloadFinishRunnable(this, mListener));
                } finally {
                    GalleryUtils.closeSilently(is);
                    GalleryUtils.closeSilently(os);
                }
                if (!ImageValidator.checkImageValidity(cacheFile)) {
                    // The file is corrupted, so we remove it from
                    // cache.
                    final Result result = decodeBitmapOnly(cacheFile);
                    if (cacheFile.isFile()) {
                        cacheFile.delete();
                    }
                    return result;
                }
                return decodeImageInternal(cacheFile);
            } catch (final Exception e) {
                mHandler.post(new DownloadErrorRunnable(this, mListener, e));
                return Result.getInstance(cacheFile, e);
            }
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            final File file = new File(mUri.getPath());
            try {
                return decodeImage(file);
            } catch (final Exception e) {
                return Result.getInstance(file, e);
            }
        }
        return Result.nullInstance();
    }

    protected Result decodeBitmapOnly(final File file) {
        final String path = file.getAbsolutePath();
        final BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        o.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(path, o);
        final int width = o.outWidth, height = o.outHeight;
        if (width <= 0 || height <= 0) return Result.getInstance(file, null);
        o.inJustDecodeBounds = false;
        o.inSampleSize = BitmapUtils.computeSampleSize(mFallbackSize / Math.max(width, height));
        final Bitmap bitmap = BitmapFactory.decodeFile(path, o);
        return Result.getInstance(bitmap, Exif.getOrientation(file), file);
    }

    protected Result decodeImage(final File file) {
        final String path = file.getAbsolutePath();
        try {
            final BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(path, false);
            final int width = decoder.getWidth();
            final int height = decoder.getHeight();
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = BitmapUtils.computeSampleSize(mFallbackSize / Math.max(width, height));
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            final Bitmap bitmap = decoder.decodeRegion(new Rect(0, 0, width, height), options);
            return Result.getInstance(decoder, bitmap, Exif.getOrientation(file), file);
        } catch (final IOException e) {
            return decodeBitmapOnly(file);
        }
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    private Result decodeImageInternal(final File file) throws IOException {
        if (ImageValidator.checkImageValidity(file)) return decodeImage(file);
        throw new InvalidImageException();
    }

    private void dump(final InputStream is, final OutputStream os) throws IOException {
        final byte buffer[] = new byte[1024];
        int rc = is.read(buffer, 0, buffer.length);
        long downloaded = 0;
        while (rc > 0) {
            downloaded += rc;
            mHandler.post(new ProgressUpdateRunnable(mListener, downloaded));
            os.write(buffer, 0, rc);
            rc = is.read(buffer, 0, buffer.length);
        }
    }

    public static interface DownloadListener {
        void onDownloadError(Throwable t);

        void onDownloadFinished();

        void onDownloadStart(long total);

        void onProgressUpdate(long downloaded);
    }

    public static class InvalidImageException extends IOException {

        private static final long serialVersionUID = 8996099908714452289L;

    }

    public static class Result {
        public final Bitmap bitmap;
        public final File file;
        public final Exception exception;
        public final BitmapRegionDecoder decoder;
        public final int orientation;

        public Result(final BitmapRegionDecoder decoder, final Bitmap bitmap, final int orientation, final File file,
                      final Exception exception) {
            this.bitmap = bitmap;
            this.file = file;
            this.decoder = decoder;
            this.orientation = orientation;
            this.exception = exception;
        }

        public static Result getInstance(final Bitmap bitmap, final int orientation, final File file) {
            return new Result(null, bitmap, orientation, file, null);
        }

        public static Result getInstance(final BitmapRegionDecoder decoder, final Bitmap bitmap, final int orientation,
                                         final File file) {
            return new Result(decoder, bitmap, orientation, file, null);
        }

        public static Result getInstance(final File file, final Exception e) {
            return new Result(null, null, 0, file, e);
        }

        public static Result nullInstance() {
            return new Result(null, null, 0, null, null);
        }
    }

    private final static class DownloadErrorRunnable implements Runnable {

        private final GLImageLoader loader;
        private final DownloadListener listener;
        private final Throwable t;

        DownloadErrorRunnable(final GLImageLoader loader, final DownloadListener listener, final Throwable t) {
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

        private final GLImageLoader loader;
        private final DownloadListener listener;

        DownloadFinishRunnable(final GLImageLoader loader, final DownloadListener listener) {
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

        private final GLImageLoader loader;
        private final DownloadListener listener;
        private final long total;

        DownloadStartRunnable(final GLImageLoader loader, final DownloadListener listener, final long total) {
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

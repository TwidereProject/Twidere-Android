/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.imageloader.AccountExtra;
import org.mariotaku.twidere.util.imageloader.OvalBitmapDisplayer;

import static org.mariotaku.twidere.util.Utils.getBestBannerUrl;

public class MediaLoaderWrapper implements Constants {

    private final ImageLoader mImageLoader;
    private final DisplayImageOptions mProfileImageDisplayOptions;
    private final DisplayImageOptions mDashboardProfileImageDisplayOptions;
    private final DisplayImageOptions mOvalProfileImageDisplayOptions;
    private final DisplayImageOptions mImageDisplayOptions, mBannerDisplayOptions;
    private final VideoLoader mVideoLoader;

    public MediaLoaderWrapper(final ImageLoader imageLoader, VideoLoader videoLoader) {
        mImageLoader = imageLoader;
        mVideoLoader = videoLoader;
        final DisplayImageOptions.Builder profileOptsBuilder = new DisplayImageOptions.Builder();
        profileOptsBuilder.cacheInMemory(true);
        profileOptsBuilder.cacheOnDisk(true);
        profileOptsBuilder.bitmapConfig(Bitmap.Config.ARGB_8888);
        profileOptsBuilder.resetViewBeforeLoading(true);
        final DisplayImageOptions.Builder ovalProfileOptsBuilder = new DisplayImageOptions.Builder();
        ovalProfileOptsBuilder.cacheInMemory(true);
        ovalProfileOptsBuilder.cacheOnDisk(true);
        ovalProfileOptsBuilder.bitmapConfig(Bitmap.Config.ARGB_8888);
        ovalProfileOptsBuilder.displayer(new OvalBitmapDisplayer());
        ovalProfileOptsBuilder.resetViewBeforeLoading(true);
        final DisplayImageOptions.Builder imageOptsBuilder = new DisplayImageOptions.Builder();
        imageOptsBuilder.cacheInMemory(true);
        imageOptsBuilder.cacheOnDisk(true);
        imageOptsBuilder.bitmapConfig(Bitmap.Config.RGB_565);
        imageOptsBuilder.resetViewBeforeLoading(true);
        final DisplayImageOptions.Builder bannerOptsBuilder = new DisplayImageOptions.Builder();
        bannerOptsBuilder.resetViewBeforeLoading(true);
        bannerOptsBuilder.showImageOnLoading(android.R.color.transparent);
        bannerOptsBuilder.cacheInMemory(true);
        bannerOptsBuilder.cacheOnDisk(true);
        bannerOptsBuilder.bitmapConfig(Bitmap.Config.RGB_565);
        bannerOptsBuilder.displayer(new FadeInBitmapDisplayer(200, true, true, true));
        final DisplayImageOptions.Builder dashboardProfileOptsBuilder = new DisplayImageOptions.Builder();
        dashboardProfileOptsBuilder.cacheInMemory(true);
        dashboardProfileOptsBuilder.cacheOnDisk(true);
        dashboardProfileOptsBuilder.bitmapConfig(Bitmap.Config.RGB_565);

        mProfileImageDisplayOptions = profileOptsBuilder.build();
        mOvalProfileImageDisplayOptions = ovalProfileOptsBuilder.build();
        mImageDisplayOptions = imageOptsBuilder.build();
        mBannerDisplayOptions = bannerOptsBuilder.build();
        mDashboardProfileImageDisplayOptions = dashboardProfileOptsBuilder.build();
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public void clearFileCache() {
        mImageLoader.clearDiskCache();
    }

    public void clearMemoryCache() {
        mImageLoader.clearMemoryCache();
    }

    public void displayPreviewImage(final String uri, final ImageView view) {
        mImageLoader.displayImage(uri, view, mImageDisplayOptions);
    }

    public void displayPreviewImage(final ImageView view, final String url, final MediaLoadingHandler loadingHandler) {
        mImageLoader.displayImage(url, view, mImageDisplayOptions, loadingHandler, loadingHandler);
    }

    public void displayPreviewImageWithCredentials(final ImageView view, final String url, final long accountId,
                                                   final MediaLoadingHandler loadingHandler) {
        if (accountId <= 0) {
            displayPreviewImage(view, url, loadingHandler);
            return;
        }
        final DisplayImageOptions.Builder b = new DisplayImageOptions.Builder();
        b.cloneFrom(mImageDisplayOptions);
        b.extraForDownloader(new AccountExtra(accountId));
        mImageLoader.displayImage(url, view, b.build(), loadingHandler, loadingHandler);
    }

    public void displayProfileBanner(final ImageView view, final String url,
                                     final ImageLoadingListener listener) {
        mImageLoader.displayImage(url, view, mBannerDisplayOptions, listener);
    }

    public void displayProfileBanner(final ImageView view, final String url) {
        displayProfileBanner(view, url, null);
    }

    public void displayProfileBanner(final ImageView view, final String baseUrl, final int width) {
        displayProfileBanner(view, getBestBannerUrl(baseUrl, width));
    }

    public void displayProfileImage(final ImageView view, final String url) {
        mImageLoader.displayImage(url, view, mProfileImageDisplayOptions);
    }

    public void displayDashboardProfileImage(final ImageView view, final String url, Drawable drawableOnLoading) {
        if (drawableOnLoading != null) {
            final Builder builder = new Builder();
            builder.cloneFrom(mDashboardProfileImageDisplayOptions);
            builder.showImageOnLoading(drawableOnLoading);
            builder.showImageOnFail(drawableOnLoading);
            mImageLoader.displayImage(url, view, builder.build());
            return;
        }
        mImageLoader.displayImage(url, view, mDashboardProfileImageDisplayOptions);
    }


    public void displayImage(final ImageView view, final String url, DisplayImageOptions options) {
        mImageLoader.displayImage(url, view, options);
    }

    public DisplayImageOptions getProfileImageDisplayOptions() {
        return mProfileImageDisplayOptions;
    }

    public void displayProfileImage(final ImageView view, final String url, final ImageLoadingListener listener) {
        mImageLoader.displayImage(url, view, mProfileImageDisplayOptions, listener);
    }

    public void loadProfileImage(final String url, final ImageLoadingListener listener) {
        mImageLoader.loadImage(url, mProfileImageDisplayOptions, listener);
    }

    public void displayOvalProfileImage(final String url, final ImageView view) {
        mImageLoader.displayImage(url, view, mOvalProfileImageDisplayOptions);
    }

    public void cancelDisplayTask(ImageView imageView) {
        mImageLoader.cancelDisplayTask(imageView);
    }
}

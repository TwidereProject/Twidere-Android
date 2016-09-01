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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableUserUtils;
import org.mariotaku.twidere.util.imageloader.OvalBitmapDisplayer;
import org.mariotaku.twidere.util.media.MediaExtra;

import javax.inject.Singleton;

import static org.mariotaku.twidere.util.InternalTwitterContentUtils.getBestBannerUrl;

@Singleton
public class MediaLoaderWrapper {

    private final ImageLoader mImageLoader;
    private final DisplayImageOptions mProfileImageDisplayOptions;
    private final DisplayImageOptions mDashboardProfileImageDisplayOptions;
    private final DisplayImageOptions mOvalProfileImageDisplayOptions;
    private final DisplayImageOptions mImageDisplayOptions, mBannerDisplayOptions;

    public MediaLoaderWrapper(final ImageLoader loader) {
        mImageLoader = loader;
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

    public void displayPreviewImageWithCredentials(final ImageView view, final String url,
                                                   final UserKey accountKey,
                                                   final MediaLoadingHandler loadingHandler) {
        if (accountKey == null) {
            displayPreviewImage(view, url, loadingHandler);
            return;
        }
        final DisplayImageOptions.Builder b = new DisplayImageOptions.Builder();
        b.cloneFrom(mImageDisplayOptions);
        MediaExtra extra = new MediaExtra();
        extra.setAccountKey(accountKey);
        b.extraForDownloader(extra);
        mImageLoader.displayImage(url, view, b.build(), loadingHandler, loadingHandler);
    }

    public void displayProfileBanner(final ImageView view, final String url,
                                     final ImageLoadingListener listener) {
        mImageLoader.displayImage(url, view, mBannerDisplayOptions, listener);
    }

    public void displayProfileBanner(final ImageView view, final String url, final int width,
                                     final ImageLoadingListener listener) {
        mImageLoader.displayImage(getBestBannerUrl(url, width), view, mBannerDisplayOptions,
                listener);
    }

    public void displayProfileBanner(final ImageView view, final String url) {
        displayProfileBanner(view, url, null);
    }

    public void displayProfileBanner(final ImageView view, final String baseUrl, final int width) {
        displayProfileBanner(view, getBestBannerUrl(baseUrl, width));
    }


    public void displayProfileBanner(final ImageView view, final ParcelableAccount account, final int width) {
        displayProfileBanner(view, getBestBannerUrl(getBannerUrl(account), width));
    }

    private String getBannerUrl(ParcelableAccount account) {
        String bannerUrl = account.profile_banner_url;
        if (bannerUrl == null && ParcelableAccount.Type.FANFOU.equals(account.account_type)) {
            if (account.account_user != null) {
                bannerUrl = ParcelableUserUtils.getProfileBannerUrl(account.account_user);
            }
        }
        return bannerUrl;
    }

    public void displayOriginalProfileImage(final ImageView view, final ParcelableUser user) {
        if (user.extras != null && !TextUtils.isEmpty(user.extras.profile_image_url_original)) {
            displayProfileImage(view, user.extras.profile_image_url_original);
        } else if (user.extras != null && !TextUtils.isEmpty(user.extras.profile_image_url_profile_size)) {
            displayProfileImage(view, user.extras.profile_image_url_profile_size);
        } else {
            displayProfileImage(view, Utils.getOriginalTwitterProfileImage(user.profile_image_url));
        }
    }

    public void displayProfileImage(final ImageView view, final ParcelableUser user) {
        if (user.extras != null && !TextUtils.isEmpty(user.extras.profile_image_url_profile_size)) {
            displayProfileImage(view, user.extras.profile_image_url_profile_size);
        } else {
            displayProfileImage(view, user.profile_image_url);
        }
    }

    public void displayProfileImage(final ImageView view, final ParcelableAccount account) {
        if (account.account_user != null && account.account_user.extras != null
                && !TextUtils.isEmpty(account.account_user.extras.profile_image_url_profile_size)) {
            displayProfileImage(view, account.account_user.extras.profile_image_url_profile_size);
        } else {
            displayProfileImage(view, account.profile_image_url);
        }
    }

    public void displayProfileImage(final ImageView view, final ParcelableStatus status) {
        if (status.extras != null && !TextUtils.isEmpty(status.extras.user_profile_image_url_profile_size)) {
            displayProfileImage(view, status.extras.user_profile_image_url_profile_size);
        } else {
            displayProfileImage(view, status.user_profile_image_url);
        }
    }

    public void displayProfileImage(final ImageView view, final String url) {
        mImageLoader.displayImage(url, view, mProfileImageDisplayOptions);
    }

    public Bitmap loadImageSync(String uri) {
        return mImageLoader.loadImageSync(uri);
    }

    public Bitmap loadImageSync(String uri, DisplayImageOptions options) {
        return mImageLoader.loadImageSync(uri, options);
    }

    public Bitmap loadImageSync(String uri, ImageSize targetImageSize) {
        return mImageLoader.loadImageSync(uri, targetImageSize);
    }

    public Bitmap loadImageSync(String uri, ImageSize targetImageSize, DisplayImageOptions options) {
        return mImageLoader.loadImageSync(uri, targetImageSize, options);
    }

    public void displayDashboardProfileImage(@NonNull final ImageView view,
                                             @NonNull final ParcelableAccount account,
                                             @Nullable final Drawable drawableOnLoading) {
        if (account.account_user != null && account.account_user.extras != null
                && !TextUtils.isEmpty(account.account_user.extras.profile_image_url_profile_size)) {
            displayDashboardProfileImage(view, account.account_user.extras.profile_image_url_profile_size,
                    drawableOnLoading);
        } else {
            displayDashboardProfileImage(view, account.profile_image_url, drawableOnLoading);
        }
    }

    void displayDashboardProfileImage(final ImageView view, final String url, Drawable drawableOnLoading) {
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


    public void displayImage(final ImageView view, final String url) {
        mImageLoader.displayImage(url, view);
    }

    public void displayProfileImage(final ImageView view, final String url, final ImageLoadingListener listener) {
        mImageLoader.displayImage(url, view, mProfileImageDisplayOptions, listener);
    }

    public void loadProfileImage(final ParcelableAccount account, final ImageLoadingListener listener) {
        if (account.account_user != null && account.account_user.extras != null
                && !TextUtils.isEmpty(account.account_user.extras.profile_image_url_profile_size)) {
            loadProfileImage(account.account_user.extras.profile_image_url_profile_size, listener);
        } else {
            loadProfileImage(account.profile_image_url, listener);
        }
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

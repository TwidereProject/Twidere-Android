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

package org.mariotaku.twidere.util

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.widget.ImageView
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.ImageSize
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.mediaPreloadKey
import org.mariotaku.twidere.constant.mediaPreloadOnWifiOnlyKey
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.model.util.getActivityStatus
import org.mariotaku.twidere.util.InternalTwitterContentUtils.getBestBannerUrl
import org.mariotaku.twidere.util.media.MediaExtra
import javax.inject.Singleton

@Singleton
class MediaLoaderWrapper(val imageLoader: ImageLoader) {

    var isNetworkMetered: Boolean = true
    private var preloadEnabled: Boolean = false
    private var preloadOnWifiOnly: Boolean = true

    private val shouldPreload: Boolean get() = preloadEnabled && (!preloadOnWifiOnly || !isNetworkMetered)

    private val profileImageDisplayOptions = DisplayImageOptions.Builder()
            .resetViewBeforeLoading(true)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build()

    private val dashboardProfileImageDisplayOptions = DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build()

    private val previewDisplayOptions = DisplayImageOptions.Builder()
            .resetViewBeforeLoading(true)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build()

    private val videoPreviewDisplayOptions = DisplayImageOptions.Builder()
            .resetViewBeforeLoading(true)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .showImageForEmptyUri(R.color.material_grey)
            .showImageOnFail(R.color.material_grey)
            .build()

    private val bannerDisplayOptions = DisplayImageOptions.Builder()
            .resetViewBeforeLoading(true)
            .showImageOnLoading(android.R.color.transparent)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build()


    private val stickerDisplayOptions = DisplayImageOptions.Builder()
            .resetViewBeforeLoading(true)
            .showImageOnLoading(android.R.color.transparent)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .build()

    fun displayPreviewImage(view: ImageView, url: String?, video: Boolean = false) {
        val options = if (video) videoPreviewDisplayOptions else previewDisplayOptions
        imageLoader.displayImage(url, view, options)
    }

    fun displayPreviewImage(view: ImageView, url: String?, loadingHandler: MediaLoadingHandler?,
            video: Boolean = false) {
        val options = if (video) videoPreviewDisplayOptions else previewDisplayOptions
        imageLoader.displayImage(url, view, options, loadingHandler, loadingHandler)
    }

    fun displayPreviewImageWithCredentials(view: ImageView, url: String?, accountKey: UserKey?,
            loadingHandler: MediaLoadingHandler?, video: Boolean = false) {
        if (accountKey == null) {
            displayPreviewImage(view, url, loadingHandler)
            return
        }
        val b = DisplayImageOptions.Builder()
        b.cloneFrom(if (video) videoPreviewDisplayOptions else previewDisplayOptions)
        val extra = MediaExtra()
        extra.accountKey = accountKey
        b.extraForDownloader(extra)
        imageLoader.displayImage(url, view, b.build(), loadingHandler, loadingHandler)
    }


    fun displayProfileBanner(view: ImageView, url: String?, listener: ImageLoadingListener? = null) {
        imageLoader.displayImage(url, view, bannerDisplayOptions, listener)
    }


    fun displayProfileBanner(view: ImageView, baseUrl: String?, width: Int) {
        displayProfileBanner(view, getBestBannerUrl(baseUrl, width))
    }

    fun displayProfileBanner(view: ImageView, account: AccountDetails, width: Int) {
        displayProfileBanner(view, getBestBannerUrl(ParcelableUserUtils.getProfileBannerUrl(account.user), width))
    }

    fun displayOriginalProfileImage(view: ImageView, user: ParcelableUser) {
        if (user.extras != null && !TextUtils.isEmpty(user.extras.profile_image_url_original)) {
            displayProfileImage(view, user.extras.profile_image_url_original)
        } else if (user.extras != null && !TextUtils.isEmpty(user.extras.profile_image_url_profile_size)) {
            displayProfileImage(view, user.extras.profile_image_url_profile_size)
        } else {
            displayProfileImage(view, Utils.getOriginalTwitterProfileImage(user.profile_image_url))
        }
    }

    fun displayProfileImage(view: ImageView, user: ParcelableUser) {
        if (user.extras != null && !TextUtils.isEmpty(user.extras.profile_image_url_profile_size)) {
            displayProfileImage(view, user.extras.profile_image_url_profile_size)
        } else {
            displayProfileImage(view, user.profile_image_url)
        }
    }

    fun displayProfileImage(view: ImageView, userList: ParcelableUserList) {
        displayProfileImage(view, userList.user_profile_image_url)
    }

    fun displayProfileImage(view: ImageView, account: AccountDetails) {
        if (account.user.extras != null && !TextUtils.isEmpty(account.user.extras.profile_image_url_profile_size)) {
            displayProfileImage(view, account.user.extras.profile_image_url_profile_size)
        } else {
            displayProfileImage(view, account.user.profile_image_url)
        }
    }

    fun displayProfileImage(view: ImageView, status: ParcelableStatus) {
        if (status.extras != null && !TextUtils.isEmpty(status.extras.user_profile_image_url_profile_size)) {
            displayProfileImage(view, status.extras.user_profile_image_url_profile_size)
        } else {
            displayProfileImage(view, status.user_profile_image_url)
        }
    }

    fun displayProfileImage(view: ImageView, url: String?) {
        imageLoader.displayImage(url, view, profileImageDisplayOptions)
    }

    fun loadImageSync(url: String, targetImageSize: ImageSize, options: DisplayImageOptions): Bitmap? {
        return imageLoader.loadImageSync(url, targetImageSize, options)
    }

    fun displayDashboardProfileImage(view: ImageView, account: AccountDetails, drawableOnLoading: Drawable?) {
        if (account.user.extras != null && !TextUtils.isEmpty(account.user.extras.profile_image_url_profile_size)) {
            displayDashboardProfileImage(view, account.user.extras.profile_image_url_profile_size,
                    drawableOnLoading)
        } else {
            displayDashboardProfileImage(view, account.user.profile_image_url, drawableOnLoading)
        }
    }


    fun displayImage(view: ImageView, url: String?) {
        imageLoader.displayImage(url, view)
    }


    fun displayStickerImage(view: ImageView, url: String?) {
        imageLoader.displayImage(url, view, stickerDisplayOptions)
    }

    fun displayProfileImage(view: ImageView, url: String?, listener: ImageLoadingListener) {
        imageLoader.displayImage(url, view, profileImageDisplayOptions, listener)
    }

    fun cancelDisplayTask(imageView: ImageView) {
        imageLoader.cancelDisplayTask(imageView)
    }

    fun preloadStatus(status: ParcelableStatus) {
        if (!shouldPreload) return
        preloadProfileImage(status.user_profile_image_url)
        preloadProfileImage(status.quoted_user_profile_image)
        preloadMedia(status.media)
        preloadMedia(status.quoted_media)
    }

    fun preloadActivity(activity: ParcelableActivity) {
        if (!shouldPreload) return
        activity.getActivityStatus()?.let { preloadStatus(it) }
    }

    fun clearFileCache() {
        imageLoader.clearDiskCache()
    }

    fun clearMemoryCache() {
        imageLoader.clearMemoryCache()
    }

    fun reloadOptions(preferences: SharedPreferences) {
        preloadEnabled = preferences[mediaPreloadKey]
        preloadOnWifiOnly = preferences[mediaPreloadOnWifiOnlyKey]
    }

    private fun displayDashboardProfileImage(view: ImageView, url: String?, drawableOnLoading: Drawable?) {
        if (drawableOnLoading != null) {
            val builder = Builder()
            builder.cloneFrom(dashboardProfileImageDisplayOptions)
            builder.showImageOnLoading(drawableOnLoading)
            builder.showImageOnFail(drawableOnLoading)
            imageLoader.displayImage(url, view, builder.build())
            return
        }
        imageLoader.displayImage(url, view, dashboardProfileImageDisplayOptions)
    }

    private fun preloadMedia(media: Array<ParcelableMedia>?) {
        media?.forEach { item ->
            val url = item.preview_url ?: item.media_url ?: return@forEach
            preloadPreviewImage(url)
        }
    }

    private fun preloadProfileImage(url: String?) {
        if (url == null) return
        imageLoader.loadImage(url, profileImageDisplayOptions, null)
    }

    private fun preloadPreviewImage(url: String?) {
        if (url == null) return
        imageLoader.loadImage(url, previewDisplayOptions, null)
    }

}

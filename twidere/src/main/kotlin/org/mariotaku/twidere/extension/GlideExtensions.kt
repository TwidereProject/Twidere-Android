/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.extension

import android.content.Context
import android.text.TextUtils
import com.bumptech.glide.DrawableRequestBuilder
import com.bumptech.glide.RequestManager
import jp.wasabeef.glide.transformations.CropCircleTransformation
import org.mariotaku.twidere.R
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.util.Utils

fun RequestManager.loadProfileImage(context: Context, url: String?): DrawableRequestBuilder<String?> {
    val size = context.getString(R.string.profile_image_size)
    return load(Utils.getTwitterProfileImageOfSize(url, size)).bitmapTransform(CropCircleTransformation(context))
}

fun RequestManager.loadProfileImage(context: Context, resourceId: Int): DrawableRequestBuilder<Int> {
    return load(resourceId).bitmapTransform(CropCircleTransformation(context))
}

fun RequestManager.loadProfileImage(context: Context, account: AccountDetails): DrawableRequestBuilder<String?> {
    return loadProfileImage(context, account.user)
}

fun RequestManager.loadProfileImage(context: Context, user: ParcelableUser): DrawableRequestBuilder<String?> {
    if (user.extras != null && !TextUtils.isEmpty(user.extras.profile_image_url_profile_size)) {
        return load(user.extras.profile_image_url_profile_size)
    } else {
        return load(user.profile_image_url)
    }
}


fun RequestManager.loadProfileImage(context: Context, status: ParcelableStatus): DrawableRequestBuilder<String?> {
    if (status.extras != null && status.extras.user_profile_image_url_fallback == null) {
        // No fallback image, use compatible logic
        return loadProfileImage(context, status.user_profile_image_url)
    }
    return load(status.user_profile_image_url).bitmapTransform(CropCircleTransformation(context))
}

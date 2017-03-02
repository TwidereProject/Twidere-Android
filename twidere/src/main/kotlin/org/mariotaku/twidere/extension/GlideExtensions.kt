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
import org.mariotaku.twidere.model.AccountDetails

fun RequestManager.loadProfileImage(context: Context, url: String?): DrawableRequestBuilder<String?> {
    return load(url).bitmapTransform(CropCircleTransformation(context))
}

fun RequestManager.loadProfileImage(context: Context, resourceId: Int): DrawableRequestBuilder<Int> {
    return load(resourceId).bitmapTransform(CropCircleTransformation(context))
}

fun RequestManager.loadProfileImage(context: Context, account: AccountDetails): DrawableRequestBuilder<String?> {
    if (account.user.extras != null && !TextUtils.isEmpty(account.user.extras.profile_image_url_profile_size)) {
        return load(account.user.extras.profile_image_url_profile_size)
    } else {
        return load(account.user.profile_image_url)
    }
}
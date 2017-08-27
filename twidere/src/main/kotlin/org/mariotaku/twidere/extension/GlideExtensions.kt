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
import com.bumptech.glide.DrawableRequestBuilder
import com.bumptech.glide.DrawableTypeRequest
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import jp.wasabeef.glide.transformations.CropCircleTransformation
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.ImageShapeStyle
import org.mariotaku.twidere.extension.model.getBestProfileBanner
import org.mariotaku.twidere.extension.model.originalProfileImage
import org.mariotaku.twidere.extension.model.user
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.glide.RoundedRectTransformation

fun RequestManager.loadProfileImage(context: Context, url: String?, @ImageShapeStyle style: Int,
        cornerRadius: Float = 0f, cornerRadiusRatio: Float = 0f, size: String? = null): DrawableRequestBuilder<String?> {
    return configureLoadProfileImage(context, style, cornerRadius, cornerRadiusRatio) {
        if (url == null || size == null) {
            return@configureLoadProfileImage load(url)
        } else {
            return@configureLoadProfileImage load(Utils.getTwitterProfileImageOfSize(url, size))
        }
    }
}

fun RequestManager.loadProfileImage(context: Context, resourceId: Int, @ImageShapeStyle shapeStyle: Int,
        cornerRadius: Float = 0f, cornerRadiusRatio: Float = 0f): DrawableRequestBuilder<Int> {
    return configureLoadProfileImage(context, shapeStyle, cornerRadius, cornerRadiusRatio) { load(resourceId) }
}

fun RequestManager.loadProfileImage(context: Context, account: AccountDetails, @ImageShapeStyle shapeStyle: Int,
        cornerRadius: Float = 0f, cornerRadiusRatio: Float = 0f, size: String? = null): DrawableRequestBuilder<String?> {
    return loadProfileImage(context, account.user, shapeStyle, cornerRadius, cornerRadiusRatio, size)
}

fun RequestManager.loadProfileImage(context: Context, user: ParcelableUser, @ImageShapeStyle shapeStyle: Int,
        cornerRadius: Float = 0f, cornerRadiusRatio: Float = 0f, size: String? = null): DrawableRequestBuilder<String?> {
    if (user.extras != null && user.extras?.profile_image_url_fallback == null) {
        // No fallback image, use compatible logic
        return loadProfileImage(context, user.profile_image_url, shapeStyle, cornerRadius,
                cornerRadiusRatio, size)
    }
    return configureLoadProfileImage(context, shapeStyle, cornerRadius, cornerRadiusRatio) {
        if (size != null) {
            return@configureLoadProfileImage load(Utils.getTwitterProfileImageOfSize(user.profile_image_url, size))
        } else {
            return@configureLoadProfileImage load(user.profile_image_url)
        }
    }
}

fun RequestManager.loadProfileImage(context: Context, user: ParcelableLiteUser, @ImageShapeStyle shapeStyle: Int,
        cornerRadius: Float = 0f, cornerRadiusRatio: Float = 0f, size: String? = null): DrawableRequestBuilder<String?> {
    return configureLoadProfileImage(context, shapeStyle, cornerRadius, cornerRadiusRatio) {
        if (size != null) {
            return@configureLoadProfileImage load(Utils.getTwitterProfileImageOfSize(user.profile_image_url, size))
        } else {
            return@configureLoadProfileImage load(user.profile_image_url)
        }
    }
}

fun RequestManager.loadProfileImage(context: Context, userList: ParcelableUserList,
        @ImageShapeStyle shapeStyle: Int,
        cornerRadius: Float = 0f, cornerRadiusRatio: Float = 0f): DrawableRequestBuilder<String?> {
    return configureLoadProfileImage(context, shapeStyle, cornerRadius, cornerRadiusRatio) {
        load(userList.user_profile_image_url)
    }
}

fun RequestManager.loadProfileImage(context: Context, group: ParcelableGroup,
        @ImageShapeStyle shapeStyle: Int,
        cornerRadius: Float = 0f, cornerRadiusRatio: Float = 0f): DrawableRequestBuilder<String?> {
    return configureLoadProfileImage(context, shapeStyle, cornerRadius, cornerRadiusRatio) {
        load(group.homepage_logo)
    }
}

fun RequestManager.loadProfileImage(context: Context, status: ParcelableStatus, @ImageShapeStyle shapeStyle: Int,
        cornerRadius: Float = 0f, cornerRadiusRatio: Float = 0f, size: String? = null): DrawableRequestBuilder<String?> {
    if (status.extras?.user_profile_image_url_fallback == null) {
        // No fallback image, use compatible logic
        return loadProfileImage(context, status.user_profile_image_url, shapeStyle, cornerRadius,
                cornerRadiusRatio, size)
    }
    return configureLoadProfileImage(context, shapeStyle, cornerRadius, cornerRadiusRatio) {
        load(status.user_profile_image_url)
    }
}

fun RequestManager.loadProfileImage(context: Context, conversation: ParcelableMessageConversation,
        @ImageShapeStyle shapeStyle: Int, cornerRadius: Float = 0f, cornerRadiusRatio: Float = 0f,
        size: String? = null): DrawableRequestBuilder<*> {
    if (conversation.conversation_type == ParcelableMessageConversation.ConversationType.ONE_TO_ONE) {
        val user = conversation.user
        if (user != null) {
            return loadProfileImage(context, user, shapeStyle, cornerRadius, cornerRadiusRatio, size)
        } else {
            // TODO: show default conversation icon
            return loadProfileImage(context, R.drawable.ic_profile_image_default_group, shapeStyle,
                    cornerRadius, cornerRadiusRatio)
        }
    } else {
        if (conversation.conversation_avatar != null) {
            return loadProfileImage(context, conversation.conversation_avatar, shapeStyle, cornerRadius,
                    cornerRadiusRatio, size)
        } else {
            return loadProfileImage(context, R.drawable.ic_profile_image_default_group, shapeStyle,
                    cornerRadius, cornerRadiusRatio)
        }
    }
}

fun RequestManager.loadOriginalProfileImage(context: Context, user: ParcelableUser,
        @ImageShapeStyle shapeStyle: Int, cornerRadius: Float = 0f, cornerRadiusRatio: Float = 0f
): DrawableRequestBuilder<String> {
    return configureLoadProfileImage(context, shapeStyle, cornerRadius, cornerRadiusRatio) {
        load(user.originalProfileImage)
    }
}

fun RequestManager.loadProfileBanner(context: Context, user: ParcelableUser, width: Int): DrawableTypeRequest<String?> {
    val ratio = context.resources.getFraction(R.fraction.aspect_ratio_profile_banner, 1, 1)
    return load(user.getBestProfileBanner(width, (width / ratio).toInt()))
}

internal inline fun <T> configureLoadProfileImage(context: Context, @ImageShapeStyle shapeStyle: Int,
        cornerRadius: Float = 0f, cornerRadiusRatio: Float = 0f, create: () -> DrawableTypeRequest<T>
): DrawableRequestBuilder<T> {
    val builder = create()
    builder.diskCacheStrategy(DiskCacheStrategy.RESULT)
    builder.centerCrop()
    builder.dontAnimate()
    when (shapeStyle) {
        ImageShapeStyle.SHAPE_CIRCLE -> {
            builder.bitmapTransform(CropCircleTransformation(context))
        }
        ImageShapeStyle.SHAPE_RECTANGLE -> {
            builder.bitmapTransform(RoundedRectTransformation(context, cornerRadius,
                    cornerRadiusRatio))
        }
    }
    return builder
}

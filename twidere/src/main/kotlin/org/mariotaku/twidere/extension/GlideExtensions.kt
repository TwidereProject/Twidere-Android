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
import android.graphics.drawable.Drawable
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.ImageShapeStyle
import org.mariotaku.twidere.extension.model.getBestProfileBanner
import org.mariotaku.twidere.extension.model.originalProfileImage
import org.mariotaku.twidere.extension.model.user
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.glide.RoundedRectTransformation

fun RequestManager.loadProfileImage(url: String?, @ImageShapeStyle style: Int, cornerRadius: Float = 0f,
        cornerRadiusRatio: Float = 0f, size: String? = null): RequestBuilder<Drawable> {
    return configureLoadProfileImage(style, cornerRadius, cornerRadiusRatio) {
        if (url == null || size == null) {
            return@configureLoadProfileImage load(url)
        } else {
            return@configureLoadProfileImage load(Utils.getTwitterProfileImageOfSize(url, size))
        }
    }
}

fun RequestManager.loadProfileImage(resourceId: Int, @ImageShapeStyle shapeStyle: Int, cornerRadius: Float = 0f,
        cornerRadiusRatio: Float = 0f): RequestBuilder<Drawable> {
    return configureLoadProfileImage(shapeStyle, cornerRadius, cornerRadiusRatio) {
        return@configureLoadProfileImage load(resourceId)
    }
}

fun RequestManager.loadProfileImage(account: AccountDetails, @ImageShapeStyle shapeStyle: Int, cornerRadius: Float = 0f,
        cornerRadiusRatio: Float = 0f, size: String? = null): RequestBuilder<Drawable> {
    return loadProfileImage(account.user.profile_image_url, shapeStyle, cornerRadius, cornerRadiusRatio, size)
}

fun RequestManager.loadProfileImage(user: ParcelableLiteUser, @ImageShapeStyle shapeStyle: Int, cornerRadius: Float = 0f,
        cornerRadiusRatio: Float = 0f, size: String? = null): RequestBuilder<Drawable> {
    return configureLoadProfileImage(shapeStyle, cornerRadius, cornerRadiusRatio) {
        if (size != null) {
            return@configureLoadProfileImage load(Utils.getTwitterProfileImageOfSize(user.profile_image_url, size))
        } else {
            return@configureLoadProfileImage load(user.profile_image_url)
        }
    }
}

fun RequestManager.loadProfileImage(userList: ParcelableUserList, @ImageShapeStyle shapeStyle: Int,
        cornerRadius: Float = 0f,
        cornerRadiusRatio: Float = 0f): RequestBuilder<Drawable> {
    return configureLoadProfileImage(shapeStyle, cornerRadius, cornerRadiusRatio) {
        return@configureLoadProfileImage load(userList.user_profile_image_url)
    }
}

fun RequestManager.loadProfileImage(group: ParcelableGroup, @ImageShapeStyle shapeStyle: Int,
        cornerRadius: Float = 0f,
        cornerRadiusRatio: Float = 0f): RequestBuilder<Drawable> {
    return configureLoadProfileImage(shapeStyle, cornerRadius, cornerRadiusRatio) {
        return@configureLoadProfileImage load(group.homepage_logo)
    }
}

fun RequestManager.loadProfileImage(conversation: ParcelableMessageConversation, @ImageShapeStyle shapeStyle: Int,
        cornerRadius: Float = 0f, cornerRadiusRatio: Float = 0f, size: String? = null): RequestBuilder<*> {
    if (conversation.conversation_type == ParcelableMessageConversation.ConversationType.ONE_TO_ONE) {
        val user = conversation.user
        if (user != null) {
            return loadProfileImage(user.profile_image_url, shapeStyle, cornerRadius, cornerRadiusRatio, size)
        } else {
            // TODO: show default conversation icon
            return loadProfileImage(R.drawable.ic_profile_image_default_group, shapeStyle, cornerRadius,
                    cornerRadiusRatio)
        }
    } else {
        if (conversation.conversation_avatar != null) {
            return loadProfileImage(conversation.conversation_avatar, shapeStyle, cornerRadius, cornerRadiusRatio,
                    size)
        } else {
            return loadProfileImage(R.drawable.ic_profile_image_default_group, shapeStyle, cornerRadius,
                    cornerRadiusRatio)
        }
    }
}

fun RequestManager.loadOriginalProfileImage(user: ParcelableUser, @ImageShapeStyle shapeStyle: Int,
        cornerRadius: Float = 0f, cornerRadiusRatio: Float = 0f
): RequestBuilder<Drawable> {
    return configureLoadProfileImage(shapeStyle, cornerRadius, cornerRadiusRatio) {
        return@configureLoadProfileImage load(user.originalProfileImage)
    }
}

fun RequestManager.loadProfileBanner(context: Context, user: ParcelableUser, width: Int): RequestBuilder<Drawable> {
    val ratio = context.resources.getFraction(R.fraction.aspect_ratio_profile_banner, 1, 1)
    return load(user.getBestProfileBanner(width, (width / ratio).toInt()))
}

internal inline fun <T> configureLoadProfileImage(@ImageShapeStyle shapeStyle: Int, cornerRadius: Float = 0f,
        cornerRadiusRatio: Float = 0f, create: () -> RequestBuilder<T>
): RequestBuilder<T> {
    val builder = create()
    return builder.apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA).dontAnimate()
            .configTransform(shapeStyle, cornerRadius, cornerRadiusRatio))
}

fun RequestOptions.configTransform(shapeStyle: Int, cornerRadius: Float, cornerRadiusRatio: Float) = apply {
    when (shapeStyle) {
        ImageShapeStyle.SHAPE_CIRCLE -> {
            transform(CircleCrop())
        }
        ImageShapeStyle.SHAPE_RECTANGLE -> {
            transform(RoundedRectTransformation(cornerRadius, cornerRadiusRatio))
        }
        ImageShapeStyle.SHAPE_NONE -> {
            centerCrop()
        }
    }
}

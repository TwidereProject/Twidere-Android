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
import org.mariotaku.twidere.extension.model.user
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.ShapedImageView

fun RequestManager.loadProfileImage(context: Context, url: String?,
        @ShapedImageView.ShapeStyle shapeStyle: Int = ShapedImageView.SHAPE_CIRCLE): DrawableRequestBuilder<String?> {
    val size = context.getString(R.string.profile_image_size)
    return configureLoadProfileImage(context, shapeStyle) { load(Utils.getTwitterProfileImageOfSize(url, size)) }
}

fun RequestManager.loadProfileImage(context: Context, resourceId: Int,
        @ShapedImageView.ShapeStyle shapeStyle: Int = ShapedImageView.SHAPE_CIRCLE): DrawableRequestBuilder<Int> {
    return configureLoadProfileImage(context, shapeStyle) { load(resourceId) }
}

fun RequestManager.loadProfileImage(context: Context, account: AccountDetails,
        @ShapedImageView.ShapeStyle shapeStyle: Int = ShapedImageView.SHAPE_CIRCLE): DrawableRequestBuilder<String?> {
    return loadProfileImage(context, account.user, shapeStyle)
}

fun RequestManager.loadProfileImage(context: Context, user: ParcelableUser,
        @ShapedImageView.ShapeStyle shapeStyle: Int = ShapedImageView.SHAPE_CIRCLE): DrawableRequestBuilder<String?> {
    if (user.extras != null && user.extras.profile_image_url_fallback == null) {
        // No fallback image, use compatible logic
        return loadProfileImage(context, user.profile_image_url)
    }
    return configureLoadProfileImage(context, shapeStyle) { load(user.profile_image_url) }
}

fun RequestManager.loadProfileImage(context: Context, userList: ParcelableUserList,
        @ShapedImageView.ShapeStyle shapeStyle: Int = ShapedImageView.SHAPE_CIRCLE): DrawableRequestBuilder<String?> {
    return configureLoadProfileImage(context, shapeStyle) { load(userList.user_profile_image_url) }
}

fun RequestManager.loadProfileImage(context: Context, group: ParcelableGroup,
        @ShapedImageView.ShapeStyle shapeStyle: Int = ShapedImageView.SHAPE_CIRCLE): DrawableRequestBuilder<String?> {
    return configureLoadProfileImage(context, shapeStyle) { load(group.homepage_logo) }
}

fun RequestManager.loadProfileImage(context: Context, status: ParcelableStatus,
        @ShapedImageView.ShapeStyle shapeStyle: Int = ShapedImageView.SHAPE_CIRCLE): DrawableRequestBuilder<String?> {
    if (status.extras != null && status.extras.user_profile_image_url_fallback == null) {
        // No fallback image, use compatible logic
        return loadProfileImage(context, status.user_profile_image_url)
    }
    return configureLoadProfileImage(context, shapeStyle) { load(status.user_profile_image_url) }
}

fun RequestManager.loadProfileImage(context: Context, conversation: ParcelableMessageConversation): DrawableRequestBuilder<*> {
    if (conversation.conversation_type == ParcelableMessageConversation.ConversationType.ONE_TO_ONE) {
        val user = conversation.user
        if (user != null) {
            return loadProfileImage(context, user)
        } else {
            // TODO: show default conversation icon
            return loadProfileImage(context, org.mariotaku.twidere.R.drawable.ic_profile_image_default_group)
        }
    } else {
        return loadProfileImage(context, conversation.conversation_avatar).placeholder(R.drawable.ic_profile_image_default_group)
    }
}

internal inline fun <T> configureLoadProfileImage(context: Context, shapeStyle: Int,
        create: () -> DrawableTypeRequest<T>): DrawableRequestBuilder<T> {
    val builder = create()
    builder.diskCacheStrategy(DiskCacheStrategy.RESULT)
    builder.dontAnimate()
    if (!ShapedImageView.OUTLINE_DRAW) {
        when (shapeStyle) {
            ShapedImageView.SHAPE_CIRCLE -> {
                builder.bitmapTransform(CropCircleTransformation(context))
            }
            ShapedImageView.SHAPE_RECTANGLE -> {
            }
        }
    }
    return builder
}

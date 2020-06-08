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

package org.mariotaku.twidere.util.shortcut

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.bumptech.glide.Glide
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.ImageShapeStyle
import org.mariotaku.twidere.constant.iWantMyStarsBackKey
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.constant.profileImageStyleKey
import org.mariotaku.twidere.extension.dismissProgressDialog
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.extension.showProgressDialog
import org.mariotaku.twidere.fragment.BaseFragment
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.dagger.DependencyHolder
import org.mariotaku.twidere.util.glide.DeferredTarget
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

/**
 * Created by mariotaku on 2017/8/23.
 */
object ShortcutCreator {

    private val useAdaptiveIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    private const val adaptiveIconSizeDp = 108
    private const val adaptiveIconOuterSidesDp = 18

    fun user(context: Context, accountKey: UserKey?, user: ParcelableUser): Promise<ShortcutInfoCompat, Exception> {
        val holder = DependencyHolder.get(context)
        val preferences = holder.preferences
        val userColorNameManager = holder.userColorNameManager

        val profileImageStyle = if (useAdaptiveIcon) ImageShapeStyle.SHAPE_RECTANGLE else preferences[profileImageStyleKey]
        val profileImageCornerRadiusRatio = if (useAdaptiveIcon) 0f else 0.1f

        val deferred = Glide.with(context).loadProfileImage(context, user,
                shapeStyle = profileImageStyle, cornerRadiusRatio = profileImageCornerRadiusRatio,
                size = context.getString(R.string.profile_image_size)).into(DeferredTarget())

        val weakContext = WeakReference(context)
        return deferred.promise.then { drawable ->
            val ctx = weakContext.get() ?: throw InterruptedException()
            val builder = ShortcutInfoCompat.Builder(ctx, "$accountKey:user:${user.key}")
            builder.setIcon(drawable.toProfileImageIcon(ctx))
            builder.setShortLabel(userColorNameManager.getDisplayName(user, preferences[nameFirstKey]))
            val launchIntent = IntentUtils.userProfile(accountKey, user.key,
                    user.screen_name, profileUrl = user.extras?.statusnet_profile_url)
            builder.setIntent(launchIntent)
            return@then builder.build()
        }
    }

    fun userFavorites(context: Context, accountKey: UserKey?, user: ParcelableUser): Promise<ShortcutInfoCompat, Exception> {
        val holder = DependencyHolder.get(context)
        val preferences = holder.preferences
        val userColorNameManager = holder.userColorNameManager

        val launchIntent = IntentUtils.userFavorites(accountKey, user.key,
                user.screen_name, profileUrl = user.extras?.statusnet_profile_url)
        val builder = ShortcutInfoCompat.Builder(context, "$accountKey:user-favorites:${user.key}")
        builder.setIntent(launchIntent)
        builder.setShortLabel(userColorNameManager.getDisplayName(user, preferences[nameFirstKey]))
        if (preferences[iWantMyStarsBackKey]) {
            builder.setIcon(IconCompat.createWithResource(context, R.mipmap.ic_shortcut_favorite))
        } else {
            builder.setIcon(IconCompat.createWithResource(context, R.mipmap.ic_shortcut_like))
        }
        return Promise.of(builder.build())
    }

    fun userTimeline(context: Context, accountKey: UserKey?, user: ParcelableUser): Promise<ShortcutInfoCompat, Exception> {
        val holder = DependencyHolder.get(context)
        val preferences = holder.preferences
        val userColorNameManager = holder.userColorNameManager

        val launchIntent = IntentUtils.userTimeline(accountKey, user.key,
                user.screen_name, profileUrl = user.extras?.statusnet_profile_url)
        val builder = ShortcutInfoCompat.Builder(context, "$accountKey:user-timeline:${user.key}")
        builder.setIntent(launchIntent)
        builder.setShortLabel(userColorNameManager.getDisplayName(user, preferences[nameFirstKey]))
        builder.setIcon(IconCompat.createWithResource(context, R.mipmap.ic_shortcut_quote))
        return Promise.of(builder.build())
    }

    fun userMediaTimeline(context: Context, accountKey: UserKey?, user: ParcelableUser): Promise<ShortcutInfoCompat, Exception> {
        val holder = DependencyHolder.get(context)
        val preferences = holder.preferences
        val userColorNameManager = holder.userColorNameManager

        val launchIntent = IntentUtils.userMediaTimeline(accountKey, user.key,
                user.screen_name, profileUrl = user.extras?.statusnet_profile_url)
        val builder = ShortcutInfoCompat.Builder(context, "$accountKey:user-media-timeline:${user.key}")
        builder.setIntent(launchIntent)
        builder.setShortLabel(userColorNameManager.getDisplayName(user, preferences[nameFirstKey]))
        builder.setIcon(IconCompat.createWithResource(context, R.mipmap.ic_shortcut_gallery))
        return Promise.of(builder.build())
    }

    fun userListTimeline(context: Context, accountKey: UserKey?, list: ParcelableUserList): Promise<ShortcutInfoCompat, Exception> {
        val launchIntent = IntentUtils.userListTimeline(accountKey, list.id,
                list.user_key, list.user_screen_name, list.name)
        val builder = ShortcutInfoCompat.Builder(context, "$accountKey:user-list-timeline:${list.id}")
        builder.setIntent(launchIntent)
        builder.setShortLabel(list.name)
        builder.setIcon(IconCompat.createWithResource(context, R.mipmap.ic_shortcut_list))
        return Promise.of(builder.build())
    }

    inline fun performCreation(fragment: BaseFragment, createPromise: () -> Promise<ShortcutInfoCompat, Exception>) {
        val fragmentContext = fragment.context ?: return
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(fragmentContext)) return
        val promise = fragment.showProgressDialog("create_shortcut")
                .and(createPromise())
        val weakThis = WeakReference(fragment)
        promise.successUi { (_, shortcut) ->
            val f = weakThis.get()?.context ?: return@successUi
            ShortcutManagerCompat.requestPinShortcut(f, shortcut, null)
        }.alwaysUi {
            val f = weakThis.get() ?: return@alwaysUi
            f.dismissProgressDialog("create_shortcut")
        }
    }

    private fun Drawable.toProfileImageIcon(context: Context): IconCompat {
        if (useAdaptiveIcon) {
            val density = context.resources.displayMetrics.density
            val adaptiveIconSize = (adaptiveIconSizeDp * density).roundToInt()
            val adaptiveIconOuterSides = (adaptiveIconOuterSidesDp * density).roundToInt()

            val bitmap = Bitmap.createBitmap(adaptiveIconSize, adaptiveIconSize,
                    Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            setBounds(adaptiveIconOuterSides, adaptiveIconOuterSides,
                    adaptiveIconSize - adaptiveIconOuterSides,
                    adaptiveIconSize - adaptiveIconOuterSides)
            draw(canvas)

            return IconCompat.createWithAdaptiveBitmap(bitmap)
        } else {
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            setBounds(0, 0, bitmap.width, bitmap.height)
            draw(canvas)

            return IconCompat.createWithBitmap(bitmap)
        }
    }

}
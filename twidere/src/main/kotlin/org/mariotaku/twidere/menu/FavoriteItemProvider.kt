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

package org.mariotaku.twidere.menu

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.ActionProvider
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.ActionMenuView
import android.view.MenuItem
import android.view.View
import org.mariotaku.ktextension.toWeak
import org.mariotaku.twidere.extension.isActivated
import org.mariotaku.twidere.extension.view.findItemView
import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable
import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable.Style

class FavoriteItemProvider(context: Context) : ActionProvider(context) {
    var useStar: Boolean = false
    var icon: Int = 0
    var tint: Int = 0
    var longClickListener: (() -> Boolean)? = null

    override fun onCreateActionView() = null

    fun invokeItem(item: MenuItem, listener: LikeAnimationDrawable.OnLikedListener) {
        if (MenuItemCompat.getActionProvider(item) !== this) throw IllegalArgumentException()
        val icon = item.icon
        if (icon is LikeAnimationDrawable) {
            icon.setOnLikedListener(listener)
            icon.start()
        }
    }

    fun init(menuBar: ActionMenuView, item: MenuItem) {
        assert(MenuItemCompat.getActionProvider(item) === this)
        val icon = ContextCompat.getDrawable(context, this.icon)
        val drawable = LikeAnimationDrawable(icon, if (useStar) Style.FAVORITE else Style.LIKE)
        if (tint != 0) {
            DrawableCompat.setTintList(drawable, ContextCompat.getColorStateList(context, tint))
        }
        drawable.mutate()
        drawable.callback = ViewCallback(menuBar)
        item.icon = drawable

        val menuView = menuBar.findItemView(item)

        menuView?.setOnLongClickListener { longClickListener?.invoke() == true }
    }

    fun setIsFavorite(item: MenuItem, isFavorite: Boolean) {
        if (MenuItemCompat.getActionProvider(item) !== this) throw IllegalArgumentException()
        val icon = item.icon
        if (icon is LikeAnimationDrawable) {
            icon.isActivated = isFavorite
        }
    }

    private class ViewCallback(view: View) : Drawable.Callback {
        private val viewRef = view.toWeak()

        override fun invalidateDrawable(who: Drawable) {
            val view = viewRef.get() ?: return
            view.invalidate()
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
            val view = viewRef.get() ?: return
            view.postDelayed(what, `when`)
        }

        override fun unscheduleDrawable(who: Drawable, what: Runnable) {
            val view = viewRef.get() ?: return
            view.post(what)
        }
    }
}
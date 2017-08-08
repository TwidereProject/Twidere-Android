/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.ktextension

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import org.mariotaku.twidere.graphic.ActionIconDrawable

fun Menu.setItemAvailability(id: Int, available: Boolean) {
    val item = findItem(id) ?: return
    item.isVisible = available
    item.isEnabled = available
}

fun Menu.setGroupAvailability(groupId: Int, available: Boolean) {
    setGroupEnabled(groupId, available)
    setGroupVisible(groupId, available)
}

fun Menu.isItemChecked(id: Int): Boolean {
    return findItem(id)?.isChecked ?: false
}

fun Menu.setItemChecked(id: Int, checked: Boolean) {
    findItem(id)?.isChecked = checked
}

fun Menu.setMenuItemIcon(id: Int, icon: Int) {
    findItem(id)?.setIcon(icon)
}

fun Menu.setMenuItemIcon(id: Int, icon: Drawable) {
    findItem(id)?.icon = icon
}

fun Menu.setMenuItemTitle(id: Int, title: Int) {
    findItem(id)?.setTitle(title)
}

fun Menu.setMenuItemShowAsActionFlags(id: Int, flags: Int) {
    val item = findItem(id) ?: return
    item.setShowAsActionFlags(flags)
}

fun Menu.findSubmenu(id: Int): SubMenu? {
    return findItem(id)?.subMenu
}

fun MenuItem.setActionIcon(context: Context, @DrawableRes iconRes: Int) {
    val oldIcon = this.icon
    if (oldIcon is ActionIconDrawable) {
        val starIcon = ContextCompat.getDrawable(context, iconRes)
        this.icon = ActionIconDrawable(starIcon, oldIcon.defaultColor)
    } else {
        setIcon(iconRes)
    }
}

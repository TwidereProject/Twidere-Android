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

package androidx.appcompat.widget

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Menu
import android.view.View
import android.widget.ImageView
import org.mariotaku.twidere.extension.findFieldByTypes
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.util.ThemeUtils

/**
 * Created by mariotaku on 15/1/16.
 */
class TwidereToolbar(context: Context, attrs: AttributeSet?) : Toolbar(context, attrs) {

    private var itemColor: Int = 0

    override fun getMenu(): Menu {
        val menu = super.getMenu()
        ThemeUtils.setActionBarOverflowColor(this, itemColor)
        val menuViewField = try {
            Toolbar::class.java.findFieldByTypes(ActionMenuView::class.java)
        } catch (e: Exception) {
            null
        } ?: return menu
        val menuView = this[menuViewField] as? ActionMenuView ?: return menu
        val presenterField = try {
            ActionMenuView::class.java.findFieldByTypes(ActionMenuPresenter::class.java)
        } catch (e: Exception) {
            null
        } ?: return menu
        val presenter = menuView[presenterField] as? ActionMenuPresenter ?: return menu
        setActionBarOverflowColor(presenter, itemColor)
        return menu
    }

    override fun setNavigationIcon(icon: Drawable?) {
        if (icon != null && itemColor != 0) {
            icon.setColorFilter(itemColor, PorterDuff.Mode.SRC_ATOP)
        }
        super.setNavigationIcon(icon)
    }

    fun setItemColor(itemColor: Int) {
        this.itemColor = itemColor
        navigationIcon = navigationIcon
    }

    companion object {

        private fun setActionBarOverflowColor(presenter: ActionMenuPresenter, itemColor: Int) {
            val viewField = try {
                ActionMenuPresenter::class.java.findFieldByTypes(ActionMenuView.ActionMenuChildView::class.java, View::class.java)
            } catch (e: Exception) {
                null
            } ?: return
            val view = presenter[viewField] as? ImageView ?: return
            view.setColorFilter(itemColor, PorterDuff.Mode.SRC_ATOP)
        }
    }
}

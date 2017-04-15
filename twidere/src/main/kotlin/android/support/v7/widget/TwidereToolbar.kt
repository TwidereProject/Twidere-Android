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

package android.support.v7.widget

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Menu
import android.view.View
import android.widget.ImageView
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 15/1/16.
 */
class TwidereToolbar(context: Context, attrs: AttributeSet?) : Toolbar(context, attrs) {

    private var itemColor: Int = 0

    override fun getMenu(): Menu {
        val menu = super.getMenu()
        ThemeUtils.setActionBarOverflowColor(this, itemColor)
        val menuView = Utils.findFieldOfTypes(this, Toolbar::class.java,
                ActionMenuView::class.java) as? ActionMenuView ?: return menu
        val presenter = Utils.findFieldOfTypes(menuView, ActionMenuView::class.java,
                ActionMenuPresenter::class.java) as? ActionMenuPresenter ?: return menu
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
            val view = Utils.findFieldOfTypes(presenter, ActionMenuPresenter::class.java,
                    ActionMenuView.ActionMenuChildView::class.java, View::class.java)  as? ImageView ?: return
            view.setColorFilter(itemColor, PorterDuff.Mode.SRC_ATOP)
        }
    }
}

/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import androidx.core.view.ActionProvider
import android.view.MenuItem
import android.view.SubMenu
import org.mariotaku.twidere.Constants.MENU_GROUP_STATUS_SHARE
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.util.MenuUtils
import org.mariotaku.twidere.util.Utils.createStatusShareIntent

/**
 * Created by mariotaku on 14/12/7.
 */
class SupportStatusShareProvider(context: Context) : ActionProvider(context) {
    var status: ParcelableStatus? = null

    override fun onCreateActionView() = null

    override fun onCreateActionView(forItem: MenuItem) = null

    override fun onPerformDefaultAction() = true

    override fun hasSubMenu() = true

    override fun onPrepareSubMenu(subMenu: SubMenu) {
        val status = status ?: return
        val shareIntent = createStatusShareIntent(context, status)
        subMenu.removeGroup(MENU_GROUP_STATUS_SHARE)
        MenuUtils.addIntentToMenu(context, subMenu, shareIntent, MENU_GROUP_STATUS_SHARE)
    }

}

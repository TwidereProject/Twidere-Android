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

package org.mariotaku.twidere.view.transformer

import androidx.viewpager.widget.ViewPager
import android.view.View

object AccountsSelectorTransformer : ViewPager.PageTransformer {
    internal const val selectorAccountsCount: Int = 3

    override fun transformPage(page: View, position: Float) {
        when {
            position < 0 -> {
                page.alpha = 1 + position * selectorAccountsCount
            }
            position > (selectorAccountsCount - 1f) / selectorAccountsCount -> {
                page.alpha = (1 - position) * selectorAccountsCount
            }
            else -> {
                page.alpha = 1f
            }
        }
    }

}
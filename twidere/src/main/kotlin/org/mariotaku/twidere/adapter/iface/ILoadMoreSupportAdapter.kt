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

package org.mariotaku.twidere.adapter.iface

import android.support.annotation.IntDef

/**
 * Created by mariotaku on 15/4/16.
 */
interface ILoadMoreSupportAdapter {

    var loadMoreIndicatorPosition: Long

    var loadMoreSupportedPosition: Long

    @IntDef(flag = true, value = *longArrayOf(NONE, START, END, BOTH))
    annotation class IndicatorPosition

    companion object {
        val ITEM_VIEW_TYPE_LOAD_INDICATOR = 0

        const val NONE: Long = 0
        const val START: Long = 1
        const val END: Long = 2
        const val BOTH: Long = START or END

        @IndicatorPosition
        fun apply(@IndicatorPosition orig: Long, @IndicatorPosition supported: Long): Long {
            return orig and supported
        }

        @IndicatorPosition
        fun has(@IndicatorPosition flags: Long, @IndicatorPosition compare: Long): Boolean {
            return flags and compare != 0L
        }
    }
}

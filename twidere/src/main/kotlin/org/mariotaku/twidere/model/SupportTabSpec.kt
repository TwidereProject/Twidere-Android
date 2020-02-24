/*
 * 				Twidere - Twitter client for Android
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

package org.mariotaku.twidere.model

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.mariotaku.twidere.annotation.CustomTabType
import org.mariotaku.twidere.model.tab.DrawableHolder
import org.mariotaku.twidere.util.CompareUtils.bundleEquals
import org.mariotaku.twidere.util.CompareUtils.objectEquals

data class SupportTabSpec(
        var name: CharSequence? = null,
        var icon: DrawableHolder? = null,
        @CustomTabType val type: String? = null,
        var cls: Class<out Fragment>,
        var args: Bundle? = null,
        var position: Int,
        var tag: String? = null
) : Comparable<SupportTabSpec> {

    init {
        if (name == null && icon == null)
            throw IllegalArgumentException("You must specify a name or icon for this tab!")
    }

    override fun compareTo(other: SupportTabSpec): Int {
        return position - other.position
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SupportTabSpec) return false
        return objectEquals(name, other.name) && objectEquals(icon, other.icon) && cls == other.cls
                && bundleEquals(args, other.args) && position == other.position
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + cls.hashCode()
        result = 31 * result + (args?.hashCode() ?: 0)
        result = 31 * result + position
        return result
    }

}

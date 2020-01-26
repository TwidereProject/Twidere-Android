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

import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import java.util.*

fun Preference.findParent(screen: PreferenceScreen): PreferenceGroup? {
    val curParents = Stack<PreferenceGroup>()
    curParents.add(screen)
    while (!curParents.isEmpty()) {
        val parent = curParents.pop()
        for (i in 0 until parent.preferenceCount) {
            val child = parent.getPreference(i)
            if (child == this) return parent
            if (child is PreferenceGroup) {
                curParents.push(child)
            }
        }
    }
    return null
}
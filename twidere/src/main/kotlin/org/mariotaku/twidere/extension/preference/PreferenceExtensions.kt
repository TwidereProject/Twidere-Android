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

package org.mariotaku.twidere.extension.preference

import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceGroup

inline fun PreferenceGroup.setOnPreferenceChangeListenerRecursive(crossinline listener: (Preference, Any?) -> Boolean) {
    setOnPreferenceChangeListenerRecursive(Preference.OnPreferenceChangeListener { preference, newValue ->
        listener(preference, newValue)
    })
}

fun PreferenceGroup.setOnPreferenceChangeListenerRecursive(listener: Preference.OnPreferenceChangeListener) {
    for (i in 0 until preferenceCount) {
        val preference = getPreference(i)
        preference.onPreferenceChangeListener = listener
        if (preference is PreferenceGroup) {
            preference.setOnPreferenceChangeListenerRecursive(listener)
        }
    }
}

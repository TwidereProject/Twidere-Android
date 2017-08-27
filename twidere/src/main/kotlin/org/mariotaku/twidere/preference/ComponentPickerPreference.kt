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

package org.mariotaku.twidere.preference

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.util.AttributeSet

abstract class ComponentPickerPreference(context: Context, attrs: AttributeSet? = null) : ThemedListPreference(context, attrs) {

    protected val packageManager: PackageManager = context.packageManager

    init {
        setup()
    }

    override fun getSummary(): CharSequence {
        if (isNoneValue(value)) return noneEntry
        return entry
    }

    protected abstract val intentAction: String

    protected abstract val noneEntry: String

    protected abstract fun getComponentName(info: ResolveInfo): ComponentName

    protected abstract fun resolve(queryIntent: Intent): List<ResolveInfo>

    private fun setup() {
        val queryIntent = Intent(intentAction)
        val infoList = resolve(queryIntent)
        val infoListSize = infoList.size
        val entries = arrayOfNulls<CharSequence>(infoListSize + 1)
        val values = arrayOfNulls<CharSequence>(infoListSize + 1)
        entries[0] = noneEntry
        values[0] = ""
        for (i in 0 until infoListSize) {
            val info = infoList[i]
            entries[i + 1] = info.loadLabel(packageManager)
            values[i + 1] = getComponentName(info).flattenToString()
        }
        setEntries(entries)
        entryValues = values
    }

    companion object {

        fun isNoneValue(value: String?): Boolean {
            return value.isNullOrEmpty() || "none" == value
        }
    }

}

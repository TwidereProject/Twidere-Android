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

package org.mariotaku.twidere.preference.theme

import android.content.Context
import android.support.v7.preference.PreferenceViewHolder
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.ThemeBackgroundOption
import org.mariotaku.twidere.constant.themeBackgroundAlphaKey
import org.mariotaku.twidere.preference.EntrySummaryDropDownPreference

class ThemeBackgroundPreference(context: Context, attrs: AttributeSet? = null) : EntrySummaryDropDownPreference(context, attrs) {

    private val opacityChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            val oldValue = sharedPreferences[themeBackgroundAlphaKey]
            val newValue = ThemeBackgroundOption.MIN_ALPHA + seekBar.progress
            sharedPreferences[themeBackgroundAlphaKey] = newValue
            if (oldValue != newValue) {
                callChangeListener(value)
            }
            notifyChanged()
        }

    }

    init {
        layoutResource = R.layout.preference_theme_background
        entries = context.resources.getStringArray(R.array.entries_theme_background)
        entryValues = context.resources.getStringArray(R.array.values_theme_background)
    }

    override fun onBindViewHolder(view: PreferenceViewHolder) {
        super.onBindViewHolder(view)
        val opacityContainer = view.findViewById(R.id.opacityContainer)
        val opacitySeekBar = view.findViewById(R.id.opacitySeekBar) as SeekBar
        if (value == ThemeBackgroundOption.TRANSPARENT) {
            opacityContainer.visibility = View.VISIBLE
        } else {
            opacityContainer.visibility = View.GONE
        }
        opacitySeekBar.max = ThemeBackgroundOption.MAX_ALPHA - ThemeBackgroundOption.MIN_ALPHA
        opacitySeekBar.progress = sharedPreferences[themeBackgroundAlphaKey] - ThemeBackgroundOption.MIN_ALPHA
        opacitySeekBar.setOnSeekBarChangeListener(opacityChangeListener)
    }
}
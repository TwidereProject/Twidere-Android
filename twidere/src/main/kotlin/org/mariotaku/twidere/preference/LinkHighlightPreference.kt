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

package org.mariotaku.twidere.preference

import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.SharedPreferenceConstants.*
import org.mariotaku.twidere.text.TwidereHighLightStyle

class LinkHighlightPreference(
        context: Context, attrs: AttributeSet?
) : EntrySummaryListPreference(context, attrs) {

    init {
        entries = Array<CharSequence>(VALUES.size) { i ->
            getStyledEntry(OPTIONS[i], context.getString(ENTRIES_RES[i]))
        }
        entryValues = VALUES
    }

    companion object {

        private val ENTRIES_RES = intArrayOf(
                R.string.none,
                R.string.highlight,
                R.string.underline,
                R.string.highlight_and_underline
        )
        private val VALUES = arrayOf(
                VALUE_LINK_HIGHLIGHT_OPTION_NONE,
                VALUE_LINK_HIGHLIGHT_OPTION_HIGHLIGHT,
                VALUE_LINK_HIGHLIGHT_OPTION_UNDERLINE,
                VALUE_LINK_HIGHLIGHT_OPTION_BOTH
        )
        private val OPTIONS = intArrayOf(
                VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE,
                VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT,
                VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE,
                VALUE_LINK_HIGHLIGHT_OPTION_CODE_BOTH
        )

        private fun getStyledEntry(option: Int, entry: CharSequence): CharSequence {
            val str = SpannableString(entry)
            str.setSpan(TwidereHighLightStyle(option), 0, str.length, 0)
            return str
        }
    }
}

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

package org.mariotaku.twidere.text

import android.text.TextPaint
import android.text.style.URLSpan
import android.view.View
import org.mariotaku.ktextension.contains
import org.mariotaku.twidere.constant.SharedPreferenceConstants.*
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener

class TwidereURLSpan(
        url: String,
        private val orig: String? = null,
        private val accountKey: UserKey? = null,
        private val extraId: Long = -1,
        private val type: Int = 0,
        private val sensitive: Boolean = false,
        private val highlightStyle: Int = VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE,
        private val start: Int = 0,
        private val end: Int = 0,
        private val listener: OnLinkClickListener? = null
) : URLSpan(url) {

    override fun onClick(widget: View) {
        listener?.onLinkClick(url, orig, accountKey, extraId, type, sensitive, start, end)
    }

    override fun updateDrawState(ds: TextPaint) {
        if (VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE in highlightStyle) {
            ds.isUnderlineText = true
        }
        if (VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT in highlightStyle) {
            ds.color = ds.linkColor
        }
    }
}

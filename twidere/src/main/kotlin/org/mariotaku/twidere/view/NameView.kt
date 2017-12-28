/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import org.mariotaku.twidere.R

class NameView(context: Context, attrs: AttributeSet? = null) : TwoLineTextView(context, attrs) {

    var nameFirst: Boolean = false

    var name: String? = null
    var screenName: String? = null

    override val displayPrimaryText: CharSequence?
        get() = if (nameFirst) name else screenName
    override val displaySecondaryText: CharSequence?
        get() = if (nameFirst) screenName else name

    init {
        ellipsize = TextUtils.TruncateAt.END
        val a = context.obtainStyledAttributes(attrs, R.styleable.NameView, 0, 0)
        nameFirst = a.getBoolean(R.styleable.NameView_nvNameFirst, true)
        a.recycle()
        if (isInEditMode) {
            name = "Name"
            screenName = "@screenname"
            updateText()
        }
    }

}

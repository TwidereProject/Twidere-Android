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

package org.mariotaku.twidere.text.util

import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import android.widget.TextView
import org.mariotaku.twidere.extension.applyTo
import org.mariotaku.twidere.text.SafeSpannableStringBuilder
import org.mariotaku.twidere.util.ExternalThemeManager
import org.mariotaku.twidere.util.dagger.GeneralComponent
import javax.inject.Inject

/**
 * Created by mariotaku on 15/12/20.
 */
class EmojiEditableFactory(textView: TextView) : Editable.Factory() {

    @Inject
    internal lateinit var externalThemeManager: ExternalThemeManager

    init {
        GeneralComponent.get(textView.context).inject(this)
    }

    override fun newEditable(source: CharSequence): Editable {
        val editable = SafeSpannableStringBuilder(source)
        val emoji = externalThemeManager.emoji
        emoji?.applyTo(editable)
        editable.setSpan(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (count <= 0) return
                emoji?.applyTo(editable)
            }

            override fun afterTextChanged(s: Editable) {

            }
        }, 0, editable.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        return editable
    }
}

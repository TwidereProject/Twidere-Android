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

import android.text.Spannable
import android.widget.TextView

import org.mariotaku.twidere.util.EmojiSupportUtils
import org.mariotaku.twidere.util.ExternalThemeManager
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper

import javax.inject.Inject

/**
 * Created by mariotaku on 15/12/20.
 */
class EmojiSpannableFactory(textView: TextView) : SafeSpannableFactory() {

    @Inject
    lateinit internal var externalThemeManager: ExternalThemeManager

    init {
        GeneralComponentHelper.build(textView.context).inject(this)
    }

    override fun newSpannable(source: CharSequence): Spannable {
        val spannable = super.newSpannable(source)
        EmojiSupportUtils.applyEmoji(externalThemeManager, spannable)
        return spannable
    }
}

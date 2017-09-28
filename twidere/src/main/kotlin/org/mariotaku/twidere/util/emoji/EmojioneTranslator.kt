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

package org.mariotaku.twidere.util.emoji

import android.content.Context
import org.apache.commons.text.translate.CharSequenceTranslator
import org.mariotaku.commons.emojione.ShortnameToUnicodeTranslator
import java.io.Writer

/**
 * Created by mariotaku on 2017/4/26.
 */
object EmojioneTranslator: CharSequenceTranslator() {

    private var implementation: ShortnameToUnicodeTranslator? = null

    fun init(context: Context) {
        if (implementation != null)return
        implementation = ShortnameToUnicodeTranslator(context)
    }

    override fun translate(input: CharSequence?, index: Int, out: Writer?): Int {
        val translator = implementation ?: return 0
        return translator.translate(input, index, out)
    }

}
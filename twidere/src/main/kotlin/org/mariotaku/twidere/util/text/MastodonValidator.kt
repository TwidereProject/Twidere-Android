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

package org.mariotaku.twidere.util.text

import org.mariotaku.ktextension.times
import org.mariotaku.twidere.alias.TwitterRegex


object MastodonValidator {

    const val textLimit: Int = 500

    private val mentionRegex = Regex("(?<=^|[^/[\\w]])@(([a-z0-9_]+)(?:@[a-z0-9.\\-]+[a-z0-9]+)?)", RegexOption.IGNORE_CASE)

    private val urlRegex = TwitterRegex.VALID_URL.toRegex()

    fun getCountableLength(summary: String?, text: String): Int {
        var length = 0
        if (summary != null) {
            length += summary.codePointCount
        }
        length += text.countableText.codePointCount
        return length
    }

    private inline val String.codePointCount: Int
        get() = codePointCount(0, length)

    private inline val String.countableText: String
        get() = replace(urlRegex) {
            it.groupValues[TwitterRegex.VALID_URL_GROUP_BEFORE] + "x" * 23
        }.replace(mentionRegex) { mr ->
            "@${mr.groupValues[2]}"
        }

}

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
import org.mariotaku.twidere.R


object EmojiOneShortCodeMap {

    val ready: Boolean
        get() = map != null

    private var map: HashMap<String, String>? = null

    fun init(context: Context) {
        if (map != null) return
        map = context.resources.openRawResource(R.raw.emojione_shortcodes).reader(Charsets.UTF_8).use {
            val newMap = HashMap<String, String>()
            it.forEachLine { line ->
                val equalIdx = line.indexOf('=')
                if (equalIdx < 0) return@forEachLine
                newMap[line.substring(0, equalIdx)] = line.substring(equalIdx + 1)
            }
            return@use newMap
        }
    }

    operator fun get(str: String): String? = map?.get(str)
}

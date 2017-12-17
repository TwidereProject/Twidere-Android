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

package org.mariotaku.twidere.model

import org.mariotaku.twidere.constant.SharedPreferenceConstants.*
import org.mariotaku.twidere.util.LinkCreator

class QuoteFormat(val format: String = DEFAULT_QUOTE_FORMAT) {
    fun get(status: ParcelableStatus): String {
        val link = LinkCreator.getStatusWebLink(status)
        return format.replace(FORMAT_PATTERN_LINK, link.toString())
                .replace(FORMAT_PATTERN_NAME, status.user_screen_name)
                .replace(FORMAT_PATTERN_TEXT, status.text_plain)
    }
}

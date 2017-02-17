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

package org.mariotaku.twidere.util


import android.net.Uri
import org.mariotaku.twidere.TwidereConstants.QUERY_PARAM_NOTIFY_URI

import org.mariotaku.twidere.provider.TwidereDataStore

object TwidereQueryBuilder {

    fun rawQuery(rawQuery: String, notifyUri: Uri? = null): Uri {
        val builder = TwidereDataStore.CONTENT_URI_RAW_QUERY.buildUpon().appendPath(rawQuery)
        if (notifyUri != null) {
            builder.appendQueryParameter(QUERY_PARAM_NOTIFY_URI, notifyUri.toString())
        }
        return builder.build()
    }


}

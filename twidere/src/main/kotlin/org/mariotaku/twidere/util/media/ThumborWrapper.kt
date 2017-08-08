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

package org.mariotaku.twidere.util.media

import android.content.SharedPreferences
import android.net.Uri
import android.text.TextUtils
import android.webkit.URLUtil
import com.squareup.pollexor.Thumbor
import com.squareup.pollexor.ThumborUrlBuilder
import org.mariotaku.twidere.constant.SharedPreferenceConstants.*

/**
 * Created by mariotaku on 2017/3/7.
 */

class ThumborWrapper {

    internal var thumbor: Thumbor? = null

    val available get() = thumbor != null

    fun buildUri(uri: String): String {
        val thumbor = this.thumbor ?: return uri
        return thumbor.buildImage(Uri.encode(uri)).filter(ThumborUrlBuilder.quality(85)).toUrl()
    }

    fun reloadSettings(preferences: SharedPreferences) {
        thumbor = run {
            if (preferences.getBoolean(KEY_THUMBOR_ENABLED, false)) {
                val address = preferences.getString(KEY_THUMBOR_ADDRESS, null)
                val securityKey = preferences.getString(KEY_THUMBOR_SECURITY_KEY, null)
                if (address != null && URLUtil.isValidUrl(address)) {
                    if (TextUtils.isEmpty(securityKey)) {
                        return@run Thumbor.create(address)
                    } else {
                        return@run Thumbor.create(address, securityKey)
                    }
                }
            }
            return@run null
        }

    }
}

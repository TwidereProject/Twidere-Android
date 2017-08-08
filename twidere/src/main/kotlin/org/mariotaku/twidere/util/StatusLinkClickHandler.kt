/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.util

import android.content.Context
import android.content.SharedPreferences
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.constant.displaySensitiveContentsKey
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey

/**
 * Created by mariotaku on 15/1/23.
 */
open class StatusLinkClickHandler(
        context: Context,
        manager: MultiSelectManager,
        preferences: SharedPreferences
) : OnLinkClickHandler(context, manager, preferences), Constants {

    var status: ParcelableStatus? = null

    override fun openMedia(accountKey: UserKey, extraId: Long, sensitive: Boolean,
            link: String, start: Int, end: Int) {
        val status = status
        val current = findByLink(status!!.media, link)
        if (current == null || current.open_browser) {
            openLink(accountKey, link)
        } else {
            IntentUtils.openMedia(context, status, current, preferences[newDocumentApiKey],
                    preferences[displaySensitiveContentsKey])
        }
    }

    companion object {

        fun findByLink(media: Array<ParcelableMedia>?, link: String?): ParcelableMedia? {
            if (link == null || media == null) return null
            return media.firstOrNull {
                link == it.media_url || link == it.url || link == it.page_url || link == it.preview_url
            }
        }
    }
}

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
import android.support.v7.widget.RecyclerView
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NEW_DOCUMENT_API
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableMediaUtils

/**
 * Created by mariotaku on 15/4/6.
 */
class StatusAdapterLinkClickHandler<D>(context: Context, preferences: SharedPreferencesWrapper) : OnLinkClickHandler(context, null, preferences), Constants {

    private var adapter: IStatusesAdapter<D>? = null

    override fun openMedia(accountKey: UserKey, extraId: Long, sensitive: Boolean,
                           link: String, start: Int, end: Int) {
        if (extraId == RecyclerView.NO_POSITION.toLong()) return
        val status = adapter!!.getStatus(extraId.toInt())
        val media = ParcelableMediaUtils.getAllMedia(status)
        val current = StatusLinkClickHandler.findByLink(media, link)
        if (current != null && current.open_browser) {
            openLink(link)
        } else {
            val newDocument = preferences.getBoolean(KEY_NEW_DOCUMENT_API)
            IntentUtils.openMedia(context, status, current, null, newDocument)
        }
    }

    override fun isMedia(link: String, extraId: Long): Boolean {
        if (extraId != RecyclerView.NO_POSITION.toLong()) {
            val status = adapter!!.getStatus(extraId.toInt())
            val media = ParcelableMediaUtils.getAllMedia(status)
            val current = StatusLinkClickHandler.findByLink(media, link)
            return current != null && !current.open_browser
        }
        return super.isMedia(link, extraId)
    }

    fun setAdapter(adapter: IStatusesAdapter<D>) {
        this.adapter = adapter
    }
}

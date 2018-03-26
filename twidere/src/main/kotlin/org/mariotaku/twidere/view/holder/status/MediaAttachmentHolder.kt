/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2018 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.view.holder.status

import android.view.View
import kotlinx.android.synthetic.main.header_status.view.*
import kotlinx.android.synthetic.main.list_item_status.view.*
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

class MediaAttachmentHolder(adapter: IStatusesAdapter, view: View) : StatusViewHolder.AttachmentHolder(adapter, view) {

    private val mediaLabel = view.attachmentLabel
    private val mediaPreview = view.mediaPreview

    override fun setupViewOptions() {
        mediaPreview.style = adapter.mediaPreviewStyle

    }

    override fun setTextSize(textSize: Float) {

    }

    override fun onClick(listener: IStatusViewHolder.StatusClickListener, holder: StatusViewHolder, v: View, position: Int) {
        when (v) {
            mediaLabel -> {
                if (position < 0) return
                val firstMedia = adapter.getStatus(position).attachment?.media?.firstOrNull()
                if (firstMedia != null) {
                    listener.onMediaClick(holder, v, firstMedia, position)
                } else {
                    listener.onStatusClick(holder, position)
                }
            }
        }
    }

}

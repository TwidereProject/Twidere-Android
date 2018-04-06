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

import android.support.constraint.ConstraintLayout
import android.view.View
import android.widget.ImageView
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.util.view.MediaAttachmentLayoutGenerator
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

class MediaAttachmentHolder(parent: StatusViewHolder, adapter: IStatusesAdapter, view: ConstraintLayout) : StatusViewHolder.AttachmentHolder(parent, adapter, view) {


    override fun setupViewOptions() {

    }

    override fun setTextSize(textSize: Float) {

    }

    override fun display(status: ParcelableStatus) {
        val media = status.attachment!!.media!!
        when (media.size) {
            1 -> MediaAttachmentLayoutGenerator.layout1(view, adapter.mediaPreviewStyle, media.first())
            2 -> MediaAttachmentLayoutGenerator.layout2(view)
            3 -> MediaAttachmentLayoutGenerator.layout3(view)
            4 -> MediaAttachmentLayoutGenerator.layout4(view)
        }
        media.forEachIndexed { index, item ->
            if (index >= view.childCount) return@forEachIndexed
            adapter.requestManager.load(item.preview_url).into(view.getChildAt(index) as ImageView)
        }
    }

    override fun onClick(listener: IStatusViewHolder.StatusClickListener, holder: StatusViewHolder, v: View, position: Int) {
    }


}

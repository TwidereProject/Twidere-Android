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
import kotlinx.android.synthetic.main.layout_content_item_attachment_media.view.*
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.annotation.PreviewStyle
import org.mariotaku.twidere.extension.setVisible
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.util.glide.GlideApp
import org.mariotaku.twidere.view.MediaPreviewImageView
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

class MediaAttachmentHolder(parent: StatusViewHolder, view: ConstraintLayout) : StatusViewHolder.AttachmentHolder(parent, view) {

    private var mediaPreviewStyle: Int = PreviewStyle.CROP
    private var mediaPreviewEnabled: Boolean = false

    private val mediaContainerHelper = view.mediaContainerHelper
    private val mediaPreviewIndicator = view.mediaPreviewIndicator

    override fun setupViewOptions(adapter: IStatusesAdapter) {
        mediaPreviewStyle = adapter.mediaPreviewStyle
        mediaPreviewEnabled = adapter.mediaPreviewEnabled
    }

    override fun setTextSize(textSize: Float) {

    }

    override fun display(status: ParcelableStatus) {
        if (!mediaPreviewEnabled) {
            mediaPreviewIndicator.setVisible(true)
            mediaContainerHelper.hideAll()
            return
        }
        mediaPreviewIndicator.setVisible(false)
        val media = status.attachment!!.media!!
        when (media.size) {
            1 -> mediaContainerHelper.layout1(mediaPreviewStyle, media.first())
            2 -> mediaContainerHelper.layoutGrid(2, 2, "W,1:1")
            3 -> mediaContainerHelper.layout3()
            4 -> mediaContainerHelper.layoutGrid(2, 4, "W,1:2")
        }
        media.forEachIndexed { index, item ->
            if (index >= mediaContainerHelper.referencedCount) return@forEachIndexed
            val child = mediaContainerHelper.getReferencedViewAt(index) as MediaPreviewImageView
            val request = GlideApp.with(child).load(item.preview_url)
            when (mediaPreviewStyle) {
                PreviewStyle.ACTUAL_SIZE, PreviewStyle.SCALE -> {
                    request.fitCenter()
                }
                else -> request.centerCrop()
            }
            request.into(child)
            child.hasPlayIcon = item.type == ParcelableMedia.Type.VIDEO
        }
        view.requestLayout()
    }

    override fun onClick(listener: IStatusViewHolder.StatusClickListener, holder: StatusViewHolder, v: View, position: Int) {
    }


}

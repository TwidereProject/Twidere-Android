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

package org.mariotaku.twidere.view.holder.compose

import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.grid_item_media_editor.view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.MediaPreviewAdapter
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableMediaUpdate

class MediaPreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener, View.OnClickListener {

    private val imageView = itemView.image
    private val videoIndicatorView = itemView.videoIndicator
    private val loadProgress = itemView.loadProgress
    private val removeView = itemView.remove
    private val editView = itemView.edit

    private val requestListener = object : RequestListener<Drawable> {

        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?,
                                     dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            loadProgress.visibility = View.GONE
            return false
        }

        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?,
                                  isFirstResource: Boolean): Boolean {
            loadProgress.visibility = View.GONE
            return false
        }

    }

    var adapter: MediaPreviewAdapter? = null

    init {
        itemView.setOnLongClickListener(this)
        itemView.setOnClickListener(this)
        removeView.setOnClickListener(this)
        editView.setOnClickListener(this)
    }

    fun displayMedia(adapter: MediaPreviewAdapter, media: ParcelableMediaUpdate) {
        loadProgress.visibility = View.VISIBLE
        adapter.requestManager.load(media.uri).listener(requestListener).into(imageView)
        videoIndicatorView.visibility = if (media.type == ParcelableMedia.Type.VIDEO) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun onLongClick(v: View): Boolean {
        adapter?.listener?.onStartDrag(this)
        return false
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.remove -> {
                val adapter = this.adapter ?: return
                if (layoutPosition >= 0 && layoutPosition < adapter.itemCount) {
                    adapter.listener?.onRemoveClick(layoutPosition, this)
                }
            }
            R.id.edit -> {
                adapter?.listener?.onEditClick(layoutPosition, this)
            }
        }
    }
}
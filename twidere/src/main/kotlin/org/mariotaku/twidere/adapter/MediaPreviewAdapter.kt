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

package org.mariotaku.twidere.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.twidere.R
import org.mariotaku.twidere.model.ParcelableMediaUpdate
import org.mariotaku.twidere.view.helper.SimpleItemTouchHelperCallback
import org.mariotaku.twidere.view.helper.SimpleItemTouchHelperCallback.ItemTouchHelperAdapter
import org.mariotaku.twidere.view.holder.compose.MediaPreviewViewHolder
import java.util.*

class MediaPreviewAdapter(
        context: Context,
        requestManager: RequestManager
) : ArrayRecyclerAdapter<ParcelableMediaUpdate, MediaPreviewViewHolder>(context, requestManager) {
    private val inflater = LayoutInflater.from(context)

    val touchAdapter: ItemTouchHelperAdapter = object : ItemTouchHelperAdapter {
        override fun onItemDismiss(position: Int) {
            // No-op
        }

        override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
            Collections.swap(data, fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
            return true
        }

    }
    var listener: Listener? = null

    init {
        setHasStableIds(true)
    }

    fun asList(): List<ParcelableMediaUpdate> = Collections.unmodifiableList(data)

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    override fun onBindViewHolder(holder: MediaPreviewViewHolder, position: Int, item: ParcelableMediaUpdate) {
        val media = getItem(position)
        holder.displayMedia(this, media)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaPreviewViewHolder {
        val view = inflater.inflate(R.layout.grid_item_media_editor, parent, false)
        return MediaPreviewViewHolder(view)
    }

    override fun onViewAttachedToWindow(holder: MediaPreviewViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.adapter = this
    }

    override fun onViewDetachedFromWindow(holder: MediaPreviewViewHolder) {
        holder.adapter = null
        super.onViewDetachedFromWindow(holder)
    }

    fun setAltText(position: Int, altText: String?) {
        data[position].alt_text = altText
        notifyDataSetChanged()
    }

    interface Listener : SimpleItemTouchHelperCallback.OnStartDragListener {
        fun onRemoveClick(position: Int, holder: MediaPreviewViewHolder) {}
        fun onEditClick(position: Int, holder: MediaPreviewViewHolder) {}
        override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {}
    }
}
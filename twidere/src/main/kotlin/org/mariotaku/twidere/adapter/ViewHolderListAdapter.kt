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

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import org.mariotaku.twidere.R

abstract class ViewHolderListAdapter<VH : ViewHolderListAdapter.ViewHolder> : BaseAdapter() {

    final override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: VH
        @Suppress("UNCHECKED_CAST")
        if (convertView != null) {
            viewHolder = convertView.getTag(R.id.tag_view_holder) as VH
        } else {
            val viewType = getItemViewType(position)
            viewHolder = onCreateViewHolder(parent, viewType)
            viewHolder.itemViewType = viewType
            viewHolder.itemView.setTag(R.id.tag_view_holder, viewHolder)
        }
        viewHolder.position = position
        onBindViewHolder(viewHolder, position)
        return viewHolder.itemView
    }

    final override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: VH
        @Suppress("UNCHECKED_CAST")
        if (convertView != null) {
            viewHolder = convertView.getTag(R.id.tag_drop_down_view_holder) as VH
        } else {
            val viewType = getItemViewType(position)
            viewHolder = onCreateDropDownViewHolder(parent, viewType)
            viewHolder.itemViewType = viewType
            viewHolder.itemView.setTag(R.id.tag_drop_down_view_holder, viewHolder)
        }
        viewHolder.position = position
        onBindDropDownViewHolder(viewHolder, position)
        return viewHolder.itemView
    }

    abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH

    abstract fun onBindViewHolder(holder: VH, position: Int)

    open fun onCreateDropDownViewHolder(parent: ViewGroup, viewType: Int): VH = onCreateViewHolder(parent, viewType)

    open fun onBindDropDownViewHolder(holder: VH, position: Int) = onBindViewHolder(holder, position)

    abstract class ViewHolder(val itemView: View) {
        var position: Int = RecyclerPagerAdapter.NO_POSITION
        var itemViewType: Int = 0
    }

    companion object {
        const val NO_POSITION = -1
    }

}

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

package org.mariotaku.twidere.view.holder

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.header_user_timeline_filter.view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.model.timeline.TimelineFilter

/**
 * Created by mariotaku on 2017/3/31.
 */

class TimelineFilterHeaderViewHolder(val adapter: IStatusesAdapter<*>, itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val filterLabel = itemView.filterLabel
    private val filterButton = itemView.filterButton

    init {
        filterButton.setOnClickListener {
            adapter.statusClickListener?.onFilterClick(this)
        }
    }

    companion object {
        const val layoutResource = R.layout.header_user_timeline_filter
    }

    fun display(filter: TimelineFilter) {
        filterLabel.text = filter.getSummary(itemView.context)
    }
}

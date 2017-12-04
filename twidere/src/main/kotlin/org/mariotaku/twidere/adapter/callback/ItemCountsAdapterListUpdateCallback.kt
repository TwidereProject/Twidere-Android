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

package org.mariotaku.twidere.adapter.callback

import android.support.v7.util.ListUpdateCallback
import android.support.v7.widget.RecyclerView
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter

class ItemCountsAdapterListUpdateCallback(
        private val adapter: IItemCountsAdapter,
        private val dataIndex: Int
) : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {
        val dataCount = adapter.itemCounts[dataIndex] + count
        adapter.updateItemCounts()
        adapter.itemCounts[dataIndex] = dataCount
        (adapter as RecyclerView.Adapter<*>).notifyItemRangeInserted(position, count)
    }

    override fun onRemoved(position: Int, count: Int) {
        val dataCount = adapter.itemCounts[dataIndex] - count
        adapter.updateItemCounts()
        adapter.itemCounts[dataIndex] = dataCount
        (adapter as RecyclerView.Adapter<*>).notifyItemRangeRemoved(position, count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        (adapter as RecyclerView.Adapter<*>).notifyItemMoved(fromPosition, toPosition)
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        (adapter as RecyclerView.Adapter<*>).notifyItemRangeChanged(position, count, payload)
    }

}
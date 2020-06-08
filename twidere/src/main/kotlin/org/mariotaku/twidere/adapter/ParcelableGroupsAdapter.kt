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

package org.mariotaku.twidere.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.contains
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IGroupsAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.Companion.ITEM_VIEW_TYPE_LOAD_INDICATOR
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.model.ParcelableGroup
import org.mariotaku.twidere.view.holder.GroupViewHolder
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder

class ParcelableGroupsAdapter(
        context: Context,
        requestManager: RequestManager
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context, requestManager), IGroupsAdapter<List<ParcelableGroup>> {
    override val showAccountsColor: Boolean
        get() = false
    override val nameFirst = preferences[nameFirstKey]
    override var groupAdapterListener: IGroupsAdapter.GroupAdapterListener? = null

    private val inflater = LayoutInflater.from(context)
    private val eventListener: EventListener
    private var data: List<ParcelableGroup>? = null


    init {
        eventListener = EventListener(this)
    }

    fun getData(): List<ParcelableGroup>? {
        return data
    }


    override fun setData(data: List<ParcelableGroup>?) {
        this.data = data
        notifyDataSetChanged()
    }

    private fun bindGroup(holder: GroupViewHolder, position: Int) {
        holder.displayGroup(getGroup(position)!!)
    }

    override fun getItemCount(): Int {
        val position = loadMoreIndicatorPosition
        var count = groupsCount
        if (position and ILoadMoreSupportAdapter.START != 0L) {
            count++
        }
        if (position and ILoadMoreSupportAdapter.END != 0L) {
            count++
        }
        return count
    }

    override fun getGroup(position: Int): ParcelableGroup? {
        if (position == groupsCount) return null
        return data!![position]
    }

    override fun getGroupId(position: Int): String? {
        if (position == groupsCount) return null
        return data!![position].id
    }

    override val groupsCount: Int
        get() = data?.size ?: 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_USER_LIST -> {
                val view: View = inflater.inflate(R.layout.card_item_group_compact, parent, false)
                val holder = GroupViewHolder(this, view)
                holder.setOnClickListeners()
                holder.setupViewOptions()
                return holder
            }
            ITEM_VIEW_TYPE_LOAD_INDICATOR -> {
                val view = inflater.inflate(R.layout.list_item_load_indicator, parent, false)
                return LoadIndicatorViewHolder(view)
            }
        }
        throw IllegalStateException("Unknown view type $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_USER_LIST -> {
                bindGroup(holder as GroupViewHolder, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0 && ILoadMoreSupportAdapter.START in loadMoreIndicatorPosition) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        if (position == groupsCount) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        return ITEM_VIEW_TYPE_USER_LIST
    }

    internal class EventListener(private val adapter: ParcelableGroupsAdapter) : IGroupsAdapter.GroupAdapterListener {

        override fun onGroupClick(holder: GroupViewHolder, position: Int) {
            adapter.groupAdapterListener?.onGroupClick(holder, position)
        }

        override fun onGroupLongClick(holder: GroupViewHolder, position: Int): Boolean {
            return adapter.groupAdapterListener?.onGroupLongClick(holder, position) ?: false
        }
    }

    companion object {

        const val ITEM_VIEW_TYPE_USER_LIST = 2
    }
}

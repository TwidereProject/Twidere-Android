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
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IGroupsAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.Companion.ITEM_VIEW_TYPE_LOAD_INDICATOR
import org.mariotaku.twidere.constant.SharedPreferenceConstants
import org.mariotaku.twidere.model.ParcelableGroup
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.holder.GroupViewHolder
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder

class ParcelableGroupsAdapter(context: Context) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context), Constants, IGroupsAdapter<List<ParcelableGroup>> {
    override val showAccountsColor: Boolean
        get() = false
    override val profileImageStyle: Int
    override val textSize: Float
    override val profileImageEnabled: Boolean
    override val isShowAbsoluteTime: Boolean
    override val nameFirst: Boolean
    override var groupAdapterListener: IGroupsAdapter.GroupAdapterListener? = null

    private val inflater: LayoutInflater
    private val mEventListener: EventListener
    private var data: List<ParcelableGroup>? = null


    init {
        mEventListener = EventListener(this)
        inflater = LayoutInflater.from(context)
        textSize = preferences.getInt(SharedPreferenceConstants.KEY_TEXT_SIZE, context.resources.getInteger(R.integer.default_text_size)).toFloat()
        profileImageStyle = Utils.getProfileImageStyle(preferences.getString(SharedPreferenceConstants.KEY_PROFILE_IMAGE_STYLE, null))
        profileImageEnabled = preferences.getBoolean(SharedPreferenceConstants.KEY_DISPLAY_PROFILE_IMAGE, true)
        nameFirst = preferences.getBoolean(SharedPreferenceConstants.KEY_NAME_FIRST, true)
        isShowAbsoluteTime = preferences.getBoolean(SharedPreferenceConstants.KEY_SHOW_ABSOLUTE_TIME, false)
    }

    fun getData(): List<ParcelableGroup>? {
        return data
    }


    override fun setData(data: List<ParcelableGroup>?) {
        this.data = data
        notifyDataSetChanged()
    }

    protected fun bindGroup(holder: GroupViewHolder, position: Int) {
        holder.displayGroup(getGroup(position))
    }

    override fun getItemCount(): Int {
        val position = loadMoreIndicatorPosition
        var count = groupsCount
        if (position and ILoadMoreSupportAdapter.START !== 0L) {
            count++
        }
        if (position and ILoadMoreSupportAdapter.END !== 0L) {
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
                val view = inflater.inflate(R.layout.card_item_load_indicator, parent, false)
                return LoadIndicatorViewHolder(view)
            }
        }
        throw IllegalStateException("Unknown view type " + viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_USER_LIST -> {
                bindGroup(holder as GroupViewHolder, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (loadMoreIndicatorPosition and ILoadMoreSupportAdapter.START !== 0L && position == 0) {
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

        val ITEM_VIEW_TYPE_USER_LIST = 2
    }
}

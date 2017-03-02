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
import android.support.v4.util.ArrayMap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.model.ItemCounts
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder
import org.mariotaku.twidere.view.holder.SelectableUserViewHolder

class SelectableUsersAdapter(
        context: Context,
        requestManager: RequestManager
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context, requestManager),
        IItemCountsAdapter {

    val ITEM_VIEW_TYPE_USER = 2

    override val itemCounts: ItemCounts = ItemCounts(3)

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val checkedState: MutableMap<UserKey, Boolean> = ArrayMap()
    private val lockedState: MutableMap<UserKey, Boolean> = ArrayMap()
    var itemCheckedListener: ((Int, Boolean) -> Unit)? = null

    var data: List<ParcelableUser>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int {
        val position = loadMoreIndicatorPosition
        itemCounts[0] = if (position and ILoadMoreSupportAdapter.START !== 0L) 1 else 0
        itemCounts[1] = userCount
        itemCounts[2] = if (position and ILoadMoreSupportAdapter.END !== 0L) 1 else 0
        return itemCounts.itemCount
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_USER -> {
                val view = inflater.inflate(R.layout.list_item_simple_user, parent, false)
                val holder = SelectableUserViewHolder(view, this)
                return holder
            }
            ILoadMoreSupportAdapter.ITEM_VIEW_TYPE_LOAD_INDICATOR -> {
                val view = inflater.inflate(R.layout.list_item_load_indicator, parent, false)
                return LoadIndicatorViewHolder(view)
            }
        }
        throw IllegalStateException("Unknown view type " + viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_USER -> {
                bindUser(holder as SelectableUserViewHolder, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        when (itemCounts.getItemCountIndex(position)) {
            0, 2 -> ILoadMoreSupportAdapter.ITEM_VIEW_TYPE_LOAD_INDICATOR
            1 -> return ITEM_VIEW_TYPE_USER
        }
        throw UnsupportedOperationException()
    }

    override fun getItemId(position: Int): Long {
        val countIndex = itemCounts.getItemCountIndex(position)
        when (countIndex) {
            0, 2 -> return Integer.MAX_VALUE.toLong() + countIndex
            1 -> return getUser(position)!!.hashCode().toLong()
        }
        throw UnsupportedOperationException()
    }

    private fun bindUser(holder: SelectableUserViewHolder, position: Int) {
        holder.displayUser(getUser(position)!!)
    }

    fun getUser(position: Int): ParcelableUser? {
        return data?.getOrNull(position - itemCounts.getItemStartPosition(1))
    }

    val userStartIndex: Int
        get() {
            val position = loadMoreIndicatorPosition
            var start = 0
            if (position and ILoadMoreSupportAdapter.START !== 0L) {
                start += 1
            }
            return start
        }

    fun getUserKey(position: Int): UserKey {
        return getUser(position)!!.key
    }

    val userCount: Int
        get() = data?.size ?: 0

    fun removeUserAt(position: Int): Boolean {
        val data = this.data as? MutableList ?: return false
        val dataPosition = position - userStartIndex
        if (dataPosition < 0 || dataPosition >= userCount) return false
        data.removeAt(dataPosition)
        notifyItemRemoved(position)
        return true
    }

    fun setUserAt(position: Int, user: ParcelableUser): Boolean {
        val data = this.data as? MutableList ?: return false
        val dataPosition = position - userStartIndex
        if (dataPosition < 0 || dataPosition >= userCount) return false
        data[dataPosition] = user
        notifyItemChanged(position)
        return true
    }

    fun findPosition(accountKey: UserKey, userKey: UserKey): Int {
        if (data == null) return RecyclerView.NO_POSITION
        for (i in userStartIndex until userStartIndex + userCount) {
            val user = data!![i]
            if (accountKey == user.account_key && userKey == user.key) {
                return i
            }
        }
        return RecyclerView.NO_POSITION
    }


    val checkedCount: Int get() {
        return data?.count { it.key !in lockedState && checkedState[it.key] ?: false } ?: 0
    }

    fun setItemChecked(position: Int, value: Boolean) {
        val userKey = getUserKey(position)
        setCheckState(userKey, value)
        itemCheckedListener?.invoke(position, value)
    }

    fun clearCheckState() {
        checkedState.clear()
    }

    fun setCheckState(userKey: UserKey, value: Boolean) {
        checkedState[userKey] = value
    }

    fun clearLockedState() {
        lockedState.clear()
    }

    fun setLockedState(userKey: UserKey, locked: Boolean) {
        lockedState[userKey] = locked
    }

    fun removeLockedState(userKey: UserKey) {
        lockedState.remove(userKey)
    }

    fun isItemChecked(position: Int): Boolean {
        return checkedState[getUserKey(position)] ?: false
    }

    fun isItemChecked(userKey: UserKey): Boolean {
        return checkedState[userKey] ?: false
    }

    fun getLockedState(position: Int): Boolean {
        return lockedState[getUserKey(position)] ?: false
    }

    fun getLockedState(userKey: UserKey): Boolean {
        return lockedState[userKey] ?: false
    }

    fun isItemLocked(position: Int): Boolean {
        return getUserKey(position) in lockedState
    }

    fun isItemLocked(userKey: UserKey): Boolean {
        return userKey in lockedState
    }

    fun clearSelection() {
        checkedState.clear()
    }
}
/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.IUserListsAdapter
import org.mariotaku.twidere.model.ItemCounts
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.view.holder.SimpleUserListViewHolder

class SimpleParcelableUserListsAdapter(
        context: Context,
        requestManager: RequestManager
) : BaseArrayAdapter<ParcelableUserList>(context, R.layout.list_item_simple_user_list,
        requestManager = requestManager), IUserListsAdapter<List<ParcelableUserList>> {
    override val itemCounts: ItemCounts = ItemCounts(2)

    override val userListsCount: Int = itemCounts[0]
    override val showAccountsColor: Boolean = false
    override val userListClickListener: IUserListsAdapter.UserListClickListener? = null

    override fun getItemId(position: Int): Long {
        when (itemCounts.getItemCountIndex(position)) {
            0 -> {
                return getUserList(position)!!.hashCode().toLong()
            }
            1 -> {
                return Integer.MAX_VALUE + 1L
            }
        }
        throw UnsupportedOperationException()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        when (getItemViewType(position)) {
            0 -> {
                val view = super.getView(position, convertView, parent)
                val holder = view.tag as? SimpleUserListViewHolder ?: run {
                    val h = SimpleUserListViewHolder(this, view)
                    view.tag = h
                    return@run h
                }
                val userList = getItem(position)
                holder.display(userList)
                return view
            }
            1 -> {
                return createViewFromResource(position, convertView, parent, R.layout.list_item_load_indicator)
            }
        }
        throw UnsupportedOperationException()
    }

    override fun getUserList(position: Int): ParcelableUserList? {
        return getItem(position - itemCounts.getItemStartPosition(0))
    }

    override fun getUserListId(position: Int): String? {
        return getUserList(position)?.id
    }

    override fun getItemViewType(position: Int): Int {
        return itemCounts.getItemCountIndex(position)
    }

    override fun getViewTypeCount(): Int {
        return itemCounts.size
    }

    override fun getCount(): Int {
        itemCounts[0] = super.getCount()
        itemCounts[1] = if (loadMoreIndicatorPosition and ILoadMoreSupportAdapter.END != 0L) 1 else 0
        return itemCounts.itemCount
    }

    override fun setData(data: List<ParcelableUserList>?): Boolean {
        clear()
        if (data == null) return false
        data.filter {
            //TODO improve compare
            findItemPosition(it.hashCode().toLong()) < 0
        }.forEach { add(it) }
        return true
    }

}

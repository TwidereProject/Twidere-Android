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
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.view.holder.SimpleUserViewHolder

class SimpleParcelableUsersAdapter(
        context: Context,
        layoutRes: Int = R.layout.list_item_simple_user,
        requestManager: RequestManager
) : BaseArrayAdapter<ParcelableUser>(context, layoutRes, requestManager = requestManager) {

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        return item?.hashCode()?.toLong() ?: -1
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val holder = view.tag as? SimpleUserViewHolder<*> ?: run {
            val h = SimpleUserViewHolder(view, this)
            view.tag = h
            return@run h
        }
        val user = getItem(position)

        holder.displayUser(user)
        return view
    }

    fun setData(data: List<ParcelableUser>?, clearOld: Boolean = false) {
        if (clearOld) {
            clear()
        }
        if (data == null) return
        for (user in data) {
            if (clearOld || findUserPosition(user.key) < 0) {
                add(user)
            }
        }
    }

    fun findUserPosition(userKey: UserKey): Int {
        for (i in 0 until count) {
            if (userKey == getItem(i).key) return i
        }
        return -1
    }

}

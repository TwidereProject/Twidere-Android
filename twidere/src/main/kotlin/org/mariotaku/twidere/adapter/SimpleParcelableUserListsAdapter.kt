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
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IBaseAdapter
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.util.Utils.configBaseAdapter
import org.mariotaku.twidere.view.holder.TwoLineWithIconViewHolder

class SimpleParcelableUserListsAdapter(
        context: Context
) : BaseArrayAdapter<ParcelableUserList>(context, R.layout.list_item_two_line), IBaseAdapter {

    init {
        configBaseAdapter(context, this)
    }

    fun appendData(data: List<ParcelableUserList>) {
        setData(data, false)
    }

    override fun getItemId(position: Int): Long {
        return (if (getItem(position) != null) getItem(position).hashCode() else -1).toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val tag = view.tag
        val holder: TwoLineWithIconViewHolder
        if (tag is TwoLineWithIconViewHolder) {
            holder = tag
        } else {
            holder = TwoLineWithIconViewHolder(view)
            view.tag = holder
        }

        // Clear images in order to prevent images in recycled view shown.
        holder.icon.setImageDrawable(null)

        val user_list = getItem(position)
        val display_name = userColorNameManager.getDisplayName(user_list, isDisplayNameFirst)
        holder.text1.text = user_list.name
        holder.text2.text = context.getString(R.string.created_by, display_name)
        holder.icon.visibility = if (isProfileImageDisplayed) View.VISIBLE else View.GONE
        if (isProfileImageDisplayed) {
            mediaLoader.displayProfileImage(holder.icon, user_list.user_profile_image_url)
        } else {
            mediaLoader.cancelDisplayTask(holder.icon)
        }
        return view
    }

    fun setData(data: List<ParcelableUserList>?, clearOld: Boolean) {
        if (clearOld) {
            clear()
        }
        if (data == null) return
        for (user in data) {
            //TODO improve compare
            if (clearOld || findItemPosition(user.hashCode().toLong()) < 0) {
                add(user)
            }
        }
    }

}

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
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup

import org.mariotaku.twidere.R
import org.mariotaku.twidere.loader.ExtensionsListLoader.ExtensionInfo
import org.mariotaku.twidere.util.PermissionsManager
import org.mariotaku.twidere.view.holder.CheckableTwoLineWithIconViewHolder

class ExtensionsAdapter(context: Context) : ArrayAdapter<ExtensionInfo>(context, R.layout.list_item_two_line_checked) {

    private val mPermissionsManager: PermissionsManager

    init {
        mPermissionsManager = PermissionsManager(context)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)

        val holder: CheckableTwoLineWithIconViewHolder
        val tag = view.tag
        if (tag is CheckableTwoLineWithIconViewHolder) {
            holder = tag
        } else {
            holder = CheckableTwoLineWithIconViewHolder(view)
            view.tag = holder
        }

        val info = getItem(position)
        val permissions = info.permissions ?: emptyArray()
        val permissionValid = PermissionsManager.isPermissionValid(*permissions)
        holder.checkbox.visibility = if (permissionValid) View.VISIBLE else View.GONE
        if (permissionValid) {
            holder.checkbox.isChecked = mPermissionsManager.checkPermission(info.pname, *permissions)
        }
        holder.text1.text = info.label
        holder.text2.visibility = if (TextUtils.isEmpty(info.description)) View.GONE else View.VISIBLE
        holder.text2.text = info.description
        holder.icon.setImageDrawable(info.icon)
        return view
    }

    fun setData(data: List<ExtensionInfo>?) {
        clear()
        if (data != null) {
            addAll(data)
        }
        notifyDataSetChanged()
    }

}

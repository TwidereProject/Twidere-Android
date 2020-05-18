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
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerViewAccessor
import com.bumptech.glide.RequestManager
import org.mariotaku.twidere.R
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.view.holder.AccountViewHolder

class AccountDetailsAdapter(
        context: Context,
        requestManager: RequestManager
) : BaseArrayAdapter<AccountDetails>(context, R.layout.list_item_account, requestManager = requestManager) {

    var sortEnabled: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var switchEnabled: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var accountToggleListener: ((Int, Boolean) -> Unit)? = null

    val checkedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        val position = buttonView.tag as? Int ?: return@OnCheckedChangeListener
        accountToggleListener?.invoke(position, isChecked)
    }

    init {
        GeneralComponent.get(context).inject(this)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getView(position, convertView, parent)
        val showType = objects.groupBy { it.type }.count().let { it > 1 }
        val holder = view.tag as? AccountViewHolder ?: run {
            val h = AccountViewHolder(this, view, showType)
            view.tag = h
            return@run h
        }
        RecyclerViewAccessor.setLayoutPosition(holder, position)
        val details = getItem(position)
        holder.display(details)
        return view
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).key.hashCode().toLong()
    }

    fun drop(from: Int, to: Int) {
        val fromItem = getItem(from)
        removeAt(from)
        insert(fromItem, to)
    }

    fun findItem(key: UserKey): AccountDetails? {
        (0 until count).forEach { i ->
            val item = getItem(i)
            if (key == item.key) return item
        }
        return null
    }

}

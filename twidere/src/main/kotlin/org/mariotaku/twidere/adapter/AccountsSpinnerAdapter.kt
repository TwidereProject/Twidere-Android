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
import kotlinx.android.synthetic.main.list_item_simple_user.view.*
import org.mariotaku.ktextension.spannable
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey

class AccountsSpinnerAdapter(
        context: Context,
        itemViewResource: Int = R.layout.list_item_simple_user,
        accounts: Collection<AccountDetails>? = null,
        requestManager: RequestManager
) : BaseArrayAdapter<AccountDetails>(context, itemViewResource, accounts, requestManager) {

    private var dummyItemText: String? = null

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        bindView(view, getItem(position))
        return view
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        bindView(view, getItem(position))
        return view
    }

    private fun bindView(view: View, item: AccountDetails) {
        val text1 = view.name
        val text2 = view.screenName
        val icon = view.profileImage
        if (!item.dummy) {
            text1?.visibility = View.VISIBLE
            text1?.spannable = item.user.name
            text2?.visibility = View.VISIBLE
            val showType = objects.filter { it.type != null }.groupBy { it.type }.count().let { it > 1 }
            text2?.spannable = if (item.type == AccountType.MASTODON || item.type == AccountType.STATUSNET) {
                item.account.name
            } else {
                "${if (showType) item.type else ""}@${item.user.screen_name}"
            }
            if (icon != null) {
                if (profileImageEnabled) {
                    icon.visibility = View.VISIBLE
                    icon.style = profileImageStyle
                    requestManager.loadProfileImage(context, item.user, profileImageStyle).into(icon)
                } else {
                    icon.visibility = View.GONE
                }
            }
        } else {
            text1?.visibility = View.VISIBLE
            text1?.spannable = dummyItemText
            text2?.visibility = View.GONE
            icon?.visibility = View.GONE
        }
    }


    fun setDummyItemText(textRes: Int) {
        setDummyItemText(context.getString(textRes))
    }

    fun setDummyItemText(text: String) {
        dummyItemText = text
        notifyDataSetChanged()
    }

    fun findPositionByKey(key: UserKey): Int {
        return (0 until count).indexOfFirst { key == getItem(it).key }
    }

}

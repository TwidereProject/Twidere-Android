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
import kotlinx.android.synthetic.main.list_item_simple_user.view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.constant.SharedPreferenceConstants
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.MediaLoaderWrapper
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import javax.inject.Inject

class AccountsSpinnerAdapter(
        context: Context,
        itemViewResource: Int = R.layout.list_item_simple_user
) : ArrayAdapter<AccountDetails>(context, itemViewResource) {

    @Inject
    lateinit var mediaLoader: MediaLoaderWrapper
    private val displayProfileImage: Boolean
    private var dummyItemText: String? = null

    init {
        GeneralComponentHelper.build(context).inject(this)
        displayProfileImage = context.getSharedPreferences(TwidereConstants.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE).getBoolean(SharedPreferenceConstants.KEY_DISPLAY_PROFILE_IMAGE, true)
    }

    constructor(context: Context, accounts: Collection<AccountDetails>) : this(context) {
        addAll(accounts)
    }

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
            if (text1 != null) {
                text1.visibility = View.VISIBLE
                text1.text = item.user.name
            }
            if (text2 != null) {
                text2.visibility = View.VISIBLE
                text2.text = String.format("@%s", item.user.screen_name)
            }
            if (icon != null) {
                icon.visibility = View.VISIBLE
                if (displayProfileImage) {
                    mediaLoader.displayProfileImage(icon, item.user)
                } else {
                    mediaLoader.cancelDisplayTask(icon)
                    //                    icon.setImageResource(R.drawable.ic_profile_image_default);
                }
            }
        } else {
            if (text1 != null) {
                text1.visibility = View.VISIBLE
                text1.text = dummyItemText
            }
            if (text2 != null) {
                text2.visibility = View.GONE
            }
            if (icon != null) {
                icon.visibility = View.GONE
            }
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

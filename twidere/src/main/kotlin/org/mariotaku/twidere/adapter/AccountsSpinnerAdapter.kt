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
import android.widget.ImageView
import android.widget.TextView

import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.constant.SharedPreferenceConstants
import org.mariotaku.twidere.model.ParcelableCredentials
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.MediaLoaderWrapper
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper

import javax.inject.Inject

class AccountsSpinnerAdapter @JvmOverloads constructor(context: Context, itemViewResource: Int = R.layout.list_item_simple_user) : ArrayAdapter<ParcelableCredentials>(context, itemViewResource) {

    @Inject
    lateinit var mediaLoader: MediaLoaderWrapper
    private val displayProfileImage: Boolean
    private var dummyItemText: String? = null

    init {
        GeneralComponentHelper.build(context).inject(this)
        displayProfileImage = context.getSharedPreferences(TwidereConstants.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE).getBoolean(SharedPreferenceConstants.KEY_DISPLAY_PROFILE_IMAGE, true)
    }

    constructor(context: Context, accounts: Collection<ParcelableCredentials>) : this(context) {
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

    private fun bindView(view: View, item: ParcelableCredentials) {
        val text1 = view.findViewById(android.R.id.text1) as TextView?
        val text2 = view.findViewById(android.R.id.text2) as TextView?
        val icon = view.findViewById(android.R.id.icon) as ImageView?
        if (!item.is_dummy) {
            if (text1 != null) {
                text1.visibility = View.VISIBLE
                text1.text = item.name
            }
            if (text2 != null) {
                text2.visibility = View.VISIBLE
                text2.text = String.format("@%s", item.screen_name)
            }
            if (icon != null) {
                icon.visibility = View.VISIBLE
                if (displayProfileImage) {
                    mediaLoader.displayProfileImage(icon, item)
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
        for (i in 0 until count) {
            if (key == getItem(i).account_key) {
                return i
            }
        }
        return -1
    }

}

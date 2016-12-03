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
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IBaseAdapter
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableAccountUtils
import org.mariotaku.twidere.util.MediaLoaderWrapper
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.view.holder.AccountViewHolder
import javax.inject.Inject

class AccountDetailsAdapter(context: Context) : ArrayAdapter<AccountDetails>(context, R.layout.list_item_account), IBaseAdapter {

    @Inject
    lateinit override var mediaLoader: MediaLoaderWrapper

    override var isProfileImageDisplayed: Boolean = false
    private var sortEnabled: Boolean = false
    private var switchEnabled: Boolean = false
    private var onAccountToggleListener: OnAccountToggleListener? = null

    private val checkedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        val tag = buttonView.tag as? String ?: return@OnCheckedChangeListener
        val accountKey = UserKey.valueOf(tag) ?: return@OnCheckedChangeListener
        onAccountToggleListener?.onAccountToggle(accountKey, isChecked)
    }

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getView(position, convertView, parent)
        val holder = view.tag as? AccountViewHolder ?: run {
            val h = AccountViewHolder(view)
            view.tag = h
            return@run h
        }
        val details = getItem(position)
        holder.screenName.text = String.format("@%s", details.user.screen_name)
        holder.setAccountColor(details.color)
        if (isProfileImageDisplayed) {
            mediaLoader.displayProfileImage(holder.profileImage, details.user)
        } else {
            mediaLoader.cancelDisplayTask(holder.profileImage)
        }
        val accountType = details.type
        holder.accountType.setImageResource(ParcelableAccountUtils.getAccountTypeIcon(accountType))
        holder.toggle.isChecked = details.activated
        holder.toggle.setOnCheckedChangeListener(checkedChangeListener)
        holder.toggle.tag = details.user.key
        holder.toggleContainer.visibility = if (switchEnabled) View.VISIBLE else View.GONE
        holder.setSortEnabled(sortEnabled)
        return view
    }

    override val linkHighlightOption: Int
        get() = 0

    override fun setLinkHighlightOption(option: String) {

    }

    override var textSize: Float
        get() = 0f
        set(textSize) {

        }

    override var isDisplayNameFirst: Boolean
        get() = false
        set(nameFirst) {

        }

    override var isShowAccountColor: Boolean
        get() = false
        set(show) {

        }

    fun setSwitchEnabled(enabled: Boolean) {
        if (switchEnabled == enabled) return
        switchEnabled = enabled
        notifyDataSetChanged()
    }

    fun setOnAccountToggleListener(listener: OnAccountToggleListener) {
        onAccountToggleListener = listener
    }

    fun setSortEnabled(sortEnabled: Boolean) {
        if (this.sortEnabled == sortEnabled) return
        this.sortEnabled = sortEnabled
        notifyDataSetChanged()
    }

    interface OnAccountToggleListener {
        fun onAccountToggle(accountId: UserKey, state: Boolean)
    }
}

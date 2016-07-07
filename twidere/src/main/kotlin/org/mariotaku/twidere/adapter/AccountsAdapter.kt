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
import android.database.Cursor
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton

import com.mobeta.android.dslv.SimpleDragSortCursorAdapter

import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IBaseAdapter
import org.mariotaku.twidere.model.ParcelableAccount
import org.mariotaku.twidere.model.ParcelableAccountCursorIndices
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableAccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts
import org.mariotaku.twidere.util.JsonSerializer
import org.mariotaku.twidere.util.MediaLoaderWrapper
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.view.holder.AccountViewHolder

import javax.inject.Inject

class AccountsAdapter(context: Context) : SimpleDragSortCursorAdapter(context,
        R.layout.list_item_account, null, arrayOf(Accounts.NAME),
        intArrayOf(android.R.id.text1), 0), IBaseAdapter {

    @Inject
    lateinit override var mediaLoader: MediaLoaderWrapper

    override var isProfileImageDisplayed: Boolean = false
    private var sortEnabled: Boolean = false
    private var indices: ParcelableAccountCursorIndices? = null
    private var switchEnabled: Boolean = false
    private var onAccountToggleListener: OnAccountToggleListener? = null

    private val mCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        val tag = buttonView.tag
        if (tag !is String) return@OnCheckedChangeListener
        val accountKey = UserKey.valueOf(tag) ?: return@OnCheckedChangeListener
        onAccountToggleListener?.onAccountToggle(accountKey, isChecked)
    }

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    fun getAccount(position: Int): ParcelableAccount? {
        val c = cursor
        if (c == null || c.isClosed || !c.moveToPosition(position)) return null
        return indices!!.newObject(c)
    }

    override fun bindView(view: View, context: Context?, cursor: Cursor) {
        val indices = indices!!
        val color = cursor.getInt(indices.color)
        val holder = view.tag as AccountViewHolder
        holder.screenName.text = String.format("@%s", cursor.getString(indices.screen_name))
        holder.setAccountColor(color)
        if (isProfileImageDisplayed) {
            val user = JsonSerializer.parse(cursor.getString(indices.account_user), ParcelableUser::class.java)
            if (user != null) {
                mediaLoader.displayProfileImage(holder.profileImage, user)
            } else {
                mediaLoader.displayProfileImage(holder.profileImage,
                        cursor.getString(indices.profile_image_url))
            }
        } else {
            mediaLoader.cancelDisplayTask(holder.profileImage)
        }
        val accountType = cursor.getString(indices.account_type)
        holder.accountType.setImageResource(ParcelableAccountUtils.getAccountTypeIcon(accountType))
        holder.toggle.isChecked = cursor.getShort(indices.is_activated).toInt() == 1
        holder.toggle.setOnCheckedChangeListener(mCheckedChangeListener)
        holder.toggle.tag = cursor.getString(indices.account_key)
        holder.toggleContainer.visibility = if (switchEnabled) View.VISIBLE else View.GONE
        holder.setSortEnabled(sortEnabled)
        super.bindView(view, context, cursor)
    }

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup): View {
        val view = super.newView(context, cursor, parent)
        val holder = AccountViewHolder(view)
        view.tag = holder
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

    override fun swapCursor(cursor: Cursor?): Cursor? {
        if (cursor != null) {
            indices = ParcelableAccountCursorIndices(cursor)
        }
        return super.swapCursor(cursor)
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

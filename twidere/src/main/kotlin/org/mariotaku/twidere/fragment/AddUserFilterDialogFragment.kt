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

package org.mariotaku.twidere.fragment

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import android.widget.Toast
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.event.FriendshipTaskEvent
import org.mariotaku.twidere.util.DataStoreUtils

class AddUserFilterDialogFragment : AbsUserMuteBlockDialogFragment() {
    override fun getMessage(user: ParcelableUser): String {
        return getString(R.string.filter_user_confirm_message, userColorNameManager.getDisplayName(user, kPreferences[nameFirstKey]))
    }

    override fun getTitle(user: ParcelableUser): String {
        return getString(R.string.action_add_to_filter)
    }

    override fun performUserAction(user: ParcelableUser, filterEverywhere: Boolean) {
        context?.let { DataStoreUtils.addToFilter(it, listOf(user), filterEverywhere) }
        val accountKey = user.account_key ?: return
        bus.post(FriendshipTaskEvent(FriendshipTaskEvent.Action.FILTER, accountKey, user.key).apply {
            isFinished = true
            isSucceeded = true
        })
        Toast.makeText(context, R.string.message_toast_added_to_filter, Toast.LENGTH_SHORT).show()
    }

    companion object {

        const val FRAGMENT_TAG = "add_user_filter"

        fun show(fm: FragmentManager, user: ParcelableUser): AddUserFilterDialogFragment {
            val args = Bundle()
            args.putParcelable(EXTRA_USER, user)
            val f = AddUserFilterDialogFragment()
            f.arguments = args
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}

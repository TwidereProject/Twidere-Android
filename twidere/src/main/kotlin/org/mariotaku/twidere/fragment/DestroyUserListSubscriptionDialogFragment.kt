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

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER_LIST
import org.mariotaku.twidere.extension.applyOnShow
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.model.ParcelableUserList

class DestroyUserListSubscriptionDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val userList = userList
                val twitter = twitterWrapper
                if (userList == null) return
                twitter.destroyUserListSubscriptionAsync(userList.account_key, userList.id)
            }
            else -> {
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity
        val builder = AlertDialog.Builder(requireContext())
        val userList = userList
        if (userList != null) {
            builder.setTitle(getString(R.string.unsubscribe_from_user_list, userList.name))
            builder.setMessage(getString(R.string.unsubscribe_from_user_list_confirm_message, userList.name))
        }
        builder.setPositiveButton(android.R.string.ok, this)
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.applyOnShow { applyTheme() }
        return dialog
    }

    private val userList: ParcelableUserList?
        get() {
            val args = arguments ?: return null
            if (!args.containsKey(EXTRA_USER_LIST)) return null
            return args.getParcelable<ParcelableUserList>(EXTRA_USER_LIST)
        }

    companion object {

        const val FRAGMENT_TAG = "destroy_user_list"

        fun show(fm: FragmentManager,
                 userList: ParcelableUserList): DestroyUserListSubscriptionDialogFragment {
            val args = Bundle()
            args.putParcelable(EXTRA_USER_LIST, userList)
            val f = DestroyUserListSubscriptionDialogFragment()
            f.arguments = args
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}

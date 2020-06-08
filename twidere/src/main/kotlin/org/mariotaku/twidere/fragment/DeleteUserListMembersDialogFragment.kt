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
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.getNullableTypedArray
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USERS
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER_LIST
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.extension.applyOnShow
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.ParcelableUserList

class DeleteUserListMembersDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val users = users ?: return
                val userList = userList ?: return
                twitterWrapper.deleteUserListMembersAsync(userList.account_key, userList.id, users)
            }
            else -> {
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val users = users
        val userList = userList
        if (users == null || userList == null) throw NullPointerException()
        if (users.size == 1) {
            val user = users[0]
            val nameFirst = preferences[nameFirstKey]
            val displayName = userColorNameManager.getDisplayName(user, nameFirst)
            builder.setTitle(getString(R.string.delete_user, displayName))
            builder.setMessage(getString(R.string.delete_user_from_list_confirm, displayName, userList.name))
        } else {
            builder.setTitle(R.string.delete_users)
            val res = resources
            val message = res.getQuantityString(R.plurals.delete_N_users_from_list_confirm, users.size,
                    users.size, userList.name)
            builder.setMessage(message)
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

    private val users: Array<ParcelableUser>?
        get() {
            val args = arguments ?: return null
            if (!args.containsKey(EXTRA_USERS)) return null
            return args.getNullableTypedArray(EXTRA_USERS)
        }

    companion object {

        const val FRAGMENT_TAG = "destroy_user_list_member"

        fun show(fm: FragmentManager, userList: ParcelableUserList,
                 vararg users: ParcelableUser): DeleteUserListMembersDialogFragment {
            val args = Bundle()
            args.putParcelable(EXTRA_USER_LIST, userList)
            args.putParcelableArray(EXTRA_USERS, users)
            val f = DeleteUserListMembersDialogFragment()
            f.arguments = args
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}

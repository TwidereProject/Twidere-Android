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
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import org.mariotaku.ktextension.Bundle
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.applyOnShow
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.extension.userList
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.promise.UserListPromises

class DestroyUserListSubscriptionDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val userList = arguments!!.userList ?: return
                UserListPromises.get(context!!).unsubscribe(userList.account_key, userList.id)
            }
            else -> {
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity!!
        val builder = AlertDialog.Builder(context)
        val userList = arguments!!.userList
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

    companion object {

        val FRAGMENT_TAG = "destroy_user_list"

        fun show(fm: FragmentManager, userList: ParcelableUserList): DestroyUserListSubscriptionDialogFragment {
            val f = DestroyUserListSubscriptionDialogFragment()
            f.arguments = Bundle {
                this.userList = userList
            }
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}

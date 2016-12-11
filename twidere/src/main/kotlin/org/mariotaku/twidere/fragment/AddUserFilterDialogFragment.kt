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
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NAME_FIRST
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.message.FriendshipTaskEvent
import org.mariotaku.twidere.util.DataStoreUtils

class AddUserFilterDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    private val user: ParcelableUser by lazy { arguments.getParcelable<ParcelableUser>(EXTRA_USER) }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val filterEverywhere = ((dialog as Dialog).findViewById(R.id.filterEverywhereToggle) as CheckBox).isChecked
                DataStoreUtils.addToFilter(context, user, filterEverywhere)
                bus.post(FriendshipTaskEvent(FriendshipTaskEvent.Action.FILTER, user.account_key, user.key).apply {
                    isFinished = true
                    isSucceeded = true
                })
                Toast.makeText(context, R.string.message_toast_added_to_filter, Toast.LENGTH_SHORT).show()
            }
            else -> {
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val nameFirst = preferences.getBoolean(KEY_NAME_FIRST)
        val displayName = userColorNameManager.getDisplayName(user, nameFirst)
        builder.setTitle(R.string.add_to_filter)
        builder.setView(R.layout.dialog_filter_user_confirm)
        builder.setPositiveButton(android.R.string.ok, this)
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.setOnShowListener {
            val confirmMessageView = dialog.findViewById(R.id.confirmMessage) as TextView
            val filterEverywhereHelp = dialog.findViewById(R.id.filterEverywhereHelp)!!
            filterEverywhereHelp.setOnClickListener {
                MessageDialogFragment.show(childFragmentManager, title = getString(R.string.filter_everywhere),
                        message = getString(R.string.filter_everywhere_description), tag = "filter_everywhere_help")
            }
            confirmMessageView.text = getString(R.string.filter_user_confirm_message, displayName)
        }
        return dialog
    }

    companion object {

        val FRAGMENT_TAG = "add_user_filter"

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

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
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NAME_FIRST
import org.mariotaku.twidere.model.ParcelableUser

class CreateUserBlockDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val user = user
                val twitter = twitterWrapper
                if (user == null) return
                twitter.createBlockAsync(user.account_key, user.key)
            }
            else -> {
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity
        val context = activity
        val builder = AlertDialog.Builder(context)
        val user = user
        if (user != null) {
            val nameFirst = preferences.getBoolean(KEY_NAME_FIRST)
            val displayName = userColorNameManager.getDisplayName(user, nameFirst)
            builder.setTitle(getString(R.string.block_user, displayName))
            builder.setMessage(getString(R.string.block_user_confirm_message, displayName))
        }
        builder.setPositiveButton(android.R.string.ok, this)
        builder.setNegativeButton(android.R.string.cancel, null)
        return builder.create()
    }

    private val user: ParcelableUser?
        get() {
            val args = arguments
            if (!args.containsKey(EXTRA_USER)) return null
            return args.getParcelable<ParcelableUser>(EXTRA_USER)
        }

    companion object {

        val FRAGMENT_TAG = "create_user_block"

        fun show(fm: FragmentManager, user: ParcelableUser): CreateUserBlockDialogFragment {
            val args = Bundle()
            args.putParcelable(EXTRA_USER, user)
            val f = CreateUserBlockDialogFragment()
            f.arguments = args
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}

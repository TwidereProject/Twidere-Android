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
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.extension.applyOnShow
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.model.ParcelableUser

class DestroyFriendshipDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val accountKey = user.account_key ?: return
                twitterWrapper.destroyFriendshipAsync(accountKey, user.key)
            }
            else -> {
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val nameFirst = preferences[nameFirstKey]
        val displayName = userColorNameManager.getDisplayName(user, nameFirst)
        builder.setTitle(getString(R.string.unfollow_user, displayName))
        builder.setMessage(getString(R.string.unfollow_user_confirm_message, displayName))
        builder.setPositiveButton(android.R.string.ok, this)
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.applyOnShow { applyTheme() }
        return dialog
    }

    private val user: ParcelableUser
        get() = arguments?.getParcelable(EXTRA_USER)!!

    companion object {

        const val FRAGMENT_TAG = "destroy_friendship"

        fun show(fm: FragmentManager, user: ParcelableUser): DestroyFriendshipDialogFragment {
            val f = DestroyFriendshipDialogFragment()
            f.arguments = Bundle {
                this[EXTRA_USER] = user
            }
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}

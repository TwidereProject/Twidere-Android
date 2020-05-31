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
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.dialog_edit_user_nickname.*
import org.mariotaku.ktextension.empty
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_NAME
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER_KEY
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.model.UserKey

class SetUserNicknameDialogFragment : BaseDialogFragment(), OnClickListener {

    override fun onClick(dialog: DialogInterface, which: Int) {
        val editName = (dialog as AlertDialog).editName
        val userKey = arguments?.getParcelable<UserKey>(EXTRA_USER_KEY)!!
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                if (editName.empty) {
                    userColorNameManager.clearUserNickname(userKey)
                } else {
                    userColorNameManager.setUserNickname(userKey, editName.text.toString())
                }
            }
            DialogInterface.BUTTON_NEUTRAL -> {
                userColorNameManager.clearUserNickname(userKey)
            }
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val nick = arguments?.getString(EXTRA_NAME)
        val context = activity
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.title_set_nickname)
        builder.setPositiveButton(android.R.string.ok, this)
        if (!nick.isNullOrEmpty()) {
            builder.setNeutralButton(R.string.action_clear, this)
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.setView(R.layout.dialog_edit_user_nickname)
        val dialog = builder.create()
        dialog.onShow { it.applyTheme() }
        return dialog
    }

    companion object {

        const val FRAGMENT_TAG = "set_user_nickname"

        fun create(userKey: UserKey, nickname: String?): SetUserNicknameDialogFragment {
            val f = SetUserNicknameDialogFragment()
            val args = Bundle()
            args.putParcelable(EXTRA_USER_KEY, userKey)
            args.putString(EXTRA_NAME, nickname)
            f.arguments = args
            return f
        }

        fun show(fm: FragmentManager, userKey: UserKey, nickname: String?): SetUserNicknameDialogFragment {
            val f = create(userKey, nickname)
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }

}

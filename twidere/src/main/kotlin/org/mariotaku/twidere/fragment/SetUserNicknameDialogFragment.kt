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
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.widget.EditText
import org.mariotaku.ktextension.empty
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_NAME
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER_KEY
import org.mariotaku.twidere.model.UserKey

class SetUserNicknameDialogFragment : BaseDialogFragment(), OnClickListener {

    override fun onClick(dialog: DialogInterface, which: Int) {
        val args = arguments!!
        val editName = (dialog as AlertDialog).findViewById(R.id.editName) as EditText
        val userId = args.getParcelable<UserKey>(EXTRA_USER_KEY)!!
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                if (editName.empty) {
                    userColorNameManager.clearUserNickname(userId)
                } else {
                    userColorNameManager.setUserNickname(userId, editName.text.toString())
                }
            }
            DialogInterface.BUTTON_NEUTRAL -> {
                userColorNameManager.clearUserNickname(userId)
            }
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments
        val nick: String? = args.getString(EXTRA_NAME)
        val context = activity
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.set_nickname)
        builder.setPositiveButton(android.R.string.ok, this)
        if (!TextUtils.isEmpty(nick)) {
            builder.setNeutralButton(R.string.clear, this)
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.setView(R.layout.dialog_edit_user_nickname)
        return builder.create()
    }

    companion object {

        private val FRAGMENT_TAG_SET_USER_NICKNAME = "set_user_nickname"

        fun show(fm: FragmentManager, userKey: UserKey, nickname: String?): SetUserNicknameDialogFragment {
            val f = SetUserNicknameDialogFragment()
            val args = Bundle()
            args.putParcelable(EXTRA_USER_KEY, userKey)
            args.putString(EXTRA_NAME, nickname)
            f.arguments = args
            f.show(fm, FRAGMENT_TAG_SET_USER_NICKNAME)
            return f
        }
    }

}

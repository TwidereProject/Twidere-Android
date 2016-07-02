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
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.widget.CheckBox
import com.rengwuxian.materialedittext.MaterialEditText
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.text.validator.UserListNameValidator
import org.mariotaku.twidere.util.ParseUtils

class CreateUserListDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val alertDialog = dialog as AlertDialog
                val args = arguments
                val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
                val editName = alertDialog.findViewById(R.id.name) as MaterialEditText?
                val editDescription = alertDialog.findViewById(R.id.description) as MaterialEditText?
                val editPublic = alertDialog.findViewById(R.id.is_public) as CheckBox?
                assert(editName != null && editDescription != null && editPublic != null)
                val name = ParseUtils.parseString(editName!!.text)
                val description = ParseUtils.parseString(editDescription!!.text)
                val isPublic = editPublic!!.isChecked
                if (TextUtils.isEmpty(name)) return
                twitterWrapper.createUserListAsync(accountKey, name, isPublic, description)
            }
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.dialog_user_list_detail_editor)

        builder.setTitle(R.string.new_user_list)
        builder.setPositiveButton(android.R.string.ok, this)
        builder.setNegativeButton(android.R.string.cancel, this)
        val dialog = builder.create()
        dialog.setOnShowListener { dialog ->
            val alertDialog = dialog as AlertDialog
            val editName = alertDialog.findViewById(R.id.name) as MaterialEditText?
            val editDescription = alertDialog.findViewById(R.id.description) as MaterialEditText?
            val publicCheckBox = alertDialog.findViewById(R.id.is_public) as CheckBox?
            editName!!.addValidator(UserListNameValidator(getString(R.string.invalid_list_name)))
        }
        return dialog
    }

}

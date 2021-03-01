/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.dialog_user_list_detail_editor.*
import org.mariotaku.ktextension.string
import org.mariotaku.microblog.library.twitter.model.UserList
import org.mariotaku.microblog.library.twitter.model.UserListUpdate
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.extension.positive
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.text.validator.UserListNameValidator

class EditUserListDialogFragment : BaseDialogFragment() {

    private val accountKey by lazy { arguments?.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)!! }
    private val listId: String by lazy { arguments?.getString(EXTRA_LIST_ID)!! }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(R.layout.dialog_user_list_detail_editor)
        builder.setTitle(R.string.title_user_list)
        builder.positive(android.R.string.ok, this::onPositiveClick)
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.onShow { alertDialog ->
            alertDialog.applyTheme()
            alertDialog.editName.addValidator(UserListNameValidator(getString(R.string.invalid_list_name)))
            if (savedInstanceState == null) {
                alertDialog.editName.setText(arguments?.getString(EXTRA_LIST_NAME))
                alertDialog.editDescription.setText(arguments?.getString(EXTRA_DESCRIPTION))
                alertDialog.isPublic.isChecked = arguments?.getBoolean(EXTRA_IS_PUBLIC, true) ?: true
            }
        }
        return dialog
    }

    private fun onPositiveClick(dialog: Dialog) {
        val name = dialog.editName.string?.takeIf(String::isNotEmpty) ?: return
        val description = dialog.editDescription.string
        val isPublic = dialog.isPublic.isChecked
        val update = UserListUpdate()
        update.setMode(if (isPublic) UserList.Mode.PUBLIC else UserList.Mode.PRIVATE)
        update.setName(name)
        update.setDescription(description)
        twitterWrapper.updateUserListDetails(accountKey, listId, update)
    }

}
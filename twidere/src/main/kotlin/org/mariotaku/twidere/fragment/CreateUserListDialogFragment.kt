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
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.text.TextUtils
import android.widget.CheckBox
import android.widget.EditText
import com.rengwuxian.materialedittext.MaterialEditText
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.extension.applyOnShow
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.positive
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.text.validator.UserListNameValidator
import org.mariotaku.twidere.util.ParseUtils

class CreateUserListDialogFragment : BaseDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(R.layout.dialog_user_list_detail_editor)

        builder.setTitle(R.string.new_user_list)
        builder.positive(android.R.string.ok) { dialog ->
            val accountKey: UserKey = arguments?.getParcelable(EXTRA_ACCOUNT_KEY)!!
            val editName = dialog.findViewById<EditText>(R.id.editName)!!
            val editDescription = dialog.findViewById<EditText>(R.id.editDescription)!!
            val editPublic = dialog.findViewById<CheckBox>(R.id.isPublic)!!
            val name = ParseUtils.parseString(editName.text)
            val description = ParseUtils.parseString(editDescription.text)
            val isPublic = editPublic.isChecked
            if (TextUtils.isEmpty(name)) return@positive
            twitterWrapper.createUserListAsync(accountKey, name, isPublic, description)
        }
        val dialog = builder.create()
        dialog.applyOnShow {
            applyTheme()
            val editName = dialog.findViewById<MaterialEditText>(R.id.editName)!!
            editName.addValidator(UserListNameValidator(getString(R.string.invalid_list_name)))
        }
        return dialog
    }

}

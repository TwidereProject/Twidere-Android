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
import androidx.appcompat.app.AlertDialog
import android.widget.CheckBox
import kotlinx.android.synthetic.main.dialog_block_mute_filter_user_confirm.*
import org.mariotaku.ktextension.spannable
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER
import org.mariotaku.twidere.extension.applyOnShow
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.model.ParcelableUser

abstract class AbsUserMuteBlockDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    private val user: ParcelableUser by lazy { arguments?.getParcelable<ParcelableUser>(EXTRA_USER)!! }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val filterEverywhere = (dialog as Dialog).findViewById<CheckBox>(R.id.filterEverywhereToggle).isChecked
                performUserAction(user, filterEverywhere)
            }
            else -> {
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getTitle(user))
        builder.setView(R.layout.dialog_block_mute_filter_user_confirm)
        builder.setPositiveButton(getPositiveButtonTitle(user), this)
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.applyOnShow {
            // Workaround for "Invalid Android class type: UNKNOWN"
            applyTheme()
            filterEverywhereHelp.setOnClickListener {
                MessageDialogFragment.show(childFragmentManager, title = getString(R.string.filter_everywhere),
                        message = getString(R.string.filter_everywhere_description), tag = "filter_everywhere_help")
            }
            confirmMessage.spannable = getMessage(user)
        }
        return dialog
    }

    abstract fun performUserAction(user: ParcelableUser, filterEverywhere: Boolean)

    protected abstract fun getTitle(user: ParcelableUser): String
    protected abstract fun getMessage(user: ParcelableUser): String
    protected open fun getPositiveButtonTitle(user: ParcelableUser): String = getString(android.R.string.ok)
}

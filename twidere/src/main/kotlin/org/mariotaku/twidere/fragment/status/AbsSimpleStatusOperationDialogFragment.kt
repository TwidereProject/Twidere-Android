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

package org.mariotaku.twidere.fragment.status

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_STATUS
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.model.ParcelableStatus

abstract class AbsSimpleStatusOperationDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    protected abstract val title: String?
    protected abstract val message: String

    protected val status: ParcelableStatus
        get() = arguments?.getParcelable(EXTRA_STATUS)!!

    final override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                onPerformAction(status)
            }
        }
    }

    final override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(android.R.string.ok, this)
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.onShow { it.applyTheme() }
        return dialog
    }

    protected abstract fun onPerformAction(status: ParcelableStatus)

}

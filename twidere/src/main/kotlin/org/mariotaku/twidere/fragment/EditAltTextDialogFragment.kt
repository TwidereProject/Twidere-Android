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
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.dialog_compose_edit_alt_text.*
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.ktextension.string
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_POSITION
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_TEXT
import org.mariotaku.twidere.extension.applyOnShow
import org.mariotaku.twidere.extension.applyTheme

class EditAltTextDialogFragment : BaseDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.edit_description)
        builder.setView(R.layout.dialog_compose_edit_alt_text)
        builder.setNegativeButton(android.R.string.cancel, null)
        val position = requireArguments().getInt(EXTRA_POSITION)
        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            val altText = (dialog as Dialog).editText.string
            callback?.onSetAltText(position, altText)
        }
        builder.setNeutralButton(R.string.action_clear) { _, _ ->
            callback?.onSetAltText(position, null)
        }
        val dialog = builder.create()
        dialog.applyOnShow {
            applyTheme()
            editText.setText(requireArguments().getString(EXTRA_TEXT))
        }
        return dialog
    }

    private val callback: EditAltTextCallback?
        get() = targetFragment as? EditAltTextCallback ?: parentFragment as? EditAltTextCallback ?: context as? EditAltTextCallback

    interface EditAltTextCallback {
        fun onSetAltText(position: Int, altText: String?)
    }

    companion object {
        fun show(fm: FragmentManager, position: Int, altText: String?) {
            val df = EditAltTextDialogFragment()
            df.arguments = Bundle {
                this[Constants.EXTRA_TEXT] = altText
                this[Constants.EXTRA_POSITION] = position
            }
            df.show(fm, "edit_alt_text")
        }
    }
}
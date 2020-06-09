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
import android.graphics.Color
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import me.uucky.colorpicker.ColorPickerDialog
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.fragment.iface.IDialogFragmentCallback

class ColorPickerDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    private var controller: ColorPickerDialog.Controller? = null

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        val a = activity
        if (a is Callback) {
            a.onCancelled()
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        val a = activity
        if (a !is Callback || controller == null) return
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val color = controller!!.color
                a.onColorSelected(color)
            }
            DialogInterface.BUTTON_NEUTRAL -> {
                a.onColorCleared()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val color: Int
        val args = arguments
        color =
            savedInstanceState?.getInt(EXTRA_COLOR, Color.WHITE) ?: args!!.getInt(EXTRA_COLOR, Color.WHITE)

        val activity = activity
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(me.uucky.colorpicker.R.layout.cp__dialog_color_picker)
        builder.setPositiveButton(android.R.string.ok, this)
        if (args!!.getBoolean(EXTRA_CLEAR_BUTTON, false)) {
            builder.setNeutralButton(R.string.action_clear, this)
        }
        builder.setNegativeButton(android.R.string.cancel, this)
        val dialog = builder.create()
        dialog.onShow {
            it.applyTheme()
            controller = ColorPickerDialog.Controller(it.context, it.window!!.decorView)

            val showAlphaSlider = args.getBoolean(EXTRA_ALPHA_SLIDER, true)
            for (presetColor in PRESET_COLORS) {
                controller!!.addColor(ContextCompat.getColor(requireContext(), presetColor))
            }
            controller!!.setAlphaEnabled(showAlphaSlider)
            controller!!.setInitialColor(color)
        }
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val a = activity
        if (a is Callback) {
            a.onDismissed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (controller != null) {
            outState.putInt(EXTRA_COLOR, controller!!.color)
        }
        super.onSaveInstanceState(outState)
    }

    interface Callback : IDialogFragmentCallback {

        fun onColorCleared()

        fun onColorSelected(color: Int)

    }

}

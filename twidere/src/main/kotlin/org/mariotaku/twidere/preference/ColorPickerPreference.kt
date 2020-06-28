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

package org.mariotaku.twidere.preference

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceViewHolder
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import me.uucky.colorpicker.ColorPickerDialog
import org.mariotaku.twidere.Constants.PRESET_COLORS
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.preference.iface.IDialogPreference
import org.mariotaku.twidere.util.TwidereColorUtils

class ColorPickerPreference(context: Context, attrs: AttributeSet? = null) :
        DialogPreference(context, attrs, R.attr.dialogPreferenceStyle), IDialogPreference {

    var defaultValue: Int = Color.WHITE
    var isAlphaSliderEnabled: Boolean

    private val value: Int
        get() {
            try {
                if (isPersistent) return getPersistedInt(defaultValue)
            } catch (e: ClassCastException) {
                Log.w(LOGTAG, e)
            }

            return defaultValue
        }

    init {
        widgetLayoutResource = R.layout.preference_widget_color_picker

        val a = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerPreferences)
        isAlphaSliderEnabled = a.getBoolean(R.styleable.ColorPickerPreferences_alphaSlider, false)
        setDefaultValue(a.getColor(R.styleable.ColorPickerPreferences_defaultColor, 0))
        a.recycle()
    }

    override fun setDefaultValue(value: Any) {
        if (value !is Int) return
        defaultValue = value
    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        if (isPersistent && defaultValue is Int) {
            persistInt(if (restoreValue) value else defaultValue)
        }
    }

    override fun displayDialog(fragment: PreferenceFragmentCompat) {
        val df = ColorPickerPreferenceDialogFragment.newInstance(key)
        df.setTargetFragment(fragment, 0)
        fragment.parentFragmentManager.let { df.show(it, key) }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val imageView = holder.findViewById(R.id.color) as ImageView
        imageView.setImageBitmap(TwidereColorUtils.getColorPreviewBitmap(context, value, false))
    }

    class ColorPickerPreferenceDialogFragment : PreferenceDialogFragmentCompat(), DialogInterface.OnShowListener, DialogInterface.OnClickListener {

        private lateinit var controller: ColorPickerDialog.Controller

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val preference = preference as ColorPickerPreference
            val context = context
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(preference.dialogTitle)
            builder.setView(R.layout.cp__dialog_color_picker)
            builder.setPositiveButton(android.R.string.ok, this)
            builder.setNegativeButton(android.R.string.cancel, this)
            val dialog = builder.create()
            dialog.setOnShowListener(this)
            return dialog
        }

        override fun onDialogClosed(positive: Boolean) {
            val preference = preference as ColorPickerPreference
            val color = controller.color
            if (preference.isPersistent) {
                preference.persistInt(color)
            }
            preference.callChangeListener(color)
            preference.notifyChanged()
        }

        override fun onShow(dialog: DialogInterface) {
            val context = context ?: return
            val preference = preference as ColorPickerPreference
            val alertDialog = dialog as AlertDialog
            alertDialog.applyTheme()
            val windowView = alertDialog.window!!.decorView
            controller = ColorPickerDialog.Controller(context, windowView)
            controller.setAlphaEnabled(preference.isAlphaSliderEnabled)
            for (presetColor in PRESET_COLORS) {
                controller.addColor(ContextCompat.getColor(context, presetColor))
            }
            controller.setInitialColor(preference.value)
        }

        companion object {


            fun newInstance(key: String): ColorPickerPreferenceDialogFragment {
                val df = ColorPickerPreferenceDialogFragment()
                val args = Bundle()
                args.putString(ARG_KEY, key)
                df.arguments = args
                return df
            }
        }

    }

}

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
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.dialog_date_time_picker.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.displayedChildId
import org.mariotaku.twidere.extension.onShow
import java.util.*

/**
 * Created by mariotaku on 2017/3/24.
 */

class DateTimePickerDialogFragment : BaseDialogFragment() {

    private val listener: OnDateTimeSelectedListener? get() {
        return targetFragment as? OnDateTimeSelectedListener ?:
                parentFragment as? OnDateTimeSelectedListener ?:
                context as? OnDateTimeSelectedListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(R.layout.dialog_date_time_picker)
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.setPositiveButton(android.R.string.ok, null)
        builder.setNeutralButton(R.string.action_clear) { _, _ ->
            listener?.onDateCleared()
        }
        val dialog = builder.create()
        dialog.onShow {
            it.applyTheme()

            val positiveButton = it.getButton(DialogInterface.BUTTON_POSITIVE)

            val viewAnimator = it.viewAnimator
            val datePicker = it.datePicker
            val timePicker = it.timePicker
            val calendar = Calendar.getInstance()

            fun showTimePicker() {
                viewAnimator.displayedChildId = R.id.timePicker
                positiveButton.text = getText(android.R.string.ok)
                positiveButton.setOnClickListener {
                    finishSelection(calendar)
                }
            }

            datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)) { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                showTimePicker()
            }
            timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
            }
            viewAnimator.displayedChildId = R.id.datePicker
            positiveButton.text = getText(R.string.action_next_step)

            positiveButton.setOnClickListener {
                showTimePicker()
            }
        }
        return dialog
    }

    private fun finishSelection(calendar: Calendar) {
        listener?.onDateSelected(calendar.time)
        dismiss()
    }

    interface OnDateTimeSelectedListener {
        fun onDateSelected(date: Date)
        fun onDateCleared()
    }
}

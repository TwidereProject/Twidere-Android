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

package org.mariotaku.twidere.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.fragment.ColorPickerDialogFragment
import org.mariotaku.twidere.fragment.ColorPickerDialogFragment.Callback

class ColorPickerDialogActivity : BaseActivity(), Callback {

    override fun onCancelled() {
        finish()
    }

    override fun onStart() {
        super.onStart()
        setVisible(true)
    }

    override fun onColorCleared() {
        setResult(RESULT_CLEARED)
        finish()
    }

    override fun onColorSelected(color: Int) {
        setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_COLOR, color)
                .putExtra(EXTRA_EXTRAS, intent.getBundleExtra(EXTRA_EXTRAS)))
        finish()
    }

    override fun onDismissed() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val intent = intent
            val f = ColorPickerDialogFragment()
            val args = Bundle()
            args.putInt(EXTRA_COLOR, intent.getIntExtra(EXTRA_COLOR, Color.WHITE))
            args.putBoolean(EXTRA_CLEAR_BUTTON, intent.getBooleanExtra(EXTRA_CLEAR_BUTTON, false))
            args.putBoolean(EXTRA_ALPHA_SLIDER, intent.getBooleanExtra(EXTRA_ALPHA_SLIDER, true))
            f.arguments = args
            f.show(supportFragmentManager, "color_picker_dialog")
        }
    }

    companion object {

        const val RESULT_CLEARED = -2
    }

}

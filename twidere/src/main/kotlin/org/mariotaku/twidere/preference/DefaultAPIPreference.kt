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

import android.content.Context
import androidx.preference.DialogPreference
import androidx.preference.PreferenceFragmentCompat
import android.util.AttributeSet
import org.mariotaku.twidere.R
import org.mariotaku.twidere.fragment.APIEditorDialogFragment
import org.mariotaku.twidere.preference.iface.IDialogPreference

class DefaultAPIPreference(
        context: Context,
        attrs: AttributeSet? = null
) : DialogPreference(context, attrs, R.attr.dialogPreferenceStyle), IDialogPreference {

    override fun displayDialog(fragment: PreferenceFragmentCompat) {
        val df = APIEditorDialogFragment()
        df.setTargetFragment(fragment, 0)
        fragment.parentFragmentManager.let { df.show(it, key) }
    }

}

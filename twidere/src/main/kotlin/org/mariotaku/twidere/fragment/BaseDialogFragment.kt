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

import android.content.ContentResolver
import android.content.Context
import android.support.v4.app.DialogFragment
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import javax.inject.Inject

open class BaseDialogFragment : DialogFragment(), Constants {

    @Inject
    lateinit var twitterWrapper: AsyncTwitterWrapper
    @Inject
    lateinit var userColorNameManager: UserColorNameManager
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    @Inject
    lateinit var validator: TwidereValidator
    @Inject
    lateinit var keyboardShortcutsHandler: KeyboardShortcutsHandler


    val application: TwidereApplication?
        get() {
            val activity = activity
            if (activity != null) return activity.application as TwidereApplication
            return null
        }

    val contentResolver: ContentResolver
        get() = activity.contentResolver

    fun getSystemService(name: String): Any? {
        val activity = activity
        if (activity != null) return activity.getSystemService(name)
        return null
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        GeneralComponentHelper.build(context!!).inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        DebugModeUtils.watchReferenceLeak(this)
    }

}

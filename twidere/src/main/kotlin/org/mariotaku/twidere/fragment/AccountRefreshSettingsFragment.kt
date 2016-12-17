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

import android.content.SharedPreferences
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_AUTO_REFRESH
import org.mariotaku.twidere.constant.defaultAutoRefreshKey
import org.mariotaku.twidere.util.Utils

class AccountRefreshSettingsFragment : BaseAccountPreferenceFragment() {

    override val preferencesResource: Int
        get() = R.xml.preferences_account_refresh

    override val switchPreferenceDefault: Boolean
        get() = preferenceManager.sharedPreferences[defaultAutoRefreshKey]

    override val switchPreferenceKey: String?
        get() = KEY_AUTO_REFRESH

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        if (KEY_AUTO_REFRESH == key) {
            Utils.startRefreshServiceIfNeeded(activity)
        }
    }
}

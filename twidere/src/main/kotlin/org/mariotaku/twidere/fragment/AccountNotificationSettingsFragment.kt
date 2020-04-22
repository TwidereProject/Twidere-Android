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

import android.os.Bundle
import androidx.preference.Preference
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.SharedPreferenceConstants.*
import org.mariotaku.twidere.preference.notification.AccountNotificationChannelsPreference

class AccountNotificationSettingsFragment : BaseAccountPreferenceFragment() {

    override val preferencesResource: Int
        get() = R.xml.preferences_account_notifications

    override val switchPreferenceDefault: Boolean
        get() = DEFAULT_NOTIFICATION

    override val switchPreferenceKey: String?
        get() = KEY_NOTIFICATION

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val account = this.account
        findPreference<Preference>(KEY_NOTIFICATION_LIGHT_COLOR)?.let {
            if (account != null) {
                it.setDefaultValue(account.color)
            }
        }
        (findPreference("notification_channels") as? AccountNotificationChannelsPreference)?.account = account
    }

}

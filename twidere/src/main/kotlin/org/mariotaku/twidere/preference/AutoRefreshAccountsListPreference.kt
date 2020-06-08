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

package org.mariotaku.twidere.preference

import android.content.Context
import android.util.AttributeSet
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_DEFAULT_AUTO_REFRESH
import org.mariotaku.twidere.fragment.AccountRefreshSettingsFragment
import org.mariotaku.twidere.model.AccountDetails

class AutoRefreshAccountsListPreference(context: Context, attrs: AttributeSet? = null) : AccountsListPreference(context, attrs) {

    override fun setupPreference(preference: AccountItemPreference, account: AccountDetails) {
        preference.fragment = AccountRefreshSettingsFragment::class.java.name
        val args = preference.extras
        args.putParcelable(EXTRA_ACCOUNT, account)
    }

    override fun getSwitchDefault(): Boolean {
        return preferenceManager.sharedPreferences.getBoolean(KEY_DEFAULT_AUTO_REFRESH, false)
    }
}

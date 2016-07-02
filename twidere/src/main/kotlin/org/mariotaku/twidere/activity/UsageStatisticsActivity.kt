/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.os.Bundle
import android.support.v4.app.Fragment
import org.mariotaku.twidere.Constants.KEY_USAGE_STATISTICS
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_RESID
import org.mariotaku.twidere.fragment.SettingsDetailsFragment

class UsageStatisticsActivity : BaseActivity() {

    override fun onDestroy() {
        if (!preferences.contains(KEY_USAGE_STATISTICS)) {
            val editor = preferences.edit()
            editor.putBoolean(KEY_USAGE_STATISTICS, preferences.getBoolean(KEY_USAGE_STATISTICS))
            editor.apply()
        }
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragmentArgs = Bundle()
        fragmentArgs.putInt(EXTRA_RESID, R.xml.preferences_usage_statistics)
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(android.R.id.content, Fragment.instantiate(this,
                SettingsDetailsFragment::class.java.name, fragmentArgs))
        ft.commit()
    }

}

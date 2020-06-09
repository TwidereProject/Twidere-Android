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
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.activity.SettingsActivity
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_SHOULD_TERMINATE
import org.mariotaku.twidere.util.Utils

class SettingsDetailsFragment : BasePreferenceFragment(), OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SHARED_PREFERENCES_NAME
        val defaultScreen = preferenceScreen
        val preferenceScreen: PreferenceScreen
        preferenceScreen = if (defaultScreen != null) {
            defaultScreen.removeAll()
            defaultScreen
        } else {
            preferenceManager.createPreferenceScreen(activity)
        }
        setPreferenceScreen(preferenceScreen)

        val args = arguments
        val rawResId = args?.get(EXTRA_RESID)
        val resId: Int
        resId = when (rawResId) {
            is Int -> {
                rawResId
            }
            is String -> {
                Utils.getResId(activity, rawResId)
            }
            else -> {
                0
            }
        }
        if (resId != 0) {
            addPreferencesFromResource(resId)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.fitsSystemWindows = true
        ViewCompat.requestApplyInsets(listView)
    }

    override fun onStart() {
        super.onStart()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        val preference = findPreference<Preference>(key) ?: return
        val currentActivity = activity ?: return
        val extras = preference.extras
        if (extras != null) {
            when {
                extras.containsKey(EXTRA_SHOULD_RESTART) -> {
                    SettingsActivity.setShouldRestart(currentActivity)
                }
                extras.containsKey(EXTRA_SHOULD_RECREATE) -> {
                    SettingsActivity.setShouldRecreate(currentActivity)
                }
                extras.containsKey(EXTRA_SHOULD_TERMINATE) -> {
                    SettingsActivity.setShouldTerminate(currentActivity)
                }
            }
            if (extras.containsKey(EXTRA_RECREATE_ACTIVITY)) {
                currentActivity.recreate()
            }
        }
    }


}

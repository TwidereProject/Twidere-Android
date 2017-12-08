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
import android.support.v4.view.ViewCompat
import android.support.v7.preference.PreferenceScreen
import android.support.v7.widget.RecyclerView
import android.view.View
import org.mariotaku.ktextension.restart
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.activity.SettingsActivity
import org.mariotaku.twidere.extension.preference.setOnPreferenceChangeListenerRecursive
import org.mariotaku.twidere.util.Utils

class SettingsDetailsFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SHARED_PREFERENCES_NAME
        val defaultScreen = preferenceScreen
        val preferenceScreen: PreferenceScreen
        if (defaultScreen != null) {
            defaultScreen.removeAll()
            preferenceScreen = defaultScreen
        } else {
            preferenceScreen = preferenceManager.createPreferenceScreen(activity)
        }
        setPreferenceScreen(preferenceScreen)

        val args = arguments!!
        val rawResId = args.get(EXTRA_RESID)
        val resId: Int
        if (rawResId is Int) {
            resId = rawResId
        } else if (rawResId is String) {
            resId = Utils.getResId(activity, rawResId)
        } else {
            resId = 0
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

    override fun onCreateAdapter(screen: PreferenceScreen): RecyclerView.Adapter<*> {
        screen.setOnPreferenceChangeListenerRecursive { preference, _ ->
            val activity = activity ?: return@setOnPreferenceChangeListenerRecursive false
            val extras = preference.extras ?: return@setOnPreferenceChangeListenerRecursive false
            when {
                extras.containsKey(EXTRA_SHOULD_RESTART) -> SettingsActivity.setShouldRestart(activity)
                extras.containsKey(EXTRA_SHOULD_RECREATE) -> SettingsActivity.setShouldRecreate(activity)
                extras.containsKey(EXTRA_SHOULD_TERMINATE) -> SettingsActivity.setShouldTerminate(activity)
            }
            when {
                extras.containsKey(EXTRA_RECREATE_ACTIVITY) -> activity.recreate()
                extras.containsKey(EXTRA_RESTART_ACTIVITY) -> activity.restart()
            }
            return@setOnPreferenceChangeListenerRecursive true
        }
        return super.onCreateAdapter(screen)
    }

}

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

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.PreferenceActivity.EXTRA_SHOW_FRAGMENT
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.widget.CompoundButton
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.ACCOUNT_PREFERENCES_NAME_PREFIX
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NAME_FIRST
import org.mariotaku.twidere.model.AccountDetails

abstract class BaseAccountPreferenceFragment : BasePreferenceFragment() {

    protected val account: AccountDetails?
        get() {
            return arguments?.getParcelable(EXTRA_ACCOUNT)
        }

    protected abstract val preferencesResource: Int

    protected abstract val switchPreferenceDefault: Boolean

    protected abstract val switchPreferenceKey: String?

    private val preferenceChangeListener = OnSharedPreferenceChangeListener { _, key ->
        if (key == switchPreferenceKey) {
            updatePreferenceScreen()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val pm = preferenceManager
        val account: AccountDetails = arguments?.getParcelable(EXTRA_ACCOUNT) ?: return
        val preferenceName = "$ACCOUNT_PREFERENCES_NAME_PREFIX${account.key}"
        pm.sharedPreferencesName = preferenceName
        addPreferencesFromResource(preferencesResource)
        val prefs = pm.sharedPreferences
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        val activity = activity
        val intent = activity?.intent
        if (intent != null && intent.hasExtra(EXTRA_SHOW_FRAGMENT)) {
            val nameFirst = prefs.getBoolean(KEY_NAME_FIRST, true)
            val name = userColorNameManager.getDisplayName(account.key,
                    account.user.name, account.user.screen_name, nameFirst)
            activity.title = name
        }
        updatePreferenceScreen()
    }

    override fun onDestroy() {
        val pm = preferenceManager
        val prefs = pm.sharedPreferences
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onDestroy()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val switchKey = switchPreferenceKey
        if (!TextUtils.isEmpty(switchKey)) {
            inflater.inflate(R.menu.menu_switch_preference, menu)
            val actionView = menu.findItem(R.id.toggle).actionView
            val toggle = actionView.findViewById<CompoundButton>(android.R.id.toggle)
            val prefs = preferenceManager.sharedPreferences
            toggle.setOnCheckedChangeListener { _, isChecked ->
                val editor = prefs.edit()
                if (prefs.getBoolean(switchPreferenceKey, switchPreferenceDefault) != isChecked) {
                    editor.putBoolean(switchPreferenceKey, isChecked)
                    editor.apply()
                    onSwitchPreferenceChanged(isChecked)
                    updatePreferenceScreen()
                }
            }
            toggle.isChecked = prefs.getBoolean(switchKey, switchPreferenceDefault)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    protected open fun onSwitchPreferenceChanged(isChecked: Boolean) {

    }


    private fun updatePreferenceScreen() {
        val screen = preferenceScreen
        val sharedPreferences = preferenceManager.sharedPreferences
        if (screen == null || sharedPreferences == null) return
        screen.isEnabled = sharedPreferences.getBoolean(switchPreferenceKey, switchPreferenceDefault)
    }
}

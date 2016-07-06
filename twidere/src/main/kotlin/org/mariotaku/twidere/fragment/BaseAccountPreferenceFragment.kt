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
import android.preference.PreferenceActivity.EXTRA_SHOW_FRAGMENT
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.ACCOUNT_PREFERENCES_NAME_PREFIX
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NAME_FIRST
import org.mariotaku.twidere.model.ParcelableAccount

abstract class BaseAccountPreferenceFragment : BasePreferenceFragment(), OnCheckedChangeListener, OnSharedPreferenceChangeListener {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val pm = preferenceManager
        val account = arguments.getParcelable<ParcelableAccount>(IntentConstants.EXTRA_ACCOUNT) ?: return
        val preferenceName = "$ACCOUNT_PREFERENCES_NAME_PREFIX${account.account_key}"
        pm.sharedPreferencesName = preferenceName
        addPreferencesFromResource(preferencesResource)
        val prefs = pm.sharedPreferences
        prefs.registerOnSharedPreferenceChangeListener(this)
        val activity = activity
        val intent = activity.intent
        if (intent.hasExtra(EXTRA_SHOW_FRAGMENT)) {
            val nameFirst = prefs.getBoolean(KEY_NAME_FIRST, true)
            val name = userColorNameManager.getDisplayName(account.account_key,
                    account.name, account.screen_name, nameFirst)
            activity.title = name
        }
        updatePreferenceScreen()
    }

    override fun onDestroy() {
        val pm = preferenceManager
        val prefs = pm.sharedPreferences
        prefs.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        val prefs = preferenceManager.sharedPreferences
        val editor = prefs.edit()
        if (prefs.getBoolean(switchPreferenceKey, switchPreferenceDefault) != isChecked) {
            editor.putBoolean(switchPreferenceKey, isChecked)
            editor.apply()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        val switchKey = switchPreferenceKey
        if (!TextUtils.isEmpty(switchKey)) {
            inflater!!.inflate(R.menu.menu_switch_preference, menu)
            val actionView = menu!!.findItem(R.id.toggle).actionView
            val toggle = actionView.findViewById(android.R.id.toggle) as CompoundButton
            val prefs = preferenceManager.sharedPreferences
            toggle.setOnCheckedChangeListener(this)
            toggle.isChecked = prefs.getBoolean(switchKey, switchPreferenceDefault)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        if (key == switchPreferenceKey) {
            updatePreferenceScreen()
        }
    }

    protected val account: ParcelableAccount?
        get() {
            val args = arguments ?: return null
            return args.getParcelable<ParcelableAccount>(IntentConstants.EXTRA_ACCOUNT)
        }

    protected abstract val preferencesResource: Int

    protected abstract val switchPreferenceDefault: Boolean

    protected abstract val switchPreferenceKey: String?

    private fun updatePreferenceScreen() {
        val screen = preferenceScreen
        val sharedPreferences = preferenceManager.sharedPreferences
        if (screen == null || sharedPreferences == null) return
        screen.isEnabled = sharedPreferences.getBoolean(switchPreferenceKey, switchPreferenceDefault)
    }
}

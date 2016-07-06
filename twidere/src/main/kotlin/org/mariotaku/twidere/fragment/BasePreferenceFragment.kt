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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat

import org.mariotaku.twidere.preference.RingtonePreference
import org.mariotaku.twidere.util.KeyboardShortcutsHandler
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper

import javax.inject.Inject

abstract class BasePreferenceFragment : PreferenceFragmentCompat() {
    private var ringtonePreferenceKey: String? = null

    @Inject
    lateinit var keyboardShortcutHandler: KeyboardShortcutsHandler
    @Inject
    lateinit var userColorNameManager: UserColorNameManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        GeneralComponentHelper.build(context).inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            ringtonePreferenceKey = savedInstanceState.getString(EXTRA_RINGTONE_PREFERENCE_KEY)
        }
        super.onActivityCreated(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState!!.putString(EXTRA_RINGTONE_PREFERENCE_KEY, ringtonePreferenceKey)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PICK_RINGTONE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val ringtone = data.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                    if (ringtonePreferenceKey != null) {
                        val ringtonePreference = findPreference(ringtonePreferenceKey) as RingtonePreference?
                        if (ringtonePreference != null) {
                            ringtonePreference.value = ringtone?.toString()
                        }
                    }
                    ringtonePreferenceKey = null
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (preference is RingtonePreference) {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, preference.ringtoneType)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, preference.isShowDefault)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, preference.isShowSilent)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI)

            val existingValue = preference.value // TODO
            if (existingValue != null) {
                if (existingValue.length == 0) {
                    // Select "Silent"
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, null as Uri)
                } else {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue))
                }
            } else {
                // No ringtone has been selected, set to the default
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI)
            }
            startActivityForResult(intent, REQUEST_PICK_RINGTONE)
            ringtonePreferenceKey = preference.key
            return true
        }
        return super.onPreferenceTreeClick(preference)
    }

    companion object {

        private val REQUEST_PICK_RINGTONE = 301
        private val EXTRA_RINGTONE_PREFERENCE_KEY = "internal:ringtone_preference_key"
    }

}

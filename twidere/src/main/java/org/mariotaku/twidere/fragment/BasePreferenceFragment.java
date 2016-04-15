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

package org.mariotaku.twidere.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.preference.RingtonePreference;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import javax.inject.Inject;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat implements Constants {

    private static final int REQUEST_PICK_RINGTONE = 301;
    private static final String EXTRA_RINGTONE_PREFERENCE_KEY = "internal:ringtone_preference_key";
    private String mRingtonePreferenceKey;

    @Inject
    protected KeyboardShortcutsHandler mKeyboardShortcutHandler;
    @Inject
    protected UserColorNameManager mUserColorNameManager;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        GeneralComponentHelper.build(context).inject(this);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mRingtonePreferenceKey = savedInstanceState.getString(EXTRA_RINGTONE_PREFERENCE_KEY);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_RINGTONE_PREFERENCE_KEY, mRingtonePreferenceKey);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_RINGTONE: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (mRingtonePreferenceKey != null) {
                        RingtonePreference ringtonePreference = (RingtonePreference)
                                findPreference(mRingtonePreferenceKey);
                        if (ringtonePreference != null) {
                            ringtonePreference.setValue(ringtone != null ? ringtone.toString() : null);
                        }
                    }
                    mRingtonePreferenceKey = null;
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RingtonePreference) {
            RingtonePreference ringtonePreference = (RingtonePreference) preference;
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ringtonePreference.getRingtoneType());
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, ringtonePreference.isShowDefault());
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, ringtonePreference.isShowSilent());
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

            String existingValue = ringtonePreference.getValue(); // TODO
            if (existingValue != null) {
                if (existingValue.length() == 0) {
                    // Select "Silent"
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                } else {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
                }
            } else {
                // No ringtone has been selected, set to the default
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
            }
            startActivityForResult(intent, REQUEST_PICK_RINGTONE);
            mRingtonePreferenceKey = ringtonePreference.getKey();
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

}

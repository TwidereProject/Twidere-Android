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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.SettingsActivity;
import org.mariotaku.twidere.util.Utils;

public class SettingsDetailsFragment extends PreferenceFragmentCompat implements Constants,
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(SHARED_PREFERENCES_NAME);
        final PreferenceScreen defaultScreen = getPreferenceScreen();
        final PreferenceScreen preferenceScreen;
        if (defaultScreen != null) {
            defaultScreen.removeAll();
            preferenceScreen = defaultScreen;
        } else {
            preferenceScreen = preferenceManager.createPreferenceScreen(getActivity());
        }
        setPreferenceScreen(preferenceScreen);

        final Bundle args = getArguments();
        final Object rawResId = args.get(EXTRA_RESID);
        final int resId;
        if (rawResId instanceof Integer) {
            resId = (Integer) rawResId;
        } else if (rawResId instanceof String) {
            resId = Utils.getResId(getActivity(), (String) rawResId);
        } else {
            resId = 0;
        }
        if (resId != 0) {
            addPreferencesFromResource(resId);
        }

//        final Context context = preferenceScreen.getContext();
//        if (args.containsKey(EXTRA_SETTINGS_INTENT_ACTION)) {
//            final Intent hiddenEntryIntent = new Intent(args.getString(EXTRA_SETTINGS_INTENT_ACTION));
//            final PackageManager pm = context.getPackageManager();
//            for (ResolveInfo info : pm.queryIntentActivities(hiddenEntryIntent, PackageManager.MATCH_DEFAULT_ONLY)) {
//                final Preference preference = new Preference(context);
//                final Intent intent = new Intent(hiddenEntryIntent);
//                intent.setPackage(info.resolvePackageName);
//                intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
//                preference.setIntent(intent);
//                preference.setTitle(info.loadLabel(pm));
//                preferenceScreen.addPreference(preference);
//            }
//        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        final SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        final SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final Preference preference = findPreference(key);
        if (preference == null) return;
        final Bundle extras = preference.getExtras();
        if (extras != null) {
            final Activity activity = getActivity();
            if (extras.containsKey(EXTRA_NOTIFY_CHANGE)) {
                SettingsActivity.setShouldNotifyChange(activity);
            }
            if (extras.containsKey(EXTRA_RESTART_ACTIVITY)) {
                Utils.restartActivity(getActivity());
            } else if (extras.containsKey(EXTRA_RECREATE_ACTIVITY)) {
                activity.recreate();
            }
        }
    }
}

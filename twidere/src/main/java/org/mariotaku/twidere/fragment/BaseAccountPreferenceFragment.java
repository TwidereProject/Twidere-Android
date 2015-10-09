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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableAccount;

public abstract class BaseAccountPreferenceFragment extends BasePreferenceFragment implements Constants,
        OnCheckedChangeListener, OnSharedPreferenceChangeListener {

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        final PreferenceManager pm = getPreferenceManager();
        final ParcelableAccount account = getArguments().getParcelable(EXTRA_ACCOUNT);
        final String preferenceName = ACCOUNT_PREFERENCES_NAME_PREFIX
                + (account != null ? account.account_id : "unknown");
        pm.setSharedPreferencesName(preferenceName);
        addPreferencesFromResource(getPreferencesResource());
        final SharedPreferences prefs = pm.getSharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(this);
        final Activity activity = getActivity();
        final Intent intent = activity.getIntent();
        if (account != null && intent.hasExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT)) {
            final boolean nameFirst = prefs.getBoolean(KEY_NAME_FIRST, true);
            final String name = mUserColorNameManager.getDisplayName(account.account_id, account.name,
                    account.screen_name, nameFirst, false);
            activity.setTitle(name);
        }
        updatePreferenceScreen();
    }

    @Override
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
        final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        if (prefs.getBoolean(getSwitchPreferenceKey(), getSwitchPreferenceDefault()) != isChecked) {
            editor.putBoolean(getSwitchPreferenceKey(), isChecked);
            editor.apply();
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        final String switchKey = getSwitchPreferenceKey();
        if (!TextUtils.isEmpty(switchKey)) {
            inflater.inflate(R.menu.menu_switch_preference, menu);
            final View actionView = menu.findItem(R.id.toggle).getActionView();
            final CompoundButton toggle = (CompoundButton) actionView.findViewById(android.R.id.toggle);
            final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
            toggle.setOnCheckedChangeListener(this);
            toggle.setChecked(prefs.getBoolean(switchKey, getSwitchPreferenceDefault()));
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (key.equals(getSwitchPreferenceKey())) {
            updatePreferenceScreen();
        }
    }

    protected ParcelableAccount getAccount() {
        final Bundle args = getArguments();
        if (args == null) return null;
        return args.getParcelable(EXTRA_ACCOUNT);
    }

    protected abstract int getPreferencesResource();

    protected abstract boolean getSwitchPreferenceDefault();

    @Nullable
    protected abstract String getSwitchPreferenceKey();

    private void updatePreferenceScreen() {
        final PreferenceScreen screen = getPreferenceScreen();
        final SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        if (screen == null || sharedPreferences == null) return;
        screen.setEnabled(sharedPreferences.getBoolean(getSwitchPreferenceKey(), getSwitchPreferenceDefault()));
    }
}

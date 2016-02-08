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

package org.mariotaku.twidere.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.SettingsDetailsFragment;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.dagger.DependencyHolder;

public class UsageStatisticsActivity extends Activity implements Constants {

    private static final int REQUEST_USAGE_STATISTICS = 201;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_USAGE_STATISTICS: {
                DependencyHolder holder = DependencyHolder.get(this);
                final SharedPreferencesWrapper prefs = holder.getPreferences();
                if (!prefs.contains(KEY_USAGE_STATISTICS)) {
                    final SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(KEY_USAGE_STATISTICS, prefs.getBoolean(KEY_USAGE_STATISTICS));
                    editor.apply();
                }
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVisible(true);
        final Bundle fragmentArgs = new Bundle();
        fragmentArgs.putInt(EXTRA_RESID, R.xml.preferences_usage_statistics);
        final Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsDetailsFragment.class.getName());
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS, fragmentArgs);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE, R.string.usage_statistics);
        startActivityForResult(intent, REQUEST_USAGE_STATISTICS);
    }

}

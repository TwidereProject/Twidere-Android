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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.SettingsDetailsFragment;

public class UsageStatisticsActivity extends BaseActivity {

    @Override
    protected void onDestroy() {
        if (!mPreferences.contains(KEY_USAGE_STATISTICS)) {
            final SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean(KEY_USAGE_STATISTICS, mPreferences.getBoolean(KEY_USAGE_STATISTICS));
            editor.apply();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle fragmentArgs = new Bundle();
        fragmentArgs.putInt(EXTRA_RESID, R.xml.preferences_usage_statistics);
        final FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(android.R.id.content, Fragment.instantiate(this,
                SettingsDetailsFragment.class.getName(), fragmentArgs));
        ft.commit();
    }

}

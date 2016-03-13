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
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import javax.inject.Inject;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat implements Constants {

    @Inject
    protected KeyboardShortcutsHandler mKeyboardShortcutHandler;
    @Inject
    protected UserColorNameManager mUserColorNameManager;

    @SuppressWarnings("deprecated")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        GeneralComponentHelper.build(activity).inject(this);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
    }

}

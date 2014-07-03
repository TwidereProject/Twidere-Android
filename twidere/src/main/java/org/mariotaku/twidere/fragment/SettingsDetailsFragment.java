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

import android.os.Bundle;
import android.preference.PreferenceScreen;

import org.mariotaku.twidere.util.Utils;

public class SettingsDetailsFragment extends BasePreferenceFragment {

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final PreferenceScreen screen = getPreferenceScreen();
		if (screen != null) {
			screen.removeAll();
		}
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
	}

}

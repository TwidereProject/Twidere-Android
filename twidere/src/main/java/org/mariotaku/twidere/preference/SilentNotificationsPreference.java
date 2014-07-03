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

package org.mariotaku.twidere.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import org.mariotaku.twidere.Constants;

public final class SilentNotificationsPreference extends MultiSelectListPreference implements Constants {

	public SilentNotificationsPreference(final Context context) {
		super(context);
	}

	public SilentNotificationsPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public SilentNotificationsPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected boolean[] getDefaults() {
		return new boolean[24];
	}

	@Override
	protected SharedPreferences getDefaultSharedPreferences() {
		return getContext().getSharedPreferences(SILENT_NOTIFICATIONS_PREFERENCE_NAME, Context.MODE_PRIVATE);
	}

	@Override
	protected String[] getKeys() {
		final String[] keys = new String[24];
		for (int i = 0; i < 24; i++) {
			keys[i] = "silent_notifications_at_" + i;
		}
		return keys;
	}

	@Override
	protected String[] getNames() {
		final String[] names = new String[24];
		for (int i = 0; i < 24; i++) {
			final String value_1 = i + ":00";
			final String value_2 = (i == 23 ? 0 : i + 1) + ":00";
			names[i] = value_1 + " - " + value_2;
		}
		return names;
	}

}

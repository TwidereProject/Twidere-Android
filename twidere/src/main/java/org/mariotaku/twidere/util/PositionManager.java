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

package org.mariotaku.twidere.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.mariotaku.twidere.Constants;

public class PositionManager implements Constants {

	private final SharedPreferences mPreferences;

	public PositionManager(final Context context) {
		mPreferences = context.getSharedPreferences(TIMELINE_POSITIONS_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}

	public long getPosition(final String key) {
		if (TextUtils.isEmpty(key)) return -1;
		return mPreferences.getLong(key, -1);
	}

	public boolean setPosition(final String key, final long status_id) {
		if (TextUtils.isEmpty(key)) return false;
		final SharedPreferences.Editor editor = mPreferences.edit();
		editor.putLong(key, status_id);
		editor.apply();
		return true;
	}

}

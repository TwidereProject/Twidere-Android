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

package org.mariotaku.twidere.util.accessor;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.BackStackEntry;

import java.lang.reflect.Field;

public class BackStackEntryAccessor {

	public static Fragment getFragmentInBackStackRecord(final FragmentManager.BackStackEntry entry) {
		try {
			final Field mHeadField = BackStackEntry.class.getField("mHead");
			final Object mHead = mHeadField.get(entry);
			final Field fragmentField = mHead.getClass().getField("fragment");
			final Object fragment = fragmentField.get(mHead);
			if (fragment instanceof Fragment) return (Fragment) fragment;
		} catch (final NoSuchFieldException e) {
		} catch (final IllegalArgumentException e) {
		} catch (final IllegalAccessException e) {
		}
		return null;
	}
}

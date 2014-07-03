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
import android.preference.Preference;
import android.util.AttributeSet;

import org.mariotaku.twidere.util.FlymeUtils;

public class LeftsideComposeButtonPreference extends AutoFixCheckBoxPreference {

	public LeftsideComposeButtonPreference(final Context context) {
		super(context);
	}

	public LeftsideComposeButtonPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public LeftsideComposeButtonPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onDependencyChanged(final Preference dependency, final boolean disableDependent) {
		super.onDependencyChanged(dependency, disableDependent || FlymeUtils.hasSmartBar());
	}

}

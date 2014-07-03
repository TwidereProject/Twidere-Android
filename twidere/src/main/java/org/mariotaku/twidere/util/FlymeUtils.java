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

import android.app.ActionBar;
import android.os.Build;

import java.lang.reflect.Method;

public final class FlymeUtils {

	private static String[] SMARTBAR_SUPPORTED_DEVICES = { "mx2", "mx3" };

	public static boolean hasSmartBar() {
		try {
			// Invoke Build.hasSmartBar()
			final Method method = Build.class.getMethod("hasSmartBar");
			return ((Boolean) method.invoke(null)).booleanValue();
		} catch (final Exception e) {
		}
		// Detect by Build.DEVICE
		if (isDeviceWithSmartBar(Build.DEVICE)) return true;
		return false;
	}

	public static boolean isDeviceWithSmartBar(final String buildDevice) {
		for (final String dev : SMARTBAR_SUPPORTED_DEVICES) {
			if (dev.equals(buildDevice)) return true;
		}
		return false;
	}

	public static boolean isFlyme() {
		try {
			// Invoke Build.hasSmartBar()
			final Method method = Build.class.getMethod("hasSmartBar");
			return method != null;
		} catch (final Exception e) {
			return false;
		}
	}

	public static void setActionModeHeaderHidden(final ActionBar actionbar, final boolean hidden) {
		try {
			final Method method = ActionBar.class.getMethod("setActionModeHeaderHidden", new Class[] { boolean.class });
			method.invoke(actionbar, hidden);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}

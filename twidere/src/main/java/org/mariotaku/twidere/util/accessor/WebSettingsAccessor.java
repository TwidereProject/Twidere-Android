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

import android.annotation.TargetApi;
import android.os.Build;
import android.webkit.WebSettings;

public class WebSettingsAccessor {

	public static void setAllowUniversalAccessFromFileURLs(final WebSettings settings, final boolean flag) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return;
		WebSettingsAccessorSDK16.setAllowUniversalAccessFromFileURLs(settings, flag);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private static class WebSettingsAccessorSDK16 {
		private static void setAllowUniversalAccessFromFileURLs(final WebSettings settings, final boolean flag) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return;
			settings.setAllowUniversalAccessFromFileURLs(flag);
		}
	}

}

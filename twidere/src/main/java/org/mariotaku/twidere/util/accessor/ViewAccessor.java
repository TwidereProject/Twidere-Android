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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.View;

public final class ViewAccessor {

	public static void enableHwAccelIfNecessary(final View view) {
		if (ViewCompat.getLayerType(view) != ViewCompat.LAYER_TYPE_HARDWARE) {
			ViewCompat.setLayerType(view, ViewCompat.LAYER_TYPE_HARDWARE, null);
		}
	}

	@SuppressWarnings("deprecation")
	public static void setBackground(final View view, final Drawable background) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			view.setBackgroundDrawable(background);
		} else {
			ViewAccessorJB.setBackground(view, background);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	static class ViewAccessorJB {
		static void setBackground(final View view, final Drawable background) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return;
			view.setBackground(background);
		}
	}
}

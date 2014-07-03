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

import android.support.v4.widget.ViewDragHelper;

import java.lang.reflect.Field;

public class ViewDragHelperAccessor {

	private ViewDragHelperAccessor() {
		throw new AssertionError();
	}

	public static boolean setEdgeSize(final ViewDragHelper helper, final int edgeSize) {
		try {
			final Field f = helper.getClass().getField("mEdgeSize");
			f.setAccessible(true);
			f.setInt(helper, edgeSize);
			return true;
		} catch (final Exception e) {
			return false;
		}
	}

}

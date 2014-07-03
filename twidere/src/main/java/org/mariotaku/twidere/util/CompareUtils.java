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

import android.os.Bundle;

import java.util.Iterator;

public class CompareUtils {

	public static boolean bundleEquals(final Bundle bundle1, final Bundle bundle2, final String... ignoredKeys) {
		if (bundle1 == null || bundle2 == null) return bundle1 == bundle2;
		final Iterator<String> keys = bundle1.keySet().iterator();
		while (keys.hasNext()) {
			final String key = keys.next();
			if (!ArrayUtils.contains(ignoredKeys, key) && !objectEquals(bundle1.get(key), bundle2.get(key)))
				return false;
		}
		return true;
	}

	public static boolean classEquals(final Class<?> cls1, final Class<?> cls2) {
		if (cls1 == null || cls2 == null) return cls1 == cls2;
		return cls1.getName().equals(cls2.getName());
	}

	public static boolean objectEquals(final Object object1, final Object object2) {
		if (object1 == null || object2 == null) return object1 == object2;
		if (object1 instanceof Bundle && object2 instanceof Bundle)
			return bundleEquals((Bundle) object1, (Bundle) object2);
		else if (object1 instanceof Class && object2 instanceof Class)
			return classEquals((Class<?>) object1, (Class<?>) object2);
		return object1.equals(object2);
	}

}

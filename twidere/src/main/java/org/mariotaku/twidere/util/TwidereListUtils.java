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

import java.util.ArrayList;
import java.util.List;

public class TwidereListUtils {

    private TwidereListUtils() {
    }

    public static List<Long> fromArray(final long[] array) {
        if (array == null) return null;
        final List<Long> list = new ArrayList<>();
        for (final long item : array) {
            list.add(item);
        }
        return list;
    }

    public static <T> String toString(final List<T> list, final char delimiter, final boolean includeSpace) {
        final StringBuilder builder = new StringBuilder();
        final int size = list.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                builder.append(delimiter);
                if (includeSpace) {
                    builder.append(" ");
                }
            }
            builder.append(list.get(i));
        }
        return builder.toString();
    }

}

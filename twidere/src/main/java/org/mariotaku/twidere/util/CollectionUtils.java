/*
 * Twidere - Twitter client for Android
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

import android.support.annotation.CheckResult;

import java.util.Collection;

/**
 * Created by mariotaku on 14/12/7.
 */
public class CollectionUtils {

    private CollectionUtils() {
    }

    @CheckResult
    public static <T> String toString(final Collection<T> collection, final char token, final boolean includeSpace) {
        final StringBuilder builder = new StringBuilder();
        int i = 0;
        for (T item : collection) {
            final String itemString = String.valueOf(item);
            if (i > 0) {
                builder.append(includeSpace ? token + " " : token);
            }
            builder.append(itemString);
            i++;
        }
        return builder.toString();
    }
}

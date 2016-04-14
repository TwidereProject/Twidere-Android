/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import java.util.regex.Matcher;

/**
 * Created by mariotaku on 15/1/11.
 */
public class RegexUtils {
    private RegexUtils() {
    }

    public static int matcherEnd(final Matcher matcher, final int group) {
        try {
            return matcher.end(group);
        } catch (final IllegalStateException e) {
            // Ignore.
        }
        return -1;
    }

    public static String matcherGroup(final Matcher matcher, final int group) {
        try {
            return matcher.group(group);
        } catch (final IllegalStateException e) {
            // Ignore.
        }
        return null;
    }

    public static int matcherStart(final Matcher matcher, final int group) {
        try {
            return matcher.start(group);
        } catch (final IllegalStateException e) {
            // Ignore.
        }
        return -1;
    }
}

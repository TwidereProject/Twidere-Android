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

import androidx.annotation.NonNull;
import android.text.Spannable;
import android.text.Spanned;

import org.mariotaku.twidere.text.ZeroWidthSpan;

/**
 * Created by mariotaku on 14/12/23.
 */
public class TwidereStringUtils {
    private TwidereStringUtils() {
    }

    public static boolean regionMatchesIgnoreCase(@NonNull final String string, final int thisStart,
                                                  @NonNull final String match, final int start,
                                                  final int length) {
        return string.substring(thisStart, thisStart + length).equalsIgnoreCase(match.substring(start, start + length));
    }


    public static boolean startsWithIgnoreCase(@NonNull String string, @NonNull String prefix) {
        return startsWithIgnoreCase(string, prefix, 0);
    }

    public static boolean startsWithIgnoreCase(@NonNull String string, @NonNull String prefix,
                                               int start) {
        if (prefix.length() > string.length()) return false;
        return regionMatchesIgnoreCase(string, start, prefix, 0, prefix.length());
    }

    /**
     * Fix to https://github.com/TwidereProject/Twidere-Android/issues/449
     */
    public static void fixSHY(Spannable string) {
        for (int i = 0, j = string.length(); i < j; i++) {
            if (string.charAt(i) == '\u00ad') {
                string.setSpan(new ZeroWidthSpan(), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

}

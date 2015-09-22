/*
 *                 Twidere - Twitter client for Android
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

import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by mariotaku on 15/9/22.
 */
public class RegexMatcher {
    public static final int NO_MATCH = -1;
    private final ArrayList<Pair<Pattern, Integer>> patternsList;
    private final int defaultCode;

    public RegexMatcher(int code) {
        defaultCode = code;
        patternsList = new ArrayList<>();
    }

    public void addPattern(String pattern, int code) {
        patternsList.add(Pair.create(Pattern.compile(pattern), code));
    }

    public int match(String path) {
        for (Pair<Pattern, Integer> item : patternsList) {
            if (item.first.matcher(path).matches()) return item.second;
        }
        return defaultCode;
    }
}

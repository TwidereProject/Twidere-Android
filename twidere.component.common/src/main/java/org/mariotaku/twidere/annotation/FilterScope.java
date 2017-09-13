/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef(value = {FilterScope.HOME, FilterScope.INTERACTIONS, FilterScope.MESSAGES,
        FilterScope.SEARCH_RESULT, FilterScope.LIST_GROUP_TIMELINE, FilterScope.FAVORITES,
        FilterScope.ALL, FilterScope.FLAG_MATCH_NAME}, flag = true)
@Retention(RetentionPolicy.SOURCE)
public @interface FilterScope {
    int HOME = 0x1;
    int INTERACTIONS = 0x2;
    int MESSAGES = 0x4;
    int SEARCH_RESULT = 0x8;
    int LIST_GROUP_TIMELINE = 0x10;
    int FAVORITES = 0x20;

    int FLAG_MATCH_NAME = 0x80000000;
    int FLAG_MATCH_TEXT = 0x40000000;

    int MASK_FLAG = 0xFF000000;
    int MASK_SCOPE = 0x00FFFFFF;

    // Contains all flags
    int ALL = 0xFFFFFFFF;
}

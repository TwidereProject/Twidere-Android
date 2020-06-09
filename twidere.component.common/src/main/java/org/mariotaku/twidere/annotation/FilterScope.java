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

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef(value = {FilterScope.HOME, FilterScope.INTERACTIONS, FilterScope.MESSAGES,
        FilterScope.SEARCH_RESULTS, FilterScope.LIST_GROUP_TIMELINE, FilterScope.FAVORITES,
        FilterScope.USER_TIMELINE, FilterScope.PUBLIC_TIMELINE, FilterScope.UGC_TIMELINE,
        FilterScope.TARGET_NAME, FilterScope.TARGET_TEXT, FilterScope.TARGET_DESCRIPTION,
        FilterScope.ALL, FilterScope.DEFAULT}, flag = true)
@Retention(RetentionPolicy.SOURCE)
public @interface FilterScope {
    int HOME = 0x1;
    int INTERACTIONS = 0x2;
    int MESSAGES = 0x4;
    int SEARCH_RESULTS = 0x8;
    int LIST_GROUP_TIMELINE = 0x10;
    int FAVORITES = 0x20;
    int USER_TIMELINE = 0x40;
    int PUBLIC_TIMELINE = 0x80;

    int UGC_TIMELINE = LIST_GROUP_TIMELINE | FAVORITES | USER_TIMELINE | PUBLIC_TIMELINE;

    int TARGET_NAME = 0x80000000;
    int TARGET_TEXT = 0x40000000;
    int TARGET_DESCRIPTION = 0x20000000;

    int MASK_TARGET = 0xFF000000;
    int MASK_SCOPE = 0x00FFFFFF;

    int VALID_MASKS_USERS = MASK_SCOPE;
    int VALID_MASKS_KEYWORDS = MASK_SCOPE | MASK_TARGET;
    int VALID_MASKS_SOURCES = MASK_SCOPE & ~MESSAGES;
    int VALID_MASKS_LINKS = MASK_SCOPE;

    // Contains all flags
    int ALL = 0xFFFFFFFF;
    int DEFAULT = ALL & ~(TARGET_NAME | TARGET_DESCRIPTION);
}

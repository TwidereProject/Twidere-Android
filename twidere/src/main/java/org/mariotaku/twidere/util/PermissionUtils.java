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

import android.content.pm.PackageManager;

import kotlin.collections.ArraysKt;

/**
 * Created by mariotaku on 15/10/8.
 */
public class PermissionUtils {
    private PermissionUtils() {
    }

    public static int getPermission(String[] permissions, int[] grantResults, String permission) {
        final int idx = ArraysKt.indexOf(permissions, permission);
        if (idx >= 0) return grantResults[idx];
        return 0;
    }

    public static boolean hasPermission(String[] permissions, int[] grantResults, String permission) {
        return getPermission(permissions, grantResults, permission) == PackageManager.PERMISSION_GRANTED;
    }
}

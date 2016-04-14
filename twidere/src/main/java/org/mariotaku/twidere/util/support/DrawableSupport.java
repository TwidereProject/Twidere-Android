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

package org.mariotaku.twidere.util.support;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;

import org.mariotaku.twidere.util.support.graphics.OutlineCompat;

/**
 * Created by mariotaku on 15/4/27.
 */
public class DrawableSupport {

    private DrawableSupport() {
    }

    public static void getOutline(Drawable drawable, OutlineCompat outlineCompat) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        DrawableSupportLollipop.getOutline(drawable, outlineCompat);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class DrawableSupportLollipop {

        public static void getOutline(Drawable drawable, OutlineCompat outlineCompat) {
            drawable.getOutline(OutlineCompat.OutlineL.getWrapped(outlineCompat));
        }
    }

}

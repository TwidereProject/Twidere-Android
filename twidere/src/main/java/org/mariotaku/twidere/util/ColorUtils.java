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

import android.graphics.Color;

/**
 * Created by mariotaku on 15/1/8.
 */
public class ColorUtils {


    public static void colorToYIQ(int color, int[] yiq) {
        final int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        yiq[0] = (r * 299 + g * 587 + b * 114) / 1000;
        yiq[1] = (r * 596 - g * 275 - b * 321) / 1000;
        yiq[2] = (r * 212 - g * 523 + b * 311) / 1000;
    }


    public static int YIQToColor(int alpha, int[] yiq) {
        final int r = MathUtils.clamp((yiq[0] * 1000 + yiq[1] * 956 + yiq[2] * 620) / 1000, 0, 255);
        final int g = MathUtils.clamp((yiq[0] * 1000 - yiq[1] * 272 - yiq[2] * 647) / 1000, 0, 255);
        final int b = MathUtils.clamp((yiq[0] * 1000 - yiq[1] * 1108 + yiq[2] * 1705) / 1000, 0, 255);
        return Color.argb(alpha, r, g, b);
    }

    public static int getContrastYIQ(int color, int colorDark, int colorLight) {
        return getContrastYIQ(color, 128, colorDark, colorLight);
    }

    public static int getContrastYIQ(int color) {
        return getContrastYIQ(color, 128);
    }

    public static int getContrastYIQ(int color, int threshold) {
        return getContrastYIQ(color, threshold, Color.BLACK, Color.WHITE);
    }

    /**
     * Get most contrasting color
     *
     * @param color Input color, alpha channel will be disposed.
     * @return {@link android.graphics.Color#WHITE} or {@link android.graphics.Color#BLACK}
     * @see <a href='http://24ways.org/2010/calculating-color-contrast/'>Calculating Color Contrast</a>
     */
    public static int getContrastYIQ(int color, int threshold, int colorDark, int colorLight) {
        final int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        final int yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;
        return (yiq >= threshold) ? colorDark : colorLight;
    }
}

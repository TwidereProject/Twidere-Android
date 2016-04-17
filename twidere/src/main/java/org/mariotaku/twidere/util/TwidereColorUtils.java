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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by mariotaku on 15/1/8.
 */
public class TwidereColorUtils {

    private TwidereColorUtils() {
    }

    public static Bitmap getColorPreviewBitmap(final Context context, final int color, final boolean border) {
        if (context == null) return null;
        final float density = context.getResources().getDisplayMetrics().density;
        final int width = (int) (32 * density), height = (int) (32 * density);

        final Bitmap bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        final Canvas canvas = new Canvas(bm);

        final int rectangleSize = (int) (density * 5);
        final int numRectanglesHorizontal = (int) Math.ceil(width / rectangleSize);
        final int numRectanglesVertical = (int) Math.ceil(height / rectangleSize);
        final Rect r = new Rect();
        boolean verticalStartWhite = true;
        for (int i = 0; i <= numRectanglesVertical; i++) {

            boolean isWhite = verticalStartWhite;
            for (int j = 0; j <= numRectanglesHorizontal; j++) {

                r.top = i * rectangleSize;
                r.left = j * rectangleSize;
                r.bottom = r.top + rectangleSize;
                r.right = r.left + rectangleSize;
                final Paint paint = new Paint();
                paint.setColor(isWhite ? Color.WHITE : Color.GRAY);

                canvas.drawRect(r, paint);

                isWhite = !isWhite;
            }

            verticalStartWhite = !verticalStartWhite;

        }
        canvas.drawColor(color);
        if (border) {
            final Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(1f * density);
            final float[] points = new float[]{0, 0, width, 0, 0, 0, 0, height, width, 0, width, height, 0, height,
                    width, height};
            canvas.drawLines(points, paint);
        }
        return bm;
    }

    public static int getYIQLuminance(int color) {
        final int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        return (r * 299 + g * 587 + b * 114) / 1000;
    }

    public static void colorToYIQ(int color, int[] yiq) {
        final int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        yiq[0] = (r * 299 + g * 587 + b * 114) / 1000;
        yiq[1] = (r * 596 - g * 275 - b * 321) / 1000;
        yiq[2] = (r * 212 - g * 523 + b * 311) / 1000;
    }


    public static int YIQToColor(int alpha, int[] yiq) {
        final int r = TwidereMathUtils.clamp((yiq[0] * 1000 + yiq[1] * 956 + yiq[2] * 620) / 1000, 0, 255);
        final int g = TwidereMathUtils.clamp((yiq[0] * 1000 - yiq[1] * 272 - yiq[2] * 647) / 1000, 0, 255);
        final int b = TwidereMathUtils.clamp((yiq[0] * 1000 - yiq[1] * 1108 + yiq[2] * 1705) / 1000, 0, 255);
        return Color.argb(alpha, r, g, b);
    }

    public static int getContrastYIQ(int color, int colorDark, int colorLight) {
        return getContrastYIQ(color, 128, colorDark, colorLight);
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

    public static int getYIQContrast(int color1, int color2) {
        return getYIQLuminance(color1) - getYIQLuminance(color2);
    }
}

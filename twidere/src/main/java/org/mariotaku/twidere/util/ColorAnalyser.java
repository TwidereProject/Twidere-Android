/*
 * 				Twidere - Twitter client for Android
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

import static android.graphics.Color.WHITE;
import static android.graphics.Color.argb;
import static android.graphics.Color.blue;
import static android.graphics.Color.colorToHSV;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Collections;
import java.util.HashMap;

/**
 * @author mariotaku Get the main color from a {@link Bitmap}<br>
 * <br>
 *         <b>Important</b>: I recommand using this method in different thread.
 *         Or it may makes your application laggy.
 */
public final class ColorAnalyser {

	/**
	 * Get the main color from a {@link Bitmap}.<br>
	 * 
	 * @param bitmap The {@link Bitmap} to analyse
	 * @return The rgb {@link Color} in integer (no alpha)
	 */
	public static int analyse(final Bitmap bitmap) {

		return analyse(bitmap, 18, 28);
	}

	/**
	 * Get the main color from a {@link Bitmap}.<br>
	 * 
	 * @param bitmap The {@link Bitmap} to analyse
	 * @param width The desired width of scaled bitmap
	 * @param height The desired height of scaled bitmap
	 * @return The rgb {@link Color} in integer (no alpha)
	 */
	public static int analyse(final Bitmap bitmap, final int width, final int height) {
		return analyse(bitmap, width, height, WHITE);
	}

	/**
	 * Get the main color from a {@link Bitmap}.<br>
	 * 
	 * @param bitmap The {@link Bitmap} to analyse
	 * @param width The desired width of scaled bitmap
	 * @param height The desired height of scaled bitmap
	 * @param def The default color returned, if bitmap is null
	 * @return The rgb {@link Color} in integer (no alpha)
	 */
	public static int analyse(final Bitmap bitmap, final int width, final int height, final int def) {

		if (bitmap == null) return def;

		final HashMap<Float, Integer> colors = new HashMap<Float, Integer>();

		final Bitmap resized = Bitmap.createScaledBitmap(bitmap, width, height, false);

		final int resized_height = resized.getHeight(), resized_width = resized.getWidth();

		for (int y = 0; y < resized_height; y++) {
			for (int x = 0; x < resized_width; x++) {
				final int temp_color = resized.getPixel(x, y);
				final int color = argb(0xFF, red(temp_color), green(temp_color), blue(temp_color));
				final float[] hsv = new float[3];
				colorToHSV(color, hsv);
				final float score = (hsv[1] * hsv[1] + 0.001f) * (hsv[2] * hsv[2]);
				colors.put(score, color);
			}
		}

		resized.recycle();
		if (colors.size() == 0) return def;
		return colors.get(Collections.max(colors.keySet()));
	}
}

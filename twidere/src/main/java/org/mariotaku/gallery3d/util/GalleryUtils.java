/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.gallery3d.util;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.Closeable;

public class GalleryUtils {

	private static float sPixelDensity = -1f;
	private static final String TAG = "Utils";

	// Throws AssertionError if the input is false.
	public static void assertTrue(final boolean cond) {
		if (!cond) throw new AssertionError();
	}

	public static int ceilLog2(final float value) {
		int i;
		for (i = 0; i < 31; i++) {
			if (1 << i >= value) {
				break;
			}
		}
		return i;
	}

	// Throws NullPointerException if the input is null.
	public static <T> T checkNotNull(final T object) {
		if (object == null) throw new NullPointerException();
		return object;
	}

	// Returns the input value x clamped to the range [min, max].
	public static float clamp(final float x, final float min, final float max) {
		if (x > max) return max;
		if (x < min) return min;
		return x;
	}

	// Returns the input value x clamped to the range [min, max].
	public static int clamp(final int x, final int min, final int max) {
		if (x > max) return max;
		if (x < min) return min;
		return x;
	}

	public static void closeSilently(final Closeable c) {
		if (c == null) return;
		try {
			c.close();
		} catch (final Throwable t) {
			Log.w(TAG, "close fail", t);
		}
	}

	public static int dpToPixel(final int dp) {
		return Math.round(dpToPixel((float) dp));
	}

	public static int floorLog2(final float value) {
		int i;
		for (i = 0; i < 31; i++) {
			if (1 << i > value) {
				break;
			}
		}
		return i - 1;
	}

	public static void initialize(final Context context) {
		final DisplayMetrics metrics = new DisplayMetrics();
		final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);
		sPixelDensity = metrics.density;
	}

	public static float[] intColorToFloatARGBArray(final int from) {
		return new float[] { Color.alpha(from) / 255f, Color.red(from) / 255f, Color.green(from) / 255f,
				Color.blue(from) / 255f };
	}

	public static boolean isHighResolution(final Context context) {
		final DisplayMetrics metrics = new DisplayMetrics();
		final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);
		return metrics.heightPixels > 2048 || metrics.widthPixels > 2048;
	}

	public static boolean isOpaque(final int color) {
		return color >>> 24 == 0xFF;
	}

	// Returns the next power of two.
	// Returns the input if it is already power of 2.
	// Throws IllegalArgumentException if the input is <= 0 or
	// the answer overflows.
	public static int nextPowerOf2(int n) {
		if (n <= 0 || n > 1 << 30) throw new IllegalArgumentException("n is invalid: " + n);
		n -= 1;
		n |= n >> 16;
		n |= n >> 8;
		n |= n >> 4;
		n |= n >> 2;
		n |= n >> 1;
		return n + 1;
	}

	// Returns the previous power of two.
	// Returns the input if it is already power of 2.
	// Throws IllegalArgumentException if the input is <= 0
	public static int prevPowerOf2(final int n) {
		if (n <= 0) throw new IllegalArgumentException();
		return Integer.highestOneBit(n);
	}

	public static void waitWithoutInterrupt(final Object object) {
		try {
			object.wait();
		} catch (final InterruptedException e) {
			Log.w(TAG, "unexpected interrupt: " + object);
		}
	}

	private static float dpToPixel(final float dp) {
		return sPixelDensity * dp;
	}

}

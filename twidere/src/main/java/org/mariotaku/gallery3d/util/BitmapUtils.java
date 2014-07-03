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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class BitmapUtils {

	// Find the max x that 1 / x <= scale.
	public static int computeSampleSize(final float scale) {
		if (scale <= 0) return 1;
		final int initialSize = Math.max(1, (int) Math.ceil(1 / scale));
		return initialSize <= 8 ? GalleryUtils.nextPowerOf2(initialSize) : (initialSize + 7) / 8 * 8;
	}

	// This computes a sample size which makes the longer side at least
	// minSideLength long. If that's not possible, return 1.
	public static int computeSampleSizeLarger(final int w, final int h, final int minSideLength) {
		final int initialSize = Math.max(w / minSideLength, h / minSideLength);
		if (initialSize <= 1) return 1;

		return initialSize <= 8 ? GalleryUtils.prevPowerOf2(initialSize) : initialSize / 8 * 8;
	}

	// Resize the bitmap if each side is >= targetSize * 2
	public static Bitmap resizeDownIfTooBig(final Bitmap bitmap, final int targetSize, final boolean recycle) {
		final int srcWidth = bitmap.getWidth();
		final int srcHeight = bitmap.getHeight();
		final float scale = Math.max((float) targetSize / srcWidth, (float) targetSize / srcHeight);
		if (scale > 0.5f) return bitmap;
		return resizeBitmapByScale(bitmap, scale, recycle);
	}

	private static Bitmap.Config getConfig(final Bitmap bitmap) {
		Bitmap.Config config = bitmap.getConfig();
		if (config == null) {
			config = Bitmap.Config.RGB_565;
		}
		return config;
	}

	private static Bitmap resizeBitmapByScale(final Bitmap bitmap, final float scale, final boolean recycle) {
		final int width = Math.round(bitmap.getWidth() * scale);
		final int height = Math.round(bitmap.getHeight() * scale);
		if (width == bitmap.getWidth() && height == bitmap.getHeight()) return bitmap;
		final Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
		final Canvas canvas = new Canvas(target);
		canvas.scale(scale, scale);
		final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		if (recycle) {
			bitmap.recycle();
		}
		return target;
	}
}

/*
 * Copyright (C) 2012 The Android Open Source Project
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

package org.mariotaku.twidere.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Debug;
import android.os.StatFs;

import java.io.File;

/**
 * Class containing some static utility methods.
 */
public class ImageLoaderUtils {

	/**
	 * Get the size in bytes of a bitmap.
	 * 
	 * @param bitmap
	 * @return size in bytes
	 */
	public static int getBitmapSize(final Bitmap bitmap) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
			return GetBitmapSizeAccessor.getBitmapSize(bitmap);
		// Pre HC-MR1
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	/**
	 * Get the memory class of this device (approx. per-app memory limit)
	 * 
	 * @param context
	 * @return
	 */
	public static int getMemoryClass(final Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) return GetMemoryClassAccessor.getMemoryClass(context);
		return (int) (Debug.getNativeHeapSize() / 1024 / 1024);
	}

	/**
	 * Check how much usable space is available at a given path.
	 * 
	 * @param path The path to check
	 * @return The space available in bytes
	 */
	@SuppressWarnings("deprecation")
	public static long getUsableSpace(final File path) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
			return GetUsableSpaceAccessor.getUsableSpace(path);
		final StatFs stats = new StatFs(path.getPath());
		return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
	}

	/**
	 * Check if OS version has a http URLConnection bug. See here for more
	 * information:
	 * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
	 * 
	 * @return
	 */
	public static boolean hasHttpConnectionBug() {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO;
	}

	static class GetBitmapSizeAccessor {

		@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
		static int getBitmapSize(final Bitmap bitmap) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) return bitmap.getByteCount();
			// Pre HC-MR1
			return bitmap.getRowBytes() * bitmap.getHeight();
		}
	}

	static class GetMemoryClassAccessor {

		@TargetApi(Build.VERSION_CODES.ECLAIR)
		public static int getMemoryClass(final Context context) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
				return ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
			return (int) (Debug.getNativeHeapSize() / 1024 / 1024);
		}
	}

	static class GetUsableSpaceAccessor {

		@SuppressWarnings("deprecation")
		@TargetApi(Build.VERSION_CODES.GINGERBREAD)
		public static long getUsableSpace(final File path) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) return path.getUsableSpace();
			final StatFs stats = new StatFs(path.getPath());
			return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
		}
	}
}

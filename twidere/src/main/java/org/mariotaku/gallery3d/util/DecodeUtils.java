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
import android.graphics.Bitmap.Config;

public class DecodeUtils {

	// TODO: This function should not be called directly from
	// DecodeUtils.requestDecode(...), since we don't have the knowledge
	// if the bitmap will be uploaded to GL.
	public static Bitmap ensureGLCompatibleBitmap(final Bitmap bitmap) {
		if (bitmap == null || bitmap.getConfig() != null) return bitmap;
		final Bitmap newBitmap = bitmap.copy(Config.RGB_565, false);
		bitmap.recycle();
		return newBitmap;
	}
}

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

package org.mariotaku.gallery3d.ui;

import android.graphics.Bitmap;

import org.mariotaku.gallery3d.util.GalleryUtils;

// BitmapTexture is a texture whose content is specified by a fixed Bitmap.
//
// The texture does not own the Bitmap. The user should make sure the Bitmap
// is valid during the texture's lifetime. When the texture is recycled, it
// does not free the Bitmap.
public class BitmapTexture extends UploadedTexture {
	protected Bitmap mContentBitmap;

	public BitmapTexture(final Bitmap bitmap) {
		this(bitmap, false);
	}

	private BitmapTexture(final Bitmap bitmap, final boolean hasBorder) {
		super(hasBorder);
		GalleryUtils.assertTrue(bitmap != null && !bitmap.isRecycled());
		mContentBitmap = bitmap;
	}

	public Bitmap getBitmap() {
		return mContentBitmap;
	}

	@Override
	protected void onFreeBitmap(final Bitmap bitmap) {
		// Do nothing.
	}

	@Override
	protected Bitmap onGetBitmap() {
		return mContentBitmap;
	}
}

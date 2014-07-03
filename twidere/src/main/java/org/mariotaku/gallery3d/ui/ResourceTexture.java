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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.mariotaku.gallery3d.util.GalleryUtils;

// ResourceTexture is a texture whose Bitmap is decoded from a resource.
// By default ResourceTexture is not opaque.
public class ResourceTexture extends UploadedTexture {

	protected final Context mContext;
	protected final int mResId;

	public ResourceTexture(final Context context, final int resId) {
		mContext = GalleryUtils.checkNotNull(context);
		mResId = resId;
		setOpaque(false);
	}

	@Override
	protected void onFreeBitmap(final Bitmap bitmap) {
		if (!inFinalizer()) {
			bitmap.recycle();
		}
	}

	@Override
	protected Bitmap onGetBitmap() {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeResource(mContext.getResources(), mResId, options);
	}
}

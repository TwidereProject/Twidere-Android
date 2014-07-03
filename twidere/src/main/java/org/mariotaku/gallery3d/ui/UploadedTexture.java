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
import android.graphics.Bitmap.Config;
import android.opengl.GLUtils;

import org.mariotaku.gallery3d.util.GalleryUtils;

import java.util.HashMap;

import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

// UploadedTextures use a Bitmap for the content of the texture.
//
// Subclasses should implement onGetBitmap() to provide the Bitmap and
// implement onFreeBitmap(mBitmap) which will be called when the Bitmap
// is not needed anymore.
//
// isContentValid() is meaningful only when the isLoaded() returns true.
// It means whether the content needs to be updated.
//
// The user of this class should call recycle() when the texture is not
// needed anymore.
//
// By default an UploadedTexture is opaque (so it can be drawn faster without
// blending). The user or subclass can override it using setOpaque().
abstract class UploadedTexture extends BasicTexture {

	// To prevent keeping allocation the borders, we store those used borders
	// here.
	// Since the length will be power of two, it won't use too much memory.
	private static HashMap<BorderKey, Bitmap> sBorderLines = new HashMap<BorderKey, Bitmap>();
	private static BorderKey sBorderKey = new BorderKey();

	@SuppressWarnings("unused")
	private static final String TAG = "Texture";
	private boolean mContentValid = true;

	// indicate this textures is being uploaded in background
	private final boolean mIsUploading = false;
	private boolean mOpaque = true;
	private static int sUploadedCount;
	private static final int UPLOAD_LIMIT = 100;

	protected Bitmap mBitmap;
	private int mBorder;

	static int[] sTextureId = new int[1];

	static float[] sCropRect = new float[4];

	protected UploadedTexture() {
		this(false);
	}

	protected UploadedTexture(final boolean hasBorder) {
		super(null, 0, STATE_UNLOADED);
		if (hasBorder) {
			setBorder(true);
			mBorder = 1;
		}
	}

	@Override
	public int getHeight() {
		if (mWidth == UNSPECIFIED) {
			getBitmap();
		}
		return mHeight;
	}

	@Override
	public int getWidth() {
		if (mWidth == UNSPECIFIED) {
			getBitmap();
		}
		return mWidth;
	}

	/**
	 * Whether the content on GPU is valid.
	 */
	public boolean isContentValid() {
		return isLoaded() && mContentValid;
	}

	@Override
	public boolean isOpaque() {
		return mOpaque;
	}

	public boolean isUploading() {
		return mIsUploading;
	}

	@Override
	public void recycle() {
		super.recycle();
		if (mBitmap != null) {
			freeBitmap();
		}
	}

	public void setOpaque(final boolean isOpaque) {
		mOpaque = isOpaque;
	}

	/**
	 * Updates the content on GPU's memory.
	 * 
	 * @param canvas
	 */
	public void updateContent(final GLCanvas canvas) {
		if (!isLoaded()) {
			uploadToCanvas(canvas);
		} else if (!mContentValid) {
			final Bitmap bitmap = getBitmap();
			if (bitmap == null) return;
			final int format = GLUtils.getInternalFormat(bitmap);
			final int type = GLUtils.getType(bitmap);
			canvas.getGLInstance().glBindTexture(GL11.GL_TEXTURE_2D, mId);
			GLUtils.texSubImage2D(GL11.GL_TEXTURE_2D, 0, mBorder, mBorder, bitmap, format, type);
			freeBitmap();
			mContentValid = true;
		}
	}

	@Override
	protected int getTarget() {
		return GL11.GL_TEXTURE_2D;
	}

	protected void invalidateContent() {
		if (mBitmap != null) {
			freeBitmap();
		}
		mContentValid = false;
		mWidth = UNSPECIFIED;
		mHeight = UNSPECIFIED;
	}

	@Override
	protected boolean onBind(final GLCanvas canvas) {
		updateContent(canvas);
		return isContentValid();
	}

	protected abstract void onFreeBitmap(Bitmap bitmap);

	protected abstract Bitmap onGetBitmap();

	private void freeBitmap() {
		GalleryUtils.assertTrue(mBitmap != null);
		onFreeBitmap(mBitmap);
		mBitmap = null;
	}

	private Bitmap getBitmap() {
		if (mBitmap == null) {
			mBitmap = onGetBitmap();
			final int w = mBitmap.getWidth() + mBorder * 2;
			final int h = mBitmap.getHeight() + mBorder * 2;
			if (mWidth == UNSPECIFIED) {
				setSize(w, h);
			}
		}
		return mBitmap;
	}

	private void uploadToCanvas(final GLCanvas canvas) {
		final GL11 gl = canvas.getGLInstance();

		final Bitmap bitmap = getBitmap();
		if (bitmap != null) {
			try {
				final int bWidth = bitmap.getWidth();
				final int bHeight = bitmap.getHeight();
				final int texWidth = getTextureWidth();
				final int texHeight = getTextureHeight();

				GalleryUtils.assertTrue(bWidth <= texWidth && bHeight <= texHeight);

				// Define a vertically flipped crop rectangle for
				// OES_draw_texture.
				// The four values in sCropRect are: left, bottom, width, and
				// height. Negative value of width or height means flip.
				sCropRect[0] = mBorder;
				sCropRect[1] = mBorder + bHeight;
				sCropRect[2] = bWidth;
				sCropRect[3] = -bHeight;

				// Upload the bitmap to a new texture.
				GLId.glGenTextures(1, sTextureId, 0);
				gl.glBindTexture(GL11.GL_TEXTURE_2D, sTextureId[0]);
				gl.glTexParameterfv(GL11.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, sCropRect, 0);
				gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
				gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
				gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

				if (bWidth == texWidth && bHeight == texHeight) {
					GLUtils.texImage2D(GL11.GL_TEXTURE_2D, 0, bitmap, 0);
				} else {
					final int format = GLUtils.getInternalFormat(bitmap);
					final int type = GLUtils.getType(bitmap);
					final Config config = bitmap.getConfig();

					gl.glTexImage2D(GL11.GL_TEXTURE_2D, 0, format, texWidth, texHeight, 0, format, type, null);
					GLUtils.texSubImage2D(GL11.GL_TEXTURE_2D, 0, mBorder, mBorder, bitmap, format, type);

					if (mBorder > 0) {
						// Left border
						Bitmap line = getBorderLine(true, config, texHeight);
						GLUtils.texSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, line, format, type);

						// Top border
						line = getBorderLine(false, config, texWidth);
						GLUtils.texSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, line, format, type);
					}

					// Right border
					if (mBorder + bWidth < texWidth) {
						final Bitmap line = getBorderLine(true, config, texHeight);
						GLUtils.texSubImage2D(GL11.GL_TEXTURE_2D, 0, mBorder + bWidth, 0, line, format, type);
					}

					// Bottom border
					if (mBorder + bHeight < texHeight) {
						final Bitmap line = getBorderLine(false, config, texWidth);
						GLUtils.texSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, mBorder + bHeight, line, format, type);
					}
				}
			} finally {
				freeBitmap();
			}
			// Update texture state.
			setAssociatedCanvas(canvas);
			mId = sTextureId[0];
			mState = STATE_LOADED;
			mContentValid = true;
		} else {
			mState = STATE_ERROR;
			// throw new RuntimeException("Texture load fail, no bitmap");
		}
	}

	public static void resetUploadLimit() {
		sUploadedCount = 0;
	}

	public static boolean uploadLimitReached() {
		return sUploadedCount > UPLOAD_LIMIT;
	}

	private static Bitmap getBorderLine(final boolean vertical, final Config config, final int length) {
		final BorderKey key = sBorderKey;
		key.vertical = vertical;
		key.config = config;
		key.length = length;
		Bitmap bitmap = sBorderLines.get(key);
		if (bitmap == null) {
			bitmap = vertical ? Bitmap.createBitmap(1, length, config) : Bitmap.createBitmap(length, 1, config);
			sBorderLines.put(key.clone(), bitmap);
		}
		return bitmap;
	}

	private static class BorderKey implements Cloneable {
		public boolean vertical;
		public Config config;
		public int length;

		@Override
		public BorderKey clone() {
			try {
				return (BorderKey) super.clone();
			} catch (final CloneNotSupportedException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public boolean equals(final Object object) {
			if (!(object instanceof BorderKey)) return false;
			final BorderKey o = (BorderKey) object;
			return vertical == o.vertical && config == o.config && length == o.length;
		}

		@Override
		public int hashCode() {
			final int x = config.hashCode() ^ length;
			return vertical ? x : -x;
		}
	}
}

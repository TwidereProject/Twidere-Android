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

import android.util.Log;

import org.mariotaku.gallery3d.util.GalleryUtils;

import java.util.WeakHashMap;

// BasicTexture is a Texture corresponds to a real GL texture.
// The state of a BasicTexture indicates whether its data is loaded to GL memory.
// If a BasicTexture is loaded into GL memory, it has a GL texture id.
abstract class BasicTexture implements Texture {

	private static final String TAG = "BasicTexture";
	protected static final int UNSPECIFIED = -1;

	protected static final int STATE_UNLOADED = 0;
	protected static final int STATE_LOADED = 1;
	protected static final int STATE_ERROR = -1;

	// Log a warning if a texture is larger along a dimension
	private static final int MAX_TEXTURE_SIZE = 4096;

	protected int mId;
	protected int mState;

	protected int mWidth = UNSPECIFIED;
	protected int mHeight = UNSPECIFIED;

	protected int mTextureWidth;
	protected int mTextureHeight;

	private boolean mHasBorder;

	protected GLCanvas mCanvasRef = null;
	private static WeakHashMap<BasicTexture, Object> sAllTextures = new WeakHashMap<BasicTexture, Object>();
	private static ThreadLocal<Class<BasicTexture>> sInFinalizer = new ThreadLocal<Class<BasicTexture>>();

	protected BasicTexture() {
		this(null, 0, STATE_UNLOADED);
	}

	protected BasicTexture(final GLCanvas canvas, final int id, final int state) {
		setAssociatedCanvas(canvas);
		mId = id;
		mState = state;
		synchronized (sAllTextures) {
			sAllTextures.put(this, null);
		}
	}

	@Override
	public void draw(final GLCanvas canvas, final int x, final int y) {
		canvas.drawTexture(this, x, y, getWidth(), getHeight());
	}

	@Override
	public void draw(final GLCanvas canvas, final int x, final int y, final int w, final int h) {
		canvas.drawTexture(this, x, y, w, h);
	}

	@Override
	public int getHeight() {
		return mHeight;
	}

	public int getId() {
		return mId;
	}

	// Returns the height rounded to the next power of 2.
	public int getTextureHeight() {
		return mTextureHeight;
	}

	// Returns the width rounded to the next power of 2.
	public int getTextureWidth() {
		return mTextureWidth;
	}

	@Override
	public int getWidth() {
		return mWidth;
	}

	// Returns true if the texture has one pixel transparent border around the
	// actual content. This is used to avoid jigged edges.
	//
	// The jigged edges appear because we use GL_CLAMP_TO_EDGE for texture wrap
	// mode (GL_CLAMP is not available in OpenGL ES), so a pixel partially
	// covered by the texture will use the color of the edge texel. If we add
	// the transparent border, the color of the edge texel will be mixed with
	// appropriate amount of transparent.
	//
	// Currently our background is black, so we can draw the thumbnails without
	// enabling blending.
	public boolean hasBorder() {
		return mHasBorder;
	}

	public boolean isLoaded() {
		return mState == STATE_LOADED;
	}

	// recycle() is called when the texture will never be used again,
	// so it can free all resources.
	public void recycle() {
		freeResource();
	}

	@Override
	protected void finalize() {
		sInFinalizer.set(BasicTexture.class);
		recycle();
		sInFinalizer.set(null);
	}

	// Returns the GL texture target for this texture (e.g. GL_TEXTURE_2D).
	abstract protected int getTarget();

	// onBind is called before GLCanvas binds this texture.
	// It should make sure the data is uploaded to GL memory.
	abstract protected boolean onBind(GLCanvas canvas);

	protected void setAssociatedCanvas(final GLCanvas canvas) {
		mCanvasRef = canvas;
	}

	protected void setBorder(final boolean hasBorder) {
		mHasBorder = hasBorder;
	}

	/**
	 * Sets the content size of this texture. In OpenGL, the actual texture size
	 * must be of power of 2, the size of the content may be smaller.
	 */
	protected void setSize(final int width, final int height) {
		mWidth = width;
		mHeight = height;
		mTextureWidth = GalleryUtils.nextPowerOf2(width);
		mTextureHeight = GalleryUtils.nextPowerOf2(height);
		if (mTextureWidth > MAX_TEXTURE_SIZE || mTextureHeight > MAX_TEXTURE_SIZE) {
			Log.w(TAG, String.format("texture is too large: %d x %d", mTextureWidth, mTextureHeight), new Exception());
		}
	}

	private void freeResource() {
		final GLCanvas canvas = mCanvasRef;
		if (canvas != null && isLoaded()) {
			canvas.unloadTexture(this);
		}
		mState = STATE_UNLOADED;
		setAssociatedCanvas(null);
	}

	// yield() is called when the texture will not be used temporarily,
	// so it can free some resources.
	// The default implementation unloads the texture from GL memory, so
	// the subclass should make sure it can reload the texture to GL memory
	// later, or it will have to override this method.
	private void yield() {
		freeResource();
	}

	// This is for deciding if we can call Bitmap's recycle().
	// We cannot call Bitmap's recycle() in finalizer because at that point
	// the finalizer of Bitmap may already be called so recycle() will crash.
	public static boolean inFinalizer() {
		return sInFinalizer.get() != null;
	}

	public static void invalidateAllTextures() {
		synchronized (sAllTextures) {
			for (final BasicTexture t : sAllTextures.keySet()) {
				t.mState = STATE_UNLOADED;
				t.setAssociatedCanvas(null);
			}
		}
	}

	public static void yieldAllTextures() {
		synchronized (sAllTextures) {
			for (final BasicTexture t : sAllTextures.keySet()) {
				t.yield();
			}
		}
	}
}

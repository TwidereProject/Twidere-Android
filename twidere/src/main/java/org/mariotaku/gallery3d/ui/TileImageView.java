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
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.FloatMath;
import android.util.Log;

import org.mariotaku.gallery3d.ImageViewerGLActivity;
import org.mariotaku.gallery3d.util.ApiHelper;
import org.mariotaku.gallery3d.util.BitmapPool;
import org.mariotaku.gallery3d.util.DecodeUtils;
import org.mariotaku.gallery3d.util.Future;
import org.mariotaku.gallery3d.util.GalleryUtils;
import org.mariotaku.gallery3d.util.LongSparseArray;
import org.mariotaku.gallery3d.util.ThreadPool;
import org.mariotaku.gallery3d.util.ThreadPool.CancelListener;
import org.mariotaku.gallery3d.util.ThreadPool.JobContext;

import java.util.concurrent.atomic.AtomicBoolean;

public class TileImageView extends GLView {
	public static final int SIZE_UNKNOWN = -1;

	private static final String TAG = "TileImageView";

	// TILE_SIZE must be 2^N - 2. We put one pixel border in each side of the
	// texture to avoid seams between tiles.
	private static int TILE_SIZE;
	private static final int TILE_BORDER = 1;
	private static int BITMAP_SIZE;
	private static final int UPLOAD_LIMIT = 1;

	private static BitmapPool sTilePool;

	/*
	 * This is the tile state in the CPU side. Life of a Tile: ACTIVATED
	 * (initial state) --> IN_QUEUE - by queueForDecode() --> RECYCLED - by
	 * recycleTile() IN_QUEUE --> DECODING - by decodeTile() --> RECYCLED - by
	 * recycleTile) DECODING --> RECYCLING - by recycleTile() --> DECODED - by
	 * decodeTile() --> DECODE_FAIL - by decodeTile() RECYCLING --> RECYCLED -
	 * by decodeTile() DECODED --> ACTIVATED - (after the decoded bitmap is
	 * uploaded) DECODED --> RECYCLED - by recycleTile() DECODE_FAIL -> RECYCLED
	 * - by recycleTile() RECYCLED --> ACTIVATED - by obtainTile()
	 */
	private static final int STATE_ACTIVATED = 0x01;
	private static final int STATE_IN_QUEUE = 0x02;
	private static final int STATE_DECODING = 0x04;
	private static final int STATE_DECODED = 0x08;
	private static final int STATE_DECODE_FAIL = 0x10;
	private static final int STATE_RECYCLING = 0x20;
	private static final int STATE_RECYCLED = 0x40;

	private PhotoView.ITileImageAdapter mModel;
	private ScreenNail mScreenNail;
	protected int mLevelCount; // cache the value of mScaledBitmaps.length

	// The mLevel variable indicates which level of bitmap we should use.
	// Level 0 means the original full-sized bitmap, and a larger value means
	// a smaller scaled bitmap (The width and height of each scaled bitmap is
	// half size of the previous one). If the value is in [0, mLevelCount), we
	// use the bitmap in mScaledBitmaps[mLevel] for display, otherwise the value
	// is mLevelCount, and that means we use mScreenNail for display.
	private int mLevel = 0;

	// The offsets of the (left, top) of the upper-left tile to the (left, top)
	// of the view.
	private int mOffsetX;
	private int mOffsetY;

	private int mUploadQuota;
	private boolean mRenderComplete;

	private final RectF mSourceRect = new RectF();
	private final RectF mTargetRect = new RectF();

	private final LongSparseArray<Tile> mActiveTiles = new LongSparseArray<Tile>();

	// The following three queue is guarded by TileImageView.this
	private final TileQueue mRecycledQueue = new TileQueue();
	private final TileQueue mUploadQueue = new TileQueue();
	private final TileQueue mDecodeQueue = new TileQueue();

	// The width and height of the full-sized bitmap
	protected int mImageWidth = SIZE_UNKNOWN;
	protected int mImageHeight = SIZE_UNKNOWN;

	protected int mCenterX;
	protected int mCenterY;
	protected float mScale;
	protected int mRotation;

	// Temp variables to avoid memory allocation
	private final Rect mTileRange = new Rect();
	private final Rect mActiveRange[] = { new Rect(), new Rect() };

	private final TileUploader mTileUploader = new TileUploader();
	private boolean mIsTextureFreed;
	private Future<Void> mTileDecoder;
	private final ThreadPool mThreadPool;
	private boolean mBackgroundTileUploaded;

	public TileImageView(final ImageViewerGLActivity context) {
		mThreadPool = context.getThreadPool();
		mTileDecoder = mThreadPool.submit(new TileDecoder());
		if (TILE_SIZE == 0) {
			if (GalleryUtils.isHighResolution(context)) {
				TILE_SIZE = 510;
			} else {
				TILE_SIZE = 254;
			}
			BITMAP_SIZE = TILE_SIZE + TILE_BORDER * 2;
			sTilePool = ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_REGION_DECODER ? new BitmapPool(BITMAP_SIZE,
					BITMAP_SIZE, 128) : null;
		}
	}

	public void freeTextures() {
		mIsTextureFreed = true;

		if (mTileDecoder != null) {
			mTileDecoder.cancel();
			mTileDecoder.get();
			mTileDecoder = null;
		}

		final int n = mActiveTiles.size();
		for (int i = 0; i < n; i++) {
			final Tile texture = mActiveTiles.valueAt(i);
			texture.recycle();
		}
		mActiveTiles.clear();
		mTileRange.set(0, 0, 0, 0);

		synchronized (this) {
			mUploadQueue.clean();
			mDecodeQueue.clean();
			Tile tile = mRecycledQueue.pop();
			while (tile != null) {
				tile.recycle();
				tile = mRecycledQueue.pop();
			}
		}
		setScreenNail(null);
		if (sTilePool != null) {
			sTilePool.clear();
		}
	}

	public void notifyModelInvalidated() {
		invalidateTiles();
		if (mModel == null) {
			mScreenNail = null;
			mImageWidth = 0;
			mImageHeight = 0;
			mLevelCount = 0;
		} else {
			setScreenNail(mModel.getScreenNail());
			mImageWidth = mModel.getImageWidth();
			mImageHeight = mModel.getImageHeight();
			mLevelCount = mModel.getLevelCount();
		}
		layoutTiles(mCenterX, mCenterY, mScale, mRotation);
		invalidate();
	}

	public void prepareTextures() {
		if (mTileDecoder == null) {
			mTileDecoder = mThreadPool.submit(new TileDecoder());
		}
		if (mIsTextureFreed) {
			layoutTiles(mCenterX, mCenterY, mScale, mRotation);
			mIsTextureFreed = false;
			setScreenNail(mModel == null ? null : mModel.getScreenNail());
		}
	}

	public void setModel(final PhotoView.ITileImageAdapter model) {
		mModel = model;
		if (model != null) {
			notifyModelInvalidated();
		}
	}

	public boolean setPosition(final int centerX, final int centerY, final float scale, final int rotation) {
		if (mCenterX == centerX && mCenterY == centerY && mScale == scale && mRotation == rotation) return false;
		mCenterX = centerX;
		mCenterY = centerY;
		mScale = scale;
		mRotation = rotation;
		layoutTiles(centerX, centerY, scale, rotation);
		invalidate();
		return true;
	}

	public void setScreenNail(final ScreenNail s) {
		mScreenNail = s;
	}

	@Override
	protected void onLayout(final boolean changeSize, final int left, final int top, final int right, final int bottom) {
		super.onLayout(changeSize, left, top, right, bottom);
		if (changeSize) {
			layoutTiles(mCenterX, mCenterY, mScale, mRotation);
		}
	}

	@Override
	protected void render(final GLCanvas canvas) {
		mUploadQuota = UPLOAD_LIMIT;
		mRenderComplete = true;

		final int level = mLevel;
		final int rotation = mRotation;
		int flags = 0;
		if (rotation != 0) {
			flags |= GLCanvas.SAVE_FLAG_MATRIX;
		}

		if (flags != 0) {
			canvas.save(flags);
			if (rotation != 0) {
				final int centerX = getWidth() / 2, centerY = getHeight() / 2;
				canvas.translate(centerX, centerY);
				canvas.rotate(rotation, 0, 0, 1);
				canvas.translate(-centerX, -centerY);
			}
		}
		try {
			if (level != mLevelCount && !isScreenNailAnimating()) {
				if (mScreenNail != null) {
					mScreenNail.noDraw();
				}

				final int size = TILE_SIZE << level;
				final float length = size * mScale;
				final Rect r = mTileRange;

				for (int ty = r.top, i = 0; ty < r.bottom; ty += size, i++) {
					final float y = mOffsetY + i * length;
					for (int tx = r.left, j = 0; tx < r.right; tx += size, j++) {
						final float x = mOffsetX + j * length;
						drawTile(canvas, tx, ty, level, x, y, length);
					}
				}
			} else if (mScreenNail != null) {
				mScreenNail.draw(canvas, mOffsetX, mOffsetY, Math.round(mImageWidth * mScale),
						Math.round(mImageHeight * mScale));
				if (isScreenNailAnimating()) {
					invalidate();
				}
			}
		} finally {
			if (flags != 0) {
				canvas.restore();
			}
		}

		if (mRenderComplete) {
			if (!mBackgroundTileUploaded) {
				uploadBackgroundTiles(canvas);
			}
		} else {
			invalidate();
		}
	}

	private void activateTile(final int x, final int y, final int level) {
		final long key = makeTileKey(x, y, level);
		Tile tile = mActiveTiles.get(key);
		if (tile != null) {
			if (tile.mTileState == STATE_IN_QUEUE) {
				tile.mTileState = STATE_ACTIVATED;
			}
			return;
		}
		tile = obtainTile(x, y, level);
		mActiveTiles.put(key, tile);
	}

	private boolean decodeTile(final Tile tile) {
		synchronized (this) {
			if (tile.mTileState != STATE_IN_QUEUE) return false;
			tile.mTileState = STATE_DECODING;
		}
		final boolean decodeComplete = tile.decode();
		synchronized (this) {
			if (tile.mTileState == STATE_RECYCLING) {
				tile.mTileState = STATE_RECYCLED;
				if (tile.mDecodedTile != null) {
					if (sTilePool != null) {
						sTilePool.recycle(tile.mDecodedTile);
					}
					tile.mDecodedTile = null;
				}
				mRecycledQueue.push(tile);
				return false;
			}
			tile.mTileState = decodeComplete ? STATE_DECODED : STATE_DECODE_FAIL;
			return decodeComplete;
		}
	}

	// Draw the tile to a square at canvas that locates at (x, y) and
	// has a side length of length.
	private void drawTile(final GLCanvas canvas, final int tx, final int ty, final int level, final float x,
			final float y, final float length) {
		final RectF source = mSourceRect;
		final RectF target = mTargetRect;
		target.set(x, y, x + length, y + length);
		source.set(0, 0, TILE_SIZE, TILE_SIZE);

		final Tile tile = getTile(tx, ty, level);
		if (tile != null) {
			if (!tile.isContentValid()) {
				if (tile.mTileState == STATE_DECODED) {
					if (mUploadQuota > 0) {
						--mUploadQuota;
						tile.updateContent(canvas);
					} else {
						mRenderComplete = false;
					}
				} else if (tile.mTileState != STATE_DECODE_FAIL) {
					mRenderComplete = false;
					queueForDecode(tile);
				}
			}
			if (drawTile(tile, canvas, source, target)) return;
		}
		if (mScreenNail != null) {
			final int size = TILE_SIZE << level;
			final float scaleX = (float) mScreenNail.getWidth() / mImageWidth;
			final float scaleY = (float) mScreenNail.getHeight() / mImageHeight;
			source.set(tx * scaleX, ty * scaleY, (tx + size) * scaleX, (ty + size) * scaleY);
			mScreenNail.draw(canvas, source, target);
		}
	}

	// If the bitmap is scaled by the given factor "scale", return the
	// rectangle containing visible range. The left-top coordinate returned is
	// aligned to the tile boundary.
	//
	// (cX, cY) is the point on the original bitmap which will be put in the
	// center of the ImageViewer.
	private void getRange(final Rect out, final int cX, final int cY, final int level, final float scale,
			final int rotation) {

		final double radians = Math.toRadians(-rotation);
		final double w = getWidth();
		final double h = getHeight();

		final double cos = Math.cos(radians);
		final double sin = Math.sin(radians);
		final int width = (int) Math.ceil(Math.max(Math.abs(cos * w - sin * h), Math.abs(cos * w + sin * h)));
		final int height = (int) Math.ceil(Math.max(Math.abs(sin * w + cos * h), Math.abs(sin * w - cos * h)));

		int left = (int) FloatMath.floor(cX - width / (2f * scale));
		int top = (int) FloatMath.floor(cY - height / (2f * scale));
		int right = (int) FloatMath.ceil(left + width / scale);
		int bottom = (int) FloatMath.ceil(top + height / scale);

		// align the rectangle to tile boundary
		final int size = TILE_SIZE << level;
		left = Math.max(0, size * (left / size));
		top = Math.max(0, size * (top / size));
		right = Math.min(mImageWidth, right);
		bottom = Math.min(mImageHeight, bottom);

		out.set(left, top, right, bottom);
	}

	private void getRange(final Rect out, final int cX, final int cY, final int level, final int rotation) {
		getRange(out, cX, cY, level, 1f / (1 << level + 1), rotation);
	}

	private Tile getTile(final int x, final int y, final int level) {
		return mActiveTiles.get(makeTileKey(x, y, level));
	}

	private synchronized void invalidateTiles() {
		mDecodeQueue.clean();
		mUploadQueue.clean();

		// TODO disable decoder
		final int n = mActiveTiles.size();
		for (int i = 0; i < n; i++) {
			final Tile tile = mActiveTiles.valueAt(i);
			recycleTile(tile);
		}
		mActiveTiles.clear();
	}

	private boolean isScreenNailAnimating() {
		return false;
	}

	// Prepare the tiles we want to use for display.
	//
	// 1. Decide the tile level we want to use for display.
	// 2. Decide the tile levels we want to keep as texture (in addition to
	// the one we use for display).
	// 3. Recycle unused tiles.
	// 4. Activate the tiles we want.
	private void layoutTiles(final int centerX, final int centerY, final float scale, final int rotation) {
		// The width and height of this view.
		final int width = getWidth();
		final int height = getHeight();

		// The tile levels we want to keep as texture is in the range
		// [fromLevel, endLevel).
		int fromLevel;
		int endLevel;

		// We want to use a texture larger than or equal to the display size.
		mLevel = GalleryUtils.clamp(GalleryUtils.floorLog2(1f / scale), 0, mLevelCount);

		// We want to keep one more tile level as texture in addition to what
		// we use for display. So it can be faster when the scale moves to the
		// next level. We choose a level closer to the current scale.
		if (mLevel != mLevelCount) {
			final Rect range = mTileRange;
			getRange(range, centerX, centerY, mLevel, scale, rotation);
			mOffsetX = Math.round(width / 2f + (range.left - centerX) * scale);
			mOffsetY = Math.round(height / 2f + (range.top - centerY) * scale);
			fromLevel = scale * (1 << mLevel) > 0.75f ? mLevel - 1 : mLevel;
		} else {
			// Activate the tiles of the smallest two levels.
			fromLevel = mLevel - 2;
			mOffsetX = Math.round(width / 2f - centerX * scale);
			mOffsetY = Math.round(height / 2f - centerY * scale);
		}

		fromLevel = Math.max(0, Math.min(fromLevel, mLevelCount - 2));
		endLevel = Math.min(fromLevel + 2, mLevelCount);

		final Rect range[] = mActiveRange;
		for (int i = fromLevel; i < endLevel; ++i) {
			getRange(range[i - fromLevel], centerX, centerY, i, rotation);
		}

		// If rotation is transient, don't update the tile.
		if (rotation % 90 != 0) return;

		synchronized (this) {
			mDecodeQueue.clean();
			mUploadQueue.clean();
			mBackgroundTileUploaded = false;

			// Recycle unused tiles: if the level of the active tile is outside
			// the
			// range [fromLevel, endLevel) or not in the visible range.
			int n = mActiveTiles.size();
			for (int i = 0; i < n; i++) {
				final Tile tile = mActiveTiles.valueAt(i);
				final int level = tile.mTileLevel;
				if (level < fromLevel || level >= endLevel || !range[level - fromLevel].contains(tile.mX, tile.mY)) {
					mActiveTiles.removeAt(i);
					i--;
					n--;
					recycleTile(tile);
				}
			}
		}

		for (int i = fromLevel; i < endLevel; ++i) {
			final int size = TILE_SIZE << i;
			final Rect r = range[i - fromLevel];
			for (int y = r.top, bottom = r.bottom; y < bottom; y += size) {
				for (int x = r.left, right = r.right; x < right; x += size) {
					activateTile(x, y, i);
				}
			}
		}
		invalidate();
	}

	private synchronized Tile obtainTile(final int x, final int y, final int level) {
		final Tile tile = mRecycledQueue.pop();
		if (tile != null) {
			tile.mTileState = STATE_ACTIVATED;
			tile.update(x, y, level);
			return tile;
		}
		return new Tile(x, y, level);
	}

	private synchronized void queueForDecode(final Tile tile) {
		if (tile.mTileState == STATE_ACTIVATED) {
			tile.mTileState = STATE_IN_QUEUE;
			if (mDecodeQueue.push(tile)) {
				notifyAll();
			}
		}
	}

	private void queueForUpload(final Tile tile) {
		synchronized (this) {
			mUploadQueue.push(tile);
		}
		if (mTileUploader.mActive.compareAndSet(false, true)) {
			getGLRoot().addOnGLIdleListener(mTileUploader);
		}
	}

	private synchronized void recycleTile(final Tile tile) {
		if (tile.mTileState == STATE_DECODING) {
			tile.mTileState = STATE_RECYCLING;
			return;
		}
		tile.mTileState = STATE_RECYCLED;
		if (tile.mDecodedTile != null) {
			if (sTilePool != null) {
				sTilePool.recycle(tile.mDecodedTile);
			}
			tile.mDecodedTile = null;
		}
		mRecycledQueue.push(tile);
	}

	private void uploadBackgroundTiles(final GLCanvas canvas) {
		mBackgroundTileUploaded = true;
		final int n = mActiveTiles.size();
		for (int i = 0; i < n; i++) {
			final Tile tile = mActiveTiles.valueAt(i);
			if (!tile.isContentValid()) {
				queueForDecode(tile);
			}
		}
	}

	private static boolean drawTile(Tile tile, final GLCanvas canvas, final RectF source, final RectF target) {
		while (true) {
			if (tile.isContentValid()) {
				// offset source rectangle for the texture border.
				source.offset(TILE_BORDER, TILE_BORDER);
				canvas.drawTexture(tile, source, target);
				return true;
			}

			// Parent can be divided to four quads and tile is one of the four.
			final Tile parent = tile.getParentTile();
			if (parent == null) return false;
			if (tile.mX == parent.mX) {
				source.left /= 2f;
				source.right /= 2f;
			} else {
				source.left = (TILE_SIZE + source.left) / 2f;
				source.right = (TILE_SIZE + source.right) / 2f;
			}
			if (tile.mY == parent.mY) {
				source.top /= 2f;
				source.bottom /= 2f;
			} else {
				source.top = (TILE_SIZE + source.top) / 2f;
				source.bottom = (TILE_SIZE + source.bottom) / 2f;
			}
			tile = parent;
		}
	}

	private static long makeTileKey(final int x, final int y, final int level) {
		long result = x;
		result = result << 16 | y;
		result = result << 16 | level;
		return result;
	}

	private class Tile extends UploadedTexture {
		public int mX;
		public int mY;
		public int mTileLevel;
		public Tile mNext;
		public Bitmap mDecodedTile;
		public volatile int mTileState = STATE_ACTIVATED;

		public Tile(final int x, final int y, final int level) {
			mX = x;
			mY = y;
			mTileLevel = level;
		}

		public Tile getParentTile() {
			if (mTileLevel + 1 == mLevelCount) return null;
			final int size = TILE_SIZE << mTileLevel + 1;
			final int x = size * (mX / size);
			final int y = size * (mY / size);
			return getTile(x, y, mTileLevel + 1);
		}

		@Override
		public int getTextureHeight() {
			return TILE_SIZE + TILE_BORDER * 2;
		}

		// We override getTextureWidth() and getTextureHeight() here, so the
		// texture can be re-used for different tiles regardless of the actual
		// size of the tile (which may be small because it is a tile at the
		// boundary).
		@Override
		public int getTextureWidth() {
			return TILE_SIZE + TILE_BORDER * 2;
		}

		@Override
		public String toString() {
			return String.format("tile(%s, %s, %s / %s)", mX / TILE_SIZE, mY / TILE_SIZE, mLevel, mLevelCount);
		}

		public void update(final int x, final int y, final int level) {
			mX = x;
			mY = y;
			mTileLevel = level;
			invalidateContent();
		}

		@Override
		protected void onFreeBitmap(final Bitmap bitmap) {
			if (sTilePool != null) {
				sTilePool.recycle(bitmap);
			}
		}

		@Override
		protected Bitmap onGetBitmap() {
			GalleryUtils.assertTrue(mTileState == STATE_DECODED);

			// We need to override the width and height, so that we won't
			// draw beyond the boundaries.
			final int rightEdge = (mImageWidth - mX >> mTileLevel) + TILE_BORDER;
			final int bottomEdge = (mImageHeight - mY >> mTileLevel) + TILE_BORDER;
			setSize(Math.min(BITMAP_SIZE, rightEdge), Math.min(BITMAP_SIZE, bottomEdge));

			final Bitmap bitmap = mDecodedTile;
			mDecodedTile = null;
			mTileState = STATE_ACTIVATED;
			return bitmap;
		}

		boolean decode() {
			// Get a tile from the original image. The tile is down-scaled
			// by (1 << mTilelevel) from a region in the original image.
			try {
				mDecodedTile = DecodeUtils.ensureGLCompatibleBitmap(mModel.getTile(mTileLevel, mX, mY, TILE_SIZE,
						TILE_BORDER, sTilePool));
			} catch (final Throwable t) {
				Log.w(TAG, "fail to decode tile", t);
			}
			return mDecodedTile != null;
		}
	}

	private class TileDecoder implements ThreadPool.Job<Void> {

		private final CancelListener mNotifier = new CancelListener() {
			@Override
			public void onCancel() {
				synchronized (TileImageView.this) {
					TileImageView.this.notifyAll();
				}
			}
		};

		@Override
		public Void run(final JobContext jc) {
			jc.setMode(ThreadPool.MODE_NONE);
			jc.setCancelListener(mNotifier);
			while (!jc.isCancelled()) {
				Tile tile = null;
				synchronized (TileImageView.this) {
					tile = mDecodeQueue.pop();
					if (tile == null && !jc.isCancelled()) {
						GalleryUtils.waitWithoutInterrupt(TileImageView.this);
					}
				}
				if (tile == null) {
					continue;
				}
				if (decodeTile(tile)) {
					queueForUpload(tile);
				}
			}
			return null;
		}
	}

	private static class TileQueue {
		private Tile mHead;

		public void clean() {
			mHead = null;
		}

		public Tile pop() {
			final Tile tile = mHead;
			if (tile != null) {
				mHead = tile.mNext;
			}
			return tile;
		}

		public boolean push(final Tile tile) {
			final boolean wasEmpty = mHead == null;
			tile.mNext = mHead;
			mHead = tile;
			return wasEmpty;
		}
	}

	private class TileUploader implements GLRoot.OnGLIdleListener {
		AtomicBoolean mActive = new AtomicBoolean(false);

		@Override
		public boolean onGLIdle(final GLCanvas canvas, final boolean renderRequested) {
			// Skips uploading if there is a pending rendering request.
			// Returns true to keep uploading in next rendering loop.
			if (renderRequested) return true;
			int quota = UPLOAD_LIMIT;
			Tile tile = null;
			while (quota > 0) {
				synchronized (TileImageView.this) {
					tile = mUploadQueue.pop();
				}
				if (tile == null) {
					break;
				}
				if (!tile.isContentValid()) {
					final boolean hasBeenLoaded = tile.isLoaded();
					if (tile.mTileState != STATE_DECODED) return false;
					tile.updateContent(canvas);
					if (!hasBeenLoaded) {
						tile.draw(canvas, 0, 0);
					}
					--quota;
				}
			}
			if (tile == null) {
				mActive.set(false);
			}
			return tile != null;
		}
	}
}

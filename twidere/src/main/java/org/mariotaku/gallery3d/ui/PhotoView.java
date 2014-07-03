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
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.os.Message;
import android.view.MotionEvent;

import org.mariotaku.gallery3d.ImageViewerGLActivity;
import org.mariotaku.gallery3d.util.BitmapPool;

public class PhotoView extends GLView {

	private static final int MSG_CANCEL_EXTRA_SCALING = 2;
	private static final int MSG_CAPTURE_ANIMATION_DONE = 4;

	private static final int HOLD_TOUCH_DOWN = 1;
	private static final int HOLD_CAPTURE_ANIMATION = 2;

	private final GestureListener mGestureListener;

	private final GestureRecognizer mGestureRecognizer;
	private final PositionController mPositionController;

	private Listener mListener;
	private ITileImageAdapter mModel;

	private final TileImageView mTileView;
	private final EdgeView mEdgeView;
	private final SynchronizedHandler mHandler;
	private boolean mCancelExtraScalingPending;
	private boolean mWantPictureCenterCallbacks = false;

	private int mDisplayRotation = 0;

	private int mCompensation = 0;
	// This variable prevents us doing snapback until its values goes to 0. This
	// happens if the user gesture is still in progress or we are in a capture
	// animation.
	private int mHolding;

	// This is the index of the last deleted item. This is only used as a hint
	// to hide the undo button when we are too far away from the deleted
	// item. The value Integer.MAX_VALUE means there is no such hint.
	private final Context mContext;
	private final FullPicture mPicture;

	public PhotoView(final ImageViewerGLActivity activity) {
		mTileView = new TileImageView(activity);
		addComponent(mTileView);
		mContext = activity;
		mEdgeView = new EdgeView(mContext);
		addComponent(mEdgeView);
		mHandler = new MyHandler(activity);
		mPicture = new FullPicture();
		mGestureListener = new GestureListener();
		mGestureRecognizer = new GestureRecognizer(mContext, mGestureListener);
		mPositionController = new PositionController(new EventListener());
	}

	public Rect getPhotoRect() {
		return mPositionController.getPosition();
	}

	public void notifyImageChange() {
		mListener.onCurrentImageUpdated();
		mPicture.reload();
		setPictureSize();
		invalidate();
	}

	public void pause() {
		mPositionController.skipAnimation();
		mTileView.freeTextures();
		for (int i = -0; i <= 0; i++) {
			mPicture.setScreenNail(null);
		}
	}

	public void resume() {
		mTileView.prepareTextures();
		mPositionController.skipToFinalPosition();
	}

	public void setListener(final Listener listener) {
		mListener = listener;
	}

	public void setModel(final ITileImageAdapter model) {
		mModel = model;
		mTileView.setModel(mModel);
	}

	public void setOpenAnimationRect(final Rect rect) {
		mPositionController.setOpenAnimationRect(rect);
	}

	public void setWantPictureCenterCallbacks(final boolean wanted) {
		mWantPictureCenterCallbacks = wanted;
	}

	@Override
	protected void onLayout(final boolean changeSize, final int left, final int top, final int right, final int bottom) {
		final int w = right - left;
		final int h = bottom - top;
		mTileView.layout(0, 0, w, h);
		mEdgeView.layout(0, 0, w, h);

		final GLRoot root = getGLRoot();
		final int displayRotation = root.getDisplayRotation();
		final int compensation = root.getCompensation();
		if (mDisplayRotation != displayRotation || mCompensation != compensation) {
			mDisplayRotation = displayRotation;
			mCompensation = compensation;
		}

		if (changeSize) {
			mPositionController.setViewSize(getWidth(), getHeight());
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Pictures
	// //////////////////////////////////////////////////////////////////////////

	@Override
	protected boolean onTouch(final MotionEvent event) {
		mGestureRecognizer.onTouchEvent(event);
		return true;
	}

	@Override
	protected void render(final GLCanvas canvas) {

		// Draw photos from back to front
		final Rect r = mPositionController.getPosition();
		mPicture.draw(canvas, r);

		renderChild(canvas, mEdgeView);

		mPositionController.advanceAnimation();
	}

	private void captureAnimationDone(final int offset) {
		mHolding &= ~HOLD_CAPTURE_ANIMATION;
		if (offset == 1) {
			// Now the capture animation is done, enable the action bar.
			mListener.onActionBarAllowed(true);
			mListener.onActionBarWanted();
		}
		snapback();
	}

	private void setPictureSize() {
		mPositionController.setImageSize(mPicture.getSize(), null);
	}

	private void snapback() {
		mPositionController.snapback();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Film mode focus switching
	// //////////////////////////////////////////////////////////////////////////

	private static int getRotated(final int degree, final int original, final int theother) {
		return degree % 180 == 0 ? original : theother;
	}

	public interface ITileImageAdapter {

		public int getImageHeight();

		// Returns the rotation for the specified picture.
		public int getImageRotation();

		public int getImageWidth();

		public int getLevelCount();

		public ScreenNail getScreenNail();

		/**
		 * The tile returned by this method can be specified this way: Assuming
		 * the image size is (width, height), first take the intersection of (0,
		 * 0) - (width, height) and (x, y) - (x + tileSize, y + tileSize). Then
		 * extend this intersection region by borderSize pixels on each side. If
		 * in extending the region, we found some part of the region are outside
		 * the image, those pixels are filled with black.<br>
		 * <br>
		 * If level > 0, it does the same operation on a down-scaled version of
		 * the original image (down-scaled by a factor of 2^level), but (x, y)
		 * still refers to the coordinate on the original image.<br>
		 * <br>
		 * The method would be called in another thread.
		 */
		public Bitmap getTile(int level, int x, int y, int tileSize, int borderSize, BitmapPool pool);

		public void recycleScreenNail();

		public boolean setData(BitmapRegionDecoder decoder, Bitmap bitmap, int orientation);

	}

	public interface Listener {
		public void onActionBarAllowed(boolean allowed);

		public void onActionBarWanted();

		public void onCurrentImageUpdated();

		public void onPictureCenter();

		public void onSingleTapUp(int x, int y);
	}

	public static class Size {
		public int width;
		public int height;
	}

	private class GestureListener implements GestureRecognizer.Listener {
		private boolean mIgnoreUpEvent = false;
		// If we can change mode for this scale gesture.
		private boolean mCanChangeMode;
		// If we have changed the film mode in this scaling gesture.
		private boolean mModeChanged;
		// If a scrolling has happened after a down gesture.
		private boolean mScrolledAfterDown;
		// The accumulated scaling change from a scaling gesture.
		private float mAccScale;

		@Override
		public boolean onDoubleTap(final float x, final float y) {
			final PositionController controller = mPositionController;
			final float scale = controller.getImageScale();
			// onDoubleTap happened on the second ACTION_DOWN.
			// We need to ignore the next UP event.
			mIgnoreUpEvent = true;
			if (scale <= .75f || controller.isAtMinimalScale()) {
				controller.zoomIn(x, y, Math.max(1.0f, scale * 1.5f));
			} else {
				controller.resetToFullView();
			}
			return true;
		}

		@Override
		public void onDown(final float x, final float y) {

			mModeChanged = false;

			mHolding |= HOLD_TOUCH_DOWN;

			mScrolledAfterDown = false;
		}

		@Override
		public boolean onFling(final float velocityX, final float velocityY) {
			if (mModeChanged) return true;
			flingImages(velocityX, velocityY);
			return true;
		}

		@Override
		public boolean onScale(final float focusX, final float focusY, final float scale) {
			if (mModeChanged) return true;
			if (Float.isNaN(scale) || Float.isInfinite(scale)) return false;

			final int outOfRange = mPositionController.scaleBy(scale, focusX, focusY);

			// We wait for a large enough scale change before changing mode.
			// Otherwise we may mistakenly treat a zoom-in gesture as zoom-out
			// or vice versa.
			mAccScale *= scale;
			final boolean largeEnough = mAccScale < 0.97f || mAccScale > 1.03f;

			// If mode changes, we treat this scaling gesture has ended.
			if (mCanChangeMode && largeEnough) {
				if (outOfRange < 0 || outOfRange > 0) {
					stopExtraScalingIfNeeded();

					// Removing the touch down flag allows snapback to happen
					// for film mode change.
					mHolding &= ~HOLD_TOUCH_DOWN;

					// We need to call onScaleEnd() before setting mModeChanged
					// to true.
					onScaleEnd();
					mModeChanged = true;
					return true;
				}
			}

			if (outOfRange != 0) {
				startExtraScalingIfNeeded();
			} else {
				stopExtraScalingIfNeeded();
			}
			return true;
		}

		@Override
		public boolean onScaleBegin(final float focusX, final float focusY) {
			// We ignore the scaling gesture if it is a camera preview.
			mPositionController.beginScale(focusX, focusY);
			// We can change mode if we are in film mode, or we are in page
			// mode and at minimal scale.
			mCanChangeMode = mPositionController.isAtMinimalScale();
			mAccScale = 1f;
			return true;
		}

		@Override
		public void onScaleEnd() {
			if (mModeChanged) return;
			mPositionController.endScale();
		}

		@Override
		public boolean onScroll(final float dx, final float dy, final float totalX, final float totalY) {
			if (!mScrolledAfterDown) {
				mScrolledAfterDown = true;
			}

			final int dxi = (int) (-dx + 0.5f);
			final int dyi = (int) (-dy + 0.5f);
			mPositionController.scrollPage(dxi, dyi);
			return true;
		}

		@Override
		public boolean onSingleTapUp(final float x, final float y) {
			// On crespo running Android 2.3.6 (gingerbread), a pinch out
			// gesture results in the
			// following call sequence: onDown(), onUp() and then
			// onSingleTapUp(). The correct
			// sequence for a single-tap-up gesture should be: onDown(),
			// onSingleTapUp() and onUp().
			// The call sequence for a pinch out gesture in JB is: onDown(),
			// then onUp() and there's
			// no onSingleTapUp(). Base on these observations, the following
			// condition is added to
			// filter out the false alarm where onSingleTapUp() is called within
			// a pinch out
			// gesture. The framework fix went into ICS. Refer to b/4588114.
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				if ((mHolding & HOLD_TOUCH_DOWN) == 0) return true;
			}

			// We do this in addition to onUp() because we want the snapback of
			// setFilmMode to happen.
			mHolding &= ~HOLD_TOUCH_DOWN;

			if (mListener != null) {
				// Do the inverse transform of the touch coordinates.
				final Matrix m = getGLRoot().getCompensationMatrix();
				final Matrix inv = new Matrix();
				m.invert(inv);
				final float[] pts = new float[] { x, y };
				inv.mapPoints(pts);
				mListener.onSingleTapUp((int) (pts[0] + 0.5f), (int) (pts[1] + 0.5f));
			}
			return true;
		}

		@Override
		public void onUp() {
			mHolding &= ~HOLD_TOUCH_DOWN;
			mEdgeView.onRelease();

			if (mIgnoreUpEvent) {
				mIgnoreUpEvent = false;
				return;
			}

			snapback();
		}

		private boolean flingImages(final float velocityX, final float velocityY) {
			final int vx = (int) (velocityX + 0.5f);
			final int vy = (int) (velocityY + 0.5f);
			return mPositionController.flingPage(vx, vy);
		}

		private void startExtraScalingIfNeeded() {
			if (!mCancelExtraScalingPending) {
				mHandler.sendEmptyMessageDelayed(MSG_CANCEL_EXTRA_SCALING, 700);
				mPositionController.setExtraScalingRange(true);
				mCancelExtraScalingPending = true;
			}
		}

		private void stopExtraScalingIfNeeded() {
			if (mCancelExtraScalingPending) {
				mHandler.removeMessages(MSG_CANCEL_EXTRA_SCALING);
				mPositionController.setExtraScalingRange(false);
				mCancelExtraScalingPending = false;
			}
		}
	}

	private class MyHandler extends SynchronizedHandler {

		private MyHandler(final ImageViewerGLActivity activity) {
			super(activity.getGLRoot());
		}

		@Override
		public void handleMessage(final Message message) {
			switch (message.what) {
				case MSG_CANCEL_EXTRA_SCALING: {
					mGestureRecognizer.cancelScale();
					mPositionController.setExtraScalingRange(false);
					mCancelExtraScalingPending = false;
					break;
				}
				case MSG_CAPTURE_ANIMATION_DONE: {
					// message.arg1 is the offset parameter passed to
					// switchWithCaptureAnimation().
					captureAnimationDone(message.arg1);
					break;
				}
				default:
					throw new AssertionError(message.what);
			}
		}
	}

	private interface Picture {
		void draw(GLCanvas canvas, Rect r);

		void forceSize(); // called when mCompensation changes

		Size getSize();

		void reload();

		void setScreenNail(ScreenNail s);
	}

	class EventListener implements PositionController.Listener {

		@Override
		public boolean isHoldingDown() {
			return (mHolding & HOLD_TOUCH_DOWN) != 0;
		}

		@Override
		public void onAbsorb(final int velocity, final int direction) {
			mEdgeView.onAbsorb(velocity, direction);
		}

		@Override
		public void onInvalidate() {
			invalidate();
		}

		@Override
		public void onPull(final int offset, final int direction) {
			mEdgeView.onPull(offset, direction);
		}
	}

	class FullPicture implements Picture {
		private int mRotation;
		private final Size mSize = new Size();

		@Override
		public void draw(final GLCanvas canvas, final Rect r) {
			drawTileView(canvas, r);

			// We want to have the following transitions:
			// (1) Move camera preview out of its place: switch to film mode
			// (2) Move camera preview into its place: switch to page mode
			// The extra mWasCenter check makes sure (1) does not apply if in
			// page mode, we move _to_ the camera preview from another picture.

			// Holdings except touch-down prevent the transitions.
			if ((mHolding & ~HOLD_TOUCH_DOWN) != 0) return;

			if (mWantPictureCenterCallbacks && mPositionController.isCenter()) {
				mListener.onPictureCenter();
			}
		}

		@Override
		public void forceSize() {
			updateSize();
			mPositionController.forceImageSize(mSize);
		}

		@Override
		public Size getSize() {
			return mSize;
		}

		@Override
		public void reload() {
			// mImageWidth and mImageHeight will get updated
			mTileView.notifyModelInvalidated();
			setScreenNail(mModel.getScreenNail());
			updateSize();
		}

		@Override
		public void setScreenNail(final ScreenNail s) {
			mTileView.setScreenNail(s);
		}

		private void drawTileView(final GLCanvas canvas, final Rect r) {
			final float imageScale = mPositionController.getImageScale();
			final int viewW = getWidth();
			final int viewH = getHeight();
			final float cx = r.exactCenterX();
			final float cy = r.exactCenterY();

			canvas.save(GLCanvas.SAVE_FLAG_MATRIX | GLCanvas.SAVE_FLAG_ALPHA);
			// Draw the tile view.
			setTileViewPosition(cx, cy, viewW, viewH, imageScale);
			renderChild(canvas, mTileView);

			// Draw the play video icon and the message.
			canvas.translate((int) (cx + 0.5f), (int) (cy + 0.5f));

			canvas.restore();
		}

		// Set the position of the tile view
		private void setTileViewPosition(final float cx, final float cy, final int viewW, final int viewH,
				final float scale) {
			// Find out the bitmap coordinates of the center of the view
			final int imageW = mPositionController.getImageWidth();
			final int imageH = mPositionController.getImageHeight();
			final int centerX = (int) (imageW / 2f + (viewW / 2f - cx) / scale + 0.5f);
			final int centerY = (int) (imageH / 2f + (viewH / 2f - cy) / scale + 0.5f);

			final int inverseX = imageW - centerX;
			final int inverseY = imageH - centerY;
			int x, y;
			switch (mRotation) {
				case 0:
					x = centerX;
					y = centerY;
					break;
				case 90:
					x = centerY;
					y = inverseX;
					break;
				case 180:
					x = inverseX;
					y = inverseY;
					break;
				case 270:
					x = inverseY;
					y = centerX;
					break;
				default:
					throw new RuntimeException(String.valueOf(mRotation));
			}
			mTileView.setPosition(x, y, scale, mRotation);
		}

		private void updateSize() {
			mRotation = mModel.getImageRotation();

			final int w = mTileView.mImageWidth;
			final int h = mTileView.mImageHeight;
			mSize.width = getRotated(mRotation, w, h);
			mSize.height = getRotated(mRotation, h, w);
		}
	}
}

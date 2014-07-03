/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.graphics.Rect;

import org.mariotaku.gallery3d.ui.PhotoView.Size;
import org.mariotaku.gallery3d.util.GalleryUtils;

class PositionController {

	public static final int IMAGE_AT_LEFT_EDGE = 1;
	public static final int IMAGE_AT_RIGHT_EDGE = 2;
	public static final int IMAGE_AT_TOP_EDGE = 4;
	public static final int IMAGE_AT_BOTTOM_EDGE = 8;

	public static final int CAPTURE_ANIMATION_TIME = 700;
	public static final int SNAPBACK_ANIMATION_TIME = 600;

	// Special values for animation time.
	private static final long NO_ANIMATION = -1;
	private static final long LAST_ANIMATION = -2;

	private static final int ANIM_KIND_NONE = -1;
	private static final int ANIM_KIND_SCROLL = 0;
	private static final int ANIM_KIND_SCALE = 1;
	private static final int ANIM_KIND_SNAPBACK = 2;
	private static final int ANIM_KIND_SLIDE = 3;
	private static final int ANIM_KIND_ZOOM = 4;
	private static final int ANIM_KIND_OPENING = 5;
	private static final int ANIM_KIND_FLING = 6;

	// Animation time in milliseconds. The order must match ANIM_KIND_* above.
	//
	// The values for ANIM_KIND_FLING_X does't matter because we use
	// mFilmScroller.isFinished() to decide when to stop. We set it to 0 so it's
	// faster for Animatable.advanceAnimation() to calculate the progress
	// (always 1).
	private static final int ANIM_TIME[] = { 0, // ANIM_KIND_SCROLL
			0, // ANIM_KIND_SCALE
			SNAPBACK_ANIMATION_TIME, // ANIM_KIND_SNAPBACK
			400, // ANIM_KIND_SLIDE
			300, // ANIM_KIND_ZOOM
			300, // ANIM_KIND_OPENING
			0, // ANIM_KIND_FLING (the duration is calculated dynamically)
			0, // ANIM_KIND_FLING_X (see the comment above)
			0, // ANIM_KIND_DELETE (the duration is calculated dynamically)
			CAPTURE_ANIMATION_TIME, // ANIM_KIND_CAPTURE
	};

	// We try to scale up the image to fill the screen. But in order not to
	// scale too much for small icons, we limit the max up-scaling factor here.
	private static final float SCALE_LIMIT = 4;

	// For user's gestures, we give a temporary extra scaling range which goes
	// above or below the usual scaling limits.
	private static final float SCALE_MIN_EXTRA = 0.7f;
	private static final float SCALE_MAX_EXTRA = 1.4f;

	// Setting this true makes the extra scaling range permanent (until this is
	// set to false again).
	private boolean mExtraScalingRange = false;

	private static final int HORIZONTAL_SLACK = GalleryUtils.dpToPixel(12);

	private final Listener mListener;
	private volatile Rect mOpenAnimationRect;

	// Use a large enough value, so we won't see the gray shadow in the
	// beginning.
	private int mViewW = 1200;
	private int mViewH = 1200;

	// A scaling gesture is in progress.
	private boolean mInScale;
	// The focus point of the scaling gesture, relative to the center of the
	// picture in bitmap pixels.
	private float mFocusX, mFocusY;

	// This is used by the fling animation (page mode).
	private final FlingScroller mPageScroller;

	// The bound of the stable region that the focused box can stay, see the
	// comments above calculateStableBound() for details.
	private int mBoundLeft, mBoundRight, mBoundTop, mBoundBottom;

	// Constrained frame is a rectangle that the focused box should fit into if
	// it is constrained. It has two effects:
	//
	// (1) In page mode, if the focused box is constrained, scaling for the
	// focused box is adjusted to fit into the constrained frame, instead of the
	// whole view.
	//
	// (2) In page mode, if the focused box is constrained, the mPlatform's
	// default center (mDefaultX/Y) is moved to the center of the constrained
	// frame, instead of the view center.
	//
	private final Rect mConstrainedFrame = new Rect();

	// Whether the focused box is constrained.
	//
	// Our current program's first call to moveBox() sets constrained = true, so
	// we set the initial value of this variable to true, and we will not see
	// see unwanted transition animation.
	private final boolean mConstrained = true;

	private final Platform mPlatform = new Platform();
	private final Box mBox;

	// The output of the PositionController. Available through getPosition().
	private final Rect mRect;

	public PositionController(final Listener listener) {
		mListener = listener;
		mPageScroller = new FlingScroller();

		// Initialize the areas.
		initPlatform();
		mBox = new Box();
		initBox();
		mRect = new Rect();
	}

	public void advanceAnimation() {
		boolean changed = false;
		changed |= mPlatform.advanceAnimation();
		changed |= mBox.advanceAnimation();
		if (changed) {
			redraw();
		}
	}

	public void beginScale(float focusX, float focusY) {
		focusX -= mViewW / 2;
		focusY -= mViewH / 2;
		final Platform p = mPlatform;
		mInScale = true;
		mFocusX = (int) ((focusX - p.mCurrentX) / mBox.mCurrentScale + 0.5f);
		mFocusY = (int) ((focusY - mBox.mCurrentY) / mBox.mCurrentScale + 0.5f);
	}

	public void endScale() {
		mInScale = false;
		snapAndRedraw();
	}

	public boolean flingPage(int velocityX, int velocityY) {

		final Platform p = mPlatform;

		// We only want to do fling when the picture is zoomed-in.
		if (viewWiderThanScaledImage(mBox.mCurrentScale) && viewTallerThanScaledImage(mBox.mCurrentScale))
			return false;

		// We only allow flinging in the directions where it won't go over the
		// picture.
		final int edges = getImageAtEdges();
		if (velocityX > 0 && (edges & IMAGE_AT_LEFT_EDGE) != 0 || velocityX < 0 && (edges & IMAGE_AT_RIGHT_EDGE) != 0) {
			velocityX = 0;
		}
		if (velocityY > 0 && (edges & IMAGE_AT_TOP_EDGE) != 0 || velocityY < 0 && (edges & IMAGE_AT_BOTTOM_EDGE) != 0) {
			velocityY = 0;
		}

		if (velocityX == 0 && velocityY == 0) return false;

		mPageScroller.fling(p.mCurrentX, mBox.mCurrentY, velocityX, velocityY, mBoundLeft, mBoundRight, mBoundTop,
				mBoundBottom);
		final int targetX = mPageScroller.getFinalX();
		final int targetY = mPageScroller.getFinalY();
		ANIM_TIME[ANIM_KIND_FLING] = mPageScroller.getDuration();
		return startAnimation(targetX, targetY, mBox.mCurrentScale, ANIM_KIND_FLING);
	}

	public void forceImageSize(final Size s) {
		if (s.width == 0 || s.height == 0) return;
		mBox.mImageW = s.width;
		mBox.mImageH = s.height;
		return;
	}

	public int getImageAtEdges() {

		final Platform p = mPlatform;
		calculateStableBound(mBox.mCurrentScale);
		int edges = 0;
		if (p.mCurrentX <= mBoundLeft) {
			edges |= IMAGE_AT_RIGHT_EDGE;
		}
		if (p.mCurrentX >= mBoundRight) {
			edges |= IMAGE_AT_LEFT_EDGE;
		}
		if (mBox.mCurrentY <= mBoundTop) {
			edges |= IMAGE_AT_BOTTOM_EDGE;
		}
		if (mBox.mCurrentY >= mBoundBottom) {
			edges |= IMAGE_AT_TOP_EDGE;
		}
		return edges;
	}

	public int getImageHeight() {

		return mBox.mImageH;
	}

	public float getImageScale() {

		return mBox.mCurrentScale;
	}

	public int getImageWidth() {

		return mBox.mImageW;
	}

	// Returns the position of a box.
	public Rect getPosition() {
		return mRect;
	}

	// Returns the index of the box which contains the given point (x, y)
	// Returns Integer.MAX_VALUE if there is no hit. There may be more than
	// one box contains the given point, and we want to give priority to the
	// one closer to the focused index (0).
	public int hitTest(final int x, final int y) {
		final Rect r = mRect;
		if (r.contains(x, y)) return 0;

		return Integer.MAX_VALUE;
	}

	public boolean isAtMinimalScale() {

		return isAlmostEqual(mBox.mCurrentScale, mBox.mScaleMin);
	}

	// //////////////////////////////////////////////////////////////////////////
	// Start an animations for the focused box
	// //////////////////////////////////////////////////////////////////////////

	public boolean isCenter() {

		return mPlatform.mCurrentX == mPlatform.mDefaultX && mBox.mCurrentY == 0;
	}

	public boolean isScrolling() {
		return mPlatform.mAnimationStartTime != NO_ANIMATION && mPlatform.mCurrentX != mPlatform.mToX;
	}

	public void resetToFullView() {

		startAnimation(mPlatform.mDefaultX, 0, mBox.mScaleMin, ANIM_KIND_ZOOM);
	}

	// Scales the image by the given factor.
	// Returns an out-of-range indicator:
	// 1 if the intended scale is too large for the stable range.
	// 0 if the intended scale is in the stable range.
	// -1 if the intended scale is too small for the stable range.
	public int scaleBy(float s, float focusX, float focusY) {
		focusX -= mViewW / 2;
		focusY -= mViewH / 2;

		// We want to keep the focus point (on the bitmap) the same as when we
		// begin the scale gesture, that is,
		//
		// (focusX' - currentX') / scale' = (focusX - currentX) / scale
		//
		s = mBox.clampScale(s * getTargetScale(mBox));
		final int x = (int) (focusX - s * mFocusX + 0.5f);
		final int y = (int) (focusY - s * mFocusY + 0.5f);
		startAnimation(x, y, s, ANIM_KIND_SCALE);
		if (s < mBox.mScaleMin) return -1;
		if (s > mBox.mScaleMax) return 1;
		return 0;
	}

	public void scrollPage(final int dx, final int dy) {
		if (!canScroll()) return;

		calculateStableBound(mBox.mCurrentScale);

		int x = mPlatform.mCurrentX + dx;
		int y = mBox.mCurrentY + dy;

		// Vertical direction: If we have space to move in the vertical
		// direction, we show the edge effect when scrolling reaches the edge.
		if (mBoundTop != mBoundBottom) {
			if (y < mBoundTop) {
				mListener.onPull(mBoundTop - y, EdgeView.BOTTOM);
			} else if (y > mBoundBottom) {
				mListener.onPull(y - mBoundBottom, EdgeView.TOP);
			}
		}

		y = GalleryUtils.clamp(y, mBoundTop, mBoundBottom);

		// Horizontal direction: we show the edge effect when the scrolling
		// tries to go left of the first image or go right of the last image.
		if (mBoundLeft != mBoundRight) {
			if (x > mBoundRight) {
				mListener.onPull(x - mBoundRight, EdgeView.LEFT);
			} else if (x < mBoundLeft) {
				mListener.onPull(mBoundLeft - x, EdgeView.RIGHT);
			}
		}

		x = GalleryUtils.clamp(x, mBoundLeft, mBoundRight);

		startAnimation(x, y, mBox.mCurrentScale, ANIM_KIND_SCROLL);
	}

	public void setConstrainedFrame(final Rect cFrame) {
		if (mConstrainedFrame.equals(cFrame)) return;
		mConstrainedFrame.set(cFrame);
		mPlatform.updateDefaultXY();
		updateScaleAndGapLimit();
		snapAndRedraw();
	}

	public void setExtraScalingRange(final boolean enabled) {
		if (mExtraScalingRange == enabled) return;
		mExtraScalingRange = enabled;
		if (!enabled) {
			snapAndRedraw();
		}
	}

	public void setFilmMode(final boolean enabled) {
		mPlatform.updateDefaultXY();
		updateScaleAndGapLimit();
		stopAnimation();
		snapAndRedraw();
	}

	public void setImageSize(final Size s, final Rect cFrame) {
		if (s.width == 0 || s.height == 0) return;

		boolean needUpdate = false;
		if (cFrame != null && !mConstrainedFrame.equals(cFrame)) {
			mConstrainedFrame.set(cFrame);
			mPlatform.updateDefaultXY();
			needUpdate = true;
		}
		needUpdate |= setBoxSize(s.width, s.height, false);

		if (!needUpdate) return;
		updateScaleAndGapLimit();
		snapAndRedraw();
	}

	public void setOpenAnimationRect(final Rect r) {
		mOpenAnimationRect = r;
	}

	public void setViewSize(final int viewW, final int viewH) {
		if (viewW == mViewW && viewH == mViewH) return;

		final boolean wasMinimal = isAtMinimalScale();

		mViewW = viewW;
		mViewH = viewH;
		initPlatform();

		setBoxSize(viewW, viewH, true);

		updateScaleAndGapLimit();

		// If the focused box was at minimal scale, we try to make it the
		// minimal scale under the new view size.
		if (wasMinimal) {

			mBox.mCurrentScale = mBox.mScaleMin;
		}

		// If we have the opening animation, do it. Otherwise go directly to the
		// right position.
		if (!startOpeningAnimationIfNeeded()) {
			skipToFinalPosition();
		}
	}

	public void skipAnimation() {
		if (mPlatform.mAnimationStartTime != NO_ANIMATION) {
			mPlatform.mCurrentX = mPlatform.mToX;
			mPlatform.mCurrentY = mPlatform.mToY;
			mPlatform.mAnimationStartTime = NO_ANIMATION;
		}

		if (mBox.mAnimationStartTime != NO_ANIMATION) {
			mBox.mCurrentY = mBox.mToY;
			mBox.mCurrentScale = mBox.mToScale;
			mBox.mAnimationStartTime = NO_ANIMATION;
		}

		redraw();
	}

	public void skipToFinalPosition() {
		stopAnimation();
		snapAndRedraw();
		skipAnimation();
	}

	public void snapback() {
		snapAndRedraw();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Layout
	// //////////////////////////////////////////////////////////////////////////

	public void zoomIn(float tapX, float tapY, float targetScale) {
		tapX -= mViewW / 2;
		tapY -= mViewH / 2;

		// Convert the tap position to distance to center in bitmap coordinates
		final float tempX = (tapX - mPlatform.mCurrentX) / mBox.mCurrentScale;
		final float tempY = (tapY - mBox.mCurrentY) / mBox.mCurrentScale;

		final int x = (int) (-tempX * targetScale + 0.5f);
		final int y = (int) (-tempY * targetScale + 0.5f);

		calculateStableBound(targetScale);
		final int targetX = GalleryUtils.clamp(x, mBoundLeft, mBoundRight);
		final int targetY = GalleryUtils.clamp(y, mBoundTop, mBoundBottom);
		targetScale = GalleryUtils.clamp(targetScale, mBox.mScaleMin, mBox.mScaleMax);

		startAnimation(targetX, targetY, targetScale, ANIM_KIND_ZOOM);
	}

	private void calculateStableBound(final float scale) {
		calculateStableBound(scale, 0);
	}

	// Calculates the stable region of mPlatform.mCurrentX and
	// mBox.mCurrentY, where "stable" means
	//
	// (1) If the dimension of scaled image >= view dimension, we will not
	// see black region outside the image (at that dimension).
	// (2) If the dimension of scaled image < view dimension, we will center
	// the scaled image.
	//
	// We might temporarily go out of this stable during user interaction,
	// but will "snap back" after user stops interaction.
	//
	// The results are stored in mBound{Left/Right/Top/Bottom}.
	//
	// An extra parameter "horizontalSlack" (which has the value of 0 usually)
	// is used to extend the stable region by some pixels on each side
	// horizontally.
	private void calculateStableBound(final float scale, final int horizontalSlack) {

		// The width and height of the box in number of view pixels
		final int w = widthOf(mBox, scale);
		final int h = heightOf(mBox, scale);

		// When the edge of the view is aligned with the edge of the box
		mBoundLeft = (mViewW + 1) / 2 - (w + 1) / 2 - horizontalSlack;
		mBoundRight = w / 2 - mViewW / 2 + horizontalSlack;
		mBoundTop = (mViewH + 1) / 2 - (h + 1) / 2;
		mBoundBottom = h / 2 - mViewH / 2;

		// If the scaled height is smaller than the view height,
		// force it to be in the center.
		if (viewTallerThanScaledImage(scale)) {
			mBoundTop = mBoundBottom = 0;
		}

		// Same for width
		if (viewWiderThanScaledImage(scale)) {
			mBoundLeft = mBoundRight = mPlatform.mDefaultX;
		}
	}

	// Only allow scrolling when we are not currently in an animation or we
	// are in some animation with can be interrupted.
	private boolean canScroll() {

		if (mBox.mAnimationStartTime == NO_ANIMATION) return true;
		switch (mBox.mAnimationKind) {
			case ANIM_KIND_SCROLL:
			case ANIM_KIND_FLING:
				return true;
		}
		return false;
	}

	private void convertBoxToRect() {

		final Rect r = mRect;
		final int y = mBox.mCurrentY + mPlatform.mCurrentY + mViewH / 2;
		final int w = widthOf(mBox);
		final int h = heightOf(mBox);
		final int x = mPlatform.mCurrentX + mViewW / 2;
		r.left = x - w / 2;
		r.right = r.left + w;
		r.top = y - h / 2;
		r.bottom = r.top + h;
	}

	private float getMaximalScale(final Box b) {
		if (mConstrained && !mConstrainedFrame.isEmpty()) return getMinimalScale(b);
		return SCALE_LIMIT;
	}

	private float getMinimalScale(final Box b) {
		final float wFactor = 1.0f;
		final float hFactor = 1.0f;
		int viewW, viewH;

		if (mConstrained && !mConstrainedFrame.isEmpty() && b == mBox) {
			viewW = mConstrainedFrame.width();
			viewH = mConstrainedFrame.height();
		} else {
			viewW = mViewW;
			viewH = mViewH;
		}

		final float s = Math.min(wFactor * viewW / b.mImageW, hFactor * viewH / b.mImageH);
		return Math.min(SCALE_LIMIT, s);
	}

	private float getTargetScale(final Box b) {
		return b.mAnimationStartTime == NO_ANIMATION ? b.mCurrentScale : b.mToScale;
	}

	// Returns the display height of this box.
	private int heightOf(final Box b) {
		return (int) (b.mImageH * b.mCurrentScale + 0.5f);
	}

	// Returns the display height of this box, using the given scale.
	private int heightOf(final Box b, final float scale) {
		return (int) (b.mImageH * scale + 0.5f);
	}

	// //////////////////////////////////////////////////////////////////////////
	// Public utilities
	// //////////////////////////////////////////////////////////////////////////

	// Initialize a box to have the size of the view.
	private void initBox() {
		mBox.mImageW = mViewW;
		mBox.mImageH = mViewH;
		mBox.mUseViewSize = true;
		mBox.mScaleMin = getMinimalScale(mBox);
		mBox.mScaleMax = getMaximalScale(mBox);
		mBox.mCurrentY = 0;
		mBox.mCurrentScale = mBox.mScaleMin;
		mBox.mAnimationStartTime = NO_ANIMATION;
		mBox.mAnimationKind = ANIM_KIND_NONE;
	}

	// Initialize the platform to be at the view center.
	private void initPlatform() {
		mPlatform.updateDefaultXY();
		mPlatform.mCurrentX = mPlatform.mDefaultX;
		mPlatform.mCurrentY = mPlatform.mDefaultY;
		mPlatform.mAnimationStartTime = NO_ANIMATION;
	}

	// Convert the information in mPlatform and mBoxes to mRects, so the user
	// can get the position of each box by getPosition().
	//
	// Note we go from center-out because each box's X coordinate
	// is relative to its anchor box (except the focused box).
	private void layoutAndSetPosition() {
		convertBoxToRect();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Redraw
	//
	// If a method changes box positions directly, redraw()
	// should be called.
	//
	// If a method may also cause a snapback to happen, snapAndRedraw() should
	// be called.
	//
	// If a method starts an animation to change the position of focused box,
	// startAnimation() should be called.
	//
	// If time advances to change the box position, advanceAnimation() should
	// be called.
	// //////////////////////////////////////////////////////////////////////////
	private void redraw() {
		layoutAndSetPosition();
		mListener.onInvalidate();
	}

	// Returns false if the box size doesn't change.
	private boolean setBoxSize(final int width, final int height, final boolean isViewSize) {

		final boolean wasViewSize = mBox.mUseViewSize;

		// If we already have an image size, we don't want to use the view size.
		if (!wasViewSize && isViewSize) return false;

		mBox.mUseViewSize = isViewSize;

		if (width == mBox.mImageW && height == mBox.mImageH) return false;

		// The ratio of the old size and the new size.
		//
		// If the aspect ratio changes, we don't know if it is because one side
		// grows or the other side shrinks. Currently we just assume the view
		// angle of the longer side doesn't change (so the aspect ratio change
		// is because the view angle of the shorter side changes). This matches
		// what camera preview does.
		final float ratio = width > height ? (float) mBox.mImageW / width : (float) mBox.mImageH / height;

		mBox.mImageW = width;
		mBox.mImageH = height;

		mBox.mCurrentScale = getMinimalScale(mBox);
		mBox.mAnimationStartTime = NO_ANIMATION;

		mFocusX /= ratio;
		mFocusY /= ratio;

		return true;
	}

	private void snapAndRedraw() {
		mPlatform.startSnapback();
		mBox.startSnapback();
		redraw();
	}

	private boolean startAnimation(final int targetX, final int targetY, final float targetScale, final int kind) {
		boolean changed = false;
		changed |= mPlatform.doAnimation(targetX, mPlatform.mDefaultY, kind);
		changed |= mBox.doAnimation(targetY, targetScale, kind);
		if (changed) {
			redraw();
		}
		return changed;
	}

	private boolean startOpeningAnimationIfNeeded() {
		if (mOpenAnimationRect == null) return false;

		if (mBox.mUseViewSize) return false;

		// Start animation from the saved rectangle if we have one.
		final Rect r = mOpenAnimationRect;
		mOpenAnimationRect = null;

		mPlatform.mCurrentX = r.centerX() - mViewW / 2;
		mBox.mCurrentY = r.centerY() - mViewH / 2;
		mBox.mCurrentScale = Math.max(r.width() / (float) mBox.mImageW, r.height() / (float) mBox.mImageH);
		startAnimation(mPlatform.mDefaultX, 0, mBox.mScaleMin, ANIM_KIND_OPENING);

		return true;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private utilities
	// //////////////////////////////////////////////////////////////////////////

	// Stop all animations at where they are now.
	private void stopAnimation() {
		mPlatform.mAnimationStartTime = NO_ANIMATION;
		mBox.mAnimationStartTime = NO_ANIMATION;
	}

	// This should be called whenever the scale range of boxes or the default
	// gap size may change. Currently this can happen due to change of view
	// size, image size, mFilmMode, mConstrained, and mConstrainedFrame.
	private void updateScaleAndGapLimit() {

		mBox.mScaleMin = getMinimalScale(mBox);
		mBox.mScaleMax = getMaximalScale(mBox);

	}

	private boolean viewTallerThanScaledImage(final float scale) {
		return mViewH >= heightOf(mBox, scale);
	}

	private boolean viewWiderThanScaledImage(final float scale) {
		return mViewW >= widthOf(mBox, scale);
	}

	// Returns the display width of this box.
	private int widthOf(final Box b) {
		return (int) (b.mImageW * b.mCurrentScale + 0.5f);
	}

	// Returns the display width of this box, using the given scale.
	private int widthOf(final Box b, final float scale) {
		return (int) (b.mImageW * scale + 0.5f);
	}

	private static boolean isAlmostEqual(final float a, final float b) {
		final float diff = a - b;
		return (diff < 0 ? -diff : diff) < 0.02f;
	}

	public interface Listener {
		boolean isHoldingDown();

		void onAbsorb(int velocity, int direction);

		void onInvalidate();

		// EdgeView
		void onPull(int offset, int direction);

	}

	// //////////////////////////////////////////////////////////////////////////
	// Animatable: an thing which can do animation.
	// //////////////////////////////////////////////////////////////////////////
	private abstract static class Animatable {
		public long mAnimationStartTime;
		public int mAnimationKind;
		public int mAnimationDuration;

		// Returns true if the animation values changes, so things need to be
		// redrawn.
		public boolean advanceAnimation() {
			if (mAnimationStartTime == NO_ANIMATION) return false;
			if (mAnimationStartTime == LAST_ANIMATION) {
				mAnimationStartTime = NO_ANIMATION;
				return startSnapback();
			}

			float progress;
			if (mAnimationDuration == 0) {
				progress = 1;
			} else {
				final long now = AnimationTime.get();
				progress = (float) (now - mAnimationStartTime) / mAnimationDuration;
			}

			if (progress >= 1) {
				progress = 1;
			} else {
				progress = applyInterpolationCurve(mAnimationKind, progress);
			}

			final boolean done = interpolate(progress);

			if (done) {
				mAnimationStartTime = LAST_ANIMATION;
			}

			return true;
		}

		public abstract boolean startSnapback();

		// This should be overridden in subclass to change the animation values
		// give the progress value in [0, 1].
		protected abstract boolean interpolate(float progress);

		private static float applyInterpolationCurve(final int kind, float progress) {
			final float f = 1 - progress;
			switch (kind) {
				case ANIM_KIND_SCROLL:
				case ANIM_KIND_FLING:
					progress = 1 - f; // linear
					break;
				case ANIM_KIND_OPENING:
				case ANIM_KIND_SCALE:
					progress = 1 - f * f; // quadratic
					break;
				case ANIM_KIND_SNAPBACK:
				case ANIM_KIND_ZOOM:
				case ANIM_KIND_SLIDE:
					progress = 1 - f * f * f * f * f; // x^5
					break;
			}
			return progress;
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Box: represents a rectangular area which shows a picture.
	// //////////////////////////////////////////////////////////////////////////
	private class Box extends Animatable {
		// Size of the bitmap
		public int mImageW, mImageH;

		// This is true if we assume the image size is the same as view size
		// until we know the actual size of image. This is also used to
		// determine if there is an image ready to show.
		public boolean mUseViewSize;

		// The minimum and maximum scale we allow for this box.
		public float mScaleMin, mScaleMax;

		// The X/Y value indicates where the center of the box is on the view
		// coordinate. We always keep the mCurrent{X,Y,Scale} sync with the
		// actual values used currently. Note that the X values are implicitly
		// defined by Platform and Gaps.
		public int mCurrentY, mFromY, mToY;
		public float mCurrentScale, mFromScale, mToScale;

		// Clamps the input scale to the range that doAnimation() can reach.
		public float clampScale(final float s) {
			return GalleryUtils.clamp(s, SCALE_MIN_EXTRA * mScaleMin, SCALE_MAX_EXTRA * mScaleMax);
		}

		@Override
		public boolean startSnapback() {
			if (mAnimationStartTime != NO_ANIMATION) return false;
			if (mAnimationKind == ANIM_KIND_SCROLL && mListener.isHoldingDown()) return false;
			if (mInScale && this == mBox) return false;

			int y = mCurrentY;
			float scale;

			if (this == mBox) {
				final float scaleMin = mExtraScalingRange ? mScaleMin * SCALE_MIN_EXTRA : mScaleMin;
				final float scaleMax = mExtraScalingRange ? mScaleMax * SCALE_MAX_EXTRA : mScaleMax;
				scale = GalleryUtils.clamp(mCurrentScale, scaleMin, scaleMax);
				calculateStableBound(scale, HORIZONTAL_SLACK);
				// If the picture is zoomed-in, we want to keep the focus
				// point stay in the same position on screen. See the
				// comment in Platform.startSnapback for details.
				if (!viewTallerThanScaledImage(scale)) {
					final float scaleDiff = mCurrentScale - scale;
					y += (int) (mFocusY * scaleDiff + 0.5f);
				}
				y = GalleryUtils.clamp(y, mBoundTop, mBoundBottom);
			} else {
				y = 0;
				scale = mScaleMin;
			}

			if (mCurrentY != y || mCurrentScale != scale) return doAnimation(y, scale, ANIM_KIND_SNAPBACK);
			return false;
		}

		@Override
		protected boolean interpolate(final float progress) {
			if (mAnimationKind == ANIM_KIND_FLING)
				return interpolateFlingPage(progress);
			else
				return interpolateLinear(progress);
		}

		private boolean doAnimation(final int targetY, float targetScale, final int kind) {
			targetScale = clampScale(targetScale);

			if (mCurrentY == targetY && mCurrentScale == targetScale) return false;

			// Now starts an animation for the box.
			mAnimationKind = kind;
			mFromY = mCurrentY;
			mFromScale = mCurrentScale;
			mToY = targetY;
			mToScale = targetScale;
			mAnimationStartTime = AnimationTime.startTime();
			mAnimationDuration = ANIM_TIME[kind];
			advanceAnimation();
			return true;
		}

		private boolean interpolateFlingPage(final float progress) {
			mPageScroller.computeScrollOffset(progress);
			calculateStableBound(mCurrentScale);

			final int oldY = mCurrentY;
			mCurrentY = mPageScroller.getCurrY();

			// Check if we hit the edges; show edge effects if we do.
			if (oldY > mBoundTop && mCurrentY == mBoundTop) {
				final int v = (int) (-mPageScroller.getCurrVelocityY() + 0.5f);
				mListener.onAbsorb(v, EdgeView.BOTTOM);
			} else if (oldY < mBoundBottom && mCurrentY == mBoundBottom) {
				final int v = (int) (mPageScroller.getCurrVelocityY() + 0.5f);
				mListener.onAbsorb(v, EdgeView.TOP);
			}

			return progress >= 1;
		}

		private boolean interpolateLinear(final float progress) {
			if (progress >= 1) {
				mCurrentY = mToY;
				mCurrentScale = mToScale;
				return true;
			} else {
				mCurrentY = (int) (mFromY + progress * (mToY - mFromY));
				mCurrentScale = mFromScale + progress * (mToScale - mFromScale);
				return mCurrentY == mToY && mCurrentScale == mToScale;
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Platform: captures the global X/Y movement.
	// //////////////////////////////////////////////////////////////////////////
	private class Platform extends Animatable {
		public int mCurrentX, mFromX, mToX, mDefaultX;
		public int mCurrentY, mFromY, mToY, mDefaultY;

		@Override
		public boolean startSnapback() {
			if (mAnimationStartTime != NO_ANIMATION) return false;
			if (mAnimationKind == ANIM_KIND_SCROLL && mListener.isHoldingDown()) return false;
			if (mInScale) return false;

			final float scaleMin = mExtraScalingRange ? mBox.mScaleMin * SCALE_MIN_EXTRA : mBox.mScaleMin;
			final float scaleMax = mExtraScalingRange ? mBox.mScaleMax * SCALE_MAX_EXTRA : mBox.mScaleMax;
			final float scale = GalleryUtils.clamp(mBox.mCurrentScale, scaleMin, scaleMax);
			int x = mCurrentX;
			final int y = mDefaultY;
			calculateStableBound(scale, HORIZONTAL_SLACK);
			// If the picture is zoomed-in, we want to keep the focus point
			// stay in the same position on screen, so we need to adjust
			// target mCurrentX (which is the center of the focused
			// box). The position of the focus point on screen (relative the
			// the center of the view) is:
			//
			// mCurrentX + scale * mFocusX = mCurrentX' + scale' * mFocusX
			// => mCurrentX' = mCurrentX + (scale - scale') * mFocusX
			//
			if (!viewWiderThanScaledImage(scale)) {
				final float scaleDiff = mBox.mCurrentScale - scale;
				x += (int) (mFocusX * scaleDiff + 0.5f);
			}
			x = GalleryUtils.clamp(x, mBoundLeft, mBoundRight);
			if (mCurrentX != x || mCurrentY != y) return doAnimation(x, y, ANIM_KIND_SNAPBACK);
			return false;
		}

		// The updateDefaultXY() should be called whenever these variables
		// changes: (1) mConstrained (2) mConstrainedFrame (3) mViewW/H (4)
		// mFilmMode
		public void updateDefaultXY() {
			// We don't check mFilmMode and return 0 for mDefaultX. Because
			// otherwise if we decide to leave film mode because we are
			// centered, we will immediately back into film mode because we find
			// we are not centered.
			if (mConstrained && !mConstrainedFrame.isEmpty()) {
				mDefaultX = mConstrainedFrame.centerX() - mViewW / 2;
				mDefaultY = mConstrainedFrame.centerY() - mViewH / 2;
			} else {
				mDefaultX = 0;
				mDefaultY = 0;
			}
		}

		@Override
		protected boolean interpolate(final float progress) {
			if (mAnimationKind == ANIM_KIND_FLING)
				return interpolateFlingPage(progress);
			else
				return interpolateLinear(progress);
		}

		// Starts an animation for the platform.
		private boolean doAnimation(final int targetX, final int targetY, final int kind) {
			if (mCurrentX == targetX && mCurrentY == targetY) return false;
			mAnimationKind = kind;
			mFromX = mCurrentX;
			mFromY = mCurrentY;
			mToX = targetX;
			mToY = targetY;
			mAnimationStartTime = AnimationTime.startTime();
			mAnimationDuration = ANIM_TIME[kind];
			advanceAnimation();
			return true;
		}

		private boolean interpolateFlingPage(final float progress) {
			mPageScroller.computeScrollOffset(progress);

			calculateStableBound(mBox.mCurrentScale);

			final int oldX = mCurrentX;
			mCurrentX = mPageScroller.getCurrX();

			// Check if we hit the edges; show edge effects if we do.
			if (oldX > mBoundLeft && mCurrentX == mBoundLeft) {
				final int v = (int) (-mPageScroller.getCurrVelocityX() + 0.5f);
				mListener.onAbsorb(v, EdgeView.RIGHT);
			} else if (oldX < mBoundRight && mCurrentX == mBoundRight) {
				final int v = (int) (mPageScroller.getCurrVelocityX() + 0.5f);
				mListener.onAbsorb(v, EdgeView.LEFT);
			}

			return progress >= 1;
		}

		private boolean interpolateLinear(final float progress) {
			// Other animations
			if (progress >= 1) {
				mCurrentX = mToX;
				mCurrentY = mToY;
				return true;
			} else {
				mCurrentX = (int) (mFromX + progress * (mToX - mFromX));
				mCurrentY = (int) (mFromY + progress * (mToY - mFromY));
				return mCurrentX == mToX && mCurrentY == mToY;
			}
		}
	}
}

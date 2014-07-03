package it.sephiroth.android.library.imagezoom;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import it.sephiroth.android.library.imagezoom.ScaleGestureDetector.OnScaleGestureListener;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;

public class ImageViewTouch extends ImageViewTouchBase implements Constants {

	private static final float SCROLL_DELTA_THRESHOLD = 1.0f;
	static final float MIN_ZOOM = 0.9f;
	protected int mTouchSlop;
	protected float mCurrentScaleFactor;
	protected float mScaleFactor;
	protected int mDoubleTapDirection;
	protected GestureDetector mGestureDetector;
	protected OnGestureListener mGestureListener;
	protected OnScaleGestureListener mScaleListener;
	protected ScaleGestureDetector mScaleDetector;
	protected boolean mDoubleTapToZoomEnabled = true;
	protected boolean mScaleEnabled = true;
	protected boolean mScrollEnabled = true;

	private OnImageViewTouchDoubleTapListener doubleTapListener;

	public ImageViewTouch(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Determines whether this ImageViewTouch can be scrolled.
	 * 
	 * @param direction - positive direction value means scroll from right to
	 *            left, negative value means scroll from left to right
	 * @return true if there is some more place to scroll, false - otherwise.
	 */
	public boolean canScroll(final int direction) {
		final RectF bitmapRect = getBitmapRect();
		updateRect(bitmapRect, mScrollRect);
		final Rect imageViewRect = new Rect();
		getGlobalVisibleRect(imageViewRect);

		if (bitmapRect.right >= imageViewRect.right) {
			if (direction < 0) return Math.abs(bitmapRect.right - imageViewRect.right) > SCROLL_DELTA_THRESHOLD;
		}

		final double bitmapScrollRectDelta = Math.abs(bitmapRect.left - mScrollRect.left);
		return bitmapScrollRectDelta > SCROLL_DELTA_THRESHOLD;
	}

	public boolean getDoubleTapEnabled() {
		return mDoubleTapToZoomEnabled;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (mScaleDetector != null) {
			mScaleDetector.onTouchEvent(event);
		}
		if (mScaleDetector != null && !mScaleDetector.isInProgress()) {
			mGestureDetector.onTouchEvent(event);
		}
		final int action = event.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_UP:
				if (getScale() < 1f) {
					zoomTo(1f, 50);
				}
				break;
		}
		return true;
	}

	public void setDoubleTapListener(final OnImageViewTouchDoubleTapListener doubleTapListener) {
		this.doubleTapListener = doubleTapListener;
	}

	public void setDoubleTapToZoomEnabled(final boolean value) {
		mDoubleTapToZoomEnabled = value;
	}

	public void setScaleEnabled(final boolean value) {
		mScaleEnabled = value;
	}

	public void setScrollEnabled(final boolean value) {
		mScrollEnabled = value;
	}

	@Override
	protected void _setImageDrawable(final Drawable drawable, final boolean reset, final Matrix initial_matrix,
			final float maxZoom) {
		super._setImageDrawable(drawable, reset, initial_matrix, maxZoom);
		mScaleFactor = getMaxZoom() / 3;
	}

	protected OnGestureListener getGestureListener() {
		return new GestureListener();
	}

	protected OnScaleGestureListener getScaleListener() {
		return new ScaleListener();
	}

	@Override
	protected void init() {
		super.init();
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		mGestureListener = getGestureListener();
		mGestureDetector = new GestureDetector(getContext(), mGestureListener);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			mScaleListener = getScaleListener();
			mScaleDetector = new ScaleGestureDetector(getContext(), mScaleListener);
		}
		mCurrentScaleFactor = 1f;
		mDoubleTapDirection = 1;
	}

	@Override
	protected void onBitmapChanged(final Drawable drawable) {
		super.onBitmapChanged(drawable);

		final float v[] = new float[9];
		mSuppMatrix.getValues(v);
		mCurrentScaleFactor = v[Matrix.MSCALE_X];
	}

	protected float onDoubleTapPost(final float scale, final float maxZoom) {
		if (mDoubleTapDirection == 1) {
			if (scale + mScaleFactor * 2 <= maxZoom)
				return scale + mScaleFactor;
			else {
				mDoubleTapDirection = -1;
				return maxZoom;
			}
		} else {
			mDoubleTapDirection = 1;
			return 1f;
		}
	}

	@Override
	protected void onZoom(final float scale) {
		super.onZoom(scale);
		if (mScaleDetector != null && !mScaleDetector.isInProgress()) {
			mCurrentScaleFactor = scale;
		}
	}

	public class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDoubleTap(final MotionEvent e) {
			if (BuildConfig.DEBUG) {
				Log.i(LOG_TAG, "onDoubleTap. double tap enabled? " + mDoubleTapToZoomEnabled);
			}
			if (mDoubleTapToZoomEnabled) {
				final float scale = getScale();
				float targetScale = scale;
				targetScale = onDoubleTapPost(scale, getMaxZoom());
				targetScale = Math.min(getMaxZoom(), Math.max(targetScale, MIN_ZOOM));
				mCurrentScaleFactor = targetScale;
				zoomTo(targetScale, e.getX(), e.getY(), 200);
				invalidate();
			}

			if (null != doubleTapListener) {
				doubleTapListener.onDoubleTap();
			}

			return super.onDoubleTap(e);
		}

		@Override
		public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
			if (!mScrollEnabled) return false;

			// if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) return
			// false;
			if (mScaleDetector != null && mScaleDetector.isInProgress()) return false;

			final float diffX = e2.getX() - e1.getX();
			final float diffY = e2.getY() - e1.getY();

			if (Math.abs(velocityX) > 800 || Math.abs(velocityY) > 800) {
				scrollBy(diffX / 2, diffY / 2, 300);
				invalidate();
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public void onLongPress(final MotionEvent e) {
			if (isLongClickable()) {
				if (mScaleDetector != null && !mScaleDetector.isInProgress()) {
					setPressed(true);
					performLongClick();
				}
			}
		}

		@Override
		public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
			if (!mScrollEnabled) return false;
			if (e1 == null || e2 == null) return false;
			// if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) return
			// false;
			if (mScaleDetector != null && mScaleDetector.isInProgress()) return false;
			if (getScale() == 1f) return false;
			scrollBy(-distanceX, -distanceY);
			invalidate();
			return super.onScroll(e1, e2, distanceX, distanceY);
		}
	}

	public interface OnImageViewTouchDoubleTapListener {
		void onDoubleTap();
	}

	public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

		@Override
		public boolean onScale(final ScaleGestureDetector detector) {
			float targetScale = mCurrentScaleFactor * detector.getScaleFactor();
			if (mScaleEnabled) {
				targetScale = Math.min(getMaxZoom(), Math.max(targetScale, MIN_ZOOM));
				zoomTo(targetScale, detector.getFocusX(), detector.getFocusY());
				mCurrentScaleFactor = Math.min(getMaxZoom(), Math.max(targetScale, MIN_ZOOM));
				mDoubleTapDirection = 1;
				invalidate();
				return true;
			}
			return false;
		}
	}
}

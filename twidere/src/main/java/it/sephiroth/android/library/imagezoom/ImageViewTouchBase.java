package it.sephiroth.android.library.imagezoom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import it.sephiroth.android.library.imagezoom.easing.Cubic;
import it.sephiroth.android.library.imagezoom.easing.Easing;
import it.sephiroth.android.library.imagezoom.graphics.FastBitmapDrawable;
import it.sephiroth.android.library.imagezoom.utils.IDisposable;

/**
 * Base View to manage image zoom/scrool/pinch operations
 * 
 * @author alessandro
 */
public class ImageViewTouchBase extends ImageView implements IDisposable {

	public static final String LOG_TAG = "image";

	protected Easing mEasing = new Cubic();

	protected Matrix mBaseMatrix = new Matrix();
	protected Matrix mSuppMatrix = new Matrix();
	protected Handler mHandler = new Handler();
	protected Runnable mOnLayoutRunnable = null;
	protected float mMaxZoom;
	protected final Matrix mDisplayMatrix = new Matrix();
	protected final float[] mMatrixValues = new float[9];
	protected int mThisWidth = -1, mThisHeight = -1;
	protected boolean mFitToScreen = false;
	final protected float MAX_ZOOM = 2.0f;
	protected RectF mBitmapRect = new RectF();

	protected RectF mCenterRect = new RectF();
	protected RectF mScrollRect = new RectF();
	private OnBitmapChangedListener mListener;

	public ImageViewTouchBase(final Context context) {
		super(context);
		init();
	}

	public ImageViewTouchBase(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public void clear() {
		setImageBitmap(null, true);
	}

	@Override
	public void dispose() {
		clear();
	}

	/**
	 * Returns the current image display matrix. This matrix can be used in the
	 * next call to the {@link #setImageBitmap(Bitmap, boolean, Matrix)} to
	 * restore the same view state of the previous {@link Bitmap}
	 * 
	 * @return
	 */
	public Matrix getDisplayMatrix() {
		return new Matrix(mSuppMatrix);
	}

	public Matrix getImageViewMatrix() {
		mDisplayMatrix.set(mBaseMatrix);
		mDisplayMatrix.postConcat(mSuppMatrix);
		return mDisplayMatrix;
	}

	public float getMaxZoom() {
		return mMaxZoom;
	}

	@Override
	public float getRotation() {
		return 0;
	}

	public float getScale() {
		return getScale(mSuppMatrix);
	}

	public void scrollBy(final float x, final float y) {
		panBy(x, y);
	}

	public void setFitToScreen(final boolean value) {
		if (value != mFitToScreen) {
			mFitToScreen = value;
			requestLayout();
		}
	}

	@Override
	public void setImageBitmap(final Bitmap bm) {
		setImageBitmap(bm, true);
	}

	/**
	 * Set the new image to display and reset the internal matrix.
	 * 
	 * @param bitmap - the {@link Bitmap} to display
	 * @param reset - if true the image bounds will be recreated, otherwise the
	 *            old {@link Matrix} is used to display the new bitmap
	 * @see #setImageBitmap(Bitmap)
	 */
	public void setImageBitmap(final Bitmap bitmap, final boolean reset) {
		setImageBitmap(bitmap, reset, null);
	}

	/**
	 * Similar to {@link #setImageBitmap(Bitmap, boolean)} but an optional view
	 * {@link Matrix} can be passed to determine the new bitmap view matrix.<br />
	 * This method is useful if you need to restore a Bitmap with the same
	 * zoom/pan values from a previous state
	 * 
	 * @param bitmap - the {@link Bitmap} to display
	 * @param reset
	 * @param matrix - the {@link Matrix} to be used to display the new bitmap
	 * @see #setImageBitmap(Bitmap, boolean)
	 * @see #setImageBitmap(Bitmap)
	 * @see #getImageViewMatrix()
	 * @see #getDisplayMatrix()
	 */
	public void setImageBitmap(final Bitmap bitmap, final boolean reset, final Matrix matrix) {
		setImageBitmap(bitmap, reset, matrix, -1);
	}

	/**
	 * @param bitmap
	 * @param reset
	 * @param matrix
	 * @param maxZoom - maximum zoom allowd during zoom gestures
	 * @see #setImageBitmap(Bitmap, boolean, Matrix)
	 */
	public void setImageBitmap(final Bitmap bitmap, final boolean reset, final Matrix matrix, final float maxZoom) {

		Log.i(LOG_TAG, "setImageBitmap: " + bitmap);

		if (bitmap != null) {
			setImageDrawable(new FastBitmapDrawable(bitmap), reset, matrix, maxZoom);
		} else {
			setImageDrawable(null, reset, matrix, maxZoom);
		}
	}

	@Override
	public void setImageDrawable(final Drawable drawable) {
		setImageDrawable(drawable, true, null, -1);
	}

	public void setImageDrawable(final Drawable drawable, final boolean reset, final Matrix initial_matrix,
			final float maxZoom) {

		final int viewWidth = getWidth();

		if (viewWidth <= 0) {
			mOnLayoutRunnable = new Runnable() {

				@Override
				public void run() {
					setImageDrawable(drawable, reset, initial_matrix, maxZoom);
				}
			};
			return;
		}

		_setImageDrawable(drawable, reset, initial_matrix, maxZoom);
	}

	@Override
	public void setImageResource(final int resId) {
		setImageDrawable(getContext().getResources().getDrawable(resId));
	}

	public void setOnBitmapChangedListener(final OnBitmapChangedListener listener) {
		mListener = listener;
	}

	public void zoomTo(final float scale, final float durationMs) {
		final float cx = getWidth() / 2F;
		final float cy = getHeight() / 2F;
		zoomTo(scale, cx, cy, durationMs);
	}

	protected void _setImageDrawable(final Drawable drawable, final boolean reset, final Matrix initial_matrix,
			final float maxZoom) {

		if (drawable != null) {
			if (mFitToScreen) {
				getProperBaseMatrix2(drawable, mBaseMatrix);
			} else {
				getProperBaseMatrix(drawable, mBaseMatrix);
			}
			super.setImageDrawable(drawable);
		} else {
			mBaseMatrix.reset();
			super.setImageDrawable(null);
		}

		if (reset) {
			mSuppMatrix.reset();
			if (initial_matrix != null) {
				mSuppMatrix = new Matrix(initial_matrix);
			}
		}

		setImageMatrix(getImageViewMatrix());

		if (maxZoom < 1) {
			mMaxZoom = maxZoom();
		} else {
			mMaxZoom = maxZoom;
		}

		onBitmapChanged(drawable);
	}

	protected void center(final boolean horizontal, final boolean vertical) {
		// Log.i(LOG_TAG, "center");
		final Drawable drawable = getDrawable();

		if (drawable == null) return;
		final RectF rect = getCenter(horizontal, vertical);
		if (rect.left != 0 || rect.top != 0) {
			postTranslate(rect.left, rect.top);
		}
	}

	protected RectF getBitmapRect() {
		final Drawable drawable = getDrawable();

		if (drawable == null) return null;
		final Matrix m = getImageViewMatrix();
		mBitmapRect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		m.mapRect(mBitmapRect);
		return mBitmapRect;
	}

	protected RectF getCenter(final boolean horizontal, final boolean vertical) {
		final Drawable drawable = getDrawable();

		if (drawable == null) return new RectF(0, 0, 0, 0);

		final RectF rect = getBitmapRect();
		final float height = rect.height();
		final float width = rect.width();
		float deltaX = 0, deltaY = 0;
		if (vertical) {
			final int viewHeight = getHeight();
			if (height < viewHeight) {
				deltaY = (viewHeight - height) / 2 - rect.top;
			} else if (rect.top > 0) {
				deltaY = -rect.top;
			} else if (rect.bottom < viewHeight) {
				deltaY = getHeight() - rect.bottom;
			}
		}
		if (horizontal) {
			final int viewWidth = getWidth();
			if (width < viewWidth) {
				deltaX = (viewWidth - width) / 2 - rect.left;
			} else if (rect.left > 0) {
				deltaX = -rect.left;
			} else if (rect.right < viewWidth) {
				deltaX = viewWidth - rect.right;
			}
		}
		mCenterRect.set(deltaX, deltaY, 0, 0);
		return mCenterRect;
	}

	/**
	 * Setup the base matrix so that the image is centered and scaled properly.
	 * 
	 * @param bitmap
	 * @param matrix
	 */
	protected void getProperBaseMatrix(final Drawable drawable, final Matrix matrix) {
		final float viewWidth = getWidth();
		final float viewHeight = getHeight();
		final float w = drawable.getIntrinsicWidth();
		final float h = drawable.getIntrinsicHeight();
		matrix.reset();

		if (w > viewWidth || h > viewHeight) {
			final float widthScale = Math.min(viewWidth / w, 2.0f);
			final float heightScale = Math.min(viewHeight / h, 2.0f);
			final float scale = Math.min(widthScale, heightScale);
			matrix.postScale(scale, scale);
			final float tw = (viewWidth - w * scale) / 2.0f;
			final float th = (viewHeight - h * scale) / 2.0f;
			matrix.postTranslate(tw, th);
		} else {
			final float tw = (viewWidth - w) / 2.0f;
			final float th = (viewHeight - h) / 2.0f;
			matrix.postTranslate(tw, th);
		}
	}

	/**
	 * Setup the base matrix so that the image is centered and scaled properly.
	 * 
	 * @param bitmap
	 * @param matrix
	 */
	protected void getProperBaseMatrix2(final Drawable bitmap, final Matrix matrix) {
		final float viewWidth = getWidth();
		final float viewHeight = getHeight();
		final float w = bitmap.getIntrinsicWidth();
		final float h = bitmap.getIntrinsicHeight();
		matrix.reset();
		final float widthScale = Math.min(viewWidth / w, MAX_ZOOM);
		final float heightScale = Math.min(viewHeight / h, MAX_ZOOM);
		final float scale = Math.min(widthScale, heightScale);
		matrix.postScale(scale, scale);
		matrix.postTranslate((viewWidth - w * scale) / MAX_ZOOM, (viewHeight - h * scale) / MAX_ZOOM);
	}

	protected float getScale(final Matrix matrix) {
		return getValue(matrix, Matrix.MSCALE_X);
	}

	protected float getValue(final Matrix matrix, final int whichValue) {
		matrix.getValues(mMatrixValues);
		return mMatrixValues[whichValue];
	}

	protected void init() {
		setScaleType(ImageView.ScaleType.MATRIX);
	}

	protected float maxZoom() {
		final Drawable drawable = getDrawable();

		if (drawable == null) return 1F;

		final float fw = (float) drawable.getIntrinsicWidth() / (float) mThisWidth;
		final float fh = (float) drawable.getIntrinsicHeight() / (float) mThisHeight;
		final float max = Math.max(fw, fh) * 4;
		return max;
	}

	protected void onBitmapChanged(final Drawable bitmap) {
		if (mListener != null) {
			mListener.onBitmapChanged(bitmap);
		}
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mThisWidth = right - left;
		mThisHeight = bottom - top;
		final Runnable r = mOnLayoutRunnable;
		if (r != null) {
			mOnLayoutRunnable = null;
			r.run();
		}
		if (getDrawable() != null) {
			if (mFitToScreen) {
				getProperBaseMatrix2(getDrawable(), mBaseMatrix);
			} else {
				getProperBaseMatrix(getDrawable(), mBaseMatrix);
			}
			setImageMatrix(getImageViewMatrix());
		}
	}

	protected void onZoom(final float scale) {
	}

	protected void panBy(final double dx, final double dy) {
		final RectF rect = getBitmapRect();
		mScrollRect.set((float) dx, (float) dy, 0, 0);
		updateRect(rect, mScrollRect);
		postTranslate(mScrollRect.left, mScrollRect.top);
		center(true, true);
	}

	protected void postScale(final float scale, final float centerX, final float centerY) {
		mSuppMatrix.postScale(scale, scale, centerX, centerY);
		setImageMatrix(getImageViewMatrix());
	}

	protected void postTranslate(final float deltaX, final float deltaY) {
		mSuppMatrix.postTranslate(deltaX, deltaY);
		setImageMatrix(getImageViewMatrix());
	}

	protected void scrollBy(final float distanceX, final float distanceY, final double durationMs) {
		final double dx = distanceX;
		final double dy = distanceY;
		final long startTime = System.currentTimeMillis();
		mHandler.post(new Runnable() {

			double old_x = 0;
			double old_y = 0;

			@Override
			public void run() {
				final long now = System.currentTimeMillis();
				final double currentMs = Math.min(durationMs, now - startTime);
				final double x = mEasing.easeOut(currentMs, 0, dx, durationMs);
				final double y = mEasing.easeOut(currentMs, 0, dy, durationMs);
				panBy(x - old_x, y - old_y);
				old_x = x;
				old_y = y;
				if (currentMs < durationMs) {
					mHandler.post(this);
				} else {
					final RectF centerRect = getCenter(true, true);
					if (centerRect.left != 0 || centerRect.top != 0) {
						scrollBy(centerRect.left, centerRect.top);
					}
				}
			}
		});
	}

	protected void updateRect(final RectF bitmapRect, final RectF scrollRect) {
		if (bitmapRect == null || scrollRect == null) return;
		final float width = getWidth();
		final float height = getHeight();

		if (bitmapRect.top >= 0 && bitmapRect.bottom <= height) {
			scrollRect.top = 0;
		}
		if (bitmapRect.left >= 0 && bitmapRect.right <= width) {
			scrollRect.left = 0;
		}
		if (bitmapRect.top + scrollRect.top >= 0 && bitmapRect.bottom > height) {
			scrollRect.top = (int) (0 - bitmapRect.top);
		}
		if (bitmapRect.bottom + scrollRect.top <= height - 0 && bitmapRect.top < 0) {
			scrollRect.top = (int) (height - 0 - bitmapRect.bottom);
		}
		if (bitmapRect.left + scrollRect.left >= 0) {
			scrollRect.left = (int) (0 - bitmapRect.left);
		}
		if (bitmapRect.right + scrollRect.left <= width - 0) {
			scrollRect.left = (int) (width - 0 - bitmapRect.right);
			// Log.d( LOG_TAG, "scrollRect(2): " + scrollRect.toString() );
		}
	}

	protected void zoomTo(final float scale) {
		final float cx = getWidth() / 2F;
		final float cy = getHeight() / 2F;
		zoomTo(scale, cx, cy);
	}

	protected void zoomTo(float scale, final float centerX, final float centerY) {
		// Log.i(LOG_TAG, "zoomTo");

		if (scale > mMaxZoom) {
			scale = mMaxZoom;
		}
		final float oldScale = getScale();
		final float deltaScale = scale / oldScale;
		postScale(deltaScale, centerX, centerY);
		onZoom(getScale());
		center(true, true);
	}

	protected void zoomTo(final float scale, final float centerX, final float centerY, final float durationMs) {
		// Log.i( LOG_TAG, "zoomTo: " + scale + ", " + centerX + ": " + centerY
		// );
		final long startTime = System.currentTimeMillis();
		final float incrementPerMs = (scale - getScale()) / durationMs;
		final float oldScale = getScale();
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				final long now = System.currentTimeMillis();
				final float currentMs = Math.min(durationMs, now - startTime);
				final float target = oldScale + incrementPerMs * currentMs;
				zoomTo(target, centerX, centerY);
				if (currentMs < durationMs) {
					mHandler.post(this);
				} else {
					// if ( getScale() < 1f ) {}
				}
			}
		});
	}

	public interface OnBitmapChangedListener {

		void onBitmapChanged(Drawable drawable);
	}
}

package me.imid.swipebacklayout.lib;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.MathUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.accessor.ViewAccessor;

public class SwipeBackLayout extends FrameLayout {
	/**
	 * Minimum velocity that will be detected as a fling
	 */
	private static final int MIN_FLING_VELOCITY = 400; // dips per second

	private static final int DEFAULT_SCRIM_COLOR = 0x99000000;

	/**
	 * Edge flag indicating that the left edge should be affected.
	 */
	public static final int EDGE_LEFT = ViewDragHelper.EDGE_LEFT;

	/**
	 * Edge flag indicating that the right edge should be affected.
	 */
	public static final int EDGE_RIGHT = ViewDragHelper.EDGE_RIGHT;

	/**
	 * Edge flag indicating that the bottom edge should be affected.
	 */
	public static final int EDGE_BOTTOM = ViewDragHelper.EDGE_BOTTOM;

	/**
	 * Edge flag set indicating all edges should be affected.
	 */
	public static final int EDGE_ALL = EDGE_LEFT | EDGE_RIGHT | EDGE_BOTTOM;

	/**
	 * A view is not currently being dragged or animating as a result of a
	 * fling/snap.
	 */
	public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;

	/**
	 * A view is currently being dragged. The position is currently changing as
	 * a result of user input or simulated user input.
	 */
	public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;

	/**
	 * A view is currently settling into place as a result of a fling or
	 * predefined non-interactive motion.
	 */
	public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;

	/**
	 * Default threshold of scroll
	 */
	private static final float DEFAULT_SCROLL_THRESHOLD = 0.3f;

	private static final int OVERSCROLL_DISTANCE = 10;

	private static final int[] EDGE_FLAGS = { EDGE_LEFT, EDGE_RIGHT, EDGE_BOTTOM, EDGE_ALL };

	private int mEdgeFlags;

	/**
	 * Threshold of scroll, we will close the activity, when scrollPercent over
	 * this value;
	 */
	private float mScrollThreshold = DEFAULT_SCROLL_THRESHOLD;

	private Activity mActivity;

	private boolean mEnable = true;

	private View mContentView;

	private ImageView mBackgroundView;
	private final ViewDragHelper mDragHelper;

	private float mScrollPercent;

	private int mContentLeft;

	private int mContentTop;

	private SwipeListener mSwipeListener;

	private Drawable mShadowLeft;

	private Drawable mShadowRight;

	private Drawable mShadowBottom;

	private float mScrimOpacity;

	private int mScrimColor;

	private float mScrimAlpha;

	private boolean mInLayout;

	private final Rect mTmpRect = new Rect();

	/**
	 * Edge being dragged
	 */
	private int mTrackingEdge;

	private float mScalePercent;

	private OnSwipeBackScrollListener mOnSwipeBackScrollListener;

	public SwipeBackLayout(final Context context) {
		this(context, null);
	}

	public SwipeBackLayout(final Context context, final AttributeSet attrs) {
		this(context, attrs, R.attr.SwipeBackLayoutStyle);
	}

	public SwipeBackLayout(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs);
		setFitsSystemWindows(true);
		mDragHelper = ViewDragHelper.create(this, new ViewDragCallback());

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeBackLayout, defStyle,
				R.style.SwipeBackLayout);

		final int edgeSize = a.getDimensionPixelSize(R.styleable.SwipeBackLayout_edgeSize, -1);
		if (edgeSize > 0) {
			setEdgeSize(edgeSize);
		}
		final int mode = EDGE_FLAGS[a.getInt(R.styleable.SwipeBackLayout_edgeFlag, 0)];
		setEdgeTrackingEnabled(mode);

		final int shadowLeft = a.getResourceId(R.styleable.SwipeBackLayout_shadowLeft, R.drawable.shadow_left);
		final int shadowRight = a.getResourceId(R.styleable.SwipeBackLayout_shadowRight, R.drawable.shadow_right);
		final int shadowBottom = a.getResourceId(R.styleable.SwipeBackLayout_shadowBottom, R.drawable.shadow_bottom);
		final int scrimColor = a.getColor(R.styleable.SwipeBackLayout_scrimColor, DEFAULT_SCRIM_COLOR);
		final float scrimAlpha = a.getFloat(R.styleable.SwipeBackLayout_scrimAlpha, Color.alpha(scrimColor) / 255.0f);
		final float scalePercent = a.getFraction(R.styleable.SwipeBackLayout_scalePercent, 1, 1, 1);
		setShadow(shadowLeft, EDGE_LEFT);
		setShadow(shadowRight, EDGE_RIGHT);
		setShadow(shadowBottom, EDGE_BOTTOM);
		setScalePercent(scalePercent);
		setScrimColor(scrimColor);
		setScrimAlpha(scrimAlpha);
		a.recycle();
		final float density = getResources().getDisplayMetrics().density;
		final float minVel = MIN_FLING_VELOCITY * density;
		mDragHelper.setMinVelocity(minVel);

	}

	public void attachToActivity(final Activity activity) {
		mActivity = activity;
		final Drawable background = ThemeUtils.getWindowBackground(activity);

		final ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
		final ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
		final ImageView backgroundChild = new ImageView(activity);
		backgroundChild.setScaleType(ScaleType.CENTER_CROP);
		ViewAccessor.setBackground(decorChild, background);
		decor.removeView(decorChild);
		setBackgroundView(backgroundChild);
		setContentView(decorChild);
		addView(decorChild, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		final FrameLayout frame = new FrameLayout(activity);
		frame.setFitsSystemWindows(true);
		frame.addView(backgroundChild, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		frame.addView(this);
		decor.addView(frame);
	}

	@Override
	public void computeScroll() {
		mScrimOpacity = 1 - mScrollPercent;
		updateWindowBackground();
		if (mDragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
		if (mOnSwipeBackScrollListener != null) {
			mOnSwipeBackScrollListener.onSwipeBackScroll(mScrollPercent);
		}
	}

	public int getEdgeFlags() {
		return mEdgeFlags;
	}

	public Drawable getShadow(final int edgeFlag) {
		if ((edgeFlag & EDGE_LEFT) != 0)
			return mShadowLeft;
		else if ((edgeFlag & EDGE_RIGHT) != 0)
			return mShadowRight;
		else if ((edgeFlag & EDGE_BOTTOM) != 0) return mShadowBottom;
		return null;
	}

	public int getTrackingEdge() {
		return mTrackingEdge;
	}

	public boolean isSwiping() {
		return mDragHelper != null && mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE;
	}

	@Override
	public boolean onInterceptTouchEvent(final MotionEvent event) {
		if (!mEnable) return false;
		try {
			return mDragHelper.shouldInterceptTouchEvent(event);
		} catch (final ArrayIndexOutOfBoundsException e) {
			// FIXME: handle exception
			// issues #9
			return false;
		}
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (!mEnable) return false;
		mDragHelper.processTouchEvent(event);
		return true;
	}

	@Override
	public void requestLayout() {
		if (!mInLayout) {
			super.requestLayout();
		}
	}

	/**
	 * Scroll out contentView and finish the activity
	 */
	public void scrollToFinishActivity() {
		final int childWidth = mContentView.getWidth();
		final int childHeight = mContentView.getHeight();

		int left = 0, top = 0;
		if ((mEdgeFlags & EDGE_LEFT) != 0) {
			left = childWidth + mShadowLeft.getIntrinsicWidth() + OVERSCROLL_DISTANCE;
			mTrackingEdge = EDGE_LEFT;
		} else if ((mEdgeFlags & EDGE_RIGHT) != 0) {
			left = -childWidth - mShadowRight.getIntrinsicWidth() - OVERSCROLL_DISTANCE;
			mTrackingEdge = EDGE_RIGHT;
		} else if ((mEdgeFlags & EDGE_BOTTOM) != 0) {
			top = -childHeight - mShadowBottom.getIntrinsicHeight() - OVERSCROLL_DISTANCE;
			mTrackingEdge = EDGE_BOTTOM;
		}

		mDragHelper.smoothSlideViewTo(mContentView, left, top);
		invalidate();
	}

	/**
	 * Set the size of an edge. This is the range in pixels along the edges of
	 * this view that will actively detect edge touches or drags if edge
	 * tracking is enabled.
	 * 
	 * @param size The size of an edge in pixels
	 */
	public void setEdgeSize(final int size) {
		mDragHelper.setEdgeSize(size);
	}

	/**
	 * Enable edge tracking for the selected edges of the parent view. The
	 * callback's
	 * {@link me.imid.swipebacklayout.lib.ViewDragHelper.Callback#onEdgeTouched(int, int)}
	 * and
	 * {@link me.imid.swipebacklayout.lib.ViewDragHelper.Callback#onEdgeDragStarted(int, int)}
	 * methods will only be invoked for edges for which edge tracking has been
	 * enabled.
	 * 
	 * @param edgeFlags Combination of edge flags describing the edges to watch
	 * @see #EDGE_LEFT
	 * @see #EDGE_RIGHT
	 * @see #EDGE_BOTTOM
	 */
	public void setEdgeTrackingEnabled(final int edgeFlags) {
		mEdgeFlags = edgeFlags;
		mDragHelper.setEdgeTrackingEnabled(mEdgeFlags);
	}

	public void setEnableGesture(final boolean enable) {
		mEnable = enable;
	}

	public void setOnSwipeBackScrollListener(final OnSwipeBackScrollListener listener) {
		mOnSwipeBackScrollListener = listener;
	}

	/**
	 * Set a color to use for the scrim that obscures primary content while a
	 * drawer is open.
	 * 
	 * @param color Color to use in 0xAARRGGBB format.
	 */
	public void setScrimColor(final int color) {
		mScrimColor = color;
		invalidate();
	}

	/**
	 * Set scroll threshold, we will close the activity, when scrollPercent over
	 * this value
	 * 
	 * @param threshold
	 */
	public void setScrollThresHold(final float threshold) {
		if (threshold >= 1.0f || threshold <= 0)
			throw new IllegalArgumentException("Threshold value should be between 0 and 1.0");
		mScrollThreshold = threshold;
	}

	/**
	 * Set a drawable used for edge shadow.
	 * 
	 * @param shadow Drawable to use
	 * @param edgeFlags Combination of edge flags describing the edge to set
	 * @see #EDGE_LEFT
	 * @see #EDGE_RIGHT
	 * @see #EDGE_BOTTOM
	 */
	public void setShadow(final Drawable shadow, final int edgeFlag) {
		if ((edgeFlag & EDGE_LEFT) != 0) {
			mShadowLeft = shadow;
		} else if ((edgeFlag & EDGE_RIGHT) != 0) {
			mShadowRight = shadow;
		} else if ((edgeFlag & EDGE_BOTTOM) != 0) {
			mShadowBottom = shadow;
		}
		invalidate();
	}

	/**
	 * Set a drawable used for edge shadow.
	 * 
	 * @param resId Resource of drawable to use
	 * @param edgeFlags Combination of edge flags describing the edge to set
	 * @see #EDGE_LEFT
	 * @see #EDGE_RIGHT
	 * @see #EDGE_BOTTOM
	 */
	public void setShadow(final int resId, final int edgeFlag) {
		setShadow(getResources().getDrawable(resId), edgeFlag);
	}

	/**
	 * Register a callback to be invoked when a swipe event is sent to this
	 * view.
	 * 
	 * @param listener the swipe listener to attach to this view
	 */
	public void setSwipeListener(final SwipeListener listener) {
		mSwipeListener = listener;
	}

	public void setWindowBackgroundDrawable(final Drawable d) {
		if (mBackgroundView == null) return;
		mBackgroundView.setImageDrawable(d);
	}

	@Override
	protected boolean drawChild(final Canvas canvas, final View child, final long drawingTime) {
		final boolean drawContent = child == mContentView;
		drawShadow(canvas, child);

		final boolean ret = super.drawChild(canvas, child, drawingTime);
		if (mScrimOpacity > 0 && drawContent && mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
			drawScrim(canvas, child);
		}
		return ret;
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
		mInLayout = true;
		if (mContentView != null) {
			mContentView.layout(mContentLeft, mContentTop, mContentLeft + mContentView.getMeasuredWidth(), mContentTop
					+ mContentView.getMeasuredHeight());
		}
		mInLayout = false;
	}

	private void drawScrim(final Canvas canvas, final View child) {
		final int alpha = (int) (mScrimAlpha * 255 * mScrimOpacity);
		final int color = alpha << 24 | mScrimColor & 0xffffff;

		if ((mTrackingEdge & EDGE_LEFT) != 0) {
			canvas.clipRect(0, 0, child.getLeft(), getHeight());
		} else if ((mTrackingEdge & EDGE_RIGHT) != 0) {
			canvas.clipRect(child.getRight(), 0, getRight(), getHeight());
		} else if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
			canvas.clipRect(child.getLeft(), child.getBottom(), getRight(), getHeight());
		}
		canvas.drawColor(color);
	}

	private void drawShadow(final Canvas canvas, final View child) {
		final Rect childRect = mTmpRect;
		child.getHitRect(childRect);

		if ((mEdgeFlags & EDGE_LEFT) != 0) {
			mShadowLeft.setBounds(childRect.left - mShadowLeft.getIntrinsicWidth(), childRect.top, childRect.left,
					childRect.bottom);
			mShadowLeft.draw(canvas);
		}

		if ((mEdgeFlags & EDGE_RIGHT) != 0) {
			mShadowRight.setBounds(childRect.right, childRect.top, childRect.right + mShadowRight.getIntrinsicWidth(),
					childRect.bottom);
			mShadowRight.draw(canvas);
		}

		if ((mEdgeFlags & EDGE_BOTTOM) != 0) {
			mShadowBottom.setBounds(childRect.left, childRect.bottom, childRect.right,
					childRect.bottom + mShadowBottom.getIntrinsicHeight());
			mShadowBottom.draw(canvas);
		}
	}

	private void setBackgroundView(final ImageView view) {
		mBackgroundView = view;
	}

	/**
	 * Set up contentView which will be moved by user gesture
	 * 
	 * @param view
	 */
	private void setContentView(final View view) {
		mContentView = view;
	}

	private void setScalePercent(final float scalePercent) {
		mScalePercent = scalePercent;
	}

	private void setScrimAlpha(final float scrimAlpha) {
		mScrimAlpha = scrimAlpha;
		invalidate();
	}

	private void updateWindowBackground() {
		if (mBackgroundView == null) return;
		final float scrollPercentAbs = Math.abs(mScrollPercent);
		final float percent = MathUtils.clamp(1 - (1 - scrollPercentAbs) * (1 - mScalePercent), 1, 0);
		mBackgroundView.setScaleType(ScaleType.CENTER_CROP);
		mBackgroundView.setScaleX(percent);
		mBackgroundView.setScaleY(percent);
		mBackgroundView.setVisibility(mScrollPercent <= 0 ? View.INVISIBLE : View.VISIBLE);
		mBackgroundView.setAlpha(scrollPercentAbs);
		// mBackgroundView.setScrollPercent(mScrollPercent / mScalePercent);
	}

	public interface OnSwipeBackScrollListener {
		void onSwipeBackScroll(float percent);
	}

	public static interface SwipeListener {
		/**
		 * Invoke when edge touched
		 * 
		 * @param edgeFlag edge flag describing the edge being touched
		 * @see #EDGE_LEFT
		 * @see #EDGE_RIGHT
		 * @see #EDGE_BOTTOM
		 */
		public void onEdgeTouch(int edgeFlag);

		/**
		 * Invoke when scroll percent over the threshold for the first time
		 */
		public void onScrollOverThreshold();

		/**
		 * Invoke when state change
		 * 
		 * @param state flag to describe scroll state
		 * @see #STATE_IDLE
		 * @see #STATE_DRAGGING
		 * @see #STATE_SETTLING
		 * @param scrollPercent scroll percent of this view
		 */
		public void onScrollStateChange(int state, float scrollPercent);
	}

	private class ViewDragCallback extends ViewDragHelper.Callback {
		private boolean mIsScrollOverValid;

		@Override
		public int clampViewPositionHorizontal(final View child, final int left, final int dx) {
			int ret = 0;
			if ((mTrackingEdge & EDGE_LEFT) != 0) {
				ret = Math.min(child.getWidth(), Math.max(left, 0));
			} else if ((mTrackingEdge & EDGE_RIGHT) != 0) {
				ret = Math.min(0, Math.max(left, -child.getWidth()));
			}
			return ret;
		}

		@Override
		public int clampViewPositionVertical(final View child, final int top, final int dy) {
			int ret = 0;
			if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
				ret = Math.min(0, Math.max(top, -child.getHeight()));
			}
			return ret;
		}

		@Override
		public int getViewHorizontalDragRange(final View child) {
			return mEdgeFlags & (EDGE_LEFT | EDGE_RIGHT);
		}

		@Override
		public int getViewVerticalDragRange(final View child) {
			return mEdgeFlags & EDGE_BOTTOM;
		}

		@Override
		public void onViewDragStateChanged(final int state) {
			super.onViewDragStateChanged(state);
			if (mSwipeListener != null) {
				mSwipeListener.onScrollStateChange(state, mScrollPercent);
			}
		}

		@Override
		public void onViewPositionChanged(final View changedView, final int left, final int top, final int dx,
				final int dy) {
			super.onViewPositionChanged(changedView, left, top, dx, dy);
			if ((mTrackingEdge & EDGE_LEFT) != 0) {
				mScrollPercent = Math.abs((float) left / (mContentView.getWidth() + mShadowLeft.getIntrinsicWidth()));
			} else if ((mTrackingEdge & EDGE_RIGHT) != 0) {
				mScrollPercent = Math.abs((float) left / (mContentView.getWidth() + mShadowRight.getIntrinsicWidth()));
			} else if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
				mScrollPercent = Math
						.abs((float) top / (mContentView.getHeight() + mShadowBottom.getIntrinsicHeight()));
			}
			mContentLeft = left;
			mContentTop = top;
			invalidate();
			if (mScrollPercent < mScrollThreshold && !mIsScrollOverValid) {
				mIsScrollOverValid = true;
			}
			if (mSwipeListener != null && mDragHelper.getViewDragState() == STATE_DRAGGING
					&& mScrollPercent >= mScrollThreshold && mIsScrollOverValid) {
				mIsScrollOverValid = false;
				mSwipeListener.onScrollOverThreshold();
			}

			if (mScrollPercent >= 1) {
				if (!mActivity.isFinishing()) {
					mActivity.finish();
					mActivity.overridePendingTransition(0, 0);
				}
			}
		}

		@Override
		public void onViewReleased(final View releasedChild, final float xvel, final float yvel) {
			final int childWidth = releasedChild.getWidth();
			final int childHeight = releasedChild.getHeight();

			int left = 0, top = 0;
			if ((mTrackingEdge & EDGE_LEFT) != 0) {
				left = xvel > 0 || xvel == 0 && mScrollPercent > mScrollThreshold ? childWidth
						+ mShadowLeft.getIntrinsicWidth() + OVERSCROLL_DISTANCE : 0;
			} else if ((mTrackingEdge & EDGE_RIGHT) != 0) {
				left = xvel < 0 || xvel == 0 && mScrollPercent > mScrollThreshold ? -(childWidth
						+ mShadowLeft.getIntrinsicWidth() + OVERSCROLL_DISTANCE) : 0;
			} else if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
				top = yvel < 0 || yvel == 0 && mScrollPercent > mScrollThreshold ? -(childHeight
						+ mShadowBottom.getIntrinsicHeight() + OVERSCROLL_DISTANCE) : 0;
			}

			mDragHelper.settleCapturedViewAt(left, top);
			invalidate();
		}

		@Override
		public boolean tryCaptureView(final View view, final int i) {
			final boolean ret = mDragHelper.isEdgeTouched(mEdgeFlags, i);
			if (ret) {
				if (mDragHelper.isEdgeTouched(EDGE_LEFT, i)) {
					mTrackingEdge = EDGE_LEFT;
				} else if (mDragHelper.isEdgeTouched(EDGE_RIGHT, i)) {
					mTrackingEdge = EDGE_RIGHT;
				} else if (mDragHelper.isEdgeTouched(EDGE_BOTTOM, i)) {
					mTrackingEdge = EDGE_BOTTOM;
				}
				if (mSwipeListener != null) {
					mSwipeListener.onEdgeTouch(mTrackingEdge);
				}
				mIsScrollOverValid = true;
			}
			return ret;
		}
	}

}

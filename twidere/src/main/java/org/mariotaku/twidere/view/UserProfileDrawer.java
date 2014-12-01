package org.mariotaku.twidere.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.MathUtils;

/**
 * Created by mariotaku on 14/11/27.
 */
public class UserProfileDrawer extends ViewGroup {

    static final String LOGTAG = "UserProfileDrawer";

    private final ViewDragHelper mDragHelper;
    private final ScrollerCompat mScroller;
    private final GestureDetector mGestureDetector;

    private final InternalContainer mContainer;
    private final DragCallback mDragCallback;

    private DrawerCallback mDrawerCallback;
    private boolean mUsingDragHelper;
    private boolean mScrollingHeaderByGesture, mScrollingContentCallback;
    private boolean mTouchDown, mTouchingScrollableContent;

    private int mHeaderOffset;

    public UserProfileDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UserProfileDrawer);
        final int headerLayoutId = a.getResourceId(R.styleable.UserProfileDrawer_headerLayout, 0);
        final int contentLayoutId = a.getResourceId(R.styleable.UserProfileDrawer_contentLayout, 0);
        addView(mContainer = new InternalContainer(this, context, headerLayoutId, contentLayoutId),
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        a.recycle();
        mDragHelper = ViewDragHelper.create(this, mDragCallback = new DragCallback(this));
        mGestureDetector = new GestureDetector(context, new GestureListener(this));
        mScroller = ScrollerCompat.create(context);
    }

    public UserProfileDrawer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserProfileDrawer(Context context) {
        this(context, null);
    }

    private static int makeChildMeasureSpec(int spec, int padding) {
        final int size = MeasureSpec.getSize(spec), mode = MeasureSpec.getMode(spec);
        return MeasureSpec.makeMeasureSpec(size - padding, mode);
    }

    private boolean isUsingDragHelper() {
        return mUsingDragHelper;
    }

    private void setScrollingHeaderByGesture(boolean scrolling) {
        mScrollingHeaderByGesture = scrolling;
    }

    public void flingHeader(float velocity) {
        if (mTouchDown) {
            mScroller.abortAnimation();
            return;
        }
        mScroller.fling(0, getHeaderTop(), 0, (int) velocity, 0, 0,
                mContainer.getHeaderTopMinimum(), mContainer.getHeaderTopMaximum());
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void flingCallback(float velocity) {
        mDrawerCallback.fling(velocity);
    }

    @Override
    public void computeScroll() {
        boolean invalidate = mDragHelper.continueSettling(true);
        if (!mTouchDown && mScroller.computeScrollOffset()) {
            if (!invalidate) {
                offsetHeaderBy(mScroller.getCurrY() - getHeaderTop());
            }
            invalidate = true;
        }
        updateViewOffset();
        if (invalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void scrollByCallback(float dy) {
        setScrollingContentCallback(true);
        mDrawerCallback.scrollBy(dy);
    }

    private boolean isScrollContentCallback(float x, float y) {
        return mDrawerCallback.isScrollContent(x, y);
    }

    private void cancelTouchCallback() {
        mDrawerCallback.cancelTouch();
    }

    @Override
    protected void onFinishInflate() {
        if (getChildCount() != 1) {
            throw new IllegalArgumentException("Add subview by XML is not allowed.");
        }
    }

    private boolean canScrollCallback(float dy) {
        return mDrawerCallback.canScroll(dy);
    }

    public void setDrawerCallback(DrawerCallback callback) {
        mDrawerCallback = callback;
    }

    private void updateViewOffset() {
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mScroller.abortAnimation();
                mTouchDown = true;
                mTouchingScrollableContent = isScrollContentCallback(ev.getX(), ev.getY());
                mUsingDragHelper = false;
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                mTouchDown = false;
                mTouchingScrollableContent = false;
                mUsingDragHelper = false;
            }
        }
        mGestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mDragHelper.shouldInterceptTouchEvent(ev) || mScrollingHeaderByGesture) {
            mUsingDragHelper = true;
            return true;
        }
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0, j = getChildCount(); i < j; i++) {
            final View child = getChildAt(i);
            final int left = getPaddingLeft(), right = left + child.getMeasuredWidth();
            final int top = i == 0 ? mHeaderOffset + getPaddingTop() : getChildAt(i - 1).getBottom();
            final int bottom = top + child.getMeasuredHeight();
            child.layout(left, top, right, bottom);
            notifyOffsetChanged();
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    public View getHeader() {
        return mContainer.getHeader();
    }

    public View getContent() {
        return mContainer.getContent();
    }

    private int getScrollRange() {
        return mContainer.getScrollRange();
    }

    public int getHeaderTop() {
        return mContainer.getTop();
    }

    private void offsetHeaderBy(int dy) {
        final int prevTop = mContainer.getTop();
        final int clampedDy = MathUtils.clamp(prevTop + dy, getHeaderTopMinimum(), getHeaderTopMaximum()) - prevTop;
        mContainer.offsetTopAndBottom(clampedDy);
    }

    public int getHeaderTopMaximum() {
        return mContainer.getHeaderTopMaximum();
    }

    public int getHeaderTopMinimum() {
        return mContainer.getHeaderTopMinimum();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final View child = getChildAt(0);

        final int childWidthMeasureSpec = makeChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight());
        final int childHeightMeasureSpec = makeChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom());

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private boolean isTouchingScrollableContent() {
        return mTouchingScrollableContent;
    }

    private void notifyOffsetChanged() {
        final int top = getHeaderTop();
        mHeaderOffset = top - getPaddingTop();
        mDrawerCallback.topChanged(top);
    }

    private float getDragTouchSlop() {
        return mDragHelper.getTouchSlop();
    }

    private boolean isValidScroll(float direction, float other) {
        return Math.abs(direction) > getDragTouchSlop() && Math.abs(direction) > Math.abs(other);
    }

    private boolean isScrollingHeaderByHelper() {
        return mDragCallback.isScrollingHeaderByHelper();
    }

    private boolean isScrollingContentCallback() {
        return mScrollingContentCallback;
    }

    private void setScrollingContentCallback(boolean scrolling) {
        mScrollingContentCallback = scrolling;
    }

    public static interface DrawerCallback {

        void fling(float velocity);

        void scrollBy(float dy);

        boolean canScroll(float dy);

        boolean isScrollContent(float x, float y);

        void cancelTouch();

        void topChanged(int offset);
    }

    private static class DragCallback extends ViewDragHelper.Callback {

        private final UserProfileDrawer mDrawer;
        private long mTime;
        private float mDx, mDy, mVelocity;
        private boolean mScrollingHeaderByHelper;

        public DragCallback(UserProfileDrawer drawer) {
            mDrawer = drawer;
            mTime = -1;
            mDx = Float.NaN;
            mDy = Float.NaN;
            mVelocity = Float.NaN;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            switch (state) {
                case ViewDragHelper.STATE_SETTLING:
                case ViewDragHelper.STATE_DRAGGING: {
                    mScrollingHeaderByHelper = false;
                    break;
                }
                case ViewDragHelper.STATE_IDLE: {
                    if (mTime > 0 && !Float.isNaN(mVelocity)) {
                        final float velocity = mVelocity;
                        if (velocity < 0 && mDrawer.getHeaderTop() <= mDrawer.getHeaderTopMinimum()) {
                            mDrawer.flingCallback(-velocity);
                        }
                    }
                    mTime = -1;
                    mDx = Float.NaN;
                    mDy = Float.NaN;
                    mVelocity = Float.NaN;
                    mScrollingHeaderByHelper = false;
                    break;
                }
            }
            super.onViewDragStateChanged(state);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            final long time = SystemClock.uptimeMillis();
            final float timeDelta = time - mTime;
            mVelocity = mDy / timeDelta * 1000;
            mTime = time;
            mDy = dy;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            mDrawer.mDragHelper.flingCapturedView(mDrawer.getPaddingLeft(),
                    mDrawer.getHeaderTopMinimum(), mDrawer.getPaddingLeft(),
                    mDrawer.getHeaderTopMaximum());
            ViewCompat.postInvalidateOnAnimation(mDrawer);
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mDrawer.getScrollRange();
        }

        @Override
        public boolean tryCaptureView(View view, int pointerId) {
            return view == mDrawer.mContainer;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            mDx = dx;
            return mDrawer.getPaddingLeft();
        }

        @Override
        public int clampViewPositionVertical(final View child, final int top, final int dy) {
            final int current = mDrawer.getHeaderTop();
            if (!Float.isNaN(mDx) && mDrawer.isValidScroll(mDx, dy)) {
                mScrollingHeaderByHelper = false;
                return current;
            }
            if (dy > 0 && mDrawer.canScrollCallback(-dy)) {
                if (!mDrawer.isUsingDragHelper()) {
                    // Scrolling up while list still has space to scroll, so make header still
                    mScrollingHeaderByHelper = false;
                    return current;
                } else if (mDrawer.isTouchingScrollableContent()) {
                    mDrawer.scrollByCallback(-dy);
                    mScrollingHeaderByHelper = false;
                    return current;
                }
            }
            final int min = mDrawer.getHeaderTopMinimum(), max = mDrawer.getHeaderTopMaximum();
            if (top < min && mDrawer.isTouchingScrollableContent() && mDrawer.isUsingDragHelper()) {
                mDrawer.scrollByCallback(-dy);
            }
            mScrollingHeaderByHelper = true;
            return MathUtils.clamp(top, min, max);
        }

        private boolean isScrollingHeaderByHelper() {
            return mScrollingHeaderByHelper;
        }
    }

    private static class GestureListener extends SimpleOnGestureListener {

        private final UserProfileDrawer mDrawer;

        public GestureListener(UserProfileDrawer drawer) {
            mDrawer = drawer;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!mDrawer.isUsingDragHelper() && mDrawer.isValidScroll(distanceY, distanceX)) {
                final int offset = mDrawer.getHeaderTop(), min = mDrawer.getHeaderTopMinimum();
                if (!mDrawer.canScrollCallback(-1)) {
                    if (distanceY < 0) {
                        if (!mDrawer.isScrollingHeaderByHelper()) {
                            mDrawer.offsetHeaderBy(Math.round(-distanceY));
                        }
                        mDrawer.setScrollingHeaderByGesture(true);
                    } else if (distanceY > 0 && offset > min) {
                        // Scrolling up when scrolling to list top, so we cancel touch event and scrolling header up
                        mDrawer.cancelTouchCallback();
                        if (!mDrawer.isScrollingHeaderByHelper()) {
                            mDrawer.offsetHeaderBy(Math.round(-distanceY));
                        }
                    } else if (offset <= min) {
                        mDrawer.scrollByCallback(-distanceX);
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            final int top = mDrawer.getHeaderTop(), min = mDrawer.getHeaderTopMinimum();
            if (velocityY > 0 && !mDrawer.canScrollCallback(-1)) {
                // Fling down when list reached top, so we fling header down here
                if (Math.abs(velocityY) > Math.abs(velocityX)) {
                    mDrawer.flingHeader(velocityY);
                }
            } else if (velocityY < 0 && top <= min && mDrawer.isScrollingContentCallback()) {
                // Fling up when showing full content, so we fling list up here
                if (Math.abs(velocityY) > Math.abs(velocityX)) {
                    mDrawer.flingCallback(-velocityY);
                }
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mDrawer.setScrollingHeaderByGesture(false);
            mDrawer.setScrollingContentCallback(false);
            return true;
        }
    }

    @SuppressLint("ViewConstructor")
    private static class InternalContainer extends ViewGroup {

        private final UserProfileDrawer mParent;
        private final View mHeaderView, mContentView;

        public InternalContainer(UserProfileDrawer parent, Context context, int headerLayoutId, int contentLayoutId) {
            super(context);
            mParent = parent;
            final LayoutInflater inflater = LayoutInflater.from(context);
            addView(mHeaderView = inflater.inflate(headerLayoutId, this, false));
            addView(mContentView = inflater.inflate(contentLayoutId, this, false));
        }

        public View getHeader() {
            return mHeaderView;
        }

        public View getContent() {
            return mContentView;
        }

        public int getHeaderTopMinimum() {
            return mParent.getPaddingTop() - mHeaderView.getHeight();
        }

        public int getHeaderTopMaximum() {
            return mParent.getPaddingTop();
        }

        public int getScrollRange() {
            return getHeaderTopMaximum() - getHeaderTopMinimum();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
            int heightSum = 0;
            for (int i = 0, j = getChildCount(); i < j; i++) {
                final View child = getChildAt(i);
                final LayoutParams lp = child.getLayoutParams();
                final int childHeightSpec;
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    childHeightSpec = heightMeasureSpec;
                } else if (lp.height == LayoutParams.WRAP_CONTENT) {
                    childHeightSpec = MeasureSpec.makeMeasureSpec(measureHeight, MeasureSpec.UNSPECIFIED);
                } else {
                    childHeightSpec = MeasureSpec.makeMeasureSpec(measureHeight, MeasureSpec.EXACTLY);
                }
                child.measure(widthMeasureSpec, childHeightSpec);
                heightSum += child.getMeasuredHeight();
            }
            final int hSpec = MeasureSpec.makeMeasureSpec(heightSum, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, hSpec);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            for (int i = 0, j = getChildCount(); i < j; i++) {
                final View child = getChildAt(i);
                final int left = getPaddingLeft(), right = left + child.getMeasuredWidth();
                final int top = i == 0 ? getPaddingTop() : getChildAt(i - 1).getBottom();
                final int bottom = top + child.getMeasuredHeight();
                child.layout(left, top, right, bottom);
            }
        }

        @Override
        public void offsetTopAndBottom(int offset) {
            super.offsetTopAndBottom(offset);
            mParent.notifyOffsetChanged();
        }
    }

}
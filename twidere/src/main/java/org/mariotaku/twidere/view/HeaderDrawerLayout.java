/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
 * Custom ViewGroup for user profile page like Google+ but with tab swipe
 *
 * @author mariotaku
 */
public class HeaderDrawerLayout extends ViewGroup {

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
    private int mTop;

    public HeaderDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HeaderDrawerLayout);
        final int headerLayoutId = a.getResourceId(R.styleable.HeaderDrawerLayout_hdl_headerLayout, 0);
        final int contentLayoutId = a.getResourceId(R.styleable.HeaderDrawerLayout_hdl_contentLayout, 0);
        addView(mContainer = new InternalContainer(this, context, headerLayoutId, contentLayoutId),
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        a.recycle();
        mDragHelper = ViewDragHelper.create(this, mDragCallback = new DragCallback(this));
        mGestureDetector = new GestureDetector(context, new GestureListener(this));
        mScroller = ScrollerCompat.create(context);
    }

    public HeaderDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderDrawerLayout(Context context) {
        this(context, null);
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
            final int top;
            if (i == 0) {
                if (shouldLayoutHeaderBottomCallback() && child.getHeight() != 0) {
                    final int heightDelta = child.getMeasuredHeight() - child.getHeight();
                    top = mHeaderOffset + getPaddingTop() - heightDelta;
                } else {
                    top = mHeaderOffset + getPaddingTop();
                }
            } else {
                top = getChildAt(i - 1).getBottom();
            }
            final int bottom = top + child.getMeasuredHeight();
            child.layout(left, top, right, bottom);
            notifyOffsetChanged();
        }
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

    public View getContent() {
        return mContainer.getContent();
    }

    public View getHeader() {
        return mContainer.getHeader();
    }

    public int getHeaderTop() {
        return mContainer.getTop();
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

    public int getHeaderTopMaximum() {
        return mContainer.getHeaderTopMaximum();
    }

    public int getHeaderTopMinimum() {
        return mContainer.getHeaderTopMinimum();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    public void setDrawerCallback(DrawerCallback callback) {
        mDrawerCallback = callback;
    }

    private boolean canScrollCallback(float dy) {
        return mDrawerCallback.canScroll(dy);
    }

    @Override
    protected void onFinishInflate() {
        if (getChildCount() != 1) {
            throw new IllegalArgumentException("Add subview by XML is not allowed.");
        }
    }

    private void cancelTouchCallback() {
        mDrawerCallback.cancelTouch();
    }

    private void flingCallback(float velocity) {
        mDrawerCallback.fling(velocity);
    }

    private float getDragTouchSlop() {
        return mDragHelper.getTouchSlop();
    }

    private int getScrollRange() {
        return mContainer.getScrollRange();
    }

    private boolean isScrollContentCallback(float x, float y) {
        return mDrawerCallback.isScrollContent(x, y);
    }

    private boolean isScrollingContentCallback() {
        return mScrollingContentCallback;
    }

    private void setScrollingContentCallback(boolean scrolling) {
        mScrollingContentCallback = scrolling;
    }

    private boolean isScrollingHeaderByHelper() {
        return mDragCallback.isScrollingHeaderByHelper();
    }

    private boolean isTouchingScrollableContent() {
        return mTouchingScrollableContent;
    }

    private boolean isUsingDragHelper() {
        return mUsingDragHelper;
    }

    private boolean isValidScroll(float direction, float other) {
        return Math.abs(direction) > getDragTouchSlop() && Math.abs(direction) > Math.abs(other);
    }

    private static int makeChildMeasureSpec(int spec, int padding) {
        final int size = MeasureSpec.getSize(spec), mode = MeasureSpec.getMode(spec);
        return MeasureSpec.makeMeasureSpec(size - padding, mode);
    }

    private void notifyOffsetChanged() {
        final int top = getHeaderTop();
        if (mTop == top) return;
        mHeaderOffset = top - getPaddingTop();
        mDrawerCallback.topChanged(top);
        mTop = top;
    }

    private void offsetHeaderBy(int dy) {
        final int prevTop = mContainer.getTop();
        final int clampedDy = MathUtils.clamp(prevTop + dy, getHeaderTopMinimum(), getHeaderTopMaximum()) - prevTop;
        mContainer.offsetTopAndBottom(clampedDy);
    }

    private void scrollByCallback(float dy) {
        final int top = getHeaderTop();
        setScrollingContentCallback(top > getHeaderTopMinimum() && top < getHeaderTopMaximum());
        mDrawerCallback.scrollBy(dy);
    }

    private void setScrollingHeaderByGesture(boolean scrolling) {
        mScrollingHeaderByGesture = scrolling;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final View child = getChildAt(0);

        final int childWidthMeasureSpec = makeChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight());
        final int childHeightMeasureSpec = makeChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom());

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private boolean shouldLayoutHeaderBottomCallback() {
        if (mDragCallback == null || isInEditMode()) return false;
        return mDrawerCallback.shouldLayoutHeaderBottom();
    }

    private void updateViewOffset() {
    }

    public interface DrawerCallback {

        boolean canScroll(float dy);

        void cancelTouch();

        void fling(float velocity);

        boolean isScrollContent(float x, float y);

        void scrollBy(float dy);

        boolean shouldLayoutHeaderBottom();

        void topChanged(int offset);
    }

    private static class DragCallback extends ViewDragHelper.Callback {

        private final HeaderDrawerLayout mDrawer;
        private long mTime;
        private float mDx, mDy, mVelocity;
        private boolean mScrollingHeaderByHelper;

        public DragCallback(HeaderDrawerLayout drawer) {
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
            if (dy > 0 && mDrawer.canScrollCallback(-dy) && mDrawer.isTouchingScrollableContent()) {
                if (!mDrawer.isUsingDragHelper()) {
                    // Scrolling up while list still has space to scroll, so make header still
                    mScrollingHeaderByHelper = false;
                    return current;
                } else {
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

        private final HeaderDrawerLayout mDrawer;

        public GestureListener(HeaderDrawerLayout drawer) {
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
            final boolean showingFullContent = top <= min, flingUp = velocityY < 0;
            final boolean verticalFling = Math.abs(velocityY) > Math.abs(velocityX);
            if (!verticalFling) return true;
            if (showingFullContent) {
                if (flingUp) {
                    // Fling list up when showing full content
                    if (mDrawer.isScrollingContentCallback()) {
                        mDrawer.flingCallback(-velocityY);
                    }
                } else {
                    // Fling down when list reached top and not dragging user ViewDragHelper,
                    // so we fling header down here
                    if (!mDrawer.canScrollCallback(1) && !mDrawer.isUsingDragHelper()) {
                        mDrawer.flingHeader(velocityY);
                    }
                }
            } else {
                // Header still visible
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

        private final HeaderDrawerLayout mParent;
        private final View mHeaderView, mContentView;

        public InternalContainer(HeaderDrawerLayout parent, Context context, int headerLayoutId, int contentLayoutId) {
            super(context);
            mParent = parent;
            final LayoutInflater inflater = LayoutInflater.from(context);
            addView(mHeaderView = inflater.inflate(headerLayoutId, this, false));
            addView(mContentView = inflater.inflate(contentLayoutId, this, false));
        }

        public View getContent() {
            return mContentView;
        }

        public View getHeader() {
            return mHeaderView;
        }

        public int getHeaderTopMaximum() {
            return mParent.getPaddingTop();
        }

        public int getHeaderTopMinimum() {
            return mParent.getPaddingTop() - mHeaderView.getHeight();
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
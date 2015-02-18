/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;

import com.diegocarloslima.byakugallery.lib.FlingScroller;
import com.diegocarloslima.byakugallery.lib.TouchGestureDetector;

public class TouchImageView extends ImageView {

    private static final int DOUBLE_TAP_ANIMATION_DURATION = 300;
    private static final int SCALE_END_ANIMATION_DURATION = 200;

    private Drawable mDrawable;
    private int mDrawableIntrinsicWidth;
    private int mDrawableIntrinsicHeight;

    private final TouchGestureDetector mTouchGestureDetector;

    private final Matrix mMatrix = new Matrix();
    private final float[] mMatrixValues = new float[9];

    private float mScale;
    private float mMaxScale = 1;
    private float mTranslationX;
    private float mTranslationY;

    private Float mLastFocusX;
    private Float mLastFocusY;

    private final FlingScroller mFlingScroller = new FlingScroller();
    private boolean mIsAnimatingBack;
    private ZoomListener mZoomListener;

    public TouchImageView(Context context) {
        this(context, null);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TouchGestureDetector.OnTouchGestureListener listener = new TouchGestureDetector.OnTouchGestureListener() {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return performClick();
            }

            @Override
            public void onLongPress(MotionEvent e) {
                performLongClick();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                loadMatrixValues();

                final float minScale = getMinScale();
                // If we have already zoomed in, we should return to our initial scale value (minScale). Otherwise, scale to full size
                final boolean shouldZoomOut = mScale > minScale;
                final float targetScale = shouldZoomOut ? minScale : mMaxScale;

                // First, we try to keep the focused point in the same position when the animation ends
                final float desiredTranslationX = e.getX() - (e.getX() - mTranslationX) * (targetScale / mScale);
                final float desiredTranslationY = e.getY() - (e.getY() - mTranslationY) * (targetScale / mScale);

                // Here, we apply a correction to avoid unwanted blank spaces
                final float targetTranslationX = desiredTranslationX + computeTranslation(getMeasuredWidth(), mDrawableIntrinsicWidth * targetScale, desiredTranslationX, 0);
                final float targetTranslationY = desiredTranslationY + computeTranslation(getMeasuredHeight(), mDrawableIntrinsicHeight * targetScale, desiredTranslationY, 0);

                clearAnimation();
                final Animation animation = new TouchAnimation(targetScale, targetTranslationX, targetTranslationY);
                animation.setDuration(DOUBLE_TAP_ANIMATION_DURATION);
                startAnimation(animation);

                if (mZoomListener != null) {
                    if (shouldZoomOut) {
                        mZoomListener.onZoomOut();
                    } else {
                        mZoomListener.onZoomIn();
                    }
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                // Sometimes, this method is called just after an onScaleEnd event. In this case, we want to wait until we animate back our image
                if (mIsAnimatingBack) {
                    return false;
                }

                loadMatrixValues();

                final float currentDrawableWidth = mDrawableIntrinsicWidth * mScale;
                final float currentDrawableHeight = mDrawableIntrinsicHeight * mScale;

                final float dx = computeTranslation(getMeasuredWidth(), currentDrawableWidth, mTranslationX, -distanceX);
                final float dy = computeTranslation(getMeasuredHeight(), currentDrawableHeight, mTranslationY, -distanceY);
                mMatrix.postTranslate(dx, dy);

                clearAnimation();
                ViewCompat.postInvalidateOnAnimation(TouchImageView.this);

                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // Sometimes, this method is called just after an onScaleEnd event. In this case, we want to wait until we animate back our image
                if (mIsAnimatingBack) {
                    return false;
                }

                loadMatrixValues();

                final float horizontalSideFreeSpace = (getMeasuredWidth() - mDrawableIntrinsicWidth * mScale) / 2F;
                final float minTranslationX = horizontalSideFreeSpace > 0 ? horizontalSideFreeSpace : getMeasuredWidth() - mDrawableIntrinsicWidth * mScale;
                final float maxTranslationX = horizontalSideFreeSpace > 0 ? horizontalSideFreeSpace : 0;

                final float verticalSideFreeSpace = (getMeasuredHeight() - mDrawableIntrinsicHeight * mScale) / 2F;
                final float minTranslationY = verticalSideFreeSpace > 0 ? verticalSideFreeSpace : getMeasuredHeight() - mDrawableIntrinsicHeight * mScale;
                final float maxTranslationY = verticalSideFreeSpace > 0 ? verticalSideFreeSpace : 0;

                // Using FlingScroller here. The results were better than the Scroller class
                // https://android.googlesource.com/platform/packages/apps/Gallery2/+/master/src/com/android/gallery3d/ui/FlingScroller.java
                mFlingScroller.fling(Math.round(mTranslationX), Math.round(mTranslationY), Math.round(velocityX), Math.round(velocityY), Math.round(minTranslationX), Math.round(maxTranslationX), Math.round(minTranslationY), Math.round(maxTranslationY));

                clearAnimation();
                final Animation animation = new FlingAnimation();
                animation.setDuration(mFlingScroller.getDuration());
                animation.setInterpolator(new LinearInterpolator());
                startAnimation(animation);

                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                mLastFocusX = null;
                mLastFocusY = null;

                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                loadMatrixValues();

                float currentDrawableWidth = mDrawableIntrinsicWidth * mScale;
                float currentDrawableHeight = mDrawableIntrinsicHeight * mScale;

                final float focusX = computeFocus(getMeasuredWidth(), currentDrawableWidth, mTranslationX, detector.getFocusX());
                final float focusY = computeFocus(getMeasuredHeight(), currentDrawableHeight, mTranslationY, detector.getFocusY());

                // Here, we provide the ability to scroll while scaling
                if (mLastFocusX != null && mLastFocusY != null) {
                    final float dx = computeScaleTranslation(getMeasuredWidth(), currentDrawableWidth, mTranslationX, focusX - mLastFocusX);
                    final float dy = computeScaleTranslation(getMeasuredHeight(), currentDrawableHeight, mTranslationY, focusY - mLastFocusY);

                    if (dx != 0 || dy != 0) {
                        mMatrix.postTranslate(dx, dy);
                    }
                }

                final float scale = computeScale(getMinScale(), mMaxScale, mScale, detector.getScaleFactor());
                mMatrix.postScale(scale, scale, focusX, focusY);

                mLastFocusX = focusX;
                mLastFocusY = focusY;

                clearAnimation();
                ViewCompat.postInvalidateOnAnimation(TouchImageView.this);

                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                loadMatrixValues();

                final float currentDrawableWidth = mDrawableIntrinsicWidth * mScale;
                final float currentDrawableHeight = mDrawableIntrinsicHeight * mScale;

                final float dx = computeTranslation(getMeasuredWidth(), currentDrawableWidth, mTranslationX, 0);
                final float dy = computeTranslation(getMeasuredHeight(), currentDrawableHeight, mTranslationY, 0);

                if (Math.abs(dx) < 1 && Math.abs(dy) < 1) {
                    return;
                }

                final float targetTranslationX = mTranslationX + dx;
                final float targetTranslationY = mTranslationY + dy;

                clearAnimation();
                final Animation animation = new TouchAnimation(mScale, targetTranslationX, targetTranslationY);
                animation.setDuration(SCALE_END_ANIMATION_DURATION);
                startAnimation(animation);

                mIsAnimatingBack = true;
            }
        };

        mTouchGestureDetector = new TouchGestureDetector(context, listener);

        super.setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int oldMeasuredWidth = getMeasuredWidth();
        final int oldMeasuredHeight = getMeasuredHeight();

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (oldMeasuredWidth != getMeasuredWidth() || oldMeasuredHeight != getMeasuredHeight()) {
            resetToInitialState();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.setImageMatrix(mMatrix);
        super.onDraw(canvas);
    }

    @Override
    public void setImageMatrix(Matrix matrix) {
        if (matrix == null) {
            matrix = new Matrix();
        }

        if (!mMatrix.equals(matrix)) {
            mMatrix.set(matrix);
            invalidate();
        }
    }

    @Override
    public Matrix getImageMatrix() {
        return mMatrix;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mTouchGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void clearAnimation() {
        super.clearAnimation();
        mIsAnimatingBack = false;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (mDrawable != drawable) {
            mDrawable = drawable;
            if (drawable != null) {
                mDrawableIntrinsicWidth = drawable.getIntrinsicWidth();
                mDrawableIntrinsicHeight = drawable.getIntrinsicHeight();
                resetToInitialState();
            } else {
                mDrawableIntrinsicWidth = 0;
                mDrawableIntrinsicHeight = 0;
            }
        }
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != ScaleType.MATRIX) {
            throw new IllegalArgumentException("Unsupported scaleType. Only ScaleType.MATRIX is allowed.");
        }
        super.setScaleType(scaleType);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        loadMatrixValues();
        return canScroll(getMeasuredWidth(), mDrawableIntrinsicWidth * mScale, mTranslationX, direction);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean canScrollVertically(int direction) {
        loadMatrixValues();
        return canScroll(getMeasuredHeight(), mDrawableIntrinsicHeight * mScale, mTranslationY, direction);
    }

    public void setMaxScale(float maxScale) {
        mMaxScale = maxScale;
    }

    public void resetScale() {
        loadMatrixValues();

        // If we have already zoomed in, we should return to our initial scale value (minScale). Otherwise, scale to full size
        final float targetScale = getMinScale();

        // First, we try to keep the focused point in the same position when the animation ends
        final float desiredTranslationX = getWidth() / 2;
        final float desiredTranslationY = getHeight() / 2;

        // Here, we apply a correction to avoid unwanted blank spaces
        final float targetTranslationX = desiredTranslationX + computeTranslation(getMeasuredWidth(), mDrawableIntrinsicWidth * targetScale, desiredTranslationX, 0);
        final float targetTranslationY = desiredTranslationY + computeTranslation(getMeasuredHeight(), mDrawableIntrinsicHeight * targetScale, desiredTranslationY, 0);

        clearAnimation();
        final Animation animation = new TouchAnimation(targetScale, targetTranslationX, targetTranslationY);
        animation.setDuration(0);
        startAnimation(animation);

    }

    private void resetToInitialState() {
        mMatrix.reset();
        final float minScale = getMinScale();
        mMatrix.postScale(minScale, minScale);

        final float[] values = new float[9];
        mMatrix.getValues(values);

        final float freeSpaceHorizontal = (getMeasuredWidth() - (mDrawableIntrinsicWidth * minScale)) / 2F;
        final float freeSpaceVertical = (getMeasuredHeight() - (mDrawableIntrinsicHeight * minScale)) / 2F;
        mMatrix.postTranslate(freeSpaceHorizontal, freeSpaceVertical);

        invalidate();
    }

    private void loadMatrixValues() {
        mMatrix.getValues(mMatrixValues);
        mScale = mMatrixValues[Matrix.MSCALE_X];
        mTranslationX = mMatrixValues[Matrix.MTRANS_X];
        mTranslationY = mMatrixValues[Matrix.MTRANS_Y];
    }

    private float getMinScale() {
        float minScale = Math.min(getMeasuredWidth() / (float) mDrawableIntrinsicWidth, getMeasuredHeight() / (float) mDrawableIntrinsicHeight);
        if (minScale > mMaxScale) {
            minScale = mMaxScale;
        }
        return minScale;
    }

    private static boolean canScroll(float viewSize, float drawableSize, float currentTranslation, int direction) {
        if (direction > 0) {
            return Math.round(currentTranslation) < 0;
        } else if (direction < 0) {
            return Math.round(currentTranslation) > viewSize - Math.round(drawableSize);
        }
        return false;
    }

    // The translation values must be in [0, viewSize - drawableSize], except if we have free space. In that case we will translate to half of the free space
    private static float computeTranslation(float viewSize, float drawableSize, float currentTranslation, float delta) {
        final float sideFreeSpace = (viewSize - drawableSize) / 2F;

        if (sideFreeSpace > 0) {
            return sideFreeSpace - currentTranslation;
        } else if (currentTranslation + delta > 0) {
            return -currentTranslation;
        } else if (currentTranslation + delta < viewSize - drawableSize) {
            return viewSize - drawableSize - currentTranslation;
        }

        return delta;
    }

    private static float computeScaleTranslation(float viewSize, float drawableSize, float currentTranslation, float delta) {
        final float minTranslation = viewSize > drawableSize ? 0 : viewSize - drawableSize;
        final float maxTranslation = viewSize > drawableSize ? viewSize - drawableSize : 0;

        if (currentTranslation < minTranslation && delta > 0) {
            if (currentTranslation + delta > maxTranslation) {
                return maxTranslation - currentTranslation;
            } else {
                return delta;
            }
        } else if (currentTranslation > maxTranslation && delta < 0) {
            if (currentTranslation + delta < minTranslation) {
                return minTranslation - currentTranslation;
            } else {
                return delta;
            }
        } else if (currentTranslation > minTranslation && currentTranslation < maxTranslation) {
            if (currentTranslation + delta < minTranslation) {
                return minTranslation - currentTranslation;
            } else if (currentTranslation + delta > maxTranslation) {
                return maxTranslation - currentTranslation;
            } else {
                return delta;
            }
        }
        return 0;
    }

    // If our focal point is outside the image, we will project it to our image bounds
    private static float computeFocus(float viewSize, float drawableSize, float currentTranslation, float focusCoordinate) {
        if (currentTranslation > 0 && focusCoordinate < currentTranslation) {
            return currentTranslation;
        } else if (currentTranslation < viewSize - drawableSize && focusCoordinate > currentTranslation + drawableSize) {
            return drawableSize + currentTranslation;
        }

        return focusCoordinate;
    }

    // The scale values must be in [minScale, maxScale]
    private static float computeScale(float minScale, float maxScale, float currentScale, float delta) {
        if (currentScale * delta < minScale) {
            return minScale / currentScale;
        } else if (currentScale * delta > maxScale) {
            return maxScale / currentScale;
        }

        return delta;
    }

    private class FlingAnimation extends Animation {

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            mFlingScroller.computeScrollOffset(interpolatedTime);

            loadMatrixValues();

            final float dx = mFlingScroller.getCurrX() - mTranslationX;
            final float dy = mFlingScroller.getCurrY() - mTranslationY;
            mMatrix.postTranslate(dx, dy);

            ViewCompat.postInvalidateOnAnimation(TouchImageView.this);
        }
    }

    private class TouchAnimation extends Animation {

        private float initialScale;
        private float initialTranslationX;
        private float initialTranslationY;

        private float targetScale;
        private float targetTranslationX;
        private float targetTranslationY;

        TouchAnimation(float targetScale, float targetTranslationX, float targetTranslationY) {
            loadMatrixValues();

            this.initialScale = mScale;
            this.initialTranslationX = mTranslationX;
            this.initialTranslationY = mTranslationY;

            this.targetScale = targetScale;
            this.targetTranslationX = targetTranslationX;
            this.targetTranslationY = targetTranslationY;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            loadMatrixValues();

            if (interpolatedTime >= 1) {
                mMatrix.getValues(mMatrixValues);
                mMatrixValues[Matrix.MSCALE_X] = this.targetScale;
                mMatrixValues[Matrix.MSCALE_Y] = this.targetScale;
                mMatrixValues[Matrix.MTRANS_X] = this.targetTranslationX;
                mMatrixValues[Matrix.MTRANS_Y] = this.targetTranslationY;
                mMatrix.setValues(mMatrixValues);

            } else {
                final float scaleFactor = (this.initialScale + interpolatedTime * (this.targetScale - this.initialScale)) / mScale;
                mMatrix.postScale(scaleFactor, scaleFactor);

                mMatrix.getValues(mMatrixValues);
                final float currentTranslationX = mMatrixValues[Matrix.MTRANS_X];
                final float currentTranslationY = mMatrixValues[Matrix.MTRANS_Y];

                final float dx = this.initialTranslationX + interpolatedTime * (this.targetTranslationX - this.initialTranslationX) - currentTranslationX;
                final float dy = this.initialTranslationY + interpolatedTime * (this.targetTranslationY - this.initialTranslationY) - currentTranslationY;
                mMatrix.postTranslate(dx, dy);
            }

            ViewCompat.postInvalidateOnAnimation(TouchImageView.this);
        }
    }

    public void setZoomListener(ZoomListener listener) {
        mZoomListener = listener;
    }

    public static interface ZoomListener {
        void onZoomOut();

        void onZoomIn();
    }
}
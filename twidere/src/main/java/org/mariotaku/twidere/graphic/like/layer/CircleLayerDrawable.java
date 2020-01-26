package org.mariotaku.twidere.graphic.like.layer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable;

/**
 * Created by mariotaku on 16/2/22.
 */
public class CircleLayerDrawable extends AnimationLayerDrawable {

    public CircleLayerDrawable(final int intrinsicWidth, final int intrinsicHeight,
                               final LikeAnimationDrawable.Palette palette) {
        super(intrinsicWidth, intrinsicHeight, palette);
    }

    @Override
    protected CircleState createConstantState(final int intrinsicWidth,
                                              final int intrinsicHeight,
                                              final LikeAnimationDrawable.Palette palette) {
        return new CircleState(intrinsicWidth, intrinsicHeight, palette);
    }

    @Override
    public void draw(@NonNull final Canvas canvas) {
        final CircleState state = (CircleState) mState;
        final float progress = getProgress();
        final Rect bounds = getBounds();
        final float radius;
        final Paint paint = state.getPaint();
        final int fullRadius = state.getFullRadius();
        if (progress < 0.5f) {
            paint.setStyle(Paint.Style.FILL);
            final float sizeProgress = Math.min(1, progress * 2);
            radius = sizeProgress * fullRadius;
        } else {
            paint.setStyle(Paint.Style.STROKE);
            final float innerLeftRatio = 1 - (progress - 0.5f) * 2f;
            final float strokeWidth = fullRadius * innerLeftRatio;
            paint.setStrokeWidth(strokeWidth);
            radius = fullRadius - strokeWidth / 2;
            if (strokeWidth <= 0) return;
        }
        paint.setColor(state.getPalette().getCircleColor(progress));
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), radius, paint);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        final CircleState state = (CircleState) mState;
        state.setFullRadius(Math.min(bounds.width(), bounds.height()) / 2);
    }

    /**
     * Created by mariotaku on 16/2/22.
     */
    static class CircleState extends AnimationLayerState {
        private final Paint mPaint;
        private int mFullRadius;

        public CircleState(int intrinsicWidth, int intrinsicHeight, LikeAnimationDrawable.Palette palette) {
            super(intrinsicWidth, intrinsicHeight, palette);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }

        @NonNull
        @Override
        public Drawable newDrawable() {
            return new CircleLayerDrawable(mIntrinsicWidth, mIntrinsicHeight, mPalette);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }

        public void setFullRadius(int fullRadius) {
            mFullRadius = fullRadius;
        }

        public Paint getPaint() {
            return mPaint;
        }

        public int getFullRadius() {
            return mFullRadius;
        }
    }
}

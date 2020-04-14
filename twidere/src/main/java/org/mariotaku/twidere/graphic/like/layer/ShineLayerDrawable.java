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
public class ShineLayerDrawable extends AnimationLayerDrawable {

    private static final int PARTICLES_PIVOTS_COUNT = 5;

    public ShineLayerDrawable(final int intrinsicWidth, final int intrinsicHeight, final LikeAnimationDrawable.Palette palette) {
        super(intrinsicWidth, intrinsicHeight, palette);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        final ShineLayerState state = (ShineLayerState) mState;
        final float progress = getProgress();
        if (progress < 0) return;
        final LikeAnimationDrawable.Palette palette = state.getPalette();
        final int particleColor = palette.getParticleColor(0, 0, progress);
        final Rect bounds = getBounds();
        final Paint paint = state.getPaint();
        paint.setColor(particleColor);
        paint.setStrokeWidth(state.getLineWidth());
        final float[] startEnd = new float[2];
        paint.setAlpha(0xFF);
        if (progress < 0.25f) {
            calcPhase1(startEnd, progress);
        } else if (progress < 0.5f) {
            calcPhase2(startEnd, progress);
        } else if (progress < 0.75f) {
            calcPhase3(startEnd, progress);
        } else {
            calcPhase4(startEnd, progress);
            paint.setAlpha(Math.round(0xFF * (1 - (progress - 0.75f) * 4)));
        }

        for (int i = 0; i < PARTICLES_PIVOTS_COUNT; i++) {
            final double degree = 360.0 / PARTICLES_PIVOTS_COUNT * i;
            final double mainParticleAngle = Math.toRadians(degree + 18);
            final float startX = (float) (bounds.centerX() + startEnd[0] * Math.cos(mainParticleAngle));
            final float startY = (float) (bounds.centerY() + startEnd[0] * Math.sin(mainParticleAngle));
            final float stopX = (float) (bounds.centerX() + startEnd[1] * Math.cos(mainParticleAngle));
            final float stopY = (float) (bounds.centerY() + startEnd[1] * Math.sin(mainParticleAngle));
            if (startEnd[1] - startEnd[0] <= 0) {
                canvas.drawPoint(startX, startY, paint);
            } else {
                canvas.drawLine(startX, startY, stopX, stopY, paint);
            }
        }
    }

    @Override
    protected ShineLayerState createConstantState(final int intrinsicWidth, final int intrinsicHeight,
                                                  final LikeAnimationDrawable.Palette palette) {
        return new ShineLayerState(intrinsicWidth, intrinsicHeight, palette);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        final int fullRadius = Math.min(bounds.width(), bounds.height()) / 2;
        final ShineLayerState state = (ShineLayerState) mState;
        state.setFullRadius(fullRadius);
    }

    private void calcPhase4(float[] startEnd, float progress) {
        calcPhase3(startEnd, 0.75f);
    }

    private void calcPhase3(float[] startEnd, float progress) {
        calcPhase2(startEnd, 0.5f);
        final float length = (startEnd[1] - startEnd[0]) * (1 - (progress - 0.5f) * 4);
        startEnd[0] = startEnd[1] - length;
    }

    private void calcPhase2(float[] startEnd, float progress) {
        final ShineLayerState state = (ShineLayerState) mState;
        calcPhase1(startEnd, 0.25f);
        final float length = startEnd[1] - startEnd[0];
        final float initialStart = startEnd[0];
        startEnd[0] = initialStart + state.getFullRadius() / 3 * (progress - 0.25f) * 4;
        startEnd[1] = startEnd[0] + length;
    }

    private void calcPhase1(float[] startEnd, float progress) {
        final ShineLayerState state = (ShineLayerState) mState;
        // Start point: 1/4 of icon radius
        final int fullRadius = state.getFullRadius();
        startEnd[0] = fullRadius / 3;
        startEnd[1] = startEnd[0] + (fullRadius / 4 * progress * 4);
    }

    /**
     * Created by mariotaku on 16/2/22.
     */
    static class ShineLayerState extends AnimationLayerState {

        private final Paint mPaint;
        private int mFullRadius;
        private float mLineWidth;

        public ShineLayerState(int intrinsicWidth, int intrinsicHeight, LikeAnimationDrawable.Palette palette) {
            super(intrinsicWidth, intrinsicHeight, palette);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            setProgress(-1);
        }

        public Paint getPaint() {
            return mPaint;
        }

        @NonNull
        @Override
        public Drawable newDrawable() {
            return new ShineLayerDrawable(mIntrinsicWidth, mIntrinsicHeight, mPalette);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }

        public int getFullRadius() {
            return mFullRadius;
        }

        public void setFullRadius(int fullRadius) {
            mFullRadius = fullRadius;
            mLineWidth = fullRadius / 10f;
        }

        public float getLineWidth() {
            return mLineWidth;
        }

    }
}

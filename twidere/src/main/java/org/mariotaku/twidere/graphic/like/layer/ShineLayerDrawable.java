package org.mariotaku.twidere.graphic.like.layer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import org.mariotaku.twidere.graphic.like.palette.Palette;
import org.mariotaku.twidere.graphic.like.state.ShineLayerState;

/**
 * Created by mariotaku on 16/2/22.
 */
public class ShineLayerDrawable extends AnimationLayerDrawable<ShineLayerState> {

    private static final int PARTICLES_PIVOTS_COUNT = 5;

    public ShineLayerDrawable(final int intrinsicWidth, final int intrinsicHeight, final Palette palette) {
        super(intrinsicWidth, intrinsicHeight, palette);
    }

    @Override
    protected ShineLayerState createConstantState(final int intrinsicWidth,
                                                  final int intrinsicHeight,
                                                  final Palette palette) {
        return new ShineLayerState(intrinsicWidth, intrinsicHeight, palette);
    }

    @Override
    public void draw(Canvas canvas) {
        final float progress = getProgress();
        if (progress < 0) return;
        final Palette palette = mState.getPalette();
        final int particleColor = palette.getParticleColor(0, 0, progress);
        final Rect bounds = getBounds();
        final Paint paint = mState.getPaint();
        paint.setColor(particleColor);
        paint.setStrokeWidth(mState.getLineWidth());
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

    private void calcPhase4(float[] startEnd, float progress) {
        calcPhase3(startEnd, 0.75f);
    }

    private void calcPhase3(float[] startEnd, float progress) {
        calcPhase2(startEnd, 0.5f);
        final float length = (startEnd[1] - startEnd[0]) * (1 - (progress - 0.5f) * 4);
        startEnd[0] = startEnd[1] - length;
    }

    private void calcPhase2(float[] startEnd, float progress) {
        calcPhase1(startEnd, 0.25f);
        final float length = startEnd[1] - startEnd[0];
        final float initialStart = startEnd[0];
        startEnd[0] = initialStart + mState.getFullRadius() / 3 * (progress - 0.25f) * 4;
        startEnd[1] = startEnd[0] + length;
    }

    private void calcPhase1(float[] startEnd, float progress) {
        // Start point: 1/4 of icon radius
        final int fullRadius = mState.getFullRadius();
        startEnd[0] = fullRadius / 3;
        startEnd[1] = startEnd[0] + (fullRadius / 4 * progress * 4);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        final int fullRadius = Math.min(bounds.width(), bounds.height()) / 2;
        mState.setFullRadius(fullRadius);
    }

}

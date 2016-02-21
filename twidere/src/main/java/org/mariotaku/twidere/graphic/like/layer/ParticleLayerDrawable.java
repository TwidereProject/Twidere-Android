package org.mariotaku.twidere.graphic.like.layer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import org.mariotaku.twidere.graphic.like.palette.Palette;
import org.mariotaku.twidere.graphic.like.state.ParticleLayerState;

/**
 * Created by mariotaku on 16/2/22.
 */
public class ParticleLayerDrawable extends AnimationLayerDrawable<ParticleLayerState> {

    private static final int PARTICLES_PIVOTS_COUNT = 7;

    public ParticleLayerDrawable(final int intrinsicWidth, final int intrinsicHeight,
                                 final Palette palette) {
        super(intrinsicWidth, intrinsicHeight, palette);
    }

    @Override
    protected ParticleLayerState createConstantState(final int intrinsicWidth,
                                                     final int intrinsicHeight,
                                                     final Palette palette) {
        return new ParticleLayerState(intrinsicWidth, intrinsicHeight, palette);
    }

    @Override
    public void draw(final Canvas canvas) {
        final float progress = getProgress();
        if (progress < 0) return;
        final Rect bounds = getBounds();
        final float expandSpinProgress = Math.min(0.5f, progress);
        final float fullRadius = mState.getFullRadius();
        final float currentRadius = fullRadius + (fullRadius * expandSpinProgress);
        final float particleSize = mState.getParticleSize();
        final float distance = particleSize + (particleSize * progress);
        final float mainStrokeWidth, subStrokeWidth;
        if (progress < 0.5) {
            // Scale factor: [1, 0.5)
            mainStrokeWidth = particleSize * (1 - progress);
            // Scale factor: [1, 1.25)
            subStrokeWidth = particleSize * (1 + progress / 2);
        } else {
            mainStrokeWidth = particleSize * (1 - progress);
            subStrokeWidth = particleSize * 1.25f * (1 - (progress - 0.5f) * 2);
        }

        for (int i = 0; i < PARTICLES_PIVOTS_COUNT; i++) {
            final double degree = 360.0 / PARTICLES_PIVOTS_COUNT * i;
            final Palette palette = mState.getPalette();
            final int color = palette.getParticleColor(PARTICLES_PIVOTS_COUNT, i, progress);

            final double mainParticleAngle = Math.toRadians(degree - 115);
            final float mainParticleX = (float) (bounds.centerX() + currentRadius * Math.cos(mainParticleAngle));
            final float mainParticleY = (float) (bounds.centerY() + currentRadius * Math.sin(mainParticleAngle));

            final Paint paint = mState.getPaint();
            paint.setColor(color);
            if (mainStrokeWidth > 0) {
                canvas.drawCircle(mainParticleX, mainParticleY, mainStrokeWidth / 2, paint);
            }

            final double particleAngle = Math.toRadians(90.0 * -expandSpinProgress + degree + 15);
            final float subParticleX = (float) (mainParticleX + distance * Math.cos(particleAngle));
            final float subParticleY = (float) (mainParticleY + distance * Math.sin(particleAngle));
            paint.setAlpha(Math.round(255f * (1 - progress / 2f)));

            if (subStrokeWidth > 0) {
                canvas.drawCircle(subParticleX, subParticleY, subStrokeWidth / 2, paint);
            }
        }

    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mState.setFullRadius(Math.min(bounds.width(), bounds.height()) / 2);
    }

}

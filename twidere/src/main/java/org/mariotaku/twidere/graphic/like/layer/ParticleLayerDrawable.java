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
public class ParticleLayerDrawable extends AnimationLayerDrawable {

    private static final int PARTICLES_PIVOTS_COUNT = 7;

    public ParticleLayerDrawable(final int intrinsicWidth, final int intrinsicHeight,
                                 final LikeAnimationDrawable.Palette palette) {
        super(intrinsicWidth, intrinsicHeight, palette);
    }

    @Override
    protected ParticleLayerState createConstantState(final int intrinsicWidth,
                                                     final int intrinsicHeight,
                                                     final LikeAnimationDrawable.Palette palette) {
        return new ParticleLayerState(intrinsicWidth, intrinsicHeight, palette);
    }

    @Override
    public void draw(@NonNull final Canvas canvas) {
        final ParticleLayerState state = (ParticleLayerState) mState;
        final float progress = getProgress();
        if (progress < 0) return;
        final Rect bounds = getBounds();
        final float expandSpinProgress = Math.min(0.5f, progress);
        final float fullRadius = state.getFullRadius();
        final float currentRadius = fullRadius + (fullRadius * expandSpinProgress);
        final float particleSize = state.getParticleSize();
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
            final LikeAnimationDrawable.Palette palette = state.getPalette();
            final int color = palette.getParticleColor(PARTICLES_PIVOTS_COUNT, i, progress);

            final double mainParticleAngle = Math.toRadians(degree - 115);
            final float mainParticleX = (float) (bounds.centerX() + currentRadius * Math.cos(mainParticleAngle));
            final float mainParticleY = (float) (bounds.centerY() + currentRadius * Math.sin(mainParticleAngle));

            final Paint paint = state.getPaint();
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
        final ParticleLayerState state = (ParticleLayerState) mState;
        state.setFullRadius(Math.min(bounds.width(), bounds.height()) / 2);
    }

    /**
     * Created by mariotaku on 16/2/22.
     */
    static class ParticleLayerState extends AnimationLayerState {

        private final Paint mPaint;
        private float mFullRadius;
        private float mParticleSize;

        public ParticleLayerState(int intrinsicWidth, int intrinsicHeight, LikeAnimationDrawable.Palette palette) {
            super(intrinsicWidth, intrinsicHeight, palette);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.FILL);
            setProgress(-1);
        }

        @NonNull
        @Override
        public Drawable newDrawable() {
            return new ParticleLayerDrawable(mIntrinsicWidth, mIntrinsicHeight, mPalette);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }

        public float getFullRadius() {
            return mFullRadius;
        }

        public float getParticleSize() {
            return mParticleSize;
        }

        public Paint getPaint() {
            return mPaint;
        }

        public void setFullRadius(int fullRadius) {
            mFullRadius = fullRadius;
            mParticleSize = fullRadius / 4f;
        }
    }
}

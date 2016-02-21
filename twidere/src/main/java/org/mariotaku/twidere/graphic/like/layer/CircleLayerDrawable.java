package org.mariotaku.twidere.graphic.like.layer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import org.mariotaku.twidere.graphic.like.palette.Palette;
import org.mariotaku.twidere.graphic.like.state.CircleState;

/**
 * Created by mariotaku on 16/2/22.
 */
public class CircleLayerDrawable extends AnimationLayerDrawable<CircleState> {

    public CircleLayerDrawable(final int intrinsicWidth, final int intrinsicHeight,
                               final Palette palette) {
        super(intrinsicWidth, intrinsicHeight, palette);
    }

    @Override
    protected CircleState createConstantState(final int intrinsicWidth,
                                              final int intrinsicHeight,
                                              final Palette palette) {
        return new CircleState(intrinsicWidth, intrinsicHeight, palette);
    }

    @Override
    public void draw(final Canvas canvas) {
        final float progress = getProgress();
        final Rect bounds = getBounds();
        final float radius;
        final Paint paint = mState.getPaint();
        final int fullRadius = mState.getFullRadius();
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
        paint.setColor(mState.getPalette().getCircleColor(progress));
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), radius, paint);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mState.setFullRadius(Math.min(bounds.width(), bounds.height()) / 2);
    }

}

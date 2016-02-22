package org.mariotaku.twidere.graphic.like.palette;

import android.animation.ArgbEvaluator;

import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable;

/**
 * Created by mariotaku on 16/2/22.
 */
public final class FavoritePalette implements LikeAnimationDrawable.Palette {

    private final ArgbEvaluator evaluator = new ArgbEvaluator();

    @Override
    public int getParticleColor(int count, int index, float progress) {
        return (Integer) evaluator.evaluate(progress, 0xFFFF7020, 0xFFFD9050);
    }

    @Override
    public int getCircleColor(float progress) {
        return (Integer) evaluator.evaluate(progress, 0xFFFF9C00, 0xFFFFB024);
    }
}

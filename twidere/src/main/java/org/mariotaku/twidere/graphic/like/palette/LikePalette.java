package org.mariotaku.twidere.graphic.like.palette;

import android.animation.ArgbEvaluator;
import android.graphics.Color;

import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable;

/**
 * Created by mariotaku on 16/2/22.
 */
public final class LikePalette implements LikeAnimationDrawable.Palette {

    private final ArgbEvaluator evaluator = new ArgbEvaluator();
    private final float[] hsv = new float[3];

    @Override
    public int getParticleColor(int count, int index, float progress) {
        final double degree = 360.0 / count * index;
        hsv[0] = (float) degree;
        hsv[1] = 0.4f;
        hsv[2] = 1f;
        return Color.HSVToColor(hsv);
    }

    @Override
    public int getCircleColor(float progress) {
        return (Integer) evaluator.evaluate(progress, 0xFFDE4689, 0xFFCD8FF5);
    }
}

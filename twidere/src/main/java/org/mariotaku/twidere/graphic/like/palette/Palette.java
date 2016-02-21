package org.mariotaku.twidere.graphic.like.palette;

/**
 * Created by mariotaku on 16/2/22.
 */
public interface Palette {
    int getParticleColor(int count, int index, float progress);

    int getCircleColor(float progress);
}

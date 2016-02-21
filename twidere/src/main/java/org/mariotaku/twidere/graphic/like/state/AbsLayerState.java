package org.mariotaku.twidere.graphic.like.state;

import android.graphics.drawable.Drawable;

import org.mariotaku.twidere.graphic.like.palette.Palette;

/**
 * Created by mariotaku on 16/2/22.
 */
public abstract class AbsLayerState extends Drawable.ConstantState {
    protected final int mIntrinsicWidth;
    protected final int mIntrinsicHeight;
    protected final Palette mPalette;

    private float mProgress;

    public AbsLayerState(int intrinsicWidth, int intrinsicHeight, Palette palette) {
        this.mPalette = palette;
        this.mIntrinsicHeight = intrinsicHeight;
        this.mIntrinsicWidth = intrinsicWidth;
    }

    public final float getProgress() {
        return mProgress;
    }

    public final void setProgress(float progress) {
        mProgress = progress;
    }

    public final Palette getPalette() {
        return mPalette;
    }

    public final int getIntrinsicWidth() {
        return mIntrinsicWidth;
    }

    public final int getIntrinsicHeight() {
        return mIntrinsicHeight;
    }
}
